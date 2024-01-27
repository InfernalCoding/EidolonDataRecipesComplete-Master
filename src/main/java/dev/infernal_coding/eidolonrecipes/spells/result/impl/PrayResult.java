package dev.infernal_coding.eidolonrecipes.spells.result.impl;

import com.google.gson.JsonObject;
import dev.infernal_coding.eidolonrecipes.ModRoot;
import dev.infernal_coding.eidolonrecipes.spells.SpellInfo;
import dev.infernal_coding.eidolonrecipes.spells.SpellRecipeWrapper;
import dev.infernal_coding.eidolonrecipes.spells.result.ISpellResult;
import dev.infernal_coding.eidolonrecipes.spells.result.ISpellResultSerializer;
import elucent.eidolon.tile.EffigyTileEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class PrayResult implements ISpellResult {
    public static final ResourceLocation ID = ModRoot.eidolonRes("pray");

    @Override
    public void onCast(SpellRecipeWrapper spell, Level world, BlockPos pos, Player caster, SpellInfo spellInfo) {
        spellInfo.effigy.ifPresent(EffigyTileEntity::pray);
        spellInfo.reputation.pray(caster, spell.getRegistryName(), world.getGameTime());
    }

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    public static class Serializer implements ISpellResultSerializer<PrayResult> {

        @Override
        public void serialize(JsonObject json, PrayResult result) {

        }

        @Override
        public PrayResult deserialize(JsonObject json) {
            return new PrayResult();
        }

        @Override
        public void write(FriendlyByteBuf buf, PrayResult obj) {

        }

        @Override
        public PrayResult read(FriendlyByteBuf buf) {
            return new PrayResult();
        }

        @Override
        public ResourceLocation getId() {
            return ID;
        }
    }
}
