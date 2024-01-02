package dev.infernal_coding.eidolonrecipes.mixin;

import dev.infernal_coding.eidolonrecipes.util.MixinCalls;
import elucent.eidolon.recipe.WorktableRecipe;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(WorktableRecipe.class)
public class WorktableRecipeMixin {

    @Inject(method = "matches(Ljava/lang/Object;Lnet/minecraft/item/ItemStack;)Z",
            at = @At(value = "HEAD"), cancellable = true)
    private static void inject_matches(Object match, ItemStack sacrifice, CallbackInfoReturnable<Boolean> cir) {
        MixinCalls.worktableRecipe_matches(match, sacrifice, cir);
    }

}
