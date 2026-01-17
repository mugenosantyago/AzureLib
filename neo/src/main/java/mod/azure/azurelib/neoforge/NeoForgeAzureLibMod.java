package mod.azure.azurelib.neoforge;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

import mod.azure.azurelib.AzureLib;
import mod.azure.azurelib.AzureLibMod;
import mod.azure.azurelib.common.blocks.TickingLightBlock;
import mod.azure.azurelib.common.network.packet.AzBlockEntityDispatchCommandPacket;
import mod.azure.azurelib.common.registry.AzureBlocksRegistry;
import mod.azure.azurelib.common.network.packet.AzEntityDispatchCommandPacket;
import mod.azure.azurelib.common.network.packet.AzItemStackDispatchCommandPacket;
import mod.azure.azurelib.common.network.packet.SendConfigDataPacket;

@Mod(AzureLib.MOD_ID)
public final class NeoForgeAzureLibMod {

    public static DeferredRegister<BlockEntityType<?>> blockEntityTypeDeferredRegister = DeferredRegister.create(
        BuiltInRegistries.BLOCK_ENTITY_TYPE,
        AzureLib.MOD_ID
    );

    // 1.21.8: Use DeferredRegister.Blocks for proper block ID handling
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(AzureLib.MOD_ID);
    
    // ResourceKey for the light block - required in 1.21.2+
    private static final ResourceKey<Block> LIGHTBLOCK_KEY = ResourceKey.create(
        Registries.BLOCK, 
        ResourceLocation.fromNamespaceAndPath(AzureLib.MOD_ID, "lightblock")
    );
    
    // Register the light block directly here with proper ID handling via setId()
    public static final DeferredBlock<TickingLightBlock> TICKING_LIGHT_BLOCK = BLOCKS.register(
        "lightblock",
        () -> new TickingLightBlock(
            BlockBehaviour.Properties.of()
                .setId(LIGHTBLOCK_KEY)
                .sound(SoundType.CANDLE)
                .lightLevel(TickingLightBlock.LIGHT_EMISSION)
                .pushReaction(PushReaction.DESTROY)
                .noLootTable()
                .noCollission()
                .replaceable()
                .noOcclusion()
        )
    );

    public static final DeferredRegister.DataComponents DATA_COMPONENTS_REGISTER = DeferredRegister
        .createDataComponents(
            Registries.DATA_COMPONENT_TYPE,
            AzureLib.MOD_ID
        );

    public NeoForgeAzureLibMod(IEventBus modEventBus) {
        AzureLib.initialize();
        // Wire up the common block supplier to our NeoForge-registered block
        AzureBlocksRegistry.setTickingLightBlock(TICKING_LIGHT_BLOCK);
        DATA_COMPONENTS_REGISTER.register(modEventBus);
        blockEntityTypeDeferredRegister.register(modEventBus);
        BLOCKS.register(modEventBus);
        // Config system disabled for 1.21.8 port
        modEventBus.addListener(this::init);
        modEventBus.addListener(this::registerMessages);
    }

    private void init(final FMLCommonSetupEvent event) {
        // Config system disabled for 1.21.8 port
    }

    public void registerMessages(final RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(AzureLib.MOD_ID);
        registrar.playBidirectional(
            AzEntityDispatchCommandPacket.TYPE,
            AzEntityDispatchCommandPacket.CODEC,
            (msg, ctx) -> msg.handle()
        );
        registrar.playBidirectional(
            AzItemStackDispatchCommandPacket.TYPE,
            AzItemStackDispatchCommandPacket.CODEC,
            (msg, ctx) -> msg.handle()
        );
        registrar.playBidirectional(
            AzBlockEntityDispatchCommandPacket.TYPE,
            AzBlockEntityDispatchCommandPacket.CODEC,
            (msg, ctx) -> msg.handle()
        );
        registrar.playBidirectional(
            SendConfigDataPacket.TYPE,
            SendConfigDataPacket.CODEC,
            (msg, ctx) -> msg.handle()
        );
    }
}
