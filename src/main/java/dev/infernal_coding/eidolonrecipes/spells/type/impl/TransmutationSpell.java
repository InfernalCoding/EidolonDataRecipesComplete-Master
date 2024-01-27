package dev.infernal_coding.eidolonrecipes.spells.type.impl;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mojang.math.Vector3d;
import dev.infernal_coding.eidolonrecipes.ModRoot;
import dev.infernal_coding.eidolonrecipes.spells.SpellInfo;
import dev.infernal_coding.eidolonrecipes.spells.SpellRecipeWrapper;
import dev.infernal_coding.eidolonrecipes.spells.type.ISpell;
import dev.infernal_coding.eidolonrecipes.spells.type.ISpellSerializer;
import dev.infernal_coding.eidolonrecipes.util.ItemUtil;
import dev.infernal_coding.eidolonrecipes.util.JSONUtils;
import elucent.eidolon.network.MagicBurstEffectPacket;
import elucent.eidolon.network.Networking;
import elucent.eidolon.spell.Signs;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.crafting.CraftingHelper;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;

public class TransmutationSpell implements ISpell {
    public static final ResourceLocation ID = ModRoot.eidolonRes("transmutation");



    private final List<TransmutationRecipe> transmutations;

    public TransmutationSpell(List<TransmutationRecipe> transmutations) {
        this.transmutations = transmutations;
    }

