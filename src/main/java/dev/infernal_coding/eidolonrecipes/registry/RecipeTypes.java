package dev.infernal_coding.eidolonrecipes.registry;

import dev.infernal_coding.eidolonrecipes.rituals.RitualRecipeWrapper;
import dev.infernal_coding.eidolonrecipes.spells.SpellRecipeWrapper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class RecipeTypes {

    public static final DeferredRegister<RecipeType<?>> RECIPES = DeferredRegister.create(ForgeRegistries.RECIPE_TYPES, "eidolon");
    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS = DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, "eidolon");
    public static final RegistryObject<RecipeType<SpellRecipeWrapper>> SPELL = RECIPES.register("spell", () -> RecipeType.simple(new ResourceLocation("eidolon:spell")));
    public static final RegistryObject<RecipeType<RitualRecipeWrapper>> RITUAL = RECIPES.register("ritual", () -> RecipeType.simple(new ResourceLocation("eidolon:ritual")));
    public static final RegistryObject<SpellRecipeWrapper.Serializer> SPELL_SERIALIZER = RECIPE_SERIALIZERS.register("spell", SpellRecipeWrapper.Serializer::new);
    public static final RegistryObject<RitualRecipeWrapper.Serializer> RITUAL_SERIALIZER = RECIPE_SERIALIZERS.register("ritual", RitualRecipeWrapper.Serializer::new);
}
