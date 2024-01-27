package dev.infernal_coding.eidolonrecipes.util;

import com.google.gson.JsonSyntaxException;
import net.minecraft.advancements.critereon.EntityTypePredicate;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.registries.ForgeRegistries;

public class EntityUtil {

    public static ResourceLocation getEntityName(EntityType<?> type) {
        return ForgeRegistries.ENTITY_TYPES.getKey(type);
    }

    public static EntityTypePredicate deserializeFromString(String s) {
        if (s.startsWith("#")) {
            ResourceLocation tagName = new ResourceLocation(s.substring(1));
            return new EntityTypePredicate.TagPredicate(TagKey.create(Registry.ENTITY_TYPE_REGISTRY, tagName));
        } else {
            ResourceLocation entityName = new ResourceLocation(s);
            EntityType<?> entitytype = ForgeRegistries.ENTITY_TYPES.getValue(entityName);
            if (entitytype == null) {
                throw new JsonSyntaxException("Unknown entity type '" + entityName + "'");
            }
            return new EntityTypePredicate.TypePredicate(entitytype);
        }
    }

    public static EntityType<?> getEntityType(String entityName) {
        if (!ForgeRegistries.ENTITY_TYPES.containsKey(new ResourceLocation(entityName))
                || entityName.equals("villager")
                || entityName.equals("zombie")
                || entityName.equals("skeleton")) {
            return null;
        }
        return ForgeRegistries.ENTITY_TYPES.getValue(new ResourceLocation(entityName));
    }

    public static class Container {
        private final EntityType<?> entityType;
        private final int count;


        public EntityType<?> getEntityType() {
            return entityType;
        }

        public int getCount() {
            return count;
        }
        public Container(EntityType<?> entityType, int count) {
            this.entityType = entityType;
            this.count = count;
        }
    }


}

