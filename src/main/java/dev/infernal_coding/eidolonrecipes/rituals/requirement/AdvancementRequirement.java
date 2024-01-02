package dev.infernal_coding.eidolonrecipes.rituals.requirement;

import dev.infernal_coding.eidolonrecipes.EventHandler;
import elucent.eidolon.ritual.IRequirement;
import elucent.eidolon.ritual.RequirementInfo;
import elucent.eidolon.ritual.Ritual;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementManager;
import net.minecraft.advancements.PlayerAdvancements;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.registries.ForgeRegistries;
import org.lwjgl.system.CallbackI;

import java.util.List;
import java.util.Optional;

public class AdvancementRequirement implements IRequirement {
    public ResourceLocation advancementName;
    public AdvancementRequirement(ResourceLocation advancementName) {
        this.advancementName = advancementName;
    }

    @Override
    public RequirementInfo isMet(Ritual ritual, World world, BlockPos blockPos) {
        Advancement advancement = EventHandler.advancements.getAdvancement(advancementName);
        List<ServerPlayerEntity> players = world.getEntitiesWithinAABB(ServerPlayerEntity.class, ritual.getSearchBounds(blockPos));

        if (advancement == null) {
            return RequirementInfo.TRUE;
        }

        for (ServerPlayerEntity player : players) {
            if (hasAdvancement(advancement, world, player)) {
                return RequirementInfo.TRUE;
            }
        }
        return RequirementInfo.FALSE;
    }

    public boolean hasAdvancement(Advancement advancement, World world, ServerPlayerEntity player) {
        PlayerAdvancements advancements = EventHandler.playerList
                .getPlayerAdvancements(player);
        return advancements.getProgress(advancement).isDone();
    }
}
