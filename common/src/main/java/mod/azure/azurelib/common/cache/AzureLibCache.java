/**
 * This class is a fork of the matching class found in the Geckolib repository. Original source:
 * https://github.com/bernie-g/geckolib Copyright © 2024 Bernie-G. Licensed under the MIT License.
 * https://github.com/bernie-g/geckolib/blob/main/LICENSE
 */
package mod.azure.azurelib.common.cache;

import net.minecraft.client.Minecraft;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ReloadableResourceManager;
import net.minecraft.server.packs.resources.ResourceManager;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import mod.azure.azurelib.common.animation.cache.AzBakedAnimationCache;
import mod.azure.azurelib.common.model.cache.AzBakedModelCache;
import mod.azure.azurelib.common.util.AzureLibException;

public final class AzureLibCache {

    private AzureLibCache() {
        throw new UnsupportedOperationException();
    }

    public static void registerReloadListener() {
        Minecraft mc = Minecraft.getInstance();

        if (mc == null) {
            return;
        }

        if (!(mc.getResourceManager() instanceof ReloadableResourceManager resourceManager)) {
            throw new AzureLibException("AzureLib was initialized too early!");
        }

        // 1.21.8: PreparableReloadListener.reload signature changed to 4 parameters
        resourceManager.registerReloadListener(new PreparableReloadListener() {
            @Override
            public CompletableFuture<Void> reload(
                PreparationBarrier stage,
                ResourceManager rm,
                Executor bgExec,
                Executor gameExec
            ) {
                return CompletableFuture
                    .allOf(
                        AzBakedAnimationCache.getInstance().loadAnimations(bgExec, rm),
                        AzBakedModelCache.getInstance().loadModels(bgExec, rm)
                    )
                    .thenCompose(stage::wait)
                    .thenAcceptAsync(empty -> {}, gameExec);
            }
        });
    }
}
