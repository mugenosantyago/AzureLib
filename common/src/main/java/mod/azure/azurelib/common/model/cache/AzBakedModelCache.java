package mod.azure.azurelib.common.model.cache;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import mod.azure.azurelib.AzureLib;
import mod.azure.azurelib.common.cache.AzResourceCache;
import mod.azure.azurelib.common.loading.FileLoader;
import mod.azure.azurelib.common.loading.json.raw.Model;
import mod.azure.azurelib.common.loading.object.GeometryTree;
import mod.azure.azurelib.common.model.AzBakedModel;
import mod.azure.azurelib.common.model.factory.registry.AzBakedModelFactoryRegistry;

/**
 * AzBakedModelCache is a singleton class that extends {@link AzResourceCache} and is designed to manage and cache baked
 * models of type {@link AzBakedModel}. It provides functionality to asynchronously load and store models associated
 * with specific resource locations.
 */
public class AzBakedModelCache extends AzResourceCache {

    private static final AzBakedModelCache INSTANCE = new AzBakedModelCache();

    public static AzBakedModelCache getInstance() {
        return INSTANCE;
    }

    private final Map<ResourceLocation, AzBakedModel> bakedModels;

    private AzBakedModelCache() {
        this.bakedModels = new Object2ObjectOpenHashMap<>();
    }

    public CompletableFuture<Void> loadModels(Executor backgroundExecutor, ResourceManager resourceManager) {
        AzureLib.LOGGER.info("AzureLib: Starting to load geo models...");
        return loadResources(backgroundExecutor, resourceManager, "geo", resource -> {
            AzureLib.LOGGER.debug("AzureLib: Loading model from: {}", resource);
            Model model = FileLoader.loadModelFile(resource, resourceManager);

            if (model == null) {
                AzureLib.LOGGER.warn("AzureLib: Failed to load model from: {}, using default", resource);
                var defaultModelLocation = AzureLib.modResource("geo/default_model.geo.json");
                model = FileLoader.loadModelFile(defaultModelLocation, resourceManager);
                var defaultBaked = AzBakedModelFactoryRegistry
                    .getForNamespace(resource.getNamespace())
                    .constructGeoModel(GeometryTree.fromModel(model));

                AzBakedModel.setDefault(defaultBaked);
            }

            var bakedModel = AzBakedModelFactoryRegistry.getForNamespace(resource.getNamespace())
                .constructGeoModel(GeometryTree.fromModel(model));
            AzureLib.LOGGER.debug("AzureLib: Loaded model {} with {} top-level bones", resource, bakedModel.getTopLevelBones().size());
            return bakedModel;
        }, (location, model) -> {
            bakedModels.put(location, model);
            AzureLib.LOGGER.debug("AzureLib: Cached model at: {}", location);
        });
    }

    public @Nullable AzBakedModel getNullable(ResourceLocation resourceLocation) {
        return bakedModels.get(resourceLocation);
    }
}
