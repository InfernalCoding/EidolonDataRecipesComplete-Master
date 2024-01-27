package dev.infernal_coding.eidolonrecipes.rituals.result;

import com.google.gson.JsonObject;
import dev.infernal_coding.eidolonrecipes.rituals.IRitualResultSerializer;
import dev.infernal_coding.eidolonrecipes.rituals.RitualManager;
import dev.infernal_coding.eidolonrecipes.rituals.RitualRecipeWrapper;
import dev.infernal_coding.eidolonrecipes.util.JSONUtils;
import elucent.eidolon.ritual.Ritual;
import elucent.eidolon.util.ColorUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientboundSetTimePacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ServerLevelData;

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
    public RitualManager.ResultColorPair getColorAndResult(FriendlyByteBuf buffer, int color, boolean isColorPreset, String type) {
        Boolean isForDay = buffer.readBoolean();

        if (color > 0 && !isColorPreset) {
            color -= ColorUtil.packColor(255, 32, 33, 11);
        } else if (!isColorPreset) {
            color += ColorUtil.packColor(255, 22, 22, 11);
        }
        return new RitualManager.ResultColorPair(color, 1, isForDay, type);
    }

    @Override
    public void writeResult(RitualRecipeWrapper.Result result, FriendlyByteBuf buffer) {
        buffer.writeBoolean((Boolean) result.getToCreate());
    }

    @Override
    public void startRitual(Ritual ritual, RitualRecipeWrapper.Result result, Level world, BlockPos pos) {}

    @Override
    public boolean onRitualTick(RitualRecipeWrapper.Result result, Level world, BlockPos pos) {
        boolean isDay = (Boolean) result.getToCreate();

        if (isDay) {
            return makeDay(world);
        }
        return makeNight(world);
    }

    public boolean makeDay(Level world) {
        if (world.getDayTime() % 24000 < 1000 || world.getDayTime() % 24000 >= 12000) {
            if (!world.isClientSide) {
                ((ServerLevelData) world.getLevelData()).setDayTime(world.getDayTime() + 100);
                for (ServerPlayer player : ((ServerLevel) world).getPlayers(a -> true)) {
                    player.connection.send(new ClientboundSetTimePacket(world.getGameTime(), world.getDayTime(), world.getGameRules().getBoolean(GameRules.RULE_DAYLIGHT)));
                }
            }
            return true;
        }
        return false;
    }

    public boolean makeNight(Level world) {
        if (world.getDayTime() % 24000 < 13000 && world.getDayTime() % 24000 >= 0) {
            if (!world.isClientSide) {
                ((ServerLevelData) world.getLevelData()).setDayTime(world.getDayTime() + 100);
                for (ServerPlayer player : ((ServerLevel) world).getPlayers(s -> true)) {
                    player.connection.send(new ClientboundSetTimePacket(world.getGameTime(), world.getDayTime(), world.getGameRules().getBoolean(GameRules.RULE_DAYLIGHT)));
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
