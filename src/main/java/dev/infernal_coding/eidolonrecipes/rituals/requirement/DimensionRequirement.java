package dev.infernal_coding.eidolonrecipes.rituals.requirement;

import elucent.eidolon.ritual.IRequirement;
import elucent.eidolon.ritual.RequirementInfo;
import elucent.eidolon.ritual.Ritual;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

public class DimensionRequirement implements IRequirement {
    public ResourceLocation dimension;
    public DimensionRequirement(ResourceLocation dimension) {
        this.dimension = dimension;
    }

    @Override
    public RequirementInfo isMet(Ritual ritual, Level world, BlockPos blockPos) {

        if (world.dimension().registry().equals(dimension)) {
            return RequirementInfo.TRUE;
        }
        return RequirementInfo.FALSE;
    }
}
