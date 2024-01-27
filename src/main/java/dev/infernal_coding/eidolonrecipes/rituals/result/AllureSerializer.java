package dev.infernal_coding.eidolonrecipes.rituals.result;

import com.google.gson.JsonObject;
import dev.infernal_coding.eidolonrecipes.rituals.IRitualResultSerializer;
import dev.infernal_coding.eidolonrecipes.rituals.RitualManager;
import dev.infernal_coding.eidolonrecipes.rituals.RitualRecipeWrapper;
import dev.infernal_coding.eidolonrecipes.util.EntityUtil;
import elucent.eidolon.entity.ai.GoToPositionGoal;
import elucent.eidolon.util.ColorUtil;
import net.minecraft.entity.CreatureEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeSpawnEggItem;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static dev.infernal_coding.eidolonrecipes.util.EntityUtil.getEntityType;
import static dev.infernal_coding.eidolonrecipes.util.ItemUtil.getEntityFromJson;
import static dev.infernal_coding.eidolonrecipes.rituals.IRitualResultSerializer.*;

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
    public RitualManager.ResultColorPair getColorAndResult(PacketBuffer buffer, int color, boolean isColorPreset, String type) {
        String name = buffer.readString();

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
    public void writeResult(RitualRecipeWrapper.Result result, PacketBuffer buffer) {
        buffer.writeString(result.getVariant());

        if (result.getToCreate() instanceof Class<?>) {
            Class<?> entityClass = (Class<?>) result.getToCreate();
            writeClassName(entityClass.getName(), buffer);
        } else if (result.getToCreate() instanceof EntityType<?>) {
            EntityType<?> entityType = (EntityType<?>) result.getToCreate();
            buffer.writeResourceLocation(Objects.requireNonNull(entityType.getRegistryName()));
        }
    }

    @Override
    public void startRitual(RitualRecipeWrapper.Result result, World world, BlockPos pos) {}
    @Override
    public boolean onRitualTick(RitualRecipeWrapper.Result result, World world, BlockPos pos) {
        if (result.getToCreate() instanceof EntityType<?>) {
            return lureMobs(world, pos, (EntityType<CreatureEntity>) result.getToCreate());
        } else if (result.getToCreate() instanceof Class<?>) {
            return lureMobType(world, pos, (Class<CreatureEntity>) result.getToCreate());
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

    private boolean lureMobs(World world, BlockPos pos, EntityType<CreatureEntity> entityType) {
        if (world.getGameTime() % 200 == 0) {

            List<CreatureEntity> entities = world.getEntitiesWithinAABB(entityType, new AxisAlignedBB(pos).grow(96, 16, 96), null);
            for (CreatureEntity entity : entities) {
                boolean hasGoal = entity.goalSelector.getRunningGoals()
                        .filter((goal) -> goal.getGoal() instanceof GoToPositionGoal)
                        .count() > 0;
                if (!hasGoal && entity.getDistanceSq(pos.getX(), pos.getY(), pos.getZ()) >= 12 * 12 && world.rand.nextInt(40) == 0) {
                    BlockPos target = pos.down().add(world.rand.nextInt(9) - 4, 0, world.rand.nextInt(9) - 4);
                    entity.goalSelector.addGoal(1, new GoToPositionGoal(entity, target, 1.0));
                } else if (hasGoal && entity.getDistanceSq(pos.getX(), pos.getY(), pos.getZ()) < 8 * 8) {
                    List<Goal> goals = entity.goalSelector.getRunningGoals().filter((goal) -> goal.getGoal() instanceof GoToPositionGoal)
                            .collect(Collectors.toList());
                    for (Goal g : goals) entity.goalSelector.removeGoal(g);
                }
            }
        }
        return true;
    }

    private boolean lureMobType(World world, BlockPos pos, Class<CreatureEntity> entityType) {
        if (world.getGameTime() % 200 == 0) {

            List<CreatureEntity> entities = world.getEntitiesWithinAABB(entityType, new AxisAlignedBB(pos).grow(96, 16, 96));
            for (CreatureEntity entity : entities) {
                boolean hasGoal = entity.goalSelector.getRunningGoals()
                        .filter((goal) -> goal.getGoal() instanceof GoToPositionGoal)
                        .count() > 0;

                if (!hasGoal && entity.getDistanceSq(pos.getX(), pos.getY(), pos.getZ()) >= 12 * 12 && world.rand.nextInt(40) == 0) {
                    BlockPos target = pos.down().add(world.rand.nextInt(9) - 4, 0, world.rand.nextInt(9) - 4);
                    entity.goalSelector.addGoal(1, new GoToPositionGoal(entity, target, 1.0));
                } else if (hasGoal && entity.getDistanceSq(pos.getX(), pos.getY(), pos.getZ()) < 8 * 8) {
                    List<Goal> goals = entity.goalSelector.getRunningGoals().filter((goal) -> goal.getGoal() instanceof GoToPositionGoal)
                            .collect(Collectors.toList());
                    for (Goal g : goals) entity.goalSelector.removeGoal(g);
                }
            }
        }
        return true;
    }
}
