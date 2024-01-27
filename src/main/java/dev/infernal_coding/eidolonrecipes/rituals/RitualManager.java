package dev.infernal_coding.eidolonrecipes.rituals;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Pair;
import dev.infernal_coding.eidolonrecipes.rituals.result.*;
import elucent.eidolon.ritual.IRequirement;
import elucent.eidolon.ritual.ItemRequirement;
import elucent.eidolon.util.ColorUtil;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tags.ITag;
import net.minecraft.tags.TagCollectionManager;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

import static dev.infernal_coding.eidolonrecipes.util.ItemUtil.*;

public class RitualManager {

    //private static final Map<ResourceLocation, ISpellRequirementSerializer> REQUIREMENTS = new HashMap<>();
    public static final HashMap<String, IRitualResultSerializer> RESULTS = new HashMap<>();

    public static void init() {
        RESULTS.put(ItemSerializer.ID, new ItemSerializer());
        RESULTS.put(SummonSerializer.ID, new SummonSerializer());
        RESULTS.put(AllureSerializer.ID, new AllureSerializer());
        RESULTS.put(RepelSerializer.ID, new RepelSerializer());
        RESULTS.put(TransformSerializer.ID, new TransformSerializer());
        RESULTS.put(AbsorbSerializer.ID, new AbsorbSerializer());
        RESULTS.put(TransmuteSerializer.ID, new TransmuteSerializer());
        RESULTS.put(DeceitSerializer.ID, new DeceitSerializer());
        RESULTS.put(TimeSerializer.ID, new TimeSerializer());
    }
    static ItemRequirement getItemRequirement(JsonObject json) {

        JSONUtils.getBoolean(json, "focused", false);
        String type = JSONUtils.getString(json, "type", "");

        ItemStack item = getItemFromJson(json.get("item"));
        ItemStack potionItem = getPotionItemFromJson(json.get("potion"));
        ItemStack splashPotionItem = getSplashPotionItemFromJson(json.get("splash_potion"));
        ItemStack lingeringPotionItem = getLingeringPotionItem(json.get("lingering_potion"));
        ItemStack enchantedBook = getEnchantedBookFromJson(json);

        if (!item.isEmpty() || !potionItem.isEmpty() || !splashPotionItem.isEmpty()
                || !lingeringPotionItem.isEmpty() || !enchantedBook.isEmpty()) {

            if (!potionItem.isEmpty()) {
                item = potionItem;
            } else if (!splashPotionItem.isEmpty()) {
                item = splashPotionItem;
            } else if (!lingeringPotionItem.isEmpty()) {
                item = lingeringPotionItem;
            } else if (!enchantedBook.isEmpty()) {
                item = enchantedBook;
            }
        }
        ITag<Item> itemTag = getItemTagFromJson(json.get("tag"));

        if (!item.isEmpty()) {
           return new ItemRequirement(item);
        } else if (itemTag != null && type.equals("tag")) {
           return new ItemRequirement(itemTag);
        }
        return null;
    }

    static Object getNecroticRequirement(JsonObject json) {
        boolean isNecroticFocused = JSONUtils.getBoolean(json, "focused", false);
        String type = JSONUtils.getString(json, "type", "");

        ItemStack item = getItemFromJson(json.get("item"));
        ItemStack potionItem = getPotionItemFromJson(json.get("potion"));
        ItemStack splashPotionItem = getSplashPotionItemFromJson(json.get("splash_potion"));
        ItemStack lingeringPotionItem = getLingeringPotionItem(json.get("lingering_potion"));
        ItemStack enchantedBook = getEnchantedBookFromJson(json);

        if (!item.isEmpty() || !potionItem.isEmpty() || !splashPotionItem.isEmpty()
                || !lingeringPotionItem.isEmpty() || !enchantedBook.isEmpty()) {

            if (!potionItem.isEmpty()) {
                item = potionItem;
            } else if (!splashPotionItem.isEmpty()) {
                item = splashPotionItem;
            } else if (!lingeringPotionItem.isEmpty()) {
                item = lingeringPotionItem;
            } else if (!enchantedBook.isEmpty()) {
                item = enchantedBook;
                }
            }

            ITag<Item> itemTag = getItemTagFromJson(json.get("tag"));

            if (!item.isEmpty() && isNecroticFocused) {
                return item;
            } else if (itemTag != null && type.equals("tag") && isNecroticFocused) {
                return itemTag;
            }
         return null;
    }

