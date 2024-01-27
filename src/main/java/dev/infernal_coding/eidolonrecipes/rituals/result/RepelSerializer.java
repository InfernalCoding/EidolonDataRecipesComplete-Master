package dev.infernal_coding.eidolonrecipes.rituals.result;

import com.mojang.math.Vector3d;
import dev.infernal_coding.eidolonrecipes.rituals.RitualRecipeWrapper;
import elucent.eidolon.entity.ai.GoToPositionGoal;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;


import java.util.List;
import java.util.stream.Collectors;

import static java.lang.Math.sqrt;

public class RepelSerializer extends AllureSerializer {
    public static final String ID = "repel";

    @Override
    public boolean onRitualTick(RitualRecipeWrapper.Result result, Level world, BlockPos pos) {
        if (result.getToCreate() instanceof EntityType<?>) {
            return repelMobs(world, pos, (EntityType<PathfinderMob>) result.getToCreate());
        } else if (result.getToCreate() instanceof Class<?>) {
            return repelMobType(world, pos, (Class<PathfinderMob>) result.getToCreate());
        }
        return false;
    }

    public boolean repelMobType(Level world, BlockPos pos, Class<PathfinderMob> mobType) {
        if (world.getGameTime() % 200 == 0) {
            List<PathfinderMob> monsters = world.getEntitiesOfClass(mobType, new AABB(pos).inflate(96, 16, 96));
            for (PathfinderMob creatureEntity : monsters) {
                boolean hasGoal = creatureEntity.goalSelector.getRunningGoals().anyMatch((goal) -> goal.getGoal() instanceof GoToPositionGoal);
                if (!hasGoal && creatureEntity.distanceToSqr(pos.getX(), pos.getY(), pos.getZ()) <= 80 * 80) {
                    Vec3 diff = creatureEntity.position().subtract(new Vec3(pos.getX(), pos.getY(), pos.getZ()));
                    Vector3d diffv = new Vector3d(diff.x, 0, diff.z);
                    diffv.scale(90 / vectorLength(diffv));
                    int i = pos.getX() + (int) diffv.x, j = pos.getZ() + (int) diffv.z;
                    int y = world.getHeight(Heightmap.Types.WORLD_SURFACE, i, j);
                    creatureEntity.goalSelector.addGoal(1, new GoToPositionGoal(creatureEntity, new BlockPos(i, y, j), 1.0));
                } else if (hasGoal && creatureEntity.distanceToSqr(pos.getX(), pos.getY(), pos.getZ()) > 88 * 88) {
                    List<Goal> goals = creatureEntity.goalSelector.getRunningGoals().filter((goal) -> goal.getGoal() instanceof GoToPositionGoal)
                            .collect(Collectors.toList());
                    for (Goal g : goals) creatureEntity.goalSelector.removeGoal(g);
                }
            }
        }
        return true;
    }

    public boolean repelMobs(Level world, BlockPos pos, EntityType<PathfinderMob> mob) {
        if (world.getGameTime() % 200 == 0) {
            List<PathfinderMob> monsters = world.getEntities(mob, new AABB(pos).inflate(96, 16, 96), m -> true);
            for (PathfinderMob creatureEntity : monsters) {
                boolean hasGoal = creatureEntity.goalSelector.getRunningGoals()
                        .filter((goal) -> goal.getGoal() instanceof GoToPositionGoal)
                        .count() > 0;
                if (!hasGoal && creatureEntity.distanceToSqr(pos.getX(), pos.getY(), pos.getZ()) <= 80 * 80) {
                    Vec3 diff = creatureEntity.position().subtract(new Vec3(pos.getX(), pos.getY(), pos.getZ()));
                    Vector3d diffv = new Vector3d(diff.x(), 0, diff.z());
                    diffv.scale(90 / vectorLength(diffv));
                    int i = pos.getX() + (int) diffv.x, j = pos.getZ() + (int) diffv.z;
                    int y = world.getHeight(Heightmap.Types.WORLD_SURFACE, i, j);
                    creatureEntity.goalSelector.addGoal(1, new GoToPositionGoal(creatureEntity, new BlockPos(i, y, j), 1.0));
                } else if (hasGoal && creatureEntity.distanceToSqr(pos.getX(), pos.getY(), pos.getZ()) > 88 * 88) {
                    List<Goal> goals = creatureEntity.goalSelector.getRunningGoals().filter((goal) -> goal.getGoal() instanceof GoToPositionGoal)
                            .collect(Collectors.toList());
                    for (Goal g : goals) creatureEntity.goalSelector.removeGoal(g);
                }
            }
        }
        return true;
    }

    public double vectorLength(Vector3d vec) {
        return sqrt(vec.y * vec.y + vec.x * vec.x + vec.z * vec.z);
    }
}
