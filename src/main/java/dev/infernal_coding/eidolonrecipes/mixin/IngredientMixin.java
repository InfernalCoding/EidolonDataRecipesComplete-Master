package dev.infernal_coding.eidolonrecipes.mixin;

import com.google.gson.JsonObject;
import dev.infernal_coding.eidolonrecipes.util.MixinCalls;
import net.minecraft.item.Item;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(value = Ingredient.class)
public class IngredientMixin {

    @Inject(method = "deserializeItemList",
            at = @At(value = "RETURN", ordinal = 0),
            locals = LocalCapture.CAPTURE_FAILHARD, cancellable = true)
    private static void inject_deserializeItemList(JsonObject json, CallbackInfoReturnable<Ingredient.IItemList> cir, ResourceLocation itemName, Item item) {
        MixinCalls.ingredient_deserializeItemList(json, cir, itemName, item);
    }
}
