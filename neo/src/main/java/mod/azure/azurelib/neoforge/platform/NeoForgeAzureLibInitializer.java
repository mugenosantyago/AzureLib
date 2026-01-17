package mod.azure.azurelib.neoforge.platform;

import mod.azure.azurelib.AzureLib;
import mod.azure.azurelib.common.platform.services.AzureLibInitializer;

public class NeoForgeAzureLibInitializer implements AzureLibInitializer {

    @Override
    public void initialize() {
        // Reload listener is now registered in ClientModListener during FMLClientSetupEvent
        // This ensures Minecraft.getInstance() is available
        AzureLib.LOGGER.info("AzureLib: Initializing NeoForge platform");
    }
}
