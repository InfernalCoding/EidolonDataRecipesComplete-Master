package dev.infernal_coding.eidolonrecipes.jei;

import dev.infernal_coding.eidolonrecipes.ModRoot;
import dev.infernal_coding.eidolonrecipes.mixin.getters.StaticSpellMixin;
import dev.infernal_coding.eidolonrecipes.registry.EidolonReflectedRegistries;
import dev.infernal_coding.eidolonrecipes.registry.RecipeTypes;
import dev.infernal_coding.eidolonrecipes.spells.SpellRecipeWrapper;
import dev.infernal_coding.eidolonrecipes.spells.requirement.ISpellRequirement;
import dev.infernal_coding.eidolonrecipes.spells.result.ISpellResult;
import dev.infernal_coding.eidolonrecipes.spells.type.ISpell;
import dev.infernal_coding.eidolonrecipes.spells.type.impl.BasicSpell;
import elucent.eidolon.Eidolon;
import elucent.eidolon.Registry;
import elucent.eidolon.deity.Deity;
import elucent.eidolon.gui.jei.CrucibleCategory;
import elucent.eidolon.gui.jei.RitualCategory;
import elucent.eidolon.spell.Sign;
import elucent.eidolon.spell.StaticSpell;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.runtime.IIngredientManager;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import java.util.*;
import java.util.stream.Collectors;

@JeiPlugin
public class JEIRegistry implements IModPlugin {

    public static IRecipeCategory SPELL_CATEGORY;
    public ResourceLocation getPluginUid() {
        return new ResourceLocation(ModRoot.ID, "jei_plugin");
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registry) {
        registry.addRecipeCatalyst(new ItemStack(Registry.STONE_ALTAR.get()),
                SPELL_CATEGORY.getUid());
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registry) {
        IGuiHelper guiHelper = registry.getJeiHelpers().getGuiHelper();
        registry.addRecipeCategories(SPELL_CATEGORY = new SpellCategory(guiHelper));
    }

    @Override
    public void registerRecipes(IRecipeRegistration registry) {
      List<SpellRecipeWrapper> spells =
             new ArrayList<>(EidolonReflectedRegistries.getRecipes(Minecraft.getInstance().world.getRecipeManager(), RecipeTypes.SPELL).values());
      /*if (EidolonReflectedRegistries.SPELLS != null) {

          EidolonReflectedRegistries.SPELLS.forEach(spell -> {
              if (spells.stream().noneMatch(wrapper -> wrapper.getRegistryName().equals(spell.getRegistryName()))) {
                  if (spell instanceof StaticSpell) {
                      List<Sign> signs = ((StaticSpellMixin) spell).getSigns();
                      SpellRecipeWrapper wrapper = new SpellRecipeWrapper(spell.getRegistryName(), null,
                              new BasicSpell(), new ISpellRequirement[0], new ISpellResult[0], Optional.empty(), (Sign[]) signs.toArray());
                      spells.add(wrapper);
                  }
              }
          });
      }*/
      registry.addRecipes(spells, SPELL_CATEGORY.getUid());
    }
}
