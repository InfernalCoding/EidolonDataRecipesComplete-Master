package dev.infernal_coding.eidolonrecipes.rituals;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Pair;
import dev.infernal_coding.eidolonrecipes.registry.RecipeTypes;
import dev.infernal_coding.eidolonrecipes.rituals.requirement.AdvancementRequirement;
import dev.infernal_coding.eidolonrecipes.rituals.requirement.DimensionRequirement;
import dev.infernal_coding.eidolonrecipes.rituals.requirement.ExperienceRequirement;
import dev.infernal_coding.eidolonrecipes.util.JSONUtils;
import elucent.eidolon.ritual.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Session;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;


import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static dev.infernal_coding.eidolonrecipes.rituals.RitualManager.*;


public class RitualRecipeWrapper extends Ritual implements Recipe<Container> {

    public final String title, description;
    final ArrayList<IRequirement> ritualRequirements, extraRequirements;
    final ArrayList<Object> necroticFocusRequirements;
    final Object brazierItemRequirement, sacrifice;
    final ArrayList<Result> results;
    final boolean usesNecroFocus;


    public ArrayList<Result> getResults() {
        return results;
    }

    public Object getSacrifice() {
        return sacrifice;
    }

    public boolean isUsesNecroFocus() {
        return usesNecroFocus;
    }

    public ArrayList<Object> getNecroticFocusRequirements() {
        return necroticFocusRequirements;
    }
    public Object getBrazierItemRequirement() {
        return brazierItemRequirement;
    }

    RitualRecipeWrapper(ResourceLocation symbol, String title, String description, int color, ArrayList<IRequirement> ritualRequirements, ArrayList<IRequirement> extraRequirements,  ArrayList<Object> necroticFocusRequirements, Object brazierItemRequirement, ResourceLocation id, ArrayList<Result> results, boolean usesNecroFocus) {
        super(symbol, color);
        setRegistryName(id);
        this.title = title;
        this.description = description;
        this.ritualRequirements = ritualRequirements;
        this.extraRequirements = extraRequirements;
        this.ritualRequirements.forEach(this::addRequirement);
        this.extraRequirements.forEach(this::addInvariant);
        this.necroticFocusRequirements = necroticFocusRequirements;
        this.brazierItemRequirement = brazierItemRequirement;
        this.results = results;
        this.usesNecroFocus = usesNecroFocus;
        this.sacrifice = usesNecroFocus ?
                new MultiItemSacrifice(brazierItemRequirement, necroticFocusRequirements.toArray()) : brazierItemRequirement;
    }

    @Override
    public boolean matches(Container inv, Level worldIn) {
        return false;
    }

