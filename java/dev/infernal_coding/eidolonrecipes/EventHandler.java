package dev.infernal_coding.eidolonrecipes;

import dev.infernal_coding.eidolonrecipes.registry.EidolonReflectedRegistries;
import elucent.eidolon.capability.ReputationProvider;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementManager;
import net.minecraft.server.management.PlayerList;
import net.minecraft.world.World;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.OnDatapackSyncEvent;
import net.minecraftforge.event.world.SleepFinishedTimeEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = ModRoot.ID)
public class EventHandler {

    public static AdvancementManager advancements;

    public static PlayerList playerList;

    @SubscribeEvent
    public static void addReloadListeners(AddReloadListenerEvent event) {

    }

    @SubscribeEvent
    public static void onDataReload(OnDatapackSyncEvent event) {
        playerList = event.getPlayerList();
        advancements = playerList.getServer().getAdvancementManager();
        EidolonReflectedRegistries.onDataPackReloaded(event.getPlayerList().getServer().getRecipeManager());
    }

    @SubscribeEvent
    public static void onSleepFinished(SleepFinishedTimeEvent event) {
        World world = ((World) event.getWorld());
        long skipped = event.getNewTime() - world.getDayTime();
        // When sleeping, reduce the prayer time to account for the slept time
        world.getCapability(ReputationProvider.CAPABILITY).ifPresent(cap -> cap.getPrayerTimes().replaceAll((id, time) -> Math.max(0, time - skipped)));
    }
}
