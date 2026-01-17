package mod.azure.azurelib.common.registry;

import net.minecraft.world.level.block.Block;

import java.util.function.Supplier;

import mod.azure.azurelib.common.blocks.TickingLightBlock;

public class AzureBlocksRegistry {

    // 1.21.8: Block registration is now handled by platform-specific code
    // For NeoForge, see NeoForgeAzureLibMod.TICKING_LIGHT_BLOCK
    // For Fabric, see FabricAzureLibMod (when implemented)
    
    // This supplier will be set by the platform-specific module
    public static Supplier<TickingLightBlock> TICKING_LIGHT_BLOCK = () -> null;
    
    /**
     * Sets the ticking light block supplier from platform-specific code.
     */
    public static void setTickingLightBlock(Supplier<TickingLightBlock> supplier) {
        TICKING_LIGHT_BLOCK = supplier;
    }

    public static void init() {}
}
