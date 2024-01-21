package dev.infernal_coding.eidolonrecipes.rituals;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Pair;
import dev.infernal_coding.eidolonrecipes.registry.RecipeTypes;
import dev.infernal_coding.eidolonrecipes.rituals.requirement.AdvancementRequirement;
import dev.infernal_coding.eidolonrecipes.rituals.requirement.DimensionRequirement;
import dev.infernal_coding.eidolonrecipes.rituals.requirement.ExperienceRequirement;
import dev.infernal_coding.eidolonrecipes.util.JsonUtil;
import elucent.eidolon.ritual.*;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.EnchantedBookItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionUtils;
import net.minecraft.potion.Potions;
import net.minecraft.tags.ITag;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.registries.ForgeRegistryEntry;

import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static dev.infernal_coding.eidolonrecipes.rituals.RitualManager.*;


public class RitualRecipeWrapper extends Ritual implements IRecipe<IInventory> {

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
        return getRegistryName();
    }

    @Override
    public IRecipeSerializer<?> getSerializer() {
        return RecipeTypes.RITUAL_SERIALIZER.get();
    }

    @Override
    public IRecipeType<?> getType() {
        return RecipeTypes.RITUAL;
    }

    public static class Serializer extends ForgeRegistryEntry<IRecipeSerializer<?>> implements IRecipeSerializer<RitualRecipeWrapper> {
        private static final Field healthField = ObfuscationReflectionHelper.findField(HealthRequirement.class,
                "health");

        @Override
        public RitualRecipeWrapper read(ResourceLocation recipeId, JsonObject json) {
            JsonObject jsonRequirements = JSONUtils.getJsonObject(json, "requirements", null);
            JsonArray jsonResults = JSONUtils.getJsonArray(json, "results", null);
            JsonObject jsonBrazier = JSONUtils.getJsonObject(jsonRequirements, "brazier", null);
            JsonArray jsonItems = JSONUtils.getJsonArray(jsonRequirements, "items", null);
            JsonArray jsonExtras = JSONUtils.getJsonArray(json, "extras", null);

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
                            JsonUtil.getOptionalResourceLocation(requirement, "advancement");
                    Optional<ResourceLocation> dimension =
                            JsonUtil.getOptionalResourceLocation(requirement, "dimension");
                    Optional<Integer> experience =
                            JsonUtil.getOptionalInt(requirement, "xpLevels");
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
        public RitualRecipeWrapper read(ResourceLocation recipeId, PacketBuffer buffer) {
            int color;
            boolean isColorPreset;
            boolean usesNecro = false;
            Object brazierItemRequirement;
            ResourceLocation symbol = SummonRitual.SYMBOL;
            String title = "";
            String description = "";

            try {
                symbol = buffer.readResourceLocation();
            } catch (Exception ignored) {}

            try {
                title = buffer.readString();
            } catch (Exception ignored) {}

            try {
                title = buffer.readString();
            } catch (Exception ignored) {}

            Pair<Integer, Boolean> colorDetails = RitualManager.getColor(buffer);

            color = colorDetails.getFirst();
            isColorPreset = colorDetails.getSecond();

            brazierItemRequirement = getBrazierRequirement(buffer);

            ArrayList<IRequirement> requirements = new ArrayList<>();
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
                String type = buffer.readString();

                if (RESULTS.get(type) != null) {

                    RitualManager.ResultColorPair resultColorPair = RESULTS.get(type).getColorAndResult(buffer, color, isColorPreset, type);

                    if (resultColorPair != null) {
                        color = resultColorPair.getColor();
                        results.add(new Result(resultColorPair.getResult(), resultColorPair.getType(), resultColorPair.getCount()));
                    }
                }
            }
            return new RitualRecipeWrapper(symbol, title, description, color, requirements, new ArrayList<>(), necroticFocusRequirements, brazierItemRequirement, recipeId, results, usesNecro);
        }

        @Override
        public void write(PacketBuffer buffer, RitualRecipeWrapper recipe) {

            buffer.writeResourceLocation(recipe.getSymbol());
            buffer.writeInt(recipe.getColor());
            buffer.writeString(recipe.title);
            buffer.writeString(recipe.description);

            if (recipe.brazierItemRequirement instanceof ITag) {
                ITag<?> brazierItemTag = (ITag<?>) recipe.brazierItemRequirement;
                buffer.writeResourceLocation(new ResourceLocation(brazierItemTag.toString()));
            } else {
                ItemStack itemRequired = (ItemStack) recipe.brazierItemRequirement;
                buffer.writeItemStack(itemRequired, false);
            }

            if (recipe.ritualRequirements.get(0) instanceof HealthRequirement) {
                HealthRequirement healthRequirement = (HealthRequirement) recipe.ritualRequirements.get(0);
                try {
                    buffer.writeFloat(healthField.getFloat(healthRequirement));
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }

            int length = recipe.ritualRequirements.get(0) instanceof HealthRequirement ?
                    recipe.ritualRequirements.size() - 1 : recipe.ritualRequirements.size();

            int index = recipe.ritualRequirements.get(0) instanceof HealthRequirement ?
                    1 : 0;

            buffer.writeVarInt(length);

            while (index < recipe.ritualRequirements.size()) {
                IRequirement requirement = recipe.ritualRequirements.get(index);
                buffer.writeBoolean(usesNecro(recipe.necroticFocusRequirements, requirement));

                if (requirement instanceof ItemRequirement) {
                    ItemRequirement itemRequirement = (ItemRequirement) requirement;
                    if (itemRequirement.getMatch() instanceof ITag) {
                        ITag<?> tag = (ITag<?>) itemRequirement.getMatch();
                        buffer.writeString("tag");
                        buffer.writeResourceLocation(new ResourceLocation(tag.toString()));
                    } else if (itemRequirement.getMatch() instanceof ItemStack) {
                        buffer.writeString("item");
                        buffer.writeItemStack((ItemStack) itemRequirement.getMatch());
                    }
                }
                index++;
            }

            buffer.writeVarInt(recipe.results.size());
            for (Result result : recipe.results) {
                RESULTS.get(result.variant).writeResult(result, buffer);
            }
        }
    }

    @Override
    public RitualResult start(World world, BlockPos pos) {
        results.forEach(result -> RESULTS.get(result.variant).startRitual(result, world, pos));
        return results.stream().anyMatch(Result::runsOnTick) ? RitualResult.PASS : RitualResult.TERMINATE;
    }

    @Override
    public RitualResult tick(World world, BlockPos pos) {

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
