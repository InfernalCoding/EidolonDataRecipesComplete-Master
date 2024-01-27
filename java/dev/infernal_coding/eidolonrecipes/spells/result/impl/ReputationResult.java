package dev.infernal_coding.eidolonrecipes.spells.result.impl;

import com.google.gson.JsonObject;
import dev.infernal_coding.eidolonrecipes.ModRoot;
import dev.infernal_coding.eidolonrecipes.spells.SpellInfo;
import dev.infernal_coding.eidolonrecipes.spells.SpellRecipeWrapper;
import dev.infernal_coding.eidolonrecipes.spells.result.ISpellResult;
import dev.infernal_coding.eidolonrecipes.spells.result.ISpellResultSerializer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ReputationResult implements ISpellResult {
    public static final ResourceLocation ID = ModRoot.eidolonRes("reputation");

	private final double constant;
	private final double altarMultiplier;

    public ReputationResult(double constant, double altarMultiplier) {
        this.constant = constant;
        this.altarMultiplier = altarMultiplier;
    }

    @Override
	public void onCast(SpellRecipeWrapper spell, World world, BlockPos pos, PlayerEntity caster, SpellInfo spellInfo) {
        double altarRep = spellInfo.altar.map(altar -> this.altarMultiplier * altar.getPower()).orElse(0.0);
        spellInfo.reputation.pray(caster, world.getGameTime());
        double prev = spellInfo.reputation.getReputation(caster, spell.getDeity().getId());
        spellInfo.reputation.addReputation(caster, spell.getDeity().getId(), this.constant + altarRep);
        spell.getDeity().onReputationChange(caster, spellInfo.reputation, prev, spellInfo.reputation.getReputation(caster, spell.getDeity().getId()));
	}

    @Override
    public ResourceLocation getId() {
        return ID;
    }

	public static class Serializer implements ISpellResultSerializer<ReputationResult> {


        @Override
        public void serialize(JsonObject json, ReputationResult result) {
            json.addProperty("constant", result.constant);
            json.addProperty("altar", result.altarMultiplier);
        }

        @Override
		public ReputationResult deserialize(JsonObject json) {
			return new ReputationResult(JSONUtils.getFloat(json, "constant", 0), JSONUtils.getFloat(json, "altar", 0));
		}

		@Override
		public void write(PacketBuffer buf, ReputationResult obj) {
			buf.writeDouble(obj.constant);
			buf.writeDouble(obj.altarMultiplier);
		}

		@Override
		public ReputationResult read(PacketBuffer buf) {
			double constant = buf.readDouble();
            double altar = buf.readDouble();
			return new ReputationResult(constant, altar);
		}

        @Override
        public ResourceLocation getId() {
            return ID;
        }
    }
}
