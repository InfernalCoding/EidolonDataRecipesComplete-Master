package dev.infernal_coding.eidolonrecipes.mixin.getters;

import net.minecraft.advancements.criterion.EntityTypePredicate;
import net.minecraft.entity.EntityType;
import net.minecraft.tags.ITag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(EntityTypePredicate.TagPredicate.class)
public interface TagPredicateMixin {
    @Accessor("tag")
    ITag<EntityType<?>> getTag();
}
