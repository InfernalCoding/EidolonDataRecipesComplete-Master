package dev.infernal_coding.eidolonrecipes.rituals.result;

import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Pair;
import dev.infernal_coding.eidolonrecipes.rituals.IRitualResultSerializer;
import dev.infernal_coding.eidolonrecipes.rituals.RitualManager;
import dev.infernal_coding.eidolonrecipes.rituals.RitualRecipeWrapper;
import elucent.eidolon.Registry;
import elucent.eidolon.network.CrystallizeEffectPacket;
import elucent.eidolon.network.Networking;
import elucent.eidolon.ritual.Ritual;
import elucent.eidolon.util.ColorUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.merchant.villager.VillagerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.concurrent.ThreadTaskExecutor;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;
import java.util.Objects;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import static dev.infernal_coding.eidolonrecipes.util.EntityUtil.getEntityType;
import static dev.infernal_coding.eidolonrecipes.util.ItemUtil.*;
import static dev.infernal_coding.eidolonrecipes.util.ItemUtil.getEnchantedBookFromJson;
import static elucent.eidolon.ritual.Ritual.getDefaultBounds;

public class AbsorbSerializer implements IRitualResultSerializer {

    public static final String ID = "absorb";
    @Override
    public RitualManager.ResultColorPair getColorAndResult(JsonObject json, int color, boolean isColorPreset, String type) {

        int countOfItem1 = JSONUtils.getInt(json, "count", 1) > 0 ?
                JSONUtils.getInt(json, "count", 1) : 1;
        int addModifier = JSONUtils.getInt(json, "addModifier", 0);

        ItemStack item = getItemFromJson(json.get("item"));
        ItemStack potionItem = getPotionItemFromJson(json.get("potion"));
        ItemStack splashPotionItem = getSplashPotionItemFromJson(json.get("splash_potion"));
        ItemStack lingeringPotionItem = getLingeringPotionItem(json.get("lingering_potion"));
        ItemStack enchantedBook = getEnchantedBookFromJson(json);

        if (!item.isEmpty() || !potionItem.isEmpty() || !splashPotionItem.isEmpty()
                || !lingeringPotionItem.isEmpty() || !enchantedBook.isEmpty()) {
            item.setCount(countOfItem1);

            if (!potionItem.isEmpty()) {
                item = potionItem;
                item.setCount(countOfItem1);
            } else if (!splashPotionItem.isEmpty()) {
                item = splashPotionItem;
                item.setCount(countOfItem1);
            } else if (!lingeringPotionItem.isEmpty()) {
                item = lingeringPotionItem;
                item.setCount(countOfItem1);
            } else if (!enchantedBook.isEmpty()) {
                item = enchantedBook;
                item.setCount(countOfItem1);
            }
        }

        String entityName = JSONUtils.getString(json, "entity", "");
        EntityType<?> entity = getEntityFromJson(json.get("entity"));

        if (entity == null && !item.isEmpty()) {
            RitualManager.ResultColorPair entityContainer =
                    IRitualResultSerializer.getClassTypeAndColor(ID, entityName, color, addModifier, isColorPreset);
            entityContainer.result = new Pair<>(entityContainer.result, item);
            return entityContainer;
        } else if (entity != null && !item.isEmpty()) {
            Pair<EntityType<?>, ItemStack> entityPair = new Pair<>(entity, item);

            if (color > 0 && !isColorPreset) {
                color -= ColorUtil.packColor(88, 29, 11, 85);
            } else if (!isColorPreset) {
                color += ColorUtil.packColor(77, 23, 11, 11);
            }
            return new RitualManager.ResultColorPair(color, addModifier, entityPair, type);
        }
        return null;
    }

    @Override
    public RitualManager.ResultColorPair getColorAndResult(PacketBuffer buffer, int color, boolean isColorPreset, String type) {
        String entityName = buffer.readString();
        EntityType<?> entity = getEntityType(entityName);
        ItemStack resultItem = buffer.readItemStack();
        int itemAddModifier = 1;

        try {
            itemAddModifier = buffer.readInt();
        } catch (Exception ignored) {}

        if (entity == null && !resultItem.isEmpty()) {
            RitualManager.ResultColorPair container =
                    IRitualResultSerializer.getClassTypeAndColor(ID, entityName, color, itemAddModifier, isColorPreset);
            container.result = new Pair<>(container.result, resultItem);
        } else if (entity != null && !resultItem.isEmpty()) {
            if (color > 0 && !isColorPreset) {
                color -= ColorUtil.packColor(88, 29, 11, 85);
            } else if (!isColorPreset) {
                color += ColorUtil.packColor(77, 23, 11, 11);
            }
            Pair<EntityType<?>, ItemStack> entityPair = new Pair<>(entity, resultItem);
            return new RitualManager.ResultColorPair(color, itemAddModifier, entityPair, type);
        }
        return null;
    }

