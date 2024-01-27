package dev.infernal_coding.eidolonrecipes.spells.requirement.impl;

import com.google.gson.JsonObject;
import dev.infernal_coding.eidolonrecipes.ModRoot;
import dev.infernal_coding.eidolonrecipes.spells.SpellInfo;
import dev.infernal_coding.eidolonrecipes.spells.SpellRecipeWrapper;
import dev.infernal_coding.eidolonrecipes.spells.requirement.ISpellRequirement;
import dev.infernal_coding.eidolonrecipes.spells.requirement.ISpellRequirementSerializer;
import dev.infernal_coding.eidolonrecipes.util.JSONUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class ReputationRequirement implements ISpellRequirement {

    public static final ResourceLocation ID = ModRoot.eidolonRes("reputation");

    private final double reputation;

    public ReputationRequirement(double reputation) {
        this.reputation = reputation;
    }

    @Override
    public boolean canCast(SpellRecipeWrapper spell, Level world, BlockPos pos, Player caster, SpellInfo spellInfo) {
        return spellInfo.reputation.getReputation(caster, spell.getDeity().getId()) >= this.reputation;
    }

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    public static class Serializer implements ISpellRequirementSerializer<ReputationRequirement> {

        @Override
        public void serialize(JsonObject json, ReputationRequirement requirement) {
            json.addProperty("reputation", requirement.reputation);
        }

        @Override
        public ReputationRequirement deserialize(JsonObject json) {
            return new ReputationRequirement(JSONUtils.getFloat(json, "reputation", 0));
        }

        @Override
        public void write(FriendlyByteBuf buf, ReputationRequirement requirement) {
            buf.writeDouble(requirement.reputation);
        }

        @Override
        public ReputationRequirement read(FriendlyByteBuf buf) {
            return new ReputationRequirement(buf.readDouble());
        }

        @Override
        public ResourceLocation getId() {
            return ID;
        }
    }
}
