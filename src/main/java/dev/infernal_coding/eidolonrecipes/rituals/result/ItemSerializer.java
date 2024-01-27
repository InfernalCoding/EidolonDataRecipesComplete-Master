package dev.infernal_coding.eidolonrecipes.rituals.result;

import com.google.gson.JsonObject;
import dev.infernal_coding.eidolonrecipes.rituals.IRitualResultSerializer;
import dev.infernal_coding.eidolonrecipes.rituals.RitualManager;
import dev.infernal_coding.eidolonrecipes.rituals.RitualRecipeWrapper;
import elucent.eidolon.util.ColorUtil;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.item.EnchantedBookItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionUtils;
import net.minecraft.potion.Potions;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.registries.ForgeRegistries;

import static dev.infernal_coding.eidolonrecipes.util.ItemUtil.*;

public class ItemSerializer implements IRitualResultSerializer {
    public static final String ID = "item";
    @Override
    public RitualManager.ResultColorPair getColorAndResult(JsonObject json, int color, boolean isColorPreset, String type) {
        int count = JSONUtils.getInt(json, "count", 1) > 0 ?
                JSONUtils.getInt(json, "count", 1) : 1;
        ItemStack item = getItemFromJson(json.get("item"));
        ItemStack potionItem = getPotionItemFromJson(json.get("potion"));
        ItemStack splashPotionItem = getSplashPotionItemFromJson(json.get("splash_potion"));
        ItemStack lingeringPotionItem = getLingeringPotionItem(json.get("lingering_potion"));
        ItemStack enchantedBook = getEnchantedBookFromJson(json);

        if (!item.isEmpty() || !potionItem.isEmpty() || !splashPotionItem.isEmpty()
                            || !lingeringPotionItem.isEmpty() || !enchantedBook.isEmpty()) {
            item.setCount(count);

            if (color > 0) {
                color -= ColorUtil.packColor(255, 255, 51, 85);
            } else {
                color += ColorUtil.packColor(255, 255, 51, 85);
            }

            if (!potionItem.isEmpty()) {
                item = potionItem;
                item.setCount(count);
            } else if (!splashPotionItem.isEmpty()) {
                item = splashPotionItem;
                item.setCount(count);
            } else if (!lingeringPotionItem.isEmpty()) {
                item = lingeringPotionItem;
                item.setCount(count);
            } else if (!enchantedBook.isEmpty()) {
                item = enchantedBook;
                item.setCount(count);
            }
            return new RitualManager.ResultColorPair(color, 1, item, type);
        }
        return null;
    }

    @Override
    public RitualManager.ResultColorPair getColorAndResult(PacketBuffer buffer, int color, boolean isColorPreset, String type) {

        ItemStack itemStack = buffer.readItemStack();
        if (!itemStack.isEmpty()) {
            return new RitualManager.ResultColorPair(color, 1, itemStack, type);
        }
        return null;
    }

    @Override
    public void writeResult(RitualRecipeWrapper.Result result, PacketBuffer buffer) {
        buffer.writeString(result.getVariant());


        if (result.getToCreate() instanceof ItemStack) {
            ItemStack itemStack = (ItemStack) result.getToCreate();
            buffer.writeItemStack(itemStack);
        }
    }

    @Override
    public void startRitual(RitualRecipeWrapper.Result result, World world, BlockPos pos) {
        if (result.getToCreate() instanceof ItemStack) {
            createItem(result.getToCreate(), world, pos);
        }
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
        return (ItemStack) result.getToCreate();
    }

    private void createItem(Object item, World world, BlockPos pos) {
        ItemStack itemToMake = (ItemStack) item;

        if (!world.isRemote) {
            world.addEntity(new ItemEntity(world, (double) pos.getX() + 0.5,
                    (double) pos.getY() + 2.5, (double) pos.getZ() + 0.5, itemToMake.copy()));
        }
    }
}
