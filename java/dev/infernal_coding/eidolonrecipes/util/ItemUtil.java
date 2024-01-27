package dev.infernal_coding.eidolonrecipes.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import dev.infernal_coding.eidolonrecipes.ModRoot;
import net.minecraft.block.Block;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentData;
import net.minecraft.entity.EntityType;
import net.minecraft.item.EnchantedBookItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.potion.PotionUtils;
import net.minecraft.tags.ITag;
import net.minecraft.tags.TagCollectionManager;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.ForgeRegistry;

public class ItemUtil {

    public static JsonObject serializeRecipeIngredient(Object ingredient) {
        JsonObject json = new JsonObject();
        if (ingredient instanceof ItemStack) {
            ItemStack item = (ItemStack) ingredient;
            json.addProperty("item", item.getItem().getRegistryName().toString());
            if (item.getCount() > 1) {
                json.addProperty("count", item.getCount());
            }
            if (item.hasTag()) {
                json.addProperty("nbt", item.getTag().toString());
            }
        } else if (ingredient instanceof Item) {
            json.addProperty("item", ((Item) ingredient).getRegistryName().toString());
        } else if (ingredient instanceof Block) {
            json.addProperty("item", ((Block) ingredient).asItem().getRegistryName().toString());
        } else if (ingredient instanceof ITag) {
            json.addProperty("tag", TagCollectionManager.getManager().getItemTags().getDirectIdFromTag((ITag<Item>) ingredient).toString());
        } else if (ingredient instanceof Ingredient) {
            JsonElement serialized = ((Ingredient) ingredient).serialize();
            if (serialized.isJsonArray()) {
                json.add("items", serialized);
            } else {
                json.add("item", serialized);
            }
        } else {
            ModRoot.LOGGER.warn("Unknown step match for writing to buffer {}", ingredient);
        }


        return json;
    }

    public static Ingredient deserializeRecipeIngredient(JsonElement json) {
        return Ingredient.deserialize(json);

        /*if (json.has("tag")) {
            return TagCollectionManager.getManager().getItemTags().get(new ResourceLocation(json.get("tag").getAsString()));
        } else if (json.has("item")) {
            if (!json.has("count") && !json.has("nbt")) {
                Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(json.get("item").getAsString()));
                if (item == null) {
                    throw new JsonParseException("Invalid item name " + json.get("item").getAsString());
                }
            }
            return CraftingHelper.getItemStack(json, true);
        }

        throw new JsonParseException("Recipe Ingredient must contain either a 'tag' or an 'item'");*/
    }

    public static void writeRecipeIngredient(Object ingredient, PacketBuffer buffer) {
        if (ingredient instanceof ItemStack) {
            buffer.writeVarInt(1);
            buffer.writeItemStack((ItemStack) ingredient);
        } else if (ingredient instanceof Item) {
            buffer.writeVarInt(2);
            buffer.writeResourceLocation(((Item) ingredient).getRegistryName());
        } else if (ingredient instanceof Block) {
            buffer.writeVarInt(3);
            buffer.writeResourceLocation(((Block) ingredient).getRegistryName());
        } else if (ingredient instanceof ITag) {
            buffer.writeVarInt(4);
            buffer.writeResourceLocation(TagCollectionManager.getManager().getItemTags().getValidatedIdFromTag((ITag<Item>) ingredient));
        } else if (ingredient instanceof Ingredient) {
            buffer.writeVarInt(5);
            Ingredient ing = (Ingredient) ingredient;
            ing.write(buffer);
        } else {
            ModRoot.LOGGER.warn("Unknown step match for writing to buffer {}", ingredient);
        }
    }

    public static Ingredient readRecipeIngredient(PacketBuffer buffer) {
        return Ingredient.read(buffer);
    }

    public static boolean matchesIngredient(Object match, ItemStack input, boolean matchCount) {
        if (match instanceof ItemStack) {
            ItemStack stack = (ItemStack) match;
            if (!matchCount ? ItemStack.areItemStacksEqual(stack, input) : ItemStack.areItemsEqual(stack, input) && ItemStack.areItemStackTagsEqual(stack, input) && input.getCount() >= stack.getCount()) {
                return true;
            }
        } else if (match instanceof Item) {
            if (match == input.getItem()) {
                return true;
            }
        } else if (match instanceof Block) {
            if (((Block) match).asItem() == input.getItem()) {
                return true;
            }
        } else if (match instanceof ITag && ((ITag) match).contains(input.getItem())) {
            return true;
        } else if (match instanceof Ingredient) {
            Ingredient ingredient = (Ingredient) match;
            for (ItemStack stack : ingredient.getMatchingStacks()) {
                if (matchesIngredient(stack, input, true)) {
                    return true;
                }
            }
        }

        return false;
    }

