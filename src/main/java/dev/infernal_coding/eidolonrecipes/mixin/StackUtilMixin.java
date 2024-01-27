package dev.infernal_coding.eidolonrecipes.mixin;

import dev.infernal_coding.eidolonrecipes.util.MixinCalls;
import elucent.eidolon.util.StackUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(StackUtil.class)
public class StackUtilMixin {

    @Inject(method = "ingredientFromObject", at = @At(value = "RETURN", ordinal = 2), cancellable = true)
    private static void inject_ingredientFromObject(Object object, CallbackInfoReturnable<Ingredient> cir) {
        MixinCalls.stackUtil_ingredientFromObject_fixCopy(object, cir);
    }

    @Inject(method = "ingredientFromObject", at = @At(value = "HEAD"), cancellable = true)
    private static void inject_ingredientFromObject_2(Object object, CallbackInfoReturnable<Ingredient> cir) {
        MixinCalls.stackUtil_ingredientFromObject_checkIng(object, cir);
    }

    @Inject(method = "stackFromObject", at = @At(value = "HEAD"), cancellable = true)
    private static void inject_stackFromObject(Object object, CallbackInfoReturnable<ItemStack> cir) {
        MixinCalls.stackUtil_stackFromObject(object, cir);
    }

}
