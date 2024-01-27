package dev.infernal_coding.eidolonrecipes.util;

import com.google.common.collect.Lists;
import elucent.eidolon.capability.IReputation;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;

import java.awt.geom.Rectangle2D;
import java.util.List;

public class SpellUtil {

	public static double getReputation(Player player, ResourceLocation deity) {
		return player.level.getCapability(IReputation.INSTANCE)
				.map(cap -> cap.getReputation(player, deity))
				.orElse(0D);
	}

	public static void addReputation(Player player, ResourceLocation deity, double amount) {
		player.level.getCapability(IReputation.INSTANCE)
				.ifPresent(cap -> cap.addReputation(player, deity, amount));
	}

	public static Component getFormattedTag(TagKey<EntityType<?>> tag) {
		StringBuilder stringBuilder = new StringBuilder();
		List<EntityType<?>> entities = getEntityTypes(tag);

		for (int i = 0; i < entities.size(); i++) {
			if (i <entities.size() - 1) {

				if (i < entities.size() - 2) {
					String entityName = EntityUtil.getEntityName(entities.get(i)).toString();
					stringBuilder.append(entityName).append(", ");
				} else {
					stringBuilder.append(EntityUtil.getEntityName(entities.get(i)).toString());
					stringBuilder.append("or ");
				}
			} else {
				stringBuilder.append(EntityUtil.getEntityName(entities.get(i)).toString());
			}
		}
		return Component.literal(stringBuilder.toString());
	}

	public static List<EntityType<?>> getEntityTypes(TagKey<EntityType<?>> tag) {
		List<EntityType<?>> list = Lists.newArrayList();

		for(Holder<EntityType<?>> holder : Registry.ENTITY_TYPE.getTagOrEmpty(tag)) {
			list.add(holder.get());
		}
		return list;
	}

	public static boolean isInTooltipRange(int x, int y, int radiusX, int radiusY, int mouseX, int mouseY) {
		Rectangle2D rectangle2d = new Rectangle2D.Double(x, y, radiusX, radiusY);
		return rectangle2d.contains(mouseX, mouseY);
	}

}
