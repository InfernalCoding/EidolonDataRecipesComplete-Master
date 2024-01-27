package dev.infernal_coding.eidolonrecipes;

import dev.infernal_coding.eidolonrecipes.registry.EidolonReflectedRegistries;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RecipesUpdatedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = ModRoot.ID, value = Dist.CLIENT)
public class ClientEventHandler {

    @SubscribeEvent
    public static void on(RecipesUpdatedEvent event) {
        EidolonReflectedRegistries.onDataPackReloaded(event.getRecipeManager());
    }
}
