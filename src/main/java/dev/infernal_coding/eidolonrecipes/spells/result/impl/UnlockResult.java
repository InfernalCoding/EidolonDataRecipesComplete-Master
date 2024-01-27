package dev.infernal_coding.eidolonrecipes.spells.result.impl;

import com.google.gson.JsonObject;
import dev.infernal_coding.eidolonrecipes.ModRoot;
import dev.infernal_coding.eidolonrecipes.spells.SpellInfo;
import dev.infernal_coding.eidolonrecipes.spells.SpellRecipeWrapper;
import dev.infernal_coding.eidolonrecipes.spells.result.ISpellResult;
import dev.infernal_coding.eidolonrecipes.spells.result.ISpellResultSerializer;
import dev.infernal_coding.eidolonrecipes.util.JSONUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class UnlockResult implements ISpellResult {
    public static final ResourceLocation ID = ModRoot.eidolonRes("unlock");
    private final ResourceLocation lock;

    public UnlockResult(ResourceLocation lock) {
        this.lock = lock;
    }


    @Override
    public void onCast(SpellRecipeWrapper spell, Level world, BlockPos pos, Player caster, SpellInfo spellInfo) {
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
            return new UnlockResult(new ResourceLocation(JSONUtils.getString(json, "lock", "")));
        }

        @Override
        public void write(FriendlyByteBuf buf, UnlockResult result) {
            buf.writeResourceLocation(result.lock);
        }

        @Override
        public UnlockResult read(FriendlyByteBuf buf) {
            return new UnlockResult(buf.readResourceLocation());
        }

        @Override
        public ResourceLocation getId() {
            return ID;
        }
    }
}