    static Pair<ItemRequirement, Object> getItemRequirementPair(PacketBuffer buffer) {
        boolean isNecroFocused = buffer.readBoolean();
        String type = buffer.readString();
        ITag<Item> itemTag;
        ItemStack itemStack;

        if (type.equals("tag")) {
            itemTag = TagCollectionManager.getManager()
                    .getItemTags()
                    .get(buffer.readResourceLocation());

            if (itemTag != null && !isNecroFocused) {
                return new Pair<>(new ItemRequirement(itemTag), null);
            } else if (itemTag != null && isNecroFocused) {
                return new Pair<>(new ItemRequirement(itemTag), itemTag);
            }
        } else {
              itemStack = buffer.readItemStack();

              if (!isNecroFocused && !itemStack.isEmpty()) {
                  return new Pair<>(new ItemRequirement(itemStack), null);
              } else if (isNecroFocused && !itemStack.isEmpty()) {
                  return new Pair<>(new ItemRequirement(itemStack), itemStack);
              }
          }
        return null;
    }
    static Pair<Integer, Boolean> getColor(JsonObject json) {
            JsonArray array = json.getAsJsonArray("color");

            int color = 0;
            boolean isColorPreset = false;

        if (array != null && array.size() == 4) {
            int[] col = new int[4];

            for (int i = 0; i < col.length; i++) {
                try {
                    col[i] = array.get(i).getAsJsonPrimitive().getAsInt();
                } catch (Exception e) {
                    col = null;
                    break;
                }
            }
            color = col != null ? ColorUtil.packColor(col[0], col[1], col[2], col[3]) : 0;
            isColorPreset = col != null;
        } else if (array != null && array.size() == 3) {
            int[] col = new int[3];

            for (int i = 0; i < col.length; i++) {
                try {
                    col[i] = array.get(i).getAsJsonPrimitive().getAsInt();
                } catch (Exception e) {
                    col = null;
                    break;
                }
            }
            color = col != null ? ColorUtil.packColor(255, col[0], col[1], col[2]) : 0;
            isColorPreset = col != null;
        }
        return new Pair<>(color, isColorPreset);
    }

    static Pair<Integer, Boolean> getColor(PacketBuffer buffer) {
        int length = buffer.readVarInt();
        int color = 0;
        boolean isColorPreset = false;

        if (length == 4) {
            int colors[] = new int[4];
            for (int i = 0; i < length; i++) {
                try {
                    colors[i] = buffer.readInt();
                } catch (Exception e) {
                    colors = null;
                    break;
                }
            }
            color = colors != null ? ColorUtil.packColor(colors[0], colors[1], colors[2], colors[3]) : 0;
            isColorPreset = colors != null;

        } else if (length == 3) {
            int[] colors = new int[3];

            for (int i = 0; i < length; i++) {
                try {
                    colors[i] = buffer.readInt();
                } catch (Exception e) {
                    colors = null;
                    break;
                }
            }
            color = colors != null ? ColorUtil.packColor(255, colors[0], colors[1], colors[2]) : 0;
            isColorPreset = colors != null;
        }
        return new Pair<>(color, isColorPreset);
    }

    static Object getBrazierRequirement(JsonObject json) {
        if (JSONUtils.getString(json, "type", "").equals("tag")) {
            return getItemTagFromJson(json.get("tag"));
        } else if (json.has("item")) {
            return getItemFromJson(json.get("item"));
        } else if (json.has("potion")) {
            return getPotionItemFromJson(json.get("potion"));
        } else if (json.has("splash_potion")) {
            return getSplashPotionItemFromJson(json.get("splash_potion"));
        } else if (json.has("lingering_potion")) {
            return getLingeringPotionItem(json.get("lingering_potion"));
        } else if (json.has("enchantment")) {
            return getEnchantedBookFromJson(json);
        }
        return null;
    }

    static Object getBrazierRequirement(PacketBuffer buffer) {
        String brazierItemType = buffer.readString();

        if (brazierItemType.equals("tag")) {
            return TagCollectionManager.getManager()
                    .getItemTags()
                    .get(buffer.readResourceLocation());
        } else if (brazierItemType.equals("item")) {
            return buffer.readItemStack();
        }
        return null;
    }

    static boolean usesNecro(ArrayList<Object> necroReqs, IRequirement requirement) {
        AtomicBoolean usesNecro = new AtomicBoolean(false);

        necroReqs.forEach(necroRequirement -> {
            if (requirement instanceof ItemRequirement) {
                ItemRequirement itemRequirement = (ItemRequirement) requirement;

                if (itemRequirement.getMatch() instanceof ItemStack && necroRequirement.getClass() == itemRequirement.getMatch().getClass()) {
                    ItemStack itemStack = (ItemStack) necroRequirement;
                    if (ItemStack.areItemStacksEqual(itemStack,  (ItemStack) itemRequirement.getMatch())) {
                        usesNecro.set(true);
                    }
                } else if (necroRequirement instanceof ITag && necroRequirement.getClass() == itemRequirement.getMatch().getClass()) {
                    ITag<?> iTag = (ITag<?>) necroRequirement;
                    ITag<?> iTag2 = (ITag<?>) itemRequirement.getMatch();

                    if (iTag.equals(iTag2)) {
                        usesNecro.set(true);
                    }
                }
            }
        });
        return usesNecro.get();
    }

    public static class ResultColorPair {
       public Object result;
       private final String type;
       private final int color, count;

       public Object getResult() {
           return result;
        }

        public int getColor() {
            return color;
        }

        public int getCount() {
            return count;
        }

        public String getType() {
            return type;
        }

        public ResultColorPair(int color, int count, Object result, String type) {
            this.color = color;
            this.count = count;
            this.result = result;
            this.type = type;
        }

    }

}
