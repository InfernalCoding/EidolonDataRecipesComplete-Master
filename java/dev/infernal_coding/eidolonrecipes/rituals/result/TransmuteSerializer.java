package dev.infernal_coding.eidolonrecipes.rituals.result;

import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Pair;
import dev.infernal_coding.eidolonrecipes.rituals.IRitualResultSerializer;
import dev.infernal_coding.eidolonrecipes.rituals.RitualManager;
import dev.infernal_coding.eidolonrecipes.rituals.RitualRecipeWrapper;
import dev.infernal_coding.eidolonrecipes.util.EntityUtil;
import elucent.eidolon.Registry;
import elucent.eidolon.network.CrystallizeEffectPacket;
import elucent.eidolon.network.Networking;
import elucent.eidolon.ritual.Ritual;
import elucent.eidolon.util.ColorUtil;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.server.SUpdateTimePacket;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.ServerWorldInfo;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.List;

import static dev.infernal_coding.eidolonrecipes.util.ItemUtil.getEntityFromJson;

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
    public RitualManager.ResultColorPair getColorAndResult(PacketBuffer buffer, int color, boolean isColorPreset, String type) {
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
    public void writeResult(RitualRecipeWrapper.Result result, PacketBuffer buffer) {
        Pair<Block, Block> transmutationPair = (Pair<Block, Block>) result.getToCreate();
        buffer.writeResourceLocation(transmutationPair.getFirst().getRegistryName());
        buffer.writeResourceLocation(transmutationPair.getSecond().getRegistryName());
    }

    @Override
    public void startRitual(RitualRecipeWrapper.Result result, World world, BlockPos pos) {
        Block toTransmute = ((Pair<Block, Block>) result.getToCreate()).getFirst();
        Block transmuted = ((Pair<Block, Block>) result.getToCreate()).getSecond();
        Thread transmute = new Thread(() -> transmuteBlocks(world, pos, toTransmute, transmuted));
        transmute.start();
    }

    public static void transmuteBlocks(World world, BlockPos pos, Block toTransmute, Block transmuted) {
        AxisAlignedBB searchBox = Ritual.getDefaultBounds(pos);
        BlockPos.getAllInBox(searchBox).forEach(blockPos -> {

            if (!world.isRemote) {

                Block block = world.getBlockState(blockPos).getBlock();
                BlockState newBlockState = transmuted.getDefaultState();

                if (block == toTransmute) {
                    world.setBlockState(blockPos, newBlockState);
                    world.playSound(null, pos, Registry.SPLASH_SOULFIRE_EVENT.get(), SoundCategory.PLAYERS, 1.0f, 1.0f);
                    Networking.sendToTracking(world, blockPos, new CrystallizeEffectPacket(blockPos));
                    try {
                        Thread.sleep(world.rand.nextInt(2000) + 1000);
                    } catch (Exception ignored) {}
                }
            }
        });
    }

    @Override
    public boolean onRitualTick(RitualRecipeWrapper.Result result, World world, BlockPos pos) {
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
