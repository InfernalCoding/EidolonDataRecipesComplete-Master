package dev.infernal_coding.eidolonrecipes.rituals.result;

import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Pair;
import dev.infernal_coding.eidolonrecipes.rituals.IRitualResultSerializer;
import dev.infernal_coding.eidolonrecipes.rituals.RitualManager;
import dev.infernal_coding.eidolonrecipes.rituals.RitualRecipeWrapper;
import elucent.eidolon.mixin.ZombieVillagerEntityMixin;
import elucent.eidolon.ritual.Ritual;
import elucent.eidolon.util.ColorUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.monster.ZombieVillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
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
    public RitualManager.ResultColorPair getColorAndResult(PacketBuffer buffer, int color, boolean isColorPreset, String type) {
        String entityOneName = buffer.readString();
        int numOfEntity2 = 1;

        EntityType<?> entityOne = getEntityType(entityOneName);
        EntityType<?> entityTwo = ForgeRegistries.ENTITIES.getValue(buffer.readResourceLocation());

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
    public void writeResult(RitualRecipeWrapper.Result result, PacketBuffer buffer) {
        buffer.writeString(result.getVariant());

        if (result.getToCreate() instanceof Pair) {
            Pair<?, EntityType<?>> entityTransformPair = (Pair<?, EntityType<?>>) result.getToCreate();

            if (entityTransformPair.getFirst() instanceof Class<?>) {
                Class<?> entity1Class = (Class<?>) entityTransformPair.getFirst();
                writeClassName(entity1Class.getName(), buffer);
            } else if (entityTransformPair.getFirst() instanceof EntityType) {
                EntityType<?> entity1Type = (EntityType<?>) entityTransformPair.getFirst();
                buffer.writeResourceLocation(Objects.requireNonNull(entity1Type.getRegistryName()));
            }
            EntityType<?> entity2Type = entityTransformPair.getSecond();
            buffer.writeResourceLocation(Objects.requireNonNull(entity2Type.getRegistryName()));
        }
    }

    @Override
    public void startRitual(RitualRecipeWrapper.Result result, World world, BlockPos pos) {
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
    public boolean onRitualTick(RitualRecipeWrapper.Result result, World world, BlockPos pos) {
         return false;
    }

    private void transformMobType(World world, BlockPos pos, Pair<Class<Entity>, EntityType<?>> pair, int numOfEntityTwo) {
        List<Entity> purifiable = world.getEntitiesWithinAABB(pair.getFirst(), Ritual.getDefaultBounds(pos));
        if (!purifiable.isEmpty() && !world.isRemote) {
            world.playSound(null, pos, SoundEvents.ENTITY_ZOMBIE_VILLAGER_CURE,
                    SoundCategory.PLAYERS, 1.0f, 1.0f);
        }

        if (!world.isRemote) for (Entity entity : purifiable) {
            if (entity instanceof ZombieVillagerEntity && pair.getSecond() == EntityType.VILLAGER) {
                ((ZombieVillagerEntityMixin) entity).callCureZombie((ServerWorld) world);
                for (int i = 1; i < numOfEntityTwo; i++) {
                    world.addEntity(entity);
                }
                return;
            } else {

                Entity newEntity = pair.getSecond().create(world);
                newEntity.copyLocationAndAnglesFrom(entity);

                if (newEntity instanceof MobEntity) {
                    ((MobEntity) newEntity).onInitialSpawn((ServerWorld) world, world.getDifficultyForLocation(pos),
                            SpawnReason.MOB_SUMMONED, null, null);
                }
                world.addEntity(newEntity);

                if (entity instanceof PlayerEntity) {
                    entity.onKillCommand();
                } else entity.remove();
            }
        }
    }
    private void transformMob(World world, BlockPos pos, Pair<EntityType<Entity>, EntityType<?>> pair, int numOfEntityTwo) {
        List<Entity> purifiable = world.getEntitiesWithinAABB(pair.getFirst(), Ritual.getDefaultBounds(pos), a -> true);

        if (!purifiable.isEmpty() && !world.isRemote)
            world.playSound(null, pos, SoundEvents.ENTITY_ZOMBIE_VILLAGER_CURE, SoundCategory.PLAYERS, 1.0f, 1.0f);
        if (!world.isRemote) for (Entity entity : purifiable) {
            if (entity instanceof ZombieVillagerEntity && pair.getSecond() == EntityType.VILLAGER) {
                ((ZombieVillagerEntityMixin) entity).callCureZombie((ServerWorld) world);
                for (int i = 1; i < numOfEntityTwo; i++) {
                    world.addEntity(entity);
                }
                return;
            } else {
                BlockPos pos1 = entity.getPosition();
                if (entity instanceof PlayerEntity) {
                    entity.onKillCommand();
                } else entity.remove();

                Entity newEntity = pair.getSecond().create(world);

                if (newEntity instanceof MobEntity) {
                    ((MobEntity) newEntity).onInitialSpawn((ServerWorld) world, world.getDifficultyForLocation(pos),
                            SpawnReason.MOB_SUMMONED, null, null);
                }
                for (int i = 0; i < numOfEntityTwo; i++) {
                    world.addEntity(newEntity);
                    newEntity.setPosition(pos1.getX(), pos1.getY(), pos1.getZ());
                }
            }
        }
    }

}
