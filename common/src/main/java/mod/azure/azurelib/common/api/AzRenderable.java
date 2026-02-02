package mod.azure.azurelib.common.api;

import mod.azure.azurelib.common.render.RenderProvider;

import java.util.function.Consumer;

/**
 * Interface for items that provide custom rendering via RenderProvider.
 * Implement this interface in your Item class to provide custom 3D models for armor.
 */
public interface AzRenderable {
    /**
     * Create and provide a custom renderer for this item.
     * 
     * @param consumer Consumer that accepts the RenderProvider
     */
    void createRenderer(Consumer<RenderProvider> consumer);
}
