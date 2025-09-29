package mod.azure.azurelib.common.registry;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.PushReaction;

import java.util.function.Supplier;

import mod.azure.azurelib.common.blocks.TickingLightBlock;
import mod.azure.azurelib.common.platform.Services;

public class AzureBlocksRegistry {

    public static final Supplier<TickingLightBlock> TICKING_LIGHT_BLOCK = registerBlock(
        "lightblock",
        () -> new TickingLightBlock(
            BlockBehaviour.Properties.of()
                .sound(SoundType.CANDLE)
                .lightLevel(TickingLightBlock.LIGHT_EMISSION)
                .pushReaction(PushReaction.DESTROY)
                .noLootTable()
                .noCollission()
                .replaceable()
                .noOcclusion()
        )
    );

    public static <T extends Block> Supplier<T> registerBlock(String blockName, Supplier<T> block) {
        return Services.COMMON_REGISTRY.register(BuiltInRegistries.BLOCK, blockName, block);
    }

    public static void init() {}
}
