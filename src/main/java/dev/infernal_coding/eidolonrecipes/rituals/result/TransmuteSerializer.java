package dev.infernal_coding.eidolonrecipes.rituals.result;

import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Pair;
import dev.infernal_coding.eidolonrecipes.rituals.IRitualResultSerializer;
import dev.infernal_coding.eidolonrecipes.rituals.RitualManager;
import dev.infernal_coding.eidolonrecipes.rituals.RitualRecipeWrapper;
import dev.infernal_coding.eidolonrecipes.util.JSONUtils;
import elucent.eidolon.Registry;
import elucent.eidolon.network.CrystallizeEffectPacket;
import elucent.eidolon.network.Networking;
import elucent.eidolon.registries.Sounds;
import elucent.eidolon.ritual.Ritual;
import elucent.eidolon.util.ColorUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.registries.ForgeRegistries;

public class TransmuteSerializer implements IRitualResultSerializer {
    public static final String ID = "transmute";

    @Override
    public RitualManager.ResultColorPair getColorAndResult(JsonObject json, int color, boolean isColorPreset, String type) {
        Block toTransmute =
                ForgeRegistries.BLOCKS.getValue(new ResourceLocation(JSONUtils.getString(json, "block", "")));
        Block transmuted =
                ForgeRegistries.BLOCKS.getValue(new ResourceLocation(JSONUtils.getString(json, "newBlock", "")));

        if (toTransmute != null && transmuted != null) {

            if (color > 0 && !isColorPreset) {
                color -= ColorUtil.packColor(32, 11, 2, 12);
            } else if (!isColorPreset) {
                color += ColorUtil.packColor(32, 11, 32, 9);
            }
            return new RitualManager.ResultColorPair(color, 1,
                    new Pair<>(toTransmute, transmuted), type);
        }
        return null;
    }

    @Override
    public RitualManager.ResultColorPair getColorAndResult(FriendlyByteBuf buffer, int color, boolean isColorPreset, String type) {
        Block toTransmute = ForgeRegistries.BLOCKS.getValue(buffer.readResourceLocation());
        Block transmuted = ForgeRegistries.BLOCKS.getValue(buffer.readResourceLocation());

        if (toTransmute != null && transmuted != null) {

            if (color > 0 && !isColorPreset) {
                color -= ColorUtil.packColor(32, 11, 2, 12);
            } else if (!isColorPreset) {
                color += ColorUtil.packColor(32, 11, 32, 9);
            }
            return new RitualManager.ResultColorPair(color, 1,
                    new Pair<>(toTransmute, transmuted), type);
        }
        return null;
    }

    @Override
    public void writeResult(RitualRecipeWrapper.Result result, FriendlyByteBuf buffer) {
        Pair<Block, Block> transmutationPair = (Pair<Block, Block>) result.getToCreate();
        buffer.writeResourceLocation(ForgeRegistries.BLOCKS.getKey(transmutationPair.getFirst()));
        buffer.writeResourceLocation(ForgeRegistries.BLOCKS.getKey(transmutationPair.getSecond()));
    }

    @Override
    public void startRitual(Ritual ritual, RitualRecipeWrapper.Result result, Level world, BlockPos pos) {
        Block toTransmute = ((Pair<Block, Block>) result.getToCreate()).getFirst();
        Block transmuted = ((Pair<Block, Block>) result.getToCreate()).getSecond();
        Thread transmute = new Thread(() -> transmuteBlocks(world, pos, toTransmute, transmuted));
        transmute.start();
    }

    public static void transmuteBlocks(Level world, BlockPos pos, Block toTransmute, Block transmuted) {
        AABB searchBox = Ritual.getDefaultBounds(pos);
        BlockPos.betweenClosedStream(searchBox).forEach(blockPos -> {

            if (!world.isClientSide) {

                Block block = world.getBlockState(blockPos).getBlock();
                BlockState newBlockState = transmuted.defaultBlockState();

                if (block == toTransmute) {
                    world.setBlock(blockPos, newBlockState, -1);
                    world.playSound(null, pos, Sounds.SPLASH_SOULFIRE_EVENT.get(), SoundSource.PLAYERS, 1.0f, 1.0f);
                    Networking.sendToTracking(world, blockPos, new CrystallizeEffectPacket(blockPos));
                    try {
                        Thread.sleep(world.random.nextInt(2000) + 1000);
                    } catch (Exception ignored) {}
                }
            }
        });
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
        Block block = ((Pair<Block, Block>) result.getToCreate()).getSecond();
        return new ItemStack(block);
    }
}
