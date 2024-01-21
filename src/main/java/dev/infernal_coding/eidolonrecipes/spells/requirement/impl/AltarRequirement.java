package dev.infernal_coding.eidolonrecipes.spells.requirement.impl;

import com.google.gson.JsonObject;
import dev.infernal_coding.eidolonrecipes.spells.requirement.ISpellRequirementSerializer;
import dev.infernal_coding.eidolonrecipes.ModRoot;
import dev.infernal_coding.eidolonrecipes.spells.SpellInfo;
import dev.infernal_coding.eidolonrecipes.spells.SpellRecipeWrapper;
import dev.infernal_coding.eidolonrecipes.spells.requirement.ISpellRequirement;
import dev.infernal_coding.eidolonrecipes.util.JsonUtil;
import elucent.eidolon.spell.AltarInfo;
import elucent.eidolon.tile.EffigyTileEntity;
import net.minecraft.block.Block;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
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
    public boolean canCast(SpellRecipeWrapper spell, World world, BlockPos pos, PlayerEntity caster, SpellInfo spellInfo) {
        if (!spellInfo.reputation.canPray(caster, world.getGameTime())) {
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
            json.addProperty("effigy", requirement.requiredEffigy.get().getRegistryName().toString());
            json.addProperty("altar", requirement.requiredAltar.get().getRegistryName().toString());
        }

        @Override
        public AltarRequirement deserialize(JsonObject json) {
            Optional<Block> effigy = JsonUtil.getOptionalResourceLocation(json, "effigy").map(ForgeRegistries.BLOCKS::getValue);
            Optional<Block> altar = JsonUtil.getOptionalResourceLocation(json, "altar").map(ForgeRegistries.BLOCKS::getValue);
            return new AltarRequirement(effigy, altar);
        }

        @Override
        public void write(PacketBuffer buf, AltarRequirement requirement) {
            // Effigy
            buf.writeBoolean(requirement.requiredEffigy.isPresent());
            requirement.requiredEffigy.ifPresent(effigy -> buf.writeResourceLocation(effigy.getRegistryName()));

            // Altar
            buf.writeBoolean(requirement.requiredAltar.isPresent());
            requirement.requiredAltar.ifPresent(altar -> buf.writeResourceLocation(altar.getRegistryName()));
        }

        @Override
        public AltarRequirement read(PacketBuffer buf) {
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