    public static ItemStack getItemFromJson(JsonElement json) {
        if (json instanceof JsonPrimitive) {
            JsonPrimitive jsonPrimitive = (JsonPrimitive) json;
            return new ItemStack(ForgeRegistries.ITEMS.getValue(new ResourceLocation(jsonPrimitive.getAsString())));
        }
        return ItemStack.EMPTY;
    }

    public static ItemStack getPotionItemFromJson(JsonElement json) {
        if (json instanceof JsonPrimitive) {
            JsonPrimitive jsonPrimitive = (JsonPrimitive) json;
            ResourceLocation potionName = new ResourceLocation(jsonPrimitive.getAsString());

            if (ForgeRegistries.POTION_TYPES.containsKey(potionName)) {
                return PotionUtils.addPotionToItemStack(new ItemStack(Items.POTION),
                        ForgeRegistries.POTION_TYPES.getValue(potionName));
            }
        }
        return ItemStack.EMPTY;
    }

    public static ItemStack getSplashPotionItemFromJson(JsonElement json) {
        if (json instanceof JsonPrimitive) {
            JsonPrimitive jsonPrimitive = (JsonPrimitive) json;
            ResourceLocation potionName = new ResourceLocation(jsonPrimitive.getAsString());

            if (ForgeRegistries.POTION_TYPES.containsKey(potionName)) {
                return PotionUtils.addPotionToItemStack(new ItemStack(Items.SPLASH_POTION),
                        ForgeRegistries.POTION_TYPES.getValue(potionName));
            }
        }
        return ItemStack.EMPTY;
    }

    public static ItemStack getLingeringPotionItem(JsonElement json) {
        if (json instanceof JsonPrimitive) {
            JsonPrimitive jsonPrimitive = (JsonPrimitive) json;
            ResourceLocation potionName = new ResourceLocation(jsonPrimitive.getAsString());

            if (ForgeRegistries.POTION_TYPES.containsKey(potionName)) {
                return PotionUtils.addPotionToItemStack(new ItemStack(Items.LINGERING_POTION),
                        ForgeRegistries.POTION_TYPES.getValue(potionName));
            }
        }
        return ItemStack.EMPTY;
    }

    public static ItemStack getEnchantedBookFromJson(JsonObject json) {
        ItemStack itemStack = new ItemStack(Items.ENCHANTED_BOOK);
        int enchantmentLevel = JSONUtils.getInt(json, "level", 1) > 0 ?
                JSONUtils.getInt(json, "level", 1) : 1;
        ResourceLocation enchantmentName = new ResourceLocation(JSONUtils.getString(json, "enchantment", ""));

        if (ForgeRegistries.ENCHANTMENTS.containsKey(enchantmentName)) {
            Enchantment enchantment = ForgeRegistries.ENCHANTMENTS.getValue(enchantmentName);
            EnchantedBookItem.addEnchantment(itemStack, new EnchantmentData(enchantment, enchantmentLevel));
            return itemStack;
        }
        return ItemStack.EMPTY;
    }

    public static ITag<Item> getItemTagFromJson(JsonElement json) {

        if (json instanceof JsonPrimitive) {
            JsonPrimitive jsonPrimitive = (JsonPrimitive) json;
            return TagCollectionManager.getManager()
                    .getItemTags()
                    .get(new ResourceLocation(jsonPrimitive.getAsString()));
        }
        return null;
    }

    public static EntityType<?> getEntityFromJson(JsonElement json) {

        if (json instanceof JsonPrimitive) {
            JsonPrimitive jsonPrimitive = (JsonPrimitive) json;

            if (!jsonPrimitive.getAsString().contains("minecraft")) {
                if (jsonPrimitive.getAsString().contains("villager")
                        || jsonPrimitive.getAsString().contains("zombie")
                        || jsonPrimitive.getAsString().contains("skeleton")) {
                    return null;
                }
            }

            if (!ForgeRegistries.ENTITIES.containsKey(new ResourceLocation(jsonPrimitive.getAsString()))) {
                return null;
            }
            return ForgeRegistries.ENTITIES.getValue(new ResourceLocation(jsonPrimitive.getAsString()));
        }
        return null;
    }
}
