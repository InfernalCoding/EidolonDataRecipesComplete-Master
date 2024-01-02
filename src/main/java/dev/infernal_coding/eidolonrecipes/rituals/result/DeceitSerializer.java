package dev.infernal_coding.eidolonrecipes.rituals.result;

import com.google.gson.JsonObject;
import dev.infernal_coding.eidolonrecipes.rituals.IRitualResultSerializer;
import dev.infernal_coding.eidolonrecipes.rituals.RitualManager;
import dev.infernal_coding.eidolonrecipes.rituals.RitualRecipeWrapper;
import elucent.eidolon.util.ColorUtil;
import net.minecraft.entity.merchant.villager.VillagerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

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
    public RitualManager.ResultColorPair getColorAndResult(PacketBuffer buffer, int color, boolean isColorPreset, String type) {
        if (color > 0 && !isColorPreset) {
            color -= ColorUtil.packColor(255, 32, 89, 11);
        } else if (!isColorPreset) {
            color += ColorUtil.packColor(255, 22, 155, 11);
        }
        return new RitualManager.ResultColorPair(color, 1, null, type);
    }

    @Override
    public void writeResult(RitualRecipeWrapper.Result result, PacketBuffer buffer) {
        buffer.writeString(result.getVariant());
    }

    @Override
    public void startRitual(RitualRecipeWrapper.Result result, World world, BlockPos pos) {

    }

    @Override
    public boolean onRitualTick(RitualRecipeWrapper.Result result, World world, BlockPos pos) {
        if (world.getGameTime() % 20 == 0) {
            List<VillagerEntity> villagers = world.getEntitiesWithinAABB(VillagerEntity.class, new AxisAlignedBB(pos).grow(48, 16, 48));
            for (VillagerEntity v : villagers) {
                if (world.rand.nextInt(120) == 0) v.getGossip().tick();
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
