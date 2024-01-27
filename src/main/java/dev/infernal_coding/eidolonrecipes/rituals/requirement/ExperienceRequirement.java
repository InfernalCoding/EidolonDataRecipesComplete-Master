package dev.infernal_coding.eidolonrecipes.rituals.requirement;

import elucent.eidolon.ritual.IRequirement;
import elucent.eidolon.ritual.RequirementInfo;
import elucent.eidolon.ritual.Ritual;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import java.util.List;

public class ExperienceRequirement implements IRequirement {
    public int experienceLevel;
    public ExperienceRequirement(int experienceLevel) {
        this.experienceLevel = experienceLevel;
    }
    @Override
    public RequirementInfo isMet(Ritual ritual, Level world, BlockPos blockPos) {
        List<Player> players = world.getEntitiesOfClass(Player.class, ritual.getSearchBounds(blockPos));
        for (Player player : players) {
            if (player.experienceLevel >= experienceLevel) {
                return RequirementInfo.TRUE;
            }
        }
        return RequirementInfo.FALSE;
    }

    @Override
    public void whenMet(Ritual ritual, Level world, BlockPos pos, RequirementInfo info) {
        List<Player> players = world.getEntitiesOfClass(Player.class, ritual.getSearchBounds(pos));
        for (Player player : players) {
            if (player.experienceLevel >= experienceLevel) {
                player.giveExperienceLevels(-experienceLevel);
            }
        }
    }
}
