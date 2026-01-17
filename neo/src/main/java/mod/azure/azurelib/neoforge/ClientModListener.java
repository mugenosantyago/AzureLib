package mod.azure.azurelib.neoforge;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

import mod.azure.azurelib.AzureLib;

// Config system disabled for 1.21.8 port
@EventBusSubscriber(modid = AzureLib.MOD_ID, value = Dist.CLIENT)
public class ClientModListener {

    @SubscribeEvent
    public static void clientInit(final FMLClientSetupEvent event) {
        // Config screen registration disabled for 1.21.8 port
    }
}
