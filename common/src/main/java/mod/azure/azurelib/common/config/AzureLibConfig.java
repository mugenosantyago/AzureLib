package mod.azure.azurelib.common.config;

import mod.azure.azurelib.common.AzureLib;

@Config(id = AzureLib.MOD_ID)
public class AzureLibConfig {

    @Configurable
    @Configurable.Synchronized
    public boolean useVanillaUseKey = true;
}
