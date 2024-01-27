package dev.infernal_coding.eidolonrecipes.mixin.getters;

import elucent.eidolon.spell.Sign;
import elucent.eidolon.spell.StaticSpell;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(value = StaticSpell.class, remap = false)
public interface StaticSpellMixin {
    @Accessor("signs")
    List<Sign> getSigns();
}
