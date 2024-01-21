package dev.infernal_coding.eidolonrecipes.spells.type;

import dev.infernal_coding.eidolonrecipes.spells.SpellInfo;
import dev.infernal_coding.eidolonrecipes.spells.SpellRecipeWrapper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface ISpell {

    default boolean canCast(SpellRecipeWrapper spell, World world, BlockPos pos, PlayerEntity caster, SpellInfo spellInfo){
        return true;
    }

    default void onCast(SpellRecipeWrapper spell, World world, BlockPos pos, PlayerEntity caster, SpellInfo spellInfo) {

    }

    ResourceLocation getId();
}
