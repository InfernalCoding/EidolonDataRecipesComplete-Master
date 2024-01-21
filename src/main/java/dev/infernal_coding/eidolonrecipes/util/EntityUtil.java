package dev.infernal_coding.eidolonrecipes.util;

import com.google.gson.JsonSyntaxException;
import com.mojang.datafixers.util.Pair;
import net.minecraft.advancements.criterion.EntityTypePredicate;
import net.minecraft.entity.EntityType;
import net.minecraft.tags.TagCollectionManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;

public class EntityUtil {

    public static EntityTypePredicate deserializeFromString(String s) {
        if (s.startsWith("#")) {
            ResourceLocation tagName = new ResourceLocation(s.substring(1));
            return new EntityTypePredicate.TagPredicate(TagCollectionManager.getManager().getEntityTypeTags().getTagByID(tagName));
        } else {
            ResourceLocation entityName = new ResourceLocation(s);
            EntityType<?> entitytype = ForgeRegistries.ENTITIES.getValue(entityName);
            if (entitytype == null) {
                throw new JsonSyntaxException("Unknown entity type '" + entityName + "'");
            }
            return new EntityTypePredicate.TypePredicate(entitytype);
        }
    }

    public static EntityType<?> getEntityType(String entityName) {
        if (!ForgeRegistries.ENTITIES.containsKey(new ResourceLocation(entityName))
                || entityName.equals("villager")
                || entityName.equals("zombie")
                || entityName.equals("skeleton")) {
            return null;
        }
        return ForgeRegistries.ENTITIES.getValue(new ResourceLocation(entityName));
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

