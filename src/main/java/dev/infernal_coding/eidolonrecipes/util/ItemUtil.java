package dev.infernal_coding.eidolonrecipes.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import dev.infernal_coding.eidolonrecipes.ModRoot;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.tags.ITag;

public class ItemUtil {


    public static ResourceLocation getItemName(Item item) {
        return ForgeRegistries.ITEMS.getKey(item);
    }

    public static ResourceLocation getBlockName(Block block) {
        return ForgeRegistries.BLOCKS.getKey(block);
    }
    public static JsonObject serializeRecipeIngredient(Object ingredient) {
        JsonObject json = new JsonObject();
        if (ingredient instanceof ItemStack) {
            ItemStack item = (ItemStack) ingredient;
            json.addProperty("item", getItemName(item.getItem()).toString());
            if (item.getCount() > 1) {
                json.addProperty("count", item.getCount());
            }
            if (item.hasTag()) {
                json.addProperty("nbt", item.getTag().toString());
            }
        } else if (ingredient instanceof Item item) {
            json.addProperty("item", getItemName(item).toString());
        } else if (ingredient instanceof Block block) {
            json.addProperty("item", getBlockName(block).toString());
        } else if (ingredient instanceof TagKey<?> tagKey) {
            json.addProperty("tag", tagKey.location().toString());
        } else if (ingredient instanceof Ingredient ing) {
            JsonElement serialized = ing.toJson();
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
        return Ingredient.fromJson(json);

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

    public static void writeRecipeIngredient(Object ingredient, FriendlyByteBuf buffer) {
        if (ingredient instanceof ItemStack) {
            buffer.writeVarInt(1);
            buffer.writeItemStack((ItemStack) ingredient, false);
        } else if (ingredient instanceof Item item) {
            buffer.writeVarInt(2);
            buffer.writeResourceLocation(getItemName(item));
        } else if (ingredient instanceof Block block) {
            buffer.writeVarInt(3);
            buffer.writeResourceLocation(getBlockName(block));
        } else if (ingredient instanceof TagKey<?> tag) {
            buffer.writeVarInt(4);
            buffer.writeResourceLocation(tag.location());
        } else if (ingredient instanceof Ingredient) {
            buffer.writeVarInt(5);
            Ingredient ing = (Ingredient) ingredient;
            ing.toNetwork(buffer);
        } else {
            ModRoot.LOGGER.warn("Unknown step match for writing to buffer {}", ingredient);
        }
    }

    public static Ingredient readRecipeIngredient(FriendlyByteBuf buffer) {
        return Ingredient.fromNetwork(buffer);
    }

    public static boolean matchesIngredient(Object match, ItemStack input, boolean matchCount) {
        if (match instanceof ItemStack) {
            ItemStack stack = (ItemStack) match;
            if (!matchCount ? ItemStack.matches(stack, input) : ItemStack.isSameItemSameTags(stack, input) && input.getCount() >= stack.getCount()) {
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
        } else if (match instanceof ITag<?> && ((ITag) match).contains(input.getItem())) {
            return true;
        } else if (match instanceof Ingredient) {
            Ingredient ingredient = (Ingredient) match;
            for (ItemStack stack : ingredient.getItems()) {
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

            if (ForgeRegistries.POTIONS.containsKey(potionName)) {
                return PotionUtils.setPotion(new ItemStack(Items.POTION),
                        ForgeRegistries.POTIONS.getValue(potionName));
            }
        }
        return ItemStack.EMPTY;
    }

    public static ItemStack getSplashPotionItemFromJson(JsonElement json) {
        if (json instanceof JsonPrimitive) {
            JsonPrimitive jsonPrimitive = (JsonPrimitive) json;
            ResourceLocation potionName = new ResourceLocation(jsonPrimitive.getAsString());

            if (ForgeRegistries.POTIONS.containsKey(potionName)) {
                return PotionUtils.setPotion(new ItemStack(Items.SPLASH_POTION),
                        ForgeRegistries.POTIONS.getValue(potionName));
            }
        }
        return ItemStack.EMPTY;
    }

    public static ItemStack getLingeringPotionItem(JsonElement json) {
        if (json instanceof JsonPrimitive) {
            JsonPrimitive jsonPrimitive = (JsonPrimitive) json;
            ResourceLocation potionName = new ResourceLocation(jsonPrimitive.getAsString());

            if (ForgeRegistries.POTIONS.containsKey(potionName)) {
                return PotionUtils.setPotion(new ItemStack(Items.LINGERING_POTION),
                        ForgeRegistries.POTIONS.getValue(potionName));
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
            EnchantedBookItem.addEnchantment(itemStack, new EnchantmentInstance(enchantment, enchantmentLevel));
            return itemStack;
        }
        return ItemStack.EMPTY;
    }

    public static TagKey<Item> getItemTagFromJson(JsonElement json) {

        if (json instanceof JsonPrimitive) {
            JsonPrimitive jsonPrimitive = (JsonPrimitive) json;
            return TagKey.create(Registry.ITEM_REGISTRY, new ResourceLocation(jsonPrimitive.getAsString()));
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

            if (!ForgeRegistries.ENTITY_TYPES.containsKey(new ResourceLocation(jsonPrimitive.getAsString()))) {
                return null;
            }
            return ForgeRegistries.ENTITY_TYPES.getValue(new ResourceLocation(jsonPrimitive.getAsString()));
        }
        return null;
    }
}
