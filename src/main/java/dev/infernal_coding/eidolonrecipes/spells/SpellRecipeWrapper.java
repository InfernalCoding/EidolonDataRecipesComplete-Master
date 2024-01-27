package dev.infernal_coding.eidolonrecipes.spells;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import dev.infernal_coding.eidolonrecipes.registry.RecipeTypes;
import dev.infernal_coding.eidolonrecipes.spells.requirement.ISpellRequirement;
import dev.infernal_coding.eidolonrecipes.spells.requirement.ISpellRequirementSerializer;
import dev.infernal_coding.eidolonrecipes.spells.result.ISpellResult;
import dev.infernal_coding.eidolonrecipes.spells.result.ISpellResultSerializer;
import dev.infernal_coding.eidolonrecipes.spells.type.ISpell;
import dev.infernal_coding.eidolonrecipes.spells.type.ISpellSerializer;
import dev.infernal_coding.eidolonrecipes.util.JSONUtils;
import elucent.eidolon.capability.IReputation;
import elucent.eidolon.deity.Deities;
import elucent.eidolon.deity.Deity;
import elucent.eidolon.ritual.Ritual;
import elucent.eidolon.spell.AltarInfo;
import elucent.eidolon.spell.Sign;
import elucent.eidolon.spell.Signs;
import elucent.eidolon.spell.StaticSpell;
import elucent.eidolon.tile.EffigyTileEntity;
import elucent.eidolon.tile.GobletTileEntity;
import elucent.eidolon.util.ColorUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class SpellRecipeWrapper extends StaticSpell implements Recipe<Container> {

    private final Sign[] signs;

    private final Deity deity;
    private final ISpell spell;
    private final ISpellRequirement[] requirements;
    private final ISpellResult[] results;
    private final String title, chant, text;
    private final Optional<Integer> color;

    public SpellRecipeWrapper(ResourceLocation name, Deity deity, ISpell spell, ISpellRequirement[] requirements, ISpellResult[] results, String title, String chant, String text, Optional<Integer> color, Sign... signs) {
        super(name, signs);
        this.deity = deity;
        this.spell = spell;
        this.requirements = requirements;
        this.results = results;
        this.title = title;
        this.chant = chant;
        this.text = text;
        this.color = color;
        this.signs = signs;
    }

    @Override
    public boolean canCast(Level world, BlockPos pos, Player caster) {
        if (!world.getCapability(IReputation.INSTANCE).isPresent()) {
            return false;
        } else {
            List<GobletTileEntity> goblets = Ritual.getTilesWithinAABB(GobletTileEntity.class, world, new AABB(pos.offset(-4, -4, -4), pos.offset(5, 5, 5)));
            List<EffigyTileEntity> effigies = Ritual.getTilesWithinAABB(EffigyTileEntity.class, world, new AABB(pos.offset(-4, -4, -4), pos.offset(5, 5, 5)));
            Optional<EffigyTileEntity> effigy = effigies.stream().min(Comparator.comparingDouble((e) -> e.getBlockPos().distSqr(pos)));
            Optional<GobletTileEntity> goblet = goblets.stream().min(Comparator.comparingDouble((e) -> e.getBlockPos().distSqr(pos)));
            Optional<AltarInfo> altar = effigy.map(effigyTileEntity -> AltarInfo.getAltarInfo(world, effigyTileEntity.getBlockPos()));

            SpellInfo spellInfo = new SpellInfo(effigy, goblet, altar, world.getCapability(IReputation.INSTANCE).resolve().get());

            for (ISpellRequirement requirement : this.requirements) {
                if (!requirement.canCast(this, world, pos, caster, spellInfo)) {
                    return false;
                }
            }

            return this.spell.canCast(this, world, pos, caster, spellInfo);
        }
    }

    @Override
    public void cast(Level world, BlockPos pos, Player caster) {
        List<GobletTileEntity> goblets = Ritual.getTilesWithinAABB(GobletTileEntity.class, world, new AABB(pos.offset(-4, -4, -4), pos.offset(5, 5, 5)));
        List<EffigyTileEntity> effigies = Ritual.getTilesWithinAABB(EffigyTileEntity.class, world, new AABB(pos.offset(-4, -4, -4), pos.offset(5, 5, 5)));
        Optional<EffigyTileEntity> effigy = effigies.stream().min(Comparator.comparingDouble((e) -> e.getBlockPos().distSqr(pos)));
        Optional<GobletTileEntity> goblet = goblets.stream().min(Comparator.comparingDouble((e) -> e.getBlockPos().distSqr(pos)));
        Optional<AltarInfo> altar = effigy.map(effigyTileEntity -> AltarInfo.getAltarInfo(world, effigyTileEntity.getBlockPos()));

        SpellInfo spellInfo = new SpellInfo(effigy, goblet, altar, world.getCapability(IReputation.INSTANCE).resolve().get());

        for (ISpellResult result : this.results) {
            result.onCast(this, world, pos, caster, spellInfo);
        }

        this.spell.onCast(this, world, pos, caster, spellInfo);
    }

    public String getTitle() {
        return title;
    }

    public String getChant() {
        return chant;
    }

    public String getText() {
        return text;
    }
    public Deity getDeity() {
        return this.deity;
    }

    public Sign[] getSigns() {
        return signs;
    }

    public ISpellRequirement[] getRequirements() {
        return requirements;
    }

    public ISpellResult[] getResults() {
        return results;
    }

    public ISpell getSpell() {
        return this.spell;
    }

    public Optional<Integer> getColor() {
        return color;
    }

    @Override
    public ResourceLocation getId() {
        return this.getRegistryName();
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return RecipeTypes.SPELL_SERIALIZER.get();
    }

    @Override
    public RecipeType<?> getType() {
        return RecipeTypes.SPELL.get();
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

    public static class Serializer implements RecipeSerializer<SpellRecipeWrapper> {

        @Override
        public SpellRecipeWrapper fromJson(ResourceLocation recipeId, JsonObject json) {
            // Deity
            ResourceLocation deityName = new ResourceLocation(JSONUtils.getString(json, "deity", ""));
            Deity deity = Deities.find(deityName);
            if (deity == null) {
                throw new JsonSyntaxException("Unknown Deity '" + deityName + "'");
            }


            // Signs
            JsonArray signsJson = JSONUtils.getJSONArray(json, "signs");
            List<Sign> signs = new ArrayList<>();
            signsJson.forEach(signJson -> {
                ResourceLocation signName = new ResourceLocation(signJson.getAsString()) ;
                Sign sign = Signs.find(signName);
                if (sign == null) {
                    throw new JsonSyntaxException("Unknown Sign '" + signName + "'");
                }
                signs.add(sign);
            });

            // Spell
            JsonObject spellJson = JSONUtils.getJsonObject(json, "spell");
            ResourceLocation spellType = new ResourceLocation(JSONUtils.getString(spellJson, "type", ""));
            ISpellSerializer<ISpell> spellTypeSerializer = SpellManager.getSpellSerializer(spellType);
            if (spellTypeSerializer == null) {
                throw new JsonSyntaxException("Unknown Spell Type '" + spellType + "'");
            }
            ISpell spell = spellTypeSerializer.deserialize(spellJson);

            // Requirements
            List<ISpellRequirement> requirements = new ArrayList<>();
            JsonArray requirementsJson = JSONUtils.getJSONArray(json, "requirements");
            requirementsJson.forEach(j -> {
                JsonObject reqJson = (JsonObject) j;
                ResourceLocation type = new ResourceLocation(JSONUtils.getString(reqJson, "type", ""));
                ISpellRequirementSerializer<ISpellRequirement> requirementSerializer = SpellManager.getRequirementSerializer(type);
                if (requirementSerializer == null) {
                    throw new JsonSyntaxException("Unknown Requirement Type '" + type + "'");
                }
                requirements.add(requirementSerializer.deserialize(reqJson));
            });

            // Results
            List<ISpellResult> results = new ArrayList<>();
            JsonArray resultsJson = JSONUtils.getJSONArray(json, "results");
            resultsJson.forEach(j -> {
                JsonObject resJson = (JsonObject) j;
                ResourceLocation type = new ResourceLocation(JSONUtils.getString(resJson, "type", ""));
                ISpellResultSerializer<ISpellResult> resultSerializer = SpellManager.getResultSerializer(type);
                if (resultSerializer == null) {
                    throw new JsonSyntaxException("Unknown Result Type '" + type + "'");
                }
                results.add(resultSerializer.deserialize(resJson));
            });

            // Color
            Optional<Integer> color = Optional.empty();
            if (json.has("particleColor")) {
                JsonArray colorArray = JSONUtils.getJSONArray(json, "particleColor");
                color = Optional.of(ColorUtil.packColor(colorArray.get(0).getAsInt(), colorArray.get(1).getAsInt(), colorArray.get(2).getAsInt(), colorArray.get(3).getAsInt()));
            }

            String title = JSONUtils.getString(json, "title", "");
            String chant = JSONUtils.getString(json, "chant", "");
            String text = JSONUtils.getString(json, "text", "");

            return new SpellRecipeWrapper(recipeId, deity, spell, requirements.toArray(new ISpellRequirement[0]), results.toArray(new ISpellResult[0]), title, chant, text, color, signs.toArray(new Sign[0]));
        }

        @Override
        public void toNetwork(FriendlyByteBuf buffer, SpellRecipeWrapper recipe) {
            // Deity
            buffer.writeResourceLocation(recipe.getDeity().getId());

            // Signs
            buffer.writeVarInt(recipe.signs.length);
            for (Sign sign : recipe.signs) {
                buffer.writeResourceLocation(sign.getRegistryName());
            }

            // Spell
            buffer.writeResourceLocation(recipe.spell.getId());
            SpellManager.getSpellSerializer(recipe.spell.getId()).write(buffer, recipe.spell);

            // Requirements
            buffer.writeVarInt(recipe.requirements.length);
            for (ISpellRequirement requirement : recipe.requirements) {
                buffer.writeResourceLocation(requirement.getId());
                SpellManager.getRequirementSerializer(requirement.getId()).write(buffer, requirement);
            }

            // Results
            buffer.writeVarInt(recipe.results.length);
            for (ISpellResult result : recipe.results) {
                buffer.writeResourceLocation(result.getId());
                SpellManager.getResultSerializer(result.getId()).write(buffer, result);
            }

            // Color
            buffer.writeBoolean(recipe.color.isPresent());
            recipe.color.ifPresent(buffer::writeInt);

            buffer.writeUtf(recipe.title);
            buffer.writeUtf(recipe.chant);
            buffer.writeUtf(recipe.text);
        }

        @Nullable
        @Override
        public SpellRecipeWrapper fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer) {
            // Deity
            ResourceLocation deityName = buffer.readResourceLocation();
            Deity deity = Deities.find(deityName);

            // Signs
            int length = buffer.readVarInt();
            List<Sign> signs = new ArrayList<>();
            for (int i = 0; i < length; i++) {
                signs.add(Signs.find(buffer.readResourceLocation()));
            }

            // Spell
            ISpell spell = SpellManager.getSpellSerializer(buffer.readResourceLocation()).read(buffer);

            // Requirements
            length = buffer.readVarInt();
            List<ISpellRequirement> requirements = new ArrayList<>();
            for (int i = 0; i < length; i++) {
                requirements.add(SpellManager.getRequirementSerializer(buffer.readResourceLocation()).read(buffer));
            }

            // Requirements
            length = buffer.readVarInt();
            List<ISpellResult> results = new ArrayList<>();
            for (int i = 0; i < length; i++) {
                results.add(SpellManager.getResultSerializer(buffer.readResourceLocation()).read(buffer));
            }

            // Color
            Optional<Integer> color = Optional.empty();
            if (buffer.readBoolean()) {
                color = Optional.of(buffer.readInt());
            }

            String title = buffer.readUtf();
            String chant = buffer.readUtf();
            String text = buffer.readUtf();

            return new SpellRecipeWrapper(recipeId, deity, spell, requirements.toArray(new ISpellRequirement[0]), results.toArray(new ISpellResult[0]), title, chant, text, color, signs.toArray(new Sign[0]));
        }
    }
}
