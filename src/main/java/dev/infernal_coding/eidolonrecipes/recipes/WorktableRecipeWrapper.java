package dev.infernal_coding.eidolonrecipes.recipes;

import com.google.common.collect.Maps;
import com.google.gson.*;
import dev.infernal_coding.eidolonrecipes.registry.RecipeTypes;
import dev.infernal_coding.eidolonrecipes.util.ItemUtil;
import elucent.eidolon.recipe.WorktableRecipe;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.registries.ForgeRegistryEntry;

import java.util.Arrays;
import java.util.Map;

public class WorktableRecipeWrapper extends WorktableRecipe implements IRecipe<IInventory> {

    public WorktableRecipeWrapper(Object[] core, Object[] extras, ItemStack result) {
        super(core, extras, result);
    }

    @Override
    public boolean matches(IInventory inv, World worldIn) {
        return false;
    }

    @Override
    public ItemStack getCraftingResult(IInventory inv) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean canFit(int width, int height) {
        return false;
    }

    @Override
    public ItemStack getRecipeOutput() {
        return ItemStack.EMPTY;
    }

    @Override
    public ResourceLocation getId() {
        return this.getRegistryName();
    }

    @Override
    public IRecipeSerializer<?> getSerializer() {
        return RecipeTypes.WORKTABLE_SERIALIZER.get();
    }

    @Override
    public IRecipeType<?> getType() {
        return RecipeTypes.WORKTABLE;
    }

    public static final class Serializer extends ForgeRegistryEntry<IRecipeSerializer<?>> implements IRecipeSerializer<WorktableRecipeWrapper> {

        // This is needed because of how I decided to parse the extras pattern in json so it's visually easier.
        private static final int[] EXTRAS_ORDER = new int[]{0, 2, 3, 1};

        @Override
        public WorktableRecipeWrapper read(ResourceLocation recipeId, JsonObject json) {
            ItemStack result = CraftingHelper.getItemStack(JSONUtils.getJsonObject(json, "result"), true);
            Map<String, Object> key = deserializeKey(JSONUtils.getJsonObject(json, "key"));

            Object[] core = new Object[9];
            Arrays.fill(core, ItemStack.EMPTY);
            Object[] extras = new Object[4];
            Arrays.fill(extras, ItemStack.EMPTY);

            String corePattern = getPattern(JSONUtils.getJsonArray(json, "core"));
            if (corePattern.length() != 9) {
                throw new JsonParseException("Core pattern must contain a total of 9 characters. Found " + corePattern.length());
            }

            String extrasPattern = getPattern(JSONUtils.getJsonArray(json, "extras"));
            if (extrasPattern.length() != 4) {
                throw new JsonParseException("Extras pattern must contain a total of 4 characters. Found " + extrasPattern.length());
            }

            for (int i = 0; i < core.length; i++) {
                core[i] = key.get(corePattern.substring(i, i+1));
            }

            for (int i = 0; i < extras.length; i++) {
                int extrasIndex = EXTRAS_ORDER[i];
                extras[i] = key.get(extrasPattern.substring(extrasIndex, extrasIndex+1));
            }

            WorktableRecipeWrapper recipe = new WorktableRecipeWrapper(core, extras, result);
            recipe.setRegistryName(recipeId);
            return recipe;
        }

        @Override
        public void write(PacketBuffer buffer, WorktableRecipeWrapper recipe) {
            buffer.writeItemStack(recipe.getResult());
            for (int i = 0; i < 9; i++) {
                ItemUtil.writeRecipeIngredient(recipe.getCore()[i], buffer);
            }
            for (int i = 0; i < 4; i++) {
                ItemUtil.writeRecipeIngredient(recipe.getOuter()[i], buffer);
            }
        }

        @Override
        public WorktableRecipeWrapper read(ResourceLocation recipeId, PacketBuffer buffer) {
            ItemStack result = buffer.readItemStack();
            Object[] core = new Object[9];
            Object[] extras = new Object[4];

            for (int i = 0; i < 9; i++) {
                core[i] = ItemUtil.readRecipeIngredient(buffer);
            }
            for (int i = 0; i < 4; i++) {
                extras[i] = ItemUtil.readRecipeIngredient(buffer);
            }

            WorktableRecipeWrapper recipe = new WorktableRecipeWrapper(core, extras, result);
            recipe.setRegistryName(recipeId);
            return recipe;
        }

        private static Map<String, Object> deserializeKey(JsonObject json) {
            Map<String, Object> map = Maps.newHashMap();

            for(Map.Entry<String, JsonElement> entry : json.entrySet()) {
                if (entry.getKey().length() != 1) {
                    throw new JsonSyntaxException("Invalid key entry: '" + entry.getKey() + "' is an invalid symbol (must be 1 character only).");
                }

                if (" ".equals(entry.getKey())) {
                    throw new JsonSyntaxException("Invalid key entry: ' ' is a reserved symbol.");
                }

                map.put(entry.getKey(), ItemUtil.deserializeRecipeIngredient(entry.getValue()));
            }

            map.put(" ", ItemStack.EMPTY);
            return map;
        }

        private static String getPattern(JsonArray jsonArray) {
            StringBuilder pattern = new StringBuilder();
            for (JsonElement element : jsonArray) {
                if (!element.isJsonPrimitive() || !((JsonPrimitive) element).isString()) {
                    throw new JsonParseException("Pattern must consist of an array of string. Found " + element);
                }

                pattern.append(element.getAsString());
            }

            return pattern.toString();
        }
    }
}
