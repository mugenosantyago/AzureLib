package mod.azure.azurelib.common.render.entity.state;

import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.world.entity.Entity;

/**
 * EntityRenderState implementation for AzureLib entities in Minecraft 1.21.8+
 * Stores entity reference and rendering data for the new RenderState system.
 */
public class AzEntityRenderState extends EntityRenderState {
    public Entity entity;  // Reference to actual entity for AzureLib compatibility
    public float partialTick;
    public float entityYaw;
}
