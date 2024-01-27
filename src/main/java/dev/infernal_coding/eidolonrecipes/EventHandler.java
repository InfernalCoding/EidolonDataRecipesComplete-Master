package dev.infernal_coding.eidolonrecipes;

import dev.infernal_coding.eidolonrecipes.registry.EidolonReflectedRegistries;
import elucent.eidolon.capability.IReputation;
import elucent.eidolon.capability.ReputationEntry;
import net.minecraft.server.ServerAdvancementManager;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.OnDatapackSyncEvent;
import net.minecraftforge.event.level.SleepFinishedTimeEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = ModRoot.ID)
public class EventHandler {


    public static ServerAdvancementManager advancements;

    public static PlayerList playerList;

    @SubscribeEvent
    public static void addReloadListeners(AddReloadListenerEvent event) {

    }

    @SubscribeEvent
    public static void onDataReload(OnDatapackSyncEvent event) {
        playerList = event.getPlayerList();
        advancements = playerList.getServer().getAdvancements();
        EidolonReflectedRegistries.onDataPackReloaded(event.getPlayerList().getServer().getRecipeManager());
    }

    @SubscribeEvent
    public static void onSleepFinished(SleepFinishedTimeEvent event) {
        Level world = ((Level) event.getLevel());
        long skipped = event.getNewTime() - world.getDayTime();
        // When sleeping, reduce the prayer time to account for the slept time
        world.getCapability(IReputation.INSTANCE)
                .ifPresent(cap -> cap.getPrayerTimes().forEach((uuid, map)
                        -> map.replaceAll((id, time) -> Math.max(0, time - skipped))));
    }
}