    @Override
    public ItemStack assemble(Container inv) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return false;
    }

    @Override
    public ItemStack getResultItem() {
        return ItemStack.EMPTY;
    }

    @Override
    public ResourceLocation getId() {
        return getRegistryName();
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return RecipeTypes.RITUAL_SERIALIZER.get();
    }

    @Override
    public RecipeType<?> getType() {
        return RecipeTypes.RITUAL.get();
    }

    public static class Serializer implements RecipeSerializer<RitualRecipeWrapper> {
        private static final Field healthField = ObfuscationReflectionHelper.findField(HealthRequirement.class,
                "health");

        @Override
        public RitualRecipeWrapper fromJson(ResourceLocation recipeId, JsonObject json) {
            JsonObject jsonRequirements = JSONUtils.getJsonObject(json, "requirements");
            JsonArray jsonResults = JSONUtils.getJSONArray(json, "results");
            JsonObject jsonBrazier = JSONUtils.getJsonObject(jsonRequirements, "brazier");
            JsonArray jsonItems = JSONUtils.getJSONArray(jsonRequirements, "items");
            JsonArray jsonExtras = JSONUtils.getJSONArray(json, "extras");

            ArrayList<IRequirement> requirements = new ArrayList<>();
            ArrayList<IRequirement> extraRequirements = new ArrayList<>();
            ArrayList<Object> necroticFocusRequirements = new ArrayList<>();
            ArrayList<Result> results = new ArrayList<>();

            Pair<Integer, Boolean> colorDetail = RitualManager.getColor(json);
            boolean isColorPreset = colorDetail.getSecond();
            AtomicInteger color = new AtomicInteger(colorDetail.getFirst());

            AtomicBoolean usesNecroFocus = new AtomicBoolean(false);
            ResourceLocation symbol = new ResourceLocation(JSONUtils.getString(json, "symbol", SummonRitual.SYMBOL.toString()));
            String title = JSONUtils.getString(json, "title", "");
            String description = JSONUtils.getString(json, "description", "");

            Object brazierRequirement = getBrazierRequirement(jsonBrazier);

            int healthRequirement = JSONUtils.getInt(jsonRequirements, "health", 0);

            if (healthRequirement > 0) {
                requirements.add(new HealthRequirement(healthRequirement));
            }

            if (jsonExtras != null) {
                jsonExtras.forEach(req -> {
                    JsonObject requirement = (JsonObject) req;
                    Optional<ResourceLocation> advancement =
                            JSONUtils.getOptionalResourceLocation(requirement, "advancement");
                    Optional<ResourceLocation> dimension =
                            JSONUtils.getOptionalResourceLocation(requirement, "dimension");
                    Optional<Integer> experience =
                            JSONUtils.getOptionalInt(requirement, "xpLevels");
                    advancement.ifPresent(a -> extraRequirements.add(new AdvancementRequirement(a)));
                    dimension.ifPresent(a -> extraRequirements.add(new DimensionRequirement(a)));
                    experience.ifPresent(a -> extraRequirements.add(new ExperienceRequirement(a)));
                });
            }

            if (jsonItems != null) {
                jsonItems.forEach(rq -> {
                    JsonObject requirement = (JsonObject) rq;

                    ItemRequirement itemRequirement = getItemRequirement(requirement);
                    Object necroRequirement = getNecroticRequirement(requirement);

                    if (itemRequirement != null) {
                        if (necroRequirement != null) {
                            usesNecroFocus.set(true);
                            necroticFocusRequirements.add(necroRequirement);
                        }
                        requirements.add(itemRequirement);
                    }
                });
            }

            if (jsonResults != null) {
                jsonResults.forEach(rs -> {
                    JsonObject result = (JsonObject) rs;
                    String type = JSONUtils.getString(result, "type", "");

                    if (RESULTS.get(type) != null) {

                        RitualManager.ResultColorPair resultAndColor =
                                RitualManager.RESULTS.get(type).getColorAndResult(result, color.get(), isColorPreset, type);

                        if (resultAndColor != null) {
                            results.add(new Result(resultAndColor.getResult(), resultAndColor.getType(), resultAndColor.getCount()));
                            color.set(resultAndColor.getColor());
                        }
                    }
                });
            }

            return new RitualRecipeWrapper(symbol, title, description, color.get(),
                    requirements, extraRequirements, necroticFocusRequirements, brazierRequirement, recipeId, results, usesNecroFocus.get());
        }

        @Nullable
        @Override
        public RitualRecipeWrapper fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer) {
            boolean usesNecro = false;
            Object brazierItemRequirement;
            ResourceLocation symbol = buffer.readResourceLocation();
            String title = buffer.readUtf();
            String description = buffer.readUtf();
            int color = buffer.readInt();

            brazierItemRequirement = getBrazierRequirement(buffer);

            ArrayList<IRequirement> requirements = new ArrayList<>();
            ArrayList<IRequirement> extras = new ArrayList<>();
            ArrayList<Object> necroticFocusRequirements = new ArrayList<>();
            ArrayList<Result> results = new ArrayList<>();

            int healthRequired = buffer.readInt();

            if (healthRequired > 0) {
                requirements.add(new HealthRequirement(healthRequired));
            }

            int length = buffer.readVarInt();

            for (int i = 0; i < length; i++) {
                Pair<ItemRequirement, Object> itemRequirementPair = getItemRequirementPair(buffer);

                if (itemRequirementPair != null && itemRequirementPair.getSecond() == null) {
                    requirements.add(itemRequirementPair.getFirst());
                } else if (itemRequirementPair != null) {
                    requirements.add(itemRequirementPair.getFirst());
                    necroticFocusRequirements.add(itemRequirementPair.getSecond());
                    usesNecro = true;
                }
            }

            length = buffer.readVarInt();

            for (int i = 0; i < length; i++) {
                String type = buffer.readUtf();
                IRequirement rq;

                if (type.equals("advancement")) {
                    rq = new AdvancementRequirement(buffer.readResourceLocation());
                } else if (type.equals("dimension")) {
                    rq = new DimensionRequirement(buffer.readResourceLocation());
                } else {
                    rq = new ExperienceRequirement(buffer.readInt());
                }
                extras.add(rq);
            }

            length = buffer.readVarInt();

            for (int i = 0; i < length; i++) {
                String type = buffer.readUtf();

                if (RESULTS.get(type) != null) {

                    RitualManager.ResultColorPair resultColorPair = RESULTS.get(type).getColorAndResult(buffer, color, true, type);

                    if (resultColorPair != null) {
                        results.add(new Result(resultColorPair.getResult(), resultColorPair.getType(), resultColorPair.getCount()));
                    }
                }
            }
            return new RitualRecipeWrapper(symbol, title, description, color, requirements, extras, necroticFocusRequirements, brazierItemRequirement, recipeId, results, usesNecro);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buffer, RitualRecipeWrapper recipe) {

            buffer.writeResourceLocation(recipe.getSymbol());
            buffer.writeUtf(recipe.title);
            buffer.writeUtf(recipe.description);
            buffer.writeInt(recipe.getColor());


            if (recipe.brazierItemRequirement instanceof TagKey<?> brazierItemTag) {
                buffer.writeUtf("tag");
                buffer.writeResourceLocation(brazierItemTag.location());
            } else {
                ItemStack itemRequired = (ItemStack) recipe.brazierItemRequirement;
                buffer.writeUtf("item");
                buffer.writeItemStack(itemRequired, false);
            }

            if (recipe.ritualRequirements.get(0) instanceof HealthRequirement) {
                HealthRequirement healthRequirement = (HealthRequirement) recipe.ritualRequirements.get(0);
                try {
                    buffer.writeFloat(healthField.getFloat(healthRequirement));
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            } else buffer.writeFloat(0);

            int length = recipe.ritualRequirements.get(0) instanceof HealthRequirement ?
                    recipe.ritualRequirements.size() - 1 : recipe.ritualRequirements.size();

            int index = recipe.ritualRequirements.get(0) instanceof HealthRequirement ?
                    1 : 0;

            buffer.writeVarInt(length);

            ArrayList<Object> temp = new ArrayList<>(recipe.necroticFocusRequirements);

            while (index < recipe.ritualRequirements.size()) {
                IRequirement requirement = recipe.ritualRequirements.get(index);

                if (requirement instanceof ItemRequirement itemRequirement) {
                    buffer.writeBoolean(usesNecro(temp, itemRequirement, recipe.ritualRequirements));
                    if (itemRequirement.getMatch() instanceof TagKey<?> tag) {
                        buffer.writeUtf("tag");
                        buffer.writeResourceLocation(tag.location());
                    } else if (itemRequirement.getMatch() instanceof ItemStack) {
                        buffer.writeUtf("item");
                        buffer.writeItemStack((ItemStack) itemRequirement.getMatch(), false);
                    }
                }
                index++;
            }
            index = 0;

            buffer.writeVarInt(length = recipe.extraRequirements.size());

            while (index < length) {
                IRequirement rq = recipe.extraRequirements.get(0);

                if (rq instanceof AdvancementRequirement requirement) {
                    buffer.writeUtf("advancement");
                    buffer.writeResourceLocation(requirement.advancementName);
                } else if (rq instanceof DimensionRequirement requirement) {
                    buffer.writeUtf("dimension");
                    buffer.writeResourceLocation(requirement.dimension);
                } else if (rq instanceof ExperienceRequirement requirement) {
                    buffer.writeUtf("experience");
                    buffer.writeInt(requirement.experienceLevel);
                }
                index++;
            }

            buffer.writeVarInt(recipe.results.size());
            for (Result result : recipe.results) {
                buffer.writeUtf(result.variant);
                RESULTS.get(result.variant).writeResult(result, buffer);
            }
        }
    }

    @Override
    public RitualResult start(Level world, BlockPos pos) {
        results.forEach(result -> RESULTS.get(result.variant).startRitual(this, result, world, pos));
        return results.stream().anyMatch(Result::runsOnTick) ? RitualResult.PASS : RitualResult.TERMINATE;
    }

    @Override
    public RitualResult tick(Level world, BlockPos pos) {

        for (Result result : results) {
            boolean continueRitual = RESULTS.get(result.variant).onRitualTick(result, world, pos);

            if (!continueRitual && RESULTS.get(result.variant).getRunsOnTick()) {
                return RitualResult.TERMINATE;
            }
        }
        return RitualResult.PASS;
    }

    public static class Result {
        private final Object toCreate;
        private final String variant;
        private final int count;
        public Object getToCreate() {
            return toCreate;
        }

        public int getCount() {
            return count;
        }

        public String getVariant() {
            return variant;
        }

        Result(Object toCreate, String variant, int count) {
            this.toCreate = toCreate;
            this.variant = variant;
            this.count = count;
        }

        private static boolean runsOnTick(Result result) {
            return RESULTS.get(result.variant).getRunsOnTick();
        }

    }
}
