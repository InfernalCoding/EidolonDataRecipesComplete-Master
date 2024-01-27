package dev.infernal_coding.eidolonrecipes.rituals.result;

import com.google.gson.JsonObject;
import dev.infernal_coding.eidolonrecipes.rituals.IRitualResultSerializer;
import dev.infernal_coding.eidolonrecipes.rituals.RitualManager;
import dev.infernal_coding.eidolonrecipes.rituals.RitualRecipeWrapper;
import elucent.eidolon.ritual.Ritual;
import elucent.eidolon.util.ColorUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

import java.util.List;

public class DeceitSerializer implements IRitualResultSerializer {

    public static final String ID = "deceit";
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

    }

    @Override
    public boolean onRitualTick(RitualRecipeWrapper.Result result, Level world, BlockPos pos) {
        if (world.getGameTime() % 20 == 0) {
            List<Villager> villagers = world.getEntitiesOfClass(Villager.class, new AABB(pos).inflate(48, 16, 48));
            for (Villager v : villagers) {
                if (world.random.nextInt(120) == 0) v.getGossips().decay();
            }
        }
        return true;
    }

    @Override
    public boolean getRunsOnTick() {
        return true;
    }

    @Override
    public ItemStack getIcon(RitualRecipeWrapper.Result result) {
        return new ItemStack(Items.EMERALD);
    }
}
