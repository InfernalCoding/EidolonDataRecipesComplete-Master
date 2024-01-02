package dev.infernal_coding.eidolonrecipes.rituals.result;

import com.google.gson.JsonObject;
import dev.infernal_coding.eidolonrecipes.rituals.IRitualResultSerializer;
import dev.infernal_coding.eidolonrecipes.rituals.RitualManager;
import dev.infernal_coding.eidolonrecipes.rituals.RitualRecipeWrapper;
import elucent.eidolon.ritual.Ritual;
import elucent.eidolon.util.ColorUtil;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.server.SUpdateTimePacket;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.ServerWorldInfo;

public class TimeSerializer implements IRitualResultSerializer {
    public static final String ID = "time";
    @Override
    public RitualManager.ResultColorPair getColorAndResult(JsonObject json, int color, boolean isColorPreset, String type) {

        Boolean isForDay = JSONUtils.getBoolean(json, "is_day", false);

        if (color > 0 && !isColorPreset) {
            color -= ColorUtil.packColor(255, 32, 33, 11);
        } else if (!isColorPreset) {
            color += ColorUtil.packColor(255, 22, 22, 11);
        }
        return new RitualManager.ResultColorPair(color, 1, isForDay, type);
    }

    @Override
    public RitualManager.ResultColorPair getColorAndResult(PacketBuffer buffer, int color, boolean isColorPreset, String type) {
        Boolean isForDay = buffer.readBoolean();

        if (color > 0 && !isColorPreset) {
            color -= ColorUtil.packColor(255, 32, 33, 11);
        } else if (!isColorPreset) {
            color += ColorUtil.packColor(255, 22, 22, 11);
        }
        return new RitualManager.ResultColorPair(color, 1, isForDay, type);
    }

    @Override
    public void writeResult(RitualRecipeWrapper.Result result, PacketBuffer buffer) {
        buffer.writeBoolean((Boolean) result.getToCreate());
    }

    @Override
    public void startRitual(RitualRecipeWrapper.Result result, World world, BlockPos pos) {}

    @Override
    public boolean onRitualTick(RitualRecipeWrapper.Result result, World world, BlockPos pos) {
        boolean isDay = (Boolean) result.getToCreate();

        if (isDay) {
            return makeDay(world);
        }
        return makeNight(world);
    }

    public boolean makeDay(World world) {
        if (world.getDayTime() % 24000 < 1000 || world.getDayTime() % 24000 >= 12000) {
            if (!world.isRemote) {
                ((ServerWorldInfo) world.getWorldInfo()).setDayTime(world.getDayTime() + 100);
                for (ServerPlayerEntity player : ((ServerWorld) world).getPlayers()) {
                    player.connection.sendPacket(new SUpdateTimePacket(world.getGameTime(), world.getDayTime(), world.getGameRules().getBoolean(GameRules.DO_DAYLIGHT_CYCLE)));
                }
            }
            return true;
        }
        return false;
    }

    public boolean makeNight(World world) {
        if (world.getDayTime() % 24000 < 13000 && world.getDayTime() % 24000 >= 0) {
            if (!world.isRemote) {
                ((ServerWorldInfo) world.getWorldInfo()).setDayTime(world.getDayTime() + 100);
                for (ServerPlayerEntity player : ((ServerWorld) world).getPlayers()) {
                    player.connection.sendPacket(new SUpdateTimePacket(world.getGameTime(), world.getDayTime(), world.getGameRules().getBoolean(GameRules.DO_DAYLIGHT_CYCLE)));
                }
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean getRunsOnTick() {
        return true;
    }

    @Override
    public ItemStack getIcon(RitualRecipeWrapper.Result result) {
        return new ItemStack(Items.CLOCK);
    }
}
