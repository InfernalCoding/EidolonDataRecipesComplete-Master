package dev.infernal_coding.eidolonrecipes.spells.requirement.impl;

import com.google.gson.JsonObject;
import dev.infernal_coding.eidolonrecipes.ModRoot;
import dev.infernal_coding.eidolonrecipes.spells.SpellInfo;
import dev.infernal_coding.eidolonrecipes.spells.SpellRecipeWrapper;
import dev.infernal_coding.eidolonrecipes.spells.requirement.ISpellRequirement;
import dev.infernal_coding.eidolonrecipes.spells.requirement.ISpellRequirementSerializer;
import dev.infernal_coding.eidolonrecipes.util.JSONUtils;
import elucent.eidolon.spell.AltarInfo;
import elucent.eidolon.tile.EffigyTileEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Optional;

public class AltarRequirement implements ISpellRequirement {

    public static final ResourceLocation ID = ModRoot.eidolonRes("altar");

    public Optional<Block> getRequiredEffigy() {
        return requiredEffigy;
    }

    public Optional<Block> getRequiredAltar() {
        return requiredAltar;
    }

    private final Optional<Block> requiredEffigy;
    private final Optional<Block> requiredAltar;

    public AltarRequirement(Optional<Block> requiredEffigy, Optional<Block> requiredAltar) {
        this.requiredEffigy = requiredEffigy;
        this.requiredAltar = requiredAltar;
    }


    @Override
    public boolean canCast(SpellRecipeWrapper spell, Level world, BlockPos pos, Player caster, SpellInfo spellInfo) {

        if (!spellInfo.reputation.canPray(caster, spell.getRegistryName(), world.getGameTime())) {
            return false;
        } else {
            if (!spellInfo.effigy.isPresent() || !spellInfo.altar.isPresent()) {
                return false;
            }

            EffigyTileEntity effigy = spellInfo.effigy.get();
            if (!effigy.ready()) {
                return false;
            }

            AltarInfo altar = spellInfo.altar.get();
            return this.requiredAltar.map(b -> b == altar.getAltar()).orElse(true) &&
                this.requiredEffigy.map(b -> b == altar.getIcon()).orElse(true);
        }
    }

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    public static class Serializer implements ISpellRequirementSerializer<AltarRequirement> {



        @Override
        public void serialize(JsonObject json, AltarRequirement requirement) {
            json.addProperty("effigy", requirement.getRegistryName(requirement.requiredEffigy.get()).toString());
            json.addProperty("altar", requirement.getRegistryName(requirement.requiredAltar.get()).toString());
        }

        @Override
        public AltarRequirement deserialize(JsonObject json) {
            Optional<Block> effigy = JSONUtils.getOptionalResourceLocation(json, "effigy").map(ForgeRegistries.BLOCKS::getValue);
            Optional<Block> altar = JSONUtils.getOptionalResourceLocation(json, "altar").map(ForgeRegistries.BLOCKS::getValue);
            return new AltarRequirement(effigy, altar);
        }

        @Override
        public void write(FriendlyByteBuf buf, AltarRequirement requirement) {
            // Effigy
            buf.writeBoolean(requirement.requiredEffigy.isPresent());
            requirement.requiredEffigy.ifPresent(effigy -> buf.writeResourceLocation(requirement.getRegistryName(effigy)));

            // Altar
            buf.writeBoolean(requirement.requiredAltar.isPresent());
            requirement.requiredAltar.ifPresent(altar -> buf.writeResourceLocation(requirement.getRegistryName(altar)));
        }

        @Override
        public AltarRequirement read(FriendlyByteBuf buf) {
            Optional<Block> effigy = Optional.ofNullable(buf.readBoolean() ? ForgeRegistries.BLOCKS.getValue(buf.readResourceLocation()) : null);
            Optional<Block> altar = Optional.ofNullable(buf.readBoolean() ? ForgeRegistries.BLOCKS.getValue(buf.readResourceLocation()) : null);
            return new AltarRequirement(effigy, altar);
        }

        @Override
        public ResourceLocation getId() {
            return ID;
        }
    }
}
