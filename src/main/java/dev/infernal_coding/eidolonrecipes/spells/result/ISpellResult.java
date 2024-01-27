package dev.infernal_coding.eidolonrecipes.spells.result;

import dev.infernal_coding.eidolonrecipes.spells.SpellInfo;
import dev.infernal_coding.eidolonrecipes.spells.SpellRecipeWrapper;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public interface ISpellResult {

	void onCast(SpellRecipeWrapper spell, Level world, BlockPos pos, Player caster, SpellInfo spellInfo);

    ResourceLocation getId();

}
