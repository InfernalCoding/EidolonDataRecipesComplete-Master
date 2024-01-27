package dev.infernal_coding.eidolonrecipes.rituals.result;

import com.google.gson.JsonObject;
import dev.infernal_coding.eidolonrecipes.rituals.IRitualResultSerializer;
import dev.infernal_coding.eidolonrecipes.rituals.RitualManager;
import dev.infernal_coding.eidolonrecipes.rituals.RitualRecipeWrapper;
import dev.infernal_coding.eidolonrecipes.util.EntityUtil;
import dev.infernal_coding.eidolonrecipes.util.JSONUtils;
import elucent.eidolon.network.CrystallizeEffectPacket;
import elucent.eidolon.network.Networking;
import elucent.eidolon.ritual.Ritual;
import elucent.eidolon.util.ColorUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Objects;
import java.util.Optional;

import static dev.infernal_coding.eidolonrecipes.util.ItemUtil.getEntityFromJson;

public class SummonSerializer implements IRitualResultSerializer {

    public static final String ID = "summon";
    @Override
    public RitualManager.ResultColorPair getColorAndResult(JsonObject json, int color, boolean isColorPreset, String type) {
        EntityType<?> entityType = getEntityFromJson(json.get("entity"));
        int count = JSONUtils.getInt(json, "count", 1) > 0 ?
                JSONUtils.getInt(json, "count", 1) : 1;

        if (entityType != null) {
            EntityUtil.Container container = new EntityUtil.Container(entityType, count);

            if (color > 0 && !isColorPreset) {
                color -= ColorUtil.packColor(255, 121, 94, 255);
            } else if (!isColorPreset) {
                color += ColorUtil.packColor(255, 121, 94, 255);
            }
            return new RitualManager.ResultColorPair(color, count, container, type);
        }
        return null;
    }

    @Override
    public RitualManager.ResultColorPair getColorAndResult(FriendlyByteBuf buffer, int color, boolean isColorPreset, String type) {
        ResourceLocation entityName = buffer.readResourceLocation();
        EntityType<?> entityType;
        int count = 1;

        try {
            count = buffer.readInt();
        } catch (Exception ignored) {}

        entityType = ForgeRegistries.ENTITY_TYPES.getValue(entityName);

        if (entityType != null) {
            EntityUtil.Container container = new EntityUtil.Container(entityType, count);

            if (color > 0 && !isColorPreset) {
                color -= ColorUtil.packColor(100, 30, 25, 45);
            } else if (!isColorPreset) {
                color += ColorUtil.packColor(100, 35, 80, 175);
            }
            return new RitualManager.ResultColorPair(color, count, container, type);
        }
        return null;
    }

    @Override
    public void writeResult(RitualRecipeWrapper.Result result, FriendlyByteBuf buffer) {
        buffer.writeUtf(result.getVariant());

        if (result.getToCreate() instanceof EntityType<?>) {
            EntityType<?> entityType = (EntityType<?>) result.getToCreate();
            buffer.writeResourceLocation(Objects.requireNonNull(ForgeRegistries.ENTITY_TYPES.getKey(entityType)));
        }
    }

    @Override
    public void startRitual(Ritual ritual, RitualRecipeWrapper.Result result, Level world, BlockPos pos) {
        if (result.getToCreate() instanceof EntityUtil.Container) {
            createMob(result, world, pos);
        }
    }

    @Override
    public boolean onRitualTick(RitualRecipeWrapper.Result result, Level world, BlockPos pos) {
        return false;
    }

    @Override
    public boolean getRunsOnTick() {
        return false;
    }

    @Override
    public ItemStack getIcon(RitualRecipeWrapper.Result result) {
        EntityUtil.Container container = (EntityUtil.Container) result.getToCreate();
        Optional<Item> egg = Optional.ofNullable(ForgeSpawnEggItem.fromEntityType(container.getEntityType()));
        return egg.map(ItemStack::new).orElse(ItemStack.EMPTY);
    }

    private void createMob(RitualRecipeWrapper.Result result, Level world, BlockPos pos) {
        EntityUtil.Container container = (EntityUtil.Container) result.getToCreate();
        EntityType<?> entityToMake = container.getEntityType();
        if (!world.isClientSide) {
            Networking.sendToTracking(world, pos, new CrystallizeEffectPacket(pos));

            for (int i = 0; i < result.getCount(); i++) {
                Entity e = entityToMake.create(world);
                e.setPos((double) pos.getX() + 0.5, (double) pos.getY() + 1.5, (double) pos.getZ() + 0.5);
                world.addFreshEntity(e);
            }
        }
    }
}
