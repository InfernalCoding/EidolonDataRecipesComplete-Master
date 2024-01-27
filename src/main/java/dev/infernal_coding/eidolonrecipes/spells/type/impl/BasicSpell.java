package dev.infernal_coding.eidolonrecipes.spells.type.impl;

import com.google.gson.JsonObject;
import dev.infernal_coding.eidolonrecipes.ModRoot;
import dev.infernal_coding.eidolonrecipes.spells.SpellInfo;
import dev.infernal_coding.eidolonrecipes.spells.SpellRecipeWrapper;
import dev.infernal_coding.eidolonrecipes.spells.type.ISpell;
import dev.infernal_coding.eidolonrecipes.spells.type.ISpellSerializer;
import elucent.eidolon.block.HorizontalBlockBase;
import elucent.eidolon.particle.Particles;
import elucent.eidolon.tile.EffigyTileEntity;
import elucent.eidolon.util.ColorUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class BasicSpell implements ISpell {

    public static final ResourceLocation ID = ModRoot.eidolonRes("basic");

    @Override
    public void onCast(SpellRecipeWrapper spell, Level world, BlockPos pos, Player caster, SpellInfo spellInfo) {
        if (spellInfo.effigy.isPresent() && world.isClientSide) {
            EffigyTileEntity effigy = spellInfo.effigy.get();
            world.playSound(caster, effigy.getBlockPos(), SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.NEUTRAL, 10000.0F, 0.6F + world.random.nextFloat() * 0.2F);
            world.playSound(caster, effigy.getBlockPos(), SoundEvents.LIGHTNING_BOLT_IMPACT, SoundSource.NEUTRAL, 2.0F, 0.5F + world.random.nextFloat() * 0.2F);
            BlockState state = world.getBlockState(effigy.getBlockPos());

            Direction dir = state.getValue(HorizontalBlockBase.HORIZONTAL_FACING);
            Direction tangent = dir.getCounterClockWise();
            float x = (float)effigy.getBlockPos().getX() + 0.5F + (float)dir.getStepX() * 0.21875F;
            float y = (float)effigy.getBlockPos().getY() + 0.8125F;
            float z = (float)effigy.getBlockPos().getZ() + 0.5F + (float)dir.getStepZ() * 0.21875F;
            float red = spell.getColor().map(c -> ColorUtil.getRed(c) / 255f).orElse(spell.getDeity().getRed());
            float green = spell.getColor().map(c -> ColorUtil.getGreen(c) / 255f).orElse(spell.getDeity().getGreen());
            float blue = spell.getColor().map(c -> ColorUtil.getBlue(c) / 255f).orElse(spell.getDeity().getBlue());
            Particles.create(elucent.eidolon.registries.Particles.FLAME_PARTICLE).setColor(red, green, blue).setAlpha(0.5F, 0.0F).setScale(0.125F, 0.0625F).randomOffset(0.01).randomVelocity(0.0025).addVelocity(0.0D, 0.005, 0.0D).repeat(world, x + 0.09375F * (float)tangent.getStepX(), y, z + 0.09375F * (float)tangent.getStepZ(), 8);
            Particles.create(elucent.eidolon.registries.Particles.FLAME_PARTICLE).setColor(red, green, blue).setAlpha(0.5F, 0.0F).setScale(0.1875F, 0.125F).randomOffset(0.01).randomVelocity(0.0025).addVelocity(0.0D, 0.005, 0.0D).repeat(world, x - 0.09375F * (float)tangent.getStepX(), y, z - 0.09375F * (float)tangent.getStepZ(), 8);
        }
    }

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    public static class Serializer implements ISpellSerializer<BasicSpell> {

        @Override
        public void serialize(JsonObject json, BasicSpell spell) {

        }

        @Override
        public BasicSpell deserialize(JsonObject json) {
            return new BasicSpell();
        }

        @Override
        public void write(FriendlyByteBuf buf, BasicSpell obj) {

        }

        @Override
        public BasicSpell read(FriendlyByteBuf buf) {
            return new BasicSpell();
        }

        @Override
        public ResourceLocation getId() {
            return ID;
        }
    }

}