    @Override
    public void writeResult(RitualRecipeWrapper.Result result, PacketBuffer buffer) {
        buffer.writeString(result.getVariant());

        if (result.getToCreate() instanceof Pair) {
            Pair<?, ItemStack> entityAbsorbPair = (Pair<?, ItemStack>) result.getToCreate();

            if (entityAbsorbPair.getFirst() instanceof Class<?>) {
                Class<?> entity1Class = (Class<?>) entityAbsorbPair.getFirst();
                writeClassName(entity1Class.getName(), buffer);
            } else if (entityAbsorbPair.getFirst() instanceof EntityType) {
                EntityType<?> entity1Type = (EntityType<?>) entityAbsorbPair.getFirst();
                buffer.writeResourceLocation(Objects.requireNonNull(entity1Type.getRegistryName()));
            }
            ItemStack resultItem = entityAbsorbPair.getSecond();
            buffer.writeItemStack(resultItem);
            buffer.writeInt(result.getCount());
        }
    }

    @Override
    public void startRitual(RitualRecipeWrapper.Result result, World world, BlockPos pos) {
        if (result.getToCreate() instanceof Pair) {

            Pair<?, ItemStack> pairCheck = (Pair<?, ItemStack>) result.getToCreate();

            if (pairCheck.getFirst() instanceof Class<?>) {
                Thread absorbMobs = new Thread(() ->
                        absorbEntities(world, pos, (Class<LivingEntity>) pairCheck.getFirst(), pairCheck.getSecond(), result.getCount()));
                absorbMobs.start();
            } else if (pairCheck.getFirst() instanceof EntityType<?>) {
                Thread absorbMobs = new Thread(() ->
                        absorbEntityType(world, pos,  (EntityType<LivingEntity>) pairCheck.getFirst(), pairCheck.getSecond(), result.getCount()));
                absorbMobs.start();
            }
        }
    }

    @Override
    public boolean onRitualTick(RitualRecipeWrapper.Result result, World world, BlockPos pos) {
        return false;
    }

    @Override
    public boolean getRunsOnTick() {
        return false;
    }

    @Override
    public ItemStack getIcon(RitualRecipeWrapper.Result result) {
        if (result.getToCreate() instanceof Pair) {
            Pair<?, ItemStack> pair = (Pair<?, ItemStack>) result.getToCreate();
            return pair.getSecond();
        }
        return ItemStack.EMPTY;
    }

    public void absorbEntities(World world, BlockPos pos, Class<LivingEntity> entityClass, ItemStack resultItem, int addModifier) {
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        List<LivingEntity> entities = world.getEntitiesWithinAABB(entityClass, getDefaultBounds(pos), null);

        for (LivingEntity e : entities) {
            scheduler.execute(() -> {

                if (getDefaultBounds(pos).contains(e.getPosX(), e.getPosY(), e.getPosZ())) {
                    e.attackEntityFrom(Registry.RITUAL_DAMAGE, e.getMaxHealth() * 1000);
                    if (!world.isRemote) {
                        Networking.sendToTracking(world, e.getPosition(), new CrystallizeEffectPacket(e.getPosition()));
                        int addedModifier = addModifier > 0 ? world.rand.nextInt(addModifier) : 1;

                        for (int i = 0; i < resultItem.getCount(); i++) {
                            ItemStack itemStack = resultItem.copy();
                            itemStack.setCount(addedModifier);

                            world.addEntity(new ItemEntity(world, e.getPosX(), e.getPosY(), e.getPosZ(),
                                    itemStack));
                        }
                    }
                }
            });

            try {
                Thread.sleep(world.rand.nextInt(2000) + 1000);
            } catch (Exception ignored) {}
        }
    }

    private void absorbEntityType(World world, BlockPos pos, EntityType<LivingEntity> entityType, ItemStack resultItem, int addModifier) {
        List<LivingEntity> entities = world.getEntitiesWithinAABB(entityType, getDefaultBounds(pos), a -> true);

        for (LivingEntity e : entities) {
            e.attackEntityFrom(Registry.RITUAL_DAMAGE, e.getMaxHealth() * 1000);
            if (!world.isRemote) {
                Networking.sendToTracking(world, e.getPosition(), new CrystallizeEffectPacket(e.getPosition()));
                int addedModifier = addModifier > 0 ? world.rand.nextInt(addModifier) : 1;

                for (int i = 0; i < resultItem.getCount(); i++) {
                    ItemStack itemStack = resultItem.copy();
                    itemStack.setCount(addedModifier);

                    world.addEntity(new ItemEntity(world, e.getPosX(), e.getPosY(), e.getPosZ(),
                            itemStack));
                }
                try {
                    Thread.sleep(world.rand.nextInt(2000) + 1000);
                } catch (Exception ignored) {}
            }
        }
    }
}
