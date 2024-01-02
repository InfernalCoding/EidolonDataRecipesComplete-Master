package dev.infernal_coding.eidolonrecipes.spells.requirement.impl;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import dev.infernal_coding.eidolonrecipes.spells.requirement.ISpellRequirementSerializer;
import dev.infernal_coding.eidolonrecipes.ModRoot;
import dev.infernal_coding.eidolonrecipes.spells.SpellInfo;
import dev.infernal_coding.eidolonrecipes.spells.SpellRecipeWrapper;
import dev.infernal_coding.eidolonrecipes.spells.requirement.ISpellRequirement;
import dev.infernal_coding.eidolonrecipes.util.EntityUtil;
import elucent.eidolon.tile.GobletTileEntity;
import net.minecraft.advancements.criterion.EntityTypePredicate;
import net.minecraft.entity.Entity;
import net.minecraft.entity.merchant.villager.AbstractVillagerEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiPredicate;

public class GobletRequirement implements ISpellRequirement {
    public static final ResourceLocation ID = ModRoot.eidolonRes("goblet");
    public static Map<ResourceLocation, BiPredicate<GobletRequirement, Entity>> ENTITY_PREDICATES = new HashMap<>();
    static {
        ENTITY_PREDICATES.put(ModRoot.eidolonRes("any"), (goblet, entity) -> true);
        ENTITY_PREDICATES.put(ModRoot.eidolonRes("is_animal"), (goblet, entity) -> entity instanceof AnimalEntity);
        ENTITY_PREDICATES.put(ModRoot.eidolonRes("is_villager_or_player"), (goblet, entity) -> entity instanceof AbstractVillagerEntity || entity instanceof PlayerEntity);
        ENTITY_PREDICATES.put(ModRoot.eidolonRes("entity_type"), (goblet, entity) -> goblet.type.map(predicate -> predicate.test(entity.getType())).orElse(false));
    }

    private final ResourceLocation sacrifice;
    private final Optional<EntityTypePredicate> type;

    public GobletRequirement(ResourceLocation sacrifice, Optional<EntityTypePredicate> type) {
        this.sacrifice = sacrifice;
        this.type = type;
    }

    @Override
    public boolean canCast(SpellRecipeWrapper spell, World world, BlockPos pos, PlayerEntity caster, SpellInfo spellInfo) {
        if (!spellInfo.goblet.isPresent()) {
            return false;
        }

        GobletTileEntity goblet = spellInfo.goblet.get();
        if (goblet.getEntityType() == null) {
            return false;
        }

        BiPredicate<GobletRequirement, Entity> predicate = ENTITY_PREDICATES.get(this.sacrifice);
        Entity test = goblet.getEntityType().create(world);
        return predicate.test(this, test);
    }

    @Override
    public ResourceLocation getId() {
        return ID;
    }
    public ResourceLocation getSacrifice() {
        return sacrifice;
    }

    public Optional<EntityTypePredicate> getType() {
        return type;
    }

    public static class Serializer implements ISpellRequirementSerializer<GobletRequirement> {

        @Override
        public void serialize(JsonObject json, GobletRequirement result) {
            json.addProperty("sacrifice", result.sacrifice.toString());
            result.type.ifPresent(type -> json.add("entity_type", type.serialize()));
        }

        @Override
        public GobletRequirement deserialize(JsonObject json) {
            ResourceLocation sacrifice = new ResourceLocation(JSONUtils.getString(json, "sacrifice"));
            Optional<EntityTypePredicate> type;
            if (sacrifice.toString().equals("eidolon:entity_type")) {
                type = Optional.of(EntityUtil.deserializeFromString(json.get("entity_type").getAsString()));
            } else {
                type = Optional.empty();
            }
            return new GobletRequirement(sacrifice, type);
        }

        @Override
        public void write(PacketBuffer buf, GobletRequirement requirement) {
            buf.writeResourceLocation(requirement.sacrifice);
            buf.writeBoolean(requirement.type.isPresent());
            if (requirement.type.isPresent()) {
                JsonElement json = requirement.type.get().serialize();
                if (json.isJsonPrimitive()) {
                    buf.writeString(json.getAsString());
                }
            }
        }

        @Override
        public GobletRequirement read(PacketBuffer buf) {
            ResourceLocation sacrifice = buf.readResourceLocation();
            Optional<EntityTypePredicate> type;
            if (buf.readBoolean()) {
                type = Optional.of(EntityTypePredicate.deserialize(new JsonPrimitive(buf.readString())));
            } else {
                type = Optional.empty();
            }

            return new GobletRequirement(sacrifice, type);
        }

        @Override
        public ResourceLocation getId() {
            return ID;
        }
    }
}
