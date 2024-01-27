package dev.infernal_coding.eidolonrecipes.recipes;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.infernal_coding.eidolonrecipes.registry.RecipeTypes;
import dev.infernal_coding.eidolonrecipes.util.ItemUtil;
import elucent.eidolon.recipe.CrucibleRecipe;
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

import java.util.ArrayList;
import java.util.List;

public class CrucibleRecipeWrapper extends CrucibleRecipe implements IRecipe<IInventory> {

    public Category category;

    public String title, description;

    public CrucibleRecipeWrapper(ItemStack result, Category category, String title, String description) {
        super(result);
        this.category = category;
        this.title = title;
        this.description = description;
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
        return RecipeTypes.CRUCIBLE_SERIALIZER.get();
    }

    @Override
    public IRecipeType<?> getType() {
        return RecipeTypes.CRUCIBLE;
    }

    public static final class Serializer extends ForgeRegistryEntry<IRecipeSerializer<?>> implements IRecipeSerializer<CrucibleRecipeWrapper> {

        @Override
        public CrucibleRecipeWrapper read(ResourceLocation recipeId, JsonObject json) {
            ItemStack result = CraftingHelper.getItemStack(JSONUtils.getJsonObject(json, "result"), true);
            Category category = findCategory(JSONUtils.getString(json, "category", "misc"));
            String title = JSONUtils.getString(json, "title", "");
            String description = JSONUtils.getString(json, "description", "");
            CrucibleRecipeWrapper recipe = new CrucibleRecipeWrapper(result, category, title, description);
            for (JsonElement jsonStep : JSONUtils.getJsonArray(json, "steps")) {
                List<Object> matches = new ArrayList<>();
                for (JsonElement jsonIngredient : JSONUtils.getJsonArray(JSONUtils.getJsonObject(jsonStep, "step"), "ingredients")) {
                    Object ingredient = ItemUtil.deserializeRecipeIngredient(jsonIngredient.getAsJsonObject());
                    if (ingredient instanceof ItemStack) {
                        ItemStack item = (ItemStack) ingredient;
                        for (int i = 0; i < item.getCount(); i++) {
                            ItemStack copy = item.copy();
                            copy.setCount(1);
                            matches.add(copy);
                        }
                    } else {
                        matches.add(ingredient);
                    }
                }
                int stirs = JSONUtils.getInt(jsonStep.getAsJsonObject(), "stirs", 0);
                recipe.addStirringStep(stirs, matches.toArray());
            }
            recipe.setRegistryName(recipeId);
            return recipe;
        }

        @Override
        public void write(PacketBuffer buffer, CrucibleRecipeWrapper recipe) {
            buffer.writeItemStack(recipe.getResult());
            buffer.writeEnumValue(recipe.category);
            buffer.writeString(recipe.title);
            buffer.writeString(recipe.description);
            buffer.writeVarInt(recipe.getSteps().size());
            for (Step step : recipe.getSteps()) {
                writeStep(buffer, step);
            }
        }

        @Override
        public CrucibleRecipeWrapper read(ResourceLocation recipeId, PacketBuffer buffer) {

            CrucibleRecipeWrapper recipe = new CrucibleRecipeWrapper(buffer.readItemStack(),
                    buffer.readEnumValue(Category.class), buffer.readString(), buffer.readString());
            int steps = buffer.readVarInt();
            for (int i = 0; i < steps; i++) {
                readStep(buffer, recipe);
            }
            recipe.setRegistryName(recipeId);
            return recipe;
        }

        public static void writeStep(PacketBuffer buffer, Step step) {
            buffer.writeVarInt(step.stirs);
            buffer.writeVarInt(step.matches.size());
            for (Object obj : step.matches) {
                ItemUtil.writeRecipeIngredient(obj, buffer);
            }
        }

        public static void readStep(PacketBuffer buffer, CrucibleRecipeWrapper recipe) {
            int stirs = buffer.readVarInt();
            int steps = buffer.readVarInt();
            List<Object> matches = new ArrayList<>();
            for (int i = 0; i < steps; i++) {
                Object ingredient = ItemUtil.readRecipeIngredient(buffer);
                if (ingredient != null) {
                    matches.add(ingredient);
                }
            }
            recipe.addStirringStep(stirs, matches.toArray());
        }
    }

    public static Category findCategory(String name) {
        try {
            return Category.valueOf(name.toUpperCase());
        } catch (Exception e) {
            return Category.MISC;
        }

    }

    public enum Category {
        REAGENTS, SOUL_GEMS, BASIC_ALCHEMY, WARPED_SPROUTS, MISC;
    }
}
