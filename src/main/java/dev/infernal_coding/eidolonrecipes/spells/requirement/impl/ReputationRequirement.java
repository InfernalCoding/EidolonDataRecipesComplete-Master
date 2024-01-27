package dev.infernal_coding.eidolonrecipes.spells.requirement.impl;

import com.google.gson.JsonObject;
import dev.infernal_coding.eidolonrecipes.ModRoot;
import dev.infernal_coding.eidolonrecipes.spells.SpellInfo;
import dev.infernal_coding.eidolonrecipes.spells.SpellRecipeWrapper;
import dev.infernal_coding.eidolonrecipes.spells.requirement.ISpellRequirement;
import dev.infernal_coding.eidolonrecipes.spells.requirement.ISpellRequirementSerializer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ReputationRequirement implements ISpellRequirement {

    public static final ResourceLocation ID = ModRoot.eidolonRes("reputation");

    private final double reputation;

    public ReputationRequirement(double reputation) {
        this.reputation = reputation;
    }

    @Override
    public boolean canCast(SpellRecipeWrapper spell, World world, BlockPos pos, PlayerEntity caster, SpellInfo spellInfo) {
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
            return new ReputationRequirement(JSONUtils.getFloat(json, "reputation"));
        }

        @Override
        public void write(PacketBuffer buf, ReputationRequirement requirement) {
            buf.writeDouble(requirement.reputation);
        }

        @Override
        public ReputationRequirement read(PacketBuffer buf) {
            return new ReputationRequirement(buf.readDouble());
        }

        @Override
        public ResourceLocation getId() {
            return ID;
        }
    }
}
