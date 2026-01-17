package mod.azure.azurelib.neoforge;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.AddClientReloadListenersEvent;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import mod.azure.azurelib.AzureLib;
import mod.azure.azurelib.common.animation.cache.AzBakedAnimationCache;
import mod.azure.azurelib.common.model.cache.AzBakedModelCache;

@EventBusSubscriber(modid = AzureLib.MOD_ID, value = Dist.CLIENT)
public class ClientModListener {

    @SubscribeEvent
    public static void onAddReloadListeners(final AddClientReloadListenersEvent event) {
        AzureLib.LOGGER.info("AzureLib: Registering reload listeners for model/animation cache");
        
        // Register the AzureLib cache reload listener
        event.addListener(
            ResourceLocation.fromNamespaceAndPath(AzureLib.MOD_ID, "azurelib_cache"),
            new PreparableReloadListener() {
                @Override
                public CompletableFuture<Void> reload(
                    PreparationBarrier stage,
                    ResourceManager resourceManager,
                    Executor backgroundExecutor,
                    Executor gameExecutor
                ) {
                    return CompletableFuture
                        .allOf(
                            AzBakedAnimationCache.getInstance().loadAnimations(backgroundExecutor, resourceManager),
                            AzBakedModelCache.getInstance().loadModels(backgroundExecutor, resourceManager)
                        )
                        .thenCompose(stage::wait)
                        .thenAcceptAsync(empty -> {}, gameExecutor);
                }
            }
        );
    }
}
