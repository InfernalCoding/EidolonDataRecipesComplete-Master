package dev.infernal_coding.eidolonrecipes.util;

import com.google.gson.JsonObject;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Arrays;

public class MixinCalls {

    public static void stackUtil_ingredientFromObject_fixCopy(Object object, CallbackInfoReturnable<Ingredient> cir) {
        cir.setReturnValue(Ingredient.fromStacks(((ItemStack)object).copy()));
    }

    public static void stackUtil_ingredientFromObject_checkIng(Object object, CallbackInfoReturnable<Ingredient> cir) {
        if (object instanceof Ingredient) {
            ItemStack[] stacks = ((Ingredient) object).getMatchingStacks();
            cir.setReturnValue(Ingredient.fromStacks(Arrays.stream(stacks).map(ItemStack::copy)));
        }
    }

    public static void stackUtil_stackFromObject(Object object, CallbackInfoReturnable<ItemStack> cir) {
        if (object instanceof Ingredient) {
            ItemStack[] matchingStacks = ((Ingredient) object).getMatchingStacks();
            if (matchingStacks.length > 0) {
                cir.setReturnValue(matchingStacks[0].copy());
            }
        }
    }

    public static void crucibleRecipe_matches(Object match, ItemStack sacrifice, CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(ItemUtil.matchesIngredient(match, sacrifice, false));
    }

    public static void worktableRecipe_matches(Object match, ItemStack sacrifice, CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(ItemUtil.matchesIngredient(match, sacrifice, false));
    }

    public static void ingredient_deserializeItemList(JsonObject json, CallbackInfoReturnable<Ingredient.IItemList> cir, ResourceLocation itemName, Item item) {
        if (json.has("count") && json.get("count").isJsonPrimitive() && json.get("count").getAsJsonPrimitive().isNumber()) {
            int count = json.get("count").getAsInt();
            cir.setReturnValue(new Ingredient.SingleItemList(new ItemStack(item, count)));
        }
    }
}
