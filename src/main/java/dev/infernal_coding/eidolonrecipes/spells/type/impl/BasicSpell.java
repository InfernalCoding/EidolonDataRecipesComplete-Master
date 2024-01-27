package dev.infernal_coding.eidolonrecipes.spells.type.impl;

import com.google.gson.JsonObject;
import dev.infernal_coding.eidolonrecipes.spells.type.ISpell;
import dev.infernal_coding.eidolonrecipes.spells.type.ISpellSerializer;
import dev.infernal_coding.eidolonrecipes.ModRoot;
import dev.infernal_coding.eidolonrecipes.spells.SpellInfo;
import dev.infernal_coding.eidolonrecipes.spells.SpellRecipeWrapper;
import elucent.eidolon.Registry;
import elucent.eidolon.block.HorizontalBlockBase;
import elucent.eidolon.particle.Particles;
import elucent.eidolon.tile.EffigyTileEntity;
import elucent.eidolon.util.ColorUtil;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BasicSpell implements ISpell {

    public static final ResourceLocation ID = ModRoot.eidolonRes("basic");

    @Override
    public void onCast(SpellRecipeWrapper spell, World world, BlockPos pos, PlayerEntity caster, SpellInfo spellInfo) {
        if (spellInfo.effigy.isPresent() && world.isRemote) {
            EffigyTileEntity effigy = spellInfo.effigy.get();
            world.playSound(caster, effigy.getPos(), SoundEvents.ENTITY_LIGHTNING_BOLT_THUNDER, SoundCategory.NEUTRAL, 10000.0F, 0.6F + world.rand.nextFloat() * 0.2F);
            world.playSound(caster, effigy.getPos(), SoundEvents.ENTITY_LIGHTNING_BOLT_IMPACT, SoundCategory.NEUTRAL, 2.0F, 0.5F + world.rand.nextFloat() * 0.2F);
            BlockState state = world.getBlockState(effigy.getPos());
            Direction dir = state.get(HorizontalBlockBase.HORIZONTAL_FACING);
            Direction tangent = dir.rotateY();
            float x = (float)effigy.getPos().getX() + 0.5F + (float)dir.getXOffset() * 0.21875F;
            float y = (float)effigy.getPos().getY() + 0.8125F;
            float z = (float)effigy.getPos().getZ() + 0.5F + (float)dir.getZOffset() * 0.21875F;
            float red = spell.getColor().map(c -> ColorUtil.getRed(c) / 255f).orElse(spell.getDeity().getRed());
            float green = spell.getColor().map(c -> ColorUtil.getGreen(c) / 255f).orElse(spell.getDeity().getGreen());
            float blue = spell.getColor().map(c -> ColorUtil.getBlue(c) / 255f).orElse(spell.getDeity().getBlue());
            Particles.create(Registry.FLAME_PARTICLE).setColor(red, green, blue).setAlpha(0.5F, 0.0F).setScale(0.125F, 0.0625F).randomOffset(0.01).randomVelocity(0.0025).addVelocity(0.0D, 0.005, 0.0D).repeat(world, x + 0.09375F * (float)tangent.getXOffset(), y, z + 0.09375F * (float)tangent.getZOffset(), 8);
            Particles.create(Registry.FLAME_PARTICLE).setColor(red, green, blue).setAlpha(0.5F, 0.0F).setScale(0.1875F, 0.125F).randomOffset(0.01).randomVelocity(0.0025).addVelocity(0.0D, 0.005, 0.0D).repeat(world, x - 0.09375F * (float)tangent.getXOffset(), y, z - 0.09375F * (float)tangent.getZOffset(), 8);
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
        public void write(PacketBuffer buf, BasicSpell obj) {

        }

        @Override
        public BasicSpell read(PacketBuffer buf) {
            return new BasicSpell();
        }

        @Override
        public ResourceLocation getId() {
            return ID;
        }
    }

}
