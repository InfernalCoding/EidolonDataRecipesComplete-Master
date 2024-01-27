package dev.infernal_coding.eidolonrecipes.rituals.requirement;

import elucent.eidolon.ritual.IRequirement;
import elucent.eidolon.ritual.RequirementInfo;
import elucent.eidolon.ritual.Ritual;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.List;

public class ExperienceRequirement implements IRequirement {
    public int experienceLevel;
    public ExperienceRequirement(int experienceLevel) {
        this.experienceLevel = experienceLevel;
    }
    @Override
    public RequirementInfo isMet(Ritual ritual, World world, BlockPos blockPos) {
        List<PlayerEntity> players = world.getEntitiesWithinAABB(PlayerEntity.class, ritual.getSearchBounds(blockPos));
        for (PlayerEntity player : players) {
            if (player.experienceLevel >= experienceLevel) {
                return RequirementInfo.TRUE;
            }
        }
        return RequirementInfo.FALSE;
    }

    @Override
    public void whenMet(Ritual ritual, World world, BlockPos pos, RequirementInfo info) {
        List<PlayerEntity> players = world.getEntitiesWithinAABB(PlayerEntity.class, ritual.getSearchBounds(pos));
        for (PlayerEntity player : players) {
            if (player.experienceLevel >= experienceLevel) {
                player.addExperienceLevel(-experienceLevel);
            }
        }
    }
}
