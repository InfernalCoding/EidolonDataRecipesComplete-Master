package dev.infernal_coding.eidolonrecipes.spells.result.impl;

import com.google.gson.JsonObject;
import dev.infernal_coding.eidolonrecipes.spells.result.ISpellResult;
import dev.infernal_coding.eidolonrecipes.ModRoot;
import dev.infernal_coding.eidolonrecipes.spells.SpellInfo;
import dev.infernal_coding.eidolonrecipes.spells.SpellRecipeWrapper;
import dev.infernal_coding.eidolonrecipes.spells.result.ISpellResultSerializer;
import elucent.eidolon.tile.EffigyTileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class PrayResult implements ISpellResult {
    public static final ResourceLocation ID = ModRoot.eidolonRes("pray");

    @Override
    public void onCast(SpellRecipeWrapper spell, World world, BlockPos pos, PlayerEntity caster, SpellInfo spellInfo) {
        spellInfo.effigy.ifPresent(EffigyTileEntity::pray);
        spellInfo.reputation.pray(caster, world.getGameTime());
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
        public void write(PacketBuffer buf, PrayResult obj) {

        }

        @Override
        public PrayResult read(PacketBuffer buf) {
            return new PrayResult();
        }

        @Override
        public ResourceLocation getId() {
            return ID;
        }
    }
}
