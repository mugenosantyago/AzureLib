package mod.azure.azurelib.neoforge;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

import mod.azure.azurelib.AzureLib;
import mod.azure.azurelib.common.cache.AzureLibCache;

@EventBusSubscriber(modid = AzureLib.MOD_ID, value = Dist.CLIENT)
public class ClientModListener {

    @SubscribeEvent
    public static void clientInit(final FMLClientSetupEvent event) {
        // Register the reload listener during client setup when Minecraft is available
        event.enqueueWork(() -> {
            AzureLib.LOGGER.info("AzureLib: Registering reload listener for model/animation cache");
            AzureLibCache.registerReloadListener();
        });
    }
}
