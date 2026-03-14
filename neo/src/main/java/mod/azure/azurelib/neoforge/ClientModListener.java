package mod.azure.azurelib.neoforge;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;
import net.neoforged.neoforge.client.event.AddClientReloadListenersEvent;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicBoolean;

import mod.azure.azurelib.AzureLib;
import mod.azure.azurelib.common.animation.cache.AzBakedAnimationCache;
import mod.azure.azurelib.common.model.cache.AzBakedModelCache;

/**
 * Registers AzureLib's geo-model and animation reload listeners with NeoForge's
 * sorted resource-manager event.
 *
 * Registration is guarded by a one-shot flag so it is safe to call from both
 * the explicit {@code modEventBus.addListener()} path in {@link NeoForgeAzureLibMod}
 * AND a residual {@code @EventBusSubscriber} scan if NeoForge finds the class
 * anyway — whichever fires first wins, the second call is a no-op.
 */
public class ClientModListener {

    private static final AtomicBoolean registered = new AtomicBoolean(false);
    static final ResourceLocation CACHE_LISTENER_KEY =
            ResourceLocation.fromNamespaceAndPath(AzureLib.MOD_ID, "azurelib_cache");

    public static void onAddReloadListeners(final AddClientReloadListenersEvent event) {
        if (!registered.compareAndSet(false, true)) {
            AzureLib.LOGGER.debug("AzureLib: reload-listener already registered, skipping duplicate call");
            return;
        }
        AzureLib.LOGGER.info("AzureLib: Registering reload listeners for model/animation cache");
        event.addListener(
            CACHE_LISTENER_KEY,
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