    public List<TransmutationRecipe> getTransmutations() {
        return transmutations;
    }
    @Override
    public boolean canCast(SpellRecipeWrapper spell, Level world, BlockPos pos, Player caster, SpellInfo spellInfo) {


        HitResult ray = world.clip(new ClipContext(caster.getEyePosition(0.0F), caster.getEyePosition(0.0F).add(caster.getLookAngle().scale(4.0D)), ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, caster));
        Vec3 v = ray.getType() == HitResult.Type.BLOCK ? ray.getLocation() : caster.getEyePosition(0.0F).add(caster.getLookAngle().scale(4.0D));
        List<ItemEntity> items = world.getEntitiesOfClass(ItemEntity.class, new AABB(v.x - 1.5D, v.y - 1.5D, v.z - 1.5D, v.x + 1.5D, v.y + 1.5D, v.z + 1.5D));

        for (TransmutationRecipe recipe : this.transmutations) {
            if (recipe.match(items, null)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public void onCast(SpellRecipeWrapper spell, Level world, BlockPos pos, Player caster, SpellInfo spellInfo) {
        HitResult ray = world.clip(new ClipContext(caster.getEyePosition(0.0F), caster.getEyePosition(0.0F).add(caster.getLookAngle().scale(4.0D)), ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, caster));
        Vec3 v = ray.getType() == HitResult.Type.BLOCK ? ray.getLocation() : caster.getEyePosition(0.0F).add(caster.getLookAngle().scale(4.0D));
        List<ItemEntity> items = world.getEntitiesOfClass(ItemEntity.class, new AABB(v.x - 1.5D, v.y - 1.5D, v.z - 1.5D, v.x + 1.5D, v.y + 1.5D, v.z + 1.5D));

        TransmutationRecipe transmutation = null;
        List<Pair<ItemEntity, Integer>> matched = new ArrayList<>();
        for (TransmutationRecipe recipe : this.transmutations) {
            if (recipe.match(items, matched)) {
                transmutation = recipe;
            }
        }

        if (transmutation == null) {
            return;
        }

        for (Pair<ItemEntity, Integer> pair : matched) {
            ItemEntity item = pair.getLeft();
            int count = pair.getRight();
            Vec3 p = item.position();
            item.getItem().shrink(count);
            if (item.getItem().isEmpty()) {
                item.remove(Entity.RemovalReason.DISCARDED);
            }
            if (!world.isClientSide) {
                Networking.sendToTracking(world, item.getOnPos(), new MagicBurstEffectPacket(p.x, p.y, p.z, Signs.WICKED_SIGN.getColor(), Signs.BLOOD_SIGN.getColor()));
            }
        }

        if (!world.isClientSide) {
            for (ItemStack item : transmutation.results) {
                ItemEntity entity = new ItemEntity(world, v.x, v.y, v.z, item.copy());
                entity.setDefaultPickUpDelay();
                world.addFreshEntity(entity);
            }

            Networking.sendToTracking(world, new BlockPos(v), new MagicBurstEffectPacket(v.x, v.y, v.z, Signs.WICKED_SIGN.getColor(), Signs.BLOOD_SIGN.getColor()));
        } else {
            world.playSound(caster, caster.getOnPos(), SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.NEUTRAL, 1.0F, 0.6F + world.random.nextFloat() * 0.2F);
        }
    }

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    public static class TransmutationRecipe {
        private final List<Ingredient> ingredients;
        private final List<ItemStack> results;

        public TransmutationRecipe(List<Ingredient> ingredients, List<ItemStack> results) {
            this.ingredients = ingredients;
            this.results = results;
        }

        public List<Ingredient> getIngredients() {
            return ingredients;
        }

        public List<ItemStack> getResults() {
            return results;
        }

        public boolean match(List<ItemEntity> items, List<Pair<ItemEntity, Integer>> matched) {
            items = new ArrayList<>(items);
            List<Object> matchList = new ArrayList<>(this.ingredients);
            for (Object match : matchList) {
                boolean foundMatch = false;
                for (int j = 0; j < items.size(); j++) {
                    ItemEntity item = items.get(j);
                    if (ItemUtil.matchesIngredient(match, item.getItem(), false)) {
                        if (matched != null) {
                            int count = match instanceof ItemStack ? ((ItemStack) match).getCount() : 1;
                            matched.add(Pair.of(item, count));
                        }
                        items.remove(j);
                        foundMatch = true;
                        break;
                    }
                }
                if (!foundMatch) {
                    return false;
                }
            }

            return true;
        }

        public JsonObject toJson() {
            JsonArray ingredientArray = new JsonArray();
            for (Object ingredient : this.ingredients) {
                ingredientArray.add(ItemUtil.serializeRecipeIngredient(ingredient));
            }

            JsonArray resultArray = new JsonArray();
            for (ItemStack result : this.results) {
                resultArray.add(ItemUtil.serializeRecipeIngredient(result));
            }

            JsonObject json = new JsonObject();
            json.add("ingredients", ingredientArray);
            json.add("results", resultArray);

            return json;
        }

        public static TransmutationRecipe fromJson(JsonObject json) {
            List<Ingredient> ingredients = new ArrayList<>();
            JsonArray ingredientArray = JSONUtils.getJSONArray(json, "ingredients");
            ingredientArray.forEach(ingredientJson -> {
                ingredients.add(ItemUtil.deserializeRecipeIngredient(ingredientJson));
            });

            List<ItemStack> results = new ArrayList<>();
            JsonArray resultArray = JSONUtils.getJSONArray(json, "results");
            resultArray.forEach(resultJson -> {
                results.add(CraftingHelper.getItemStack((JsonObject) resultJson, true));
            });

            return new TransmutationRecipe(ingredients, results);
        }

        public void write(FriendlyByteBuf buf) {
            buf.writeVarInt(this.ingredients.size());
            for (Object ingredient : this.ingredients) {
                ItemUtil.writeRecipeIngredient(ingredient, buf);
            }

            buf.writeVarInt(this.results.size());
            for (ItemStack result : this.results) {
                buf.writeItemStack(result, false);
            }
        }

        public static TransmutationRecipe fromPacketBuffer(FriendlyByteBuf buf) {
            List<Ingredient> ingredients = new ArrayList<>();
            int size = buf.readVarInt();
            for (int i = 0; i < size; i++) {
                ingredients.add(ItemUtil.readRecipeIngredient(buf));
            }

            List<ItemStack> results = new ArrayList<>();
            size = buf.readVarInt();
            for (int i = 0; i < size; i++) {
                results.add(buf.readItem());
            }

            return new TransmutationRecipe(ingredients, results);
        }
    }

    public static class Serializer implements ISpellSerializer<TransmutationSpell> {

        @Override
        public void serialize(JsonObject json, TransmutationSpell spell) {
            JsonArray transmutations = new JsonArray();
            for (TransmutationRecipe recipe : spell.transmutations) {
                transmutations.add(recipe.toJson());
            }
            json.add("transmutations", transmutations);
        }

        @Override
        public TransmutationSpell deserialize(JsonObject json) {
            List<TransmutationRecipe> recipes = new ArrayList<>();
            JsonArray transmutations = JSONUtils.getJSONArray(json, "transmutations");
            transmutations.forEach(tr -> {
                JsonObject trJson = (JsonObject) tr;
                recipes.add(TransmutationRecipe.fromJson(trJson));
            });
            return new TransmutationSpell(recipes);
        }

        @Override
        public void write(FriendlyByteBuf buf, TransmutationSpell spell) {
            buf.writeVarInt(spell.transmutations.size());
            for (TransmutationRecipe recipe : spell.transmutations) {
                recipe.write(buf);
            }
        }

        @Override
        public TransmutationSpell read(FriendlyByteBuf buf) {
            List<TransmutationRecipe> recipes = new ArrayList<>();
            int size = buf.readVarInt();
            for (int i = 0; i < size; i++) {
                recipes.add(TransmutationRecipe.fromPacketBuffer(buf));
            }
            return new TransmutationSpell(recipes);
        }

        @Override
        public ResourceLocation getId() {
            return ID;
        }
    }
}
