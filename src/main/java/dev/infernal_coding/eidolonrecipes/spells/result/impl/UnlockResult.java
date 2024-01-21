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

public class UnlockResult implements ISpellResult {
    public static final ResourceLocation ID = ModRoot.eidolonRes("unlock");
    private final ResourceLocation lock;

    public UnlockResult(ResourceLocation lock) {
        this.lock = lock;
    }


    @Override
    public void onCast(SpellRecipeWrapper spell, World world, BlockPos pos, PlayerEntity caster, SpellInfo spellInfo) {
        if (spellInfo.reputation.unlock(caster, spell.getDeity().getId(), this.lock)) {
            spell.getDeity().onReputationUnlock(caster, spellInfo.reputation, this.lock);
        }
    }

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    public static class Serializer implements ISpellResultSerializer<UnlockResult> {

        @Override
        public void serialize(JsonObject json, UnlockResult result) {
            json.addProperty("lock", result.lock.toString());
        }

        @Override
        public UnlockResult deserialize(JsonObject json) {
            return new UnlockResult(new ResourceLocation(JSONUtils.getString(json, "lock")));
        }

        @Override
        public void write(PacketBuffer buf, UnlockResult result) {
            buf.writeResourceLocation(result.lock);
        }

        @Override
        public UnlockResult read(PacketBuffer buf) {
            return new UnlockResult(buf.readResourceLocation());
        }

        @Override
        public ResourceLocation getId() {
            return ID;
        }
    }
}
