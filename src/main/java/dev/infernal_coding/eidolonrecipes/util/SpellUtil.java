package dev.infernal_coding.eidolonrecipes.util;

import elucent.eidolon.capability.ReputationProvider;
import net.minecraft.client.renderer.Rectangle2d;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.tags.ITag;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.StringTextComponent;

public class SpellUtil {

	public static double getReputation(PlayerEntity player, ResourceLocation deity) {
		return player.world.getCapability(ReputationProvider.CAPABILITY)
				.map(cap -> cap.getReputation(player, deity))
				.orElse(0D);
	}

	public static void addReputation(PlayerEntity player, ResourceLocation deity, double amount) {
		player.world.getCapability(ReputationProvider.CAPABILITY)
				.ifPresent(cap -> cap.addReputation(player, deity, amount));
	}

	public static StringTextComponent getFormattedTag(ITag<EntityType<?>> tag) {
		StringBuilder stringBuilder = new StringBuilder();

		for (int i = 0; i < tag.getAllElements().size(); i++) {
			if (i < tag.getAllElements().size() - 1) {

				if (i < tag.getAllElements().size() - 2) {
					String entityName = I18n.format(tag.getAllElements().get(i).getTranslationKey());
					stringBuilder.append(entityName).append(", ");
				} else {
					stringBuilder.append(I18n.format(tag.getAllElements().get(i).getTranslationKey()));
					stringBuilder.append("or ");
				}
			} else {
				stringBuilder.append(I18n.format(tag.getAllElements().get(i).getTranslationKey()));
			}
		}
		return new StringTextComponent(stringBuilder.toString());
	}

	public static boolean isInTooltipRange(int x, int y, int radiusX, int radiusY, int mouseX, int mouseY) {
		Rectangle2d rectangle2d = new Rectangle2d(x, y, radiusX, radiusY);
		return rectangle2d.contains(mouseX, mouseY);
	}

}
