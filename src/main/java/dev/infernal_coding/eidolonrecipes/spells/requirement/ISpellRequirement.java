package dev.infernal_coding.eidolonrecipes.spells.requirement;

import dev.infernal_coding.eidolonrecipes.spells.SpellInfo;
import dev.infernal_coding.eidolonrecipes.spells.SpellRecipeWrapper;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.ForgeRegistries;

public interface ISpellRequirement {

    boolean canCast(SpellRecipeWrapper spell, Level world, BlockPos pos, Player caster, SpellInfo spellInfo);

    ResourceLocation getId();


    default ResourceLocation getRegistryName(Block block) {
        return ForgeRegistries.BLOCKS.getKey(block);
    }

}
