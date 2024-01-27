package dev.infernal_coding.eidolonrecipes.rituals.result;

import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Pair;
import dev.infernal_coding.eidolonrecipes.rituals.IRitualResultSerializer;
import dev.infernal_coding.eidolonrecipes.rituals.RitualManager;
import dev.infernal_coding.eidolonrecipes.rituals.RitualRecipeWrapper;
import dev.infernal_coding.eidolonrecipes.util.JSONUtils;
import elucent.eidolon.ritual.Ritual;
import elucent.eidolon.util.ColorUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.monster.ZombieVillager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static dev.infernal_coding.eidolonrecipes.util.EntityUtil.getEntityType;
import static dev.infernal_coding.eidolonrecipes.util.ItemUtil.getEntityFromJson;

public class TransformSerializer implements IRitualResultSerializer {
    public static final String ID = "transform";

    @Override
    public RitualManager.ResultColorPair getColorAndResult(JsonObject json, int color, boolean isColorPreset, String type) {
        int count = JSONUtils.getInt(json, "count", 1) > 0 ?
                JSONUtils.getInt(json, "count", 1) : 1;
        String entityOneName = JSONUtils.getString(json, "entityOne", "");
        EntityType<?> entityOne = getEntityFromJson(json.get("entityOne"));
        EntityType<?> entityTwo = getEntityFromJson(json.get("entityTwo"));

        if (entityOne == null && entityTwo != null) {
            RitualManager.ResultColorPair entityContainer =
                    IRitualResultSerializer.getClassTypeAndColor(ID, entityOneName, color, count, isColorPreset);
            entityContainer.result = new Pair<>(entityContainer.result, entityTwo);

            return entityContainer;
        } else if (entityOne != null && entityTwo != null) {
            Pair<EntityType<?>, EntityType<?>> entityPair = new Pair<>(entityOne, entityTwo);
            if (color > 0 && !isColorPreset) {
                color -= ColorUtil.packColor(88, 29, 11, 85);
            } else if (!isColorPreset) {
                color += ColorUtil.packColor(77, 23, 11, 11);
            }
            return new RitualManager.ResultColorPair(color, count, entityPair, type);
        }
        return null;
    }
    @Override
    public RitualManager.ResultColorPair getColorAndResult(FriendlyByteBuf buffer, int color, boolean isColorPreset, String type) {
        String entityOneName = buffer.readUtf();
        int numOfEntity2 = 1;

        EntityType<?> entityOne = getEntityType(entityOneName);
        EntityType<?> entityTwo = ForgeRegistries.ENTITY_TYPES.getValue(buffer.readResourceLocation());

        try {
            numOfEntity2 = buffer.readInt();
        } catch (Exception ignored) {}

         if (entityOne == null && entityTwo != null) {
            RitualManager.ResultColorPair container =
                    IRitualResultSerializer.getClassTypeAndColor(ID, entityOneName, color, numOfEntity2, isColorPreset);
            container.result = new Pair<>(container.result, entityTwo);
        } else if (entityOne != null && entityTwo != null) {
            if (color > 0 && !isColorPreset) {
                color -= ColorUtil.packColor(88, 29, 11, 85);
            } else if (!isColorPreset) {
                color += ColorUtil.packColor(77, 23, 11, 11);
            }
            Pair<EntityType<?>, EntityType<?>> entityPair = new Pair<>(entityOne, entityTwo);
            return new RitualManager.ResultColorPair(color, numOfEntity2, entityPair, type);
        }
        return null;
    }

    @Override
    public void writeResult(RitualRecipeWrapper.Result result, FriendlyByteBuf buffer) {
        buffer.writeUtf(result.getVariant());

        if (result.getToCreate() instanceof Pair) {
            Pair<?, EntityType<?>> entityTransformPair = (Pair<?, EntityType<?>>) result.getToCreate();

            if (entityTransformPair.getFirst() instanceof Class<?>) {
                Class<?> entity1Class = (Class<?>) entityTransformPair.getFirst();
                writeClassName(entity1Class.getName(), buffer);
            } else if (entityTransformPair.getFirst() instanceof EntityType) {
                EntityType<?> entity1Type = (EntityType<?>) entityTransformPair.getFirst();
                buffer.writeResourceLocation(Objects.requireNonNull(ForgeRegistries.ENTITY_TYPES.getKey(entity1Type)));
            }
            EntityType<?> entity2Type = entityTransformPair.getSecond();
            buffer.writeResourceLocation(Objects.requireNonNull(ForgeRegistries.ENTITY_TYPES.getKey(entity2Type)));
        }
    }

