package dev.infernal_coding.eidolonrecipes.rituals.result;

import com.google.gson.JsonObject;
import dev.infernal_coding.eidolonrecipes.rituals.IRitualResultSerializer;
import dev.infernal_coding.eidolonrecipes.rituals.RitualManager;
import dev.infernal_coding.eidolonrecipes.rituals.RitualRecipeWrapper;
import dev.infernal_coding.eidolonrecipes.util.EntityUtil;
import dev.infernal_coding.eidolonrecipes.util.JSONUtils;
import elucent.eidolon.entity.ai.GoToPositionGoal;
import elucent.eidolon.ritual.Ritual;
import elucent.eidolon.util.ColorUtil;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static dev.infernal_coding.eidolonrecipes.util.EntityUtil.getEntityType;
import static dev.infernal_coding.eidolonrecipes.util.ItemUtil.getEntityFromJson;

public class AllureSerializer implements IRitualResultSerializer {
    public static final String ID = "allure";
    @Override
    public RitualManager.ResultColorPair getColorAndResult(JsonObject json, int color, boolean isColorPreset, String type) {
        EntityType<?> entityType = getEntityFromJson(json.get("entity"));
        String entityName = JSONUtils.getString(json, "entity", "");

        if (entityType != null) {
            EntityUtil.Container container = new EntityUtil.Container(entityType, 1);

            if (color > 0 && !isColorPreset) {
                color -= ColorUtil.packColor(100, 30, 25, 40);
            } else if (!isColorPreset) {
                color += ColorUtil.packColor(100, 35, 80, 175);
            }
            return new RitualManager.ResultColorPair(color, 1, container, type);
        } else
            return IRitualResultSerializer.getClassTypeAndColor(ID, entityName, color, 1, isColorPreset);
    }

    @Override
    public RitualManager.ResultColorPair getColorAndResult(FriendlyByteBuf buffer, int color, boolean isColorPreset, String type) {
        String name = buffer.readUtf();

        if (getEntityType(name) != null) {
            if (color > 0 && !isColorPreset) {
                color -= ColorUtil.packColor(100, 30, 25, 40);
            } else if (!isColorPreset) {
                color += ColorUtil.packColor(100, 35, 80, 175);
            }
            return new RitualManager.ResultColorPair(color, 1, getEntityType(name), type);
        }
        return IRitualResultSerializer.getClassTypeAndColor(ID, name, color,1, isColorPreset);
    }

    @Override
    public void writeResult(RitualRecipeWrapper.Result result, FriendlyByteBuf buffer) {
        buffer.writeUtf(result.getVariant());

        if (result.getToCreate() instanceof Class<?>) {
            Class<?> entityClass = (Class<?>) result.getToCreate();
            writeClassName(entityClass.getName(), buffer);
        } else if (result.getToCreate() instanceof EntityType<?>) {
            EntityType<?> entityType = (EntityType<?>) result.getToCreate();
            buffer.writeResourceLocation(Objects.requireNonNull(ForgeRegistries.ENTITY_TYPES.getKey(entityType)));
        }
    }

    @Override
    public void startRitual(Ritual ritual, RitualRecipeWrapper.Result result, Level world, BlockPos pos) {}

    @Override
    public boolean onRitualTick(RitualRecipeWrapper.Result result, Level world, BlockPos pos) {
        if (result.getToCreate() instanceof EntityType<?>) {
            return lureMobs(world, pos, (EntityType<PathfinderMob>) result.getToCreate());
        } else if (result.getToCreate() instanceof Class<?>) {
            return lureMobType(world, pos, (Class<PathfinderMob>) result.getToCreate());
        }

        return false;
    }

    @Override
    public boolean getRunsOnTick() {
        return true;
    }

    @Override
    public ItemStack getIcon(RitualRecipeWrapper.Result result) {
        if (result.getToCreate() instanceof EntityType<?>) {
            EntityType<?> entityType = (EntityType<?>) result.getToCreate();
            Optional<Item> spawnEgg = Optional.ofNullable(ForgeSpawnEggItem.fromEntityType(entityType));
            return spawnEgg.map(ItemStack::new).orElse(new ItemStack(Items.FOX_SPAWN_EGG));
        }
        return new ItemStack(Items.FOX_SPAWN_EGG);
    }

    private boolean lureMobs(Level world, BlockPos pos, EntityType<PathfinderMob> entityType) {
        if (world.getGameTime() % 200 == 0) {

            List<PathfinderMob> entities = world.getEntities(entityType, new AABB(pos).inflate(96, 16, 96), s -> true);
            for (PathfinderMob entity : entities) {
                boolean hasGoal = entity.goalSelector.getRunningGoals()
                        .filter((goal) -> goal.getGoal() instanceof GoToPositionGoal)
                        .count() > 0;
                if (!hasGoal && entity.distanceToSqr(pos.getX(), pos.getY(), pos.getZ()) >= 12 * 12 && world.random.nextInt(40) == 0) {
                    BlockPos target = pos.below().offset(world.random.nextInt(9) - 4, 0, world.random.nextInt(9) - 4);
                    entity.goalSelector.addGoal(1, new GoToPositionGoal(entity, target, 1.0));
                } else if (hasGoal && entity.distanceToSqr(pos.getX(), pos.getY(), pos.getZ()) < 8 * 8) {
                    List<Goal> goals = entity.goalSelector.getRunningGoals().filter((goal) -> goal.getGoal() instanceof GoToPositionGoal)
                            .collect(Collectors.toList());
                    for (Goal g : goals) entity.goalSelector.removeGoal(g);
                }
            }
        }
        return true;
    }

    private boolean lureMobType(Level world, BlockPos pos, Class<PathfinderMob> entityType) {
        if (world.getGameTime() % 200 == 0) {

            List<PathfinderMob> entities = world.getEntitiesOfClass(entityType, new AABB(pos).inflate(96, 16, 96));
            for (PathfinderMob entity : entities) {
                boolean hasGoal = entity.goalSelector.getRunningGoals()
                        .filter((goal) -> goal.getGoal() instanceof GoToPositionGoal)
                        .count() > 0;

                if (!hasGoal && entity.distanceToSqr(pos.getX(), pos.getY(), pos.getZ()) >= 12 * 12 && world.random.nextInt(40) == 0) {
                    BlockPos target = pos.below().offset(world.random.nextInt(9) - 4, 0, world.random.nextInt(9) - 4);
                    entity.goalSelector.addGoal(1, new GoToPositionGoal(entity, target, 1.0));
                } else if (hasGoal && entity.distanceToSqr(pos.getX(), pos.getY(), pos.getZ()) < 8 * 8) {
                    List<Goal> goals = entity.goalSelector.getRunningGoals().filter((goal) -> goal.getGoal() instanceof GoToPositionGoal)
                            .collect(Collectors.toList());
                    for (Goal g : goals) entity.goalSelector.removeGoal(g);
                }
            }
        }
        return true;
    }
}
