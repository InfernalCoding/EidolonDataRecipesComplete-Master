package dev.infernal_coding.eidolonrecipes.mixin;

import dev.infernal_coding.eidolonrecipes.util.ItemUtil;
import dev.infernal_coding.eidolonrecipes.util.MixinCalls;
import elucent.eidolon.recipe.CrucibleRecipe;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(CrucibleRecipe.class)
public class CrucibleRecipeMixin {

    @Inject(method = "matches(Ljava/lang/Object;Lnet/minecraft/item/ItemStack;)Z",
            at = @At(value = "HEAD"), cancellable = true)
    private static void inject_matches(Object match, ItemStack sacrifice, CallbackInfoReturnable<Boolean> cir) {
        MixinCalls.crucibleRecipe_matches(match, sacrifice, cir);
        cir.setReturnValue(ItemUtil.matchesIngredient(match, sacrifice, false));
    }

}
