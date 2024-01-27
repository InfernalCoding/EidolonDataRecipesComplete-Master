package dev.infernal_coding.eidolonrecipes.rituals.result;

import com.google.gson.JsonObject;
import dev.infernal_coding.eidolonrecipes.rituals.IRitualResultSerializer;
import dev.infernal_coding.eidolonrecipes.rituals.RitualManager;
import dev.infernal_coding.eidolonrecipes.rituals.RitualRecipeWrapper;
import elucent.eidolon.Eidolon;
import elucent.eidolon.Registry;
import elucent.eidolon.item.SummoningStaffItem;
import elucent.eidolon.network.MagicBurstEffectPacket;
import elucent.eidolon.network.Networking;
import elucent.eidolon.network.RitualConsumePacket;
import elucent.eidolon.ritual.IRitualItemFocus;
import elucent.eidolon.ritual.Ritual;
import elucent.eidolon.util.ColorUtil;
import elucent.eidolon.util.EntityUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.List;

public class AbsorptionSerializer implements IRitualResultSerializer {

    public static final String ID = "absorption";

    //Absorption Color: 255, 123, 140, 70
    //Absorption Texture: "eidolon:particle/absorption_ritual"

    @Override
    public RitualManager.ResultColorPair getColorAndResult(JsonObject json, int color, boolean isColorPreset, String type) {
        if (color > 0 && !isColorPreset) {
            color -= ColorUtil.packColor(255, 32, 89, 11);
        } else if (!isColorPreset) {
            color += ColorUtil.packColor(255, 22, 155, 11);
        }
        return new RitualManager.ResultColorPair(color, 1, null, type);
    }

    @Override
    public RitualManager.ResultColorPair getColorAndResult(FriendlyByteBuf buffer, int color, boolean isColorPreset, String type) {
        if (color > 0 && !isColorPreset) {
            color -= ColorUtil.packColor(255, 32, 89, 11);
        } else if (!isColorPreset) {
            color += ColorUtil.packColor(255, 22, 155, 11);
        }
        return new RitualManager.ResultColorPair(color, 1, null, type);
    }

    @Override
    public void writeResult(RitualRecipeWrapper.Result result, FriendlyByteBuf buffer) {
        buffer.writeUtf(result.getVariant());
    }

    @Override
    public void startRitual(Ritual ritual, RitualRecipeWrapper.Result result, Level world, BlockPos pos) {

        List<IRitualItemFocus> tiles = Ritual.getTilesWithinAABB(IRitualItemFocus.class, world, Ritual.getDefaultBounds(pos));
        BlockPos toRecharge = null;
        if (!tiles.isEmpty()) for (IRitualItemFocus tile : tiles) {
            ItemStack stack = tile.provide();
            if (stack.getItem() instanceof SummoningStaffItem s) {
                toRecharge = ((BlockEntity) tile).getBlockPos();
                break;
            }
        }

        List<LivingEntity> entities = world.getEntitiesOfClass(LivingEntity.class, Ritual.getDefaultBounds(pos), (e) -> Eidolon.getTrueMobType(e) == MobType.UNDEAD && !EntityUtil.isEnthralled(e) && e.getHealth() <= e.getMaxHealth() / 5);
        ListTag entityTags = new ListTag();
        for (LivingEntity e : entities) {
            e.setHealth(e.getMaxHealth());
            if (!world.isClientSide) {
                Networking.sendToTracking(world, e.blockPosition(), new MagicBurstEffectPacket(e.getX(), e.getY() + 0.1, e.getZ(),
                        ColorUtil.packColor(255, 61, 70, 35), ColorUtil.packColor(255, 36, 24, 41)));
                if (toRecharge != null) {
                    Networking.sendToTracking(world, toRecharge, new RitualConsumePacket(e.blockPosition().above(), toRecharge, ritual.getRed(), ritual.getGreen(), ritual.getBlue()));
                }
            }
            CompoundTag eTag = e.serializeNBT();
            entityTags.add(eTag);
            entityTags.add(eTag);
            entityTags.add(eTag);
            entityTags.add(eTag);
            entityTags.add(eTag);
            e.remove(Entity.RemovalReason.KILLED);
        }
        if (!tiles.isEmpty()) for (IRitualItemFocus tile : tiles) {
            ItemStack stack = tile.provide();
            if (stack.getItem() instanceof SummoningStaffItem s) {
                tile.replace(s.addCharges(stack, entityTags));
                break;
            }
        }
    }

    @Override
    public boolean onRitualTick(RitualRecipeWrapper.Result result, Level world, BlockPos pos) {
        return false;
    }

    @Override
    public boolean getRunsOnTick() {
        return false;
    }

    @Override
    public ItemStack getIcon(RitualRecipeWrapper.Result result) {
        return new ItemStack(Registry.SUMMONING_STAFF.get());
    }
}