    @Override
    public void startRitual(Ritual ritual, RitualRecipeWrapper.Result result, Level world, BlockPos pos) {
        if (result.getToCreate() instanceof Pair) {

             Pair<?, ?> pairCheck = (Pair<?, ?>) result.getToCreate();

            if (pairCheck.getSecond() instanceof EntityType<?>) {
                if (pairCheck.getFirst() instanceof Class) {
                    transformMobType(world, pos, (Pair<Class<Entity>, EntityType<?>>) pairCheck, result.getCount());
                } else if (pairCheck.getFirst() instanceof EntityType<?>) {
                    transformMob(world, pos, (Pair<EntityType<Entity>, EntityType<?>>) pairCheck, result.getCount());
                }
            }
        }
    }

    @Override
    public boolean getRunsOnTick() {
        return false;
    }

    @Override
    public ItemStack getIcon(RitualRecipeWrapper.Result result) {
        EntityType<?> entityType = ((Pair<?, EntityType<?>>) result.getToCreate()).getSecond();
        Optional<Item> egg = Optional.ofNullable(ForgeSpawnEggItem.fromEntityType(entityType));
        return egg.map(ItemStack::new).orElse(ItemStack.EMPTY);
    }

    @Override
    public boolean onRitualTick(RitualRecipeWrapper.Result result, Level world, BlockPos pos) {
         return false;
    }

    private void transformMobType(Level world, BlockPos pos, Pair<Class<Entity>, EntityType<?>> pair, int numOfEntityTwo) {
        List<Entity> purifiable = world.getEntitiesOfClass(pair.getFirst(), Ritual.getDefaultBounds(pos));
        if (!purifiable.isEmpty() && !world.isClientSide) {
            world.playSound(null, pos, SoundEvents.ZOMBIE_VILLAGER_CURE,
                    SoundSource.PLAYERS, 1.0f, 1.0f);
        }

        if (!world.isClientSide) for (Entity entity : purifiable) {
            BlockPos pos1 = entity.getOnPos();
            if (entity instanceof ZombieVillager zombie && pair.getSecond() == EntityType.VILLAGER) {
                zombie.finishConversion((ServerLevel) world);
                for (int i = 1; i < numOfEntityTwo; i++) {
                    world.addFreshEntity(entity);
                    entity.setPos(pos1.getX(), pos1.getY(), pos1.getZ());
                }
                return;
            } else {

                Entity newEntity = pair.getSecond().create(world);
                newEntity.copyPosition(entity);

                if (newEntity instanceof Mob mob) {
                    mob.finalizeSpawn((ServerLevel) world, world.getCurrentDifficultyAt(pos),
                            MobSpawnType.MOB_SUMMONED, null, null);
                }
                world.addFreshEntity(newEntity);
                newEntity.setPos(pos1.getX(), pos1.getY(), pos1.getZ());

                if (entity instanceof Player) {
                    entity.kill();
                } else entity.remove(Entity.RemovalReason.KILLED);
            }
        }
    }
    private void transformMob(Level world, BlockPos pos, Pair<EntityType<Entity>, EntityType<?>> pair, int numOfEntityTwo) {
        List<Entity> purifiable = world.getEntities(pair.getFirst(), Ritual.getDefaultBounds(pos), a -> true);

        if (!purifiable.isEmpty() && !world.isClientSide)
            world.playSound(null, pos, SoundEvents.ZOMBIE_VILLAGER_CURE, SoundSource.PLAYERS, 1.0f, 1.0f);
        if (!world.isClientSide) for (Entity entity : purifiable) {
            BlockPos pos1 = entity.getOnPos();
            if (entity instanceof ZombieVillager zombie && pair.getSecond() == EntityType.VILLAGER) {
                zombie.finishConversion((ServerLevel) world);
                for (int i = 1; i < numOfEntityTwo; i++) {
                    world.addFreshEntity(entity);
                    entity.setPos(pos1.getX(), pos1.getY(), pos1.getZ());
                }
                return;
            } else {
                if (entity instanceof Player) {
                    entity.kill();
                } else entity.remove(Entity.RemovalReason.KILLED);

                Entity newEntity = pair.getSecond().create(world);

                if (newEntity instanceof Mob mob) {
                    mob.finalizeSpawn((ServerLevel) world, world.getCurrentDifficultyAt(pos),
                            MobSpawnType.MOB_SUMMONED, null, null);
                }
                for (int i = 0; i < numOfEntityTwo; i++) {
                    world.addFreshEntity(newEntity);
                    newEntity.setPos(pos1.getX(), pos1.getY(), pos1.getZ());
                }
            }
        }
    }

}
