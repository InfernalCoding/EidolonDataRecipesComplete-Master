package dev.infernal_coding.eidolonrecipes.rituals.result;

import dev.infernal_coding.eidolonrecipes.rituals.RitualRecipeWrapper;
import elucent.eidolon.entity.ai.GoToPositionGoal;
import net.minecraft.entity.CreatureEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3i;
import net.minecraft.world.World;
import net.minecraft.world.gen.Heightmap;

import java.util.List;
import java.util.stream.Collectors;

public class RepelSerializer extends AllureSerializer {
    public static final String ID = "repel";

    @Override
    public boolean onRitualTick(RitualRecipeWrapper.Result result, World world, BlockPos pos) {
        if (result.getToCreate() instanceof EntityType<?>) {
            return repelMobs(world, pos, (EntityType<CreatureEntity>) result.getToCreate());
        } else if (result.getToCreate() instanceof Class<?>) {
            return repelMobType(world, pos, (Class<CreatureEntity>) result.getToCreate());
        }
        return false;
    }

    public boolean repelMobType(World world, BlockPos pos, Class<CreatureEntity> mobType) {
        if (world.getGameTime() % 200 == 0) {
            List<CreatureEntity> monsters = world.getEntitiesWithinAABB(mobType, new AxisAlignedBB(pos).grow(96, 16, 96));
            for (CreatureEntity creatureEntity : monsters) {
                boolean hasGoal = creatureEntity.goalSelector.getRunningGoals().anyMatch((goal) -> goal.getGoal() instanceof GoToPositionGoal);
                if (!hasGoal && creatureEntity.getDistanceSq(pos.getX(), pos.getY(), pos.getZ()) <= 80 * 80) {
                    Vector3i diff = creatureEntity.getPosition().subtract(pos);
                    Vector3d diffv = new Vector3d(diff.getX(), 0, diff.getZ());
                    diffv = diffv.scale(90 / diffv.length());
                    int i = pos.getX() + (int) diffv.x, j = pos.getZ() + (int) diffv.z;
                    BlockPos target = world.getHeight(Heightmap.Type.WORLD_SURFACE, new BlockPos(i, 0, j));
                    creatureEntity.goalSelector.addGoal(1, new GoToPositionGoal(creatureEntity, target, 1.0));
                } else if (hasGoal && creatureEntity.getDistanceSq(pos.getX(), pos.getY(), pos.getZ()) > 88 * 88) {
                    List<Goal> goals = creatureEntity.goalSelector.getRunningGoals().filter((goal) -> goal.getGoal() instanceof GoToPositionGoal)
                            .collect(Collectors.toList());
                    for (Goal g : goals) creatureEntity.goalSelector.removeGoal(g);
                }
            }
        }
        return true;
    }

    public boolean repelMobs(World world, BlockPos pos, EntityType<CreatureEntity> mob) {
        if (world.getGameTime() % 200 == 0) {
            List<CreatureEntity> monsters = world.getEntitiesWithinAABB(mob, new AxisAlignedBB(pos).grow(96, 16, 96), m -> true);
            for (CreatureEntity creatureEntity : monsters) {
                boolean hasGoal = creatureEntity.goalSelector.getRunningGoals()
                        .filter((goal) -> goal.getGoal() instanceof GoToPositionGoal)
                        .count() > 0;
                if (!hasGoal && creatureEntity.getDistanceSq(pos.getX(), pos.getY(), pos.getZ()) <= 80 * 80) {
                    Vector3i diff = creatureEntity.getPosition().subtract(pos);
                    Vector3d diffv = new Vector3d(diff.getX(), 0, diff.getZ());
                    diffv = diffv.scale(90 / diffv.length());
                    int i = pos.getX() + (int) diffv.x, j = pos.getZ() + (int) diffv.z;
                    BlockPos target = world.getHeight(Heightmap.Type.WORLD_SURFACE, new BlockPos(i, 0, j));
                    creatureEntity.goalSelector.addGoal(1, new GoToPositionGoal(creatureEntity, target, 1.0));
                } else if (hasGoal && creatureEntity.getDistanceSq(pos.getX(), pos.getY(), pos.getZ()) > 88 * 88) {
                    List<Goal> goals = creatureEntity.goalSelector.getRunningGoals().filter((goal) -> goal.getGoal() instanceof GoToPositionGoal)
                            .collect(Collectors.toList());
                    for (Goal g : goals) creatureEntity.goalSelector.removeGoal(g);
                }
            }
        }
        return true;
    }
}
