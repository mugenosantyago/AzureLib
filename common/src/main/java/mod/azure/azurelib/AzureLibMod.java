package mod.azure.azurelib;

import mod.azure.azurelib.common.registry.AzureBlocksEntityRegistry;
import mod.azure.azurelib.common.registry.AzureBlocksRegistry;

public final class AzureLibMod {

    // Config system disabled for 1.21.8 port - not needed for Samurai Dynasty
    public static Object config;

    private AzureLibMod() {
        throw new UnsupportedOperationException();
    }

    public static void initRegistry() {
        AzureBlocksRegistry.init();
        AzureBlocksEntityRegistry.init();
    }

    // Config registration disabled - not needed for entity/armor rendering
    public static <C> Object registerConfig(Class<C> configClass, Object formatFactory) {
        return null;
    }
}
