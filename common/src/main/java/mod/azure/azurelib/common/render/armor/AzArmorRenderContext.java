package mod.azure.azurelib.common.render.armor;

import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;

/**
 * Thread-local context for armor rendering.
 * Stores the current entity being rendered so that armor renderers can access it.
 */
public class AzArmorRenderContext {
    
    private static final ThreadLocal<LivingEntity> CURRENT_ENTITY = new ThreadLocal<>();
    
    /**
     * Set the current entity being rendered.
     * Called from LivingEntityRenderer mixin when extracting render state.
     */
    public static void setCurrentEntity(@Nullable LivingEntity entity) {
        CURRENT_ENTITY.set(entity);
    }
    
    /**
     * Get the current entity being rendered.
     * @return The current entity, or null if not in a render context.
     */
    @Nullable
    public static LivingEntity getCurrentEntity() {
        return CURRENT_ENTITY.get();
    }
    
    /**
     * Clear the current entity.
     * Should be called after rendering is complete.
     */
    public static void clearCurrentEntity() {
        CURRENT_ENTITY.remove();
    }
}
