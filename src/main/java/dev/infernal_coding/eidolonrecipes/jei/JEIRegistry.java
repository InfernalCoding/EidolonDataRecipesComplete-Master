package dev.infernal_coding.eidolonrecipes.jei;

import dev.infernal_coding.eidolonrecipes.ModRoot;
import dev.infernal_coding.eidolonrecipes.registry.EidolonReflectedRegistries;
import dev.infernal_coding.eidolonrecipes.registry.RecipeTypes;
import dev.infernal_coding.eidolonrecipes.spells.SpellRecipeWrapper;
import elucent.eidolon.Registry;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;


import java.util.ArrayList;
import java.util.List;

@JeiPlugin
public class JEIRegistry implements IModPlugin {

    public static RecipeType<SpellRecipeWrapper> SPELL = new RecipeType<>(RecipeTypes.SPELL.getId(), SpellRecipeWrapper.class);
    public static IRecipeCategory SPELL_CATEGORY;
    public ResourceLocation getPluginUid() {
        return new ResourceLocation(ModRoot.ID, "jei_plugin");
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registry) {
        registry.addRecipeCatalyst(new ItemStack(Registry.STONE_ALTAR.get()),
                SPELL);
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registry) {
        IGuiHelper guiHelper = registry.getJeiHelpers().getGuiHelper();
        registry.addRecipeCategories(SPELL_CATEGORY = new SpellCategory(guiHelper));
    }

    @Override
    public void registerRecipes(IRecipeRegistration registry) {
      List<SpellRecipeWrapper> spells =
             new ArrayList<>(EidolonReflectedRegistries.getRecipes(Minecraft.getInstance().level.getRecipeManager(), RecipeTypes.SPELL.get()).values());
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
      registry.addRecipes(SPELL, spells);
    }
}
