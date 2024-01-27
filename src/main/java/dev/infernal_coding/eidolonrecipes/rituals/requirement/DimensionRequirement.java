package dev.infernal_coding.eidolonrecipes.rituals.requirement;

import elucent.eidolon.ritual.IRequirement;
import elucent.eidolon.ritual.RequirementInfo;
import elucent.eidolon.ritual.Ritual;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class DimensionRequirement implements IRequirement {
    public ResourceLocation dimension;
    public DimensionRequirement(ResourceLocation dimension) {
        this.dimension = dimension;
    }

    @Override
    public RequirementInfo isMet(Ritual ritual, World world, BlockPos blockPos) {

        if (world.getDimensionKey().getLocation().equals(dimension)) {
            return RequirementInfo.TRUE;
        }
        return RequirementInfo.FALSE;
    }
}
