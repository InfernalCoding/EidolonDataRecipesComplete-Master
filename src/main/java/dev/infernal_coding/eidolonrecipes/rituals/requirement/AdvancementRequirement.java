package dev.infernal_coding.eidolonrecipes.rituals.requirement;

import dev.infernal_coding.eidolonrecipes.EventHandler;
import elucent.eidolon.ritual.IRequirement;
import elucent.eidolon.ritual.RequirementInfo;
import elucent.eidolon.ritual.Ritual;
import net.minecraft.advancements.Advancement;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.PlayerAdvancements;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;

import java.util.List;

public class AdvancementRequirement implements IRequirement {
    public ResourceLocation advancementName;
    public AdvancementRequirement(ResourceLocation advancementName) {
        this.advancementName = advancementName;
    }

    @Override
    public RequirementInfo isMet(Ritual ritual, Level world, BlockPos blockPos) {
        Advancement advancement = EventHandler.advancements.getAdvancement(advancementName);
        List<ServerPlayer> players = world.getEntitiesOfClass(ServerPlayer.class, ritual.getSearchBounds(blockPos));

        if (advancement == null) {
            return RequirementInfo.TRUE;
        }

        for (ServerPlayer player : players) {
            if (hasAdvancement(advancement, world, player)) {
                return RequirementInfo.TRUE;
            }
        }
        return RequirementInfo.FALSE;
    }

    public boolean hasAdvancement(Advancement advancement, Level world, ServerPlayer player) {
        PlayerAdvancements advancements = EventHandler.playerList
                .getPlayerAdvancements(player);
        return advancements.getOrStartProgress(advancement).isDone();
    }
}
