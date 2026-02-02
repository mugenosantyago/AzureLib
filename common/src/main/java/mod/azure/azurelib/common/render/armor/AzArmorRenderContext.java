package mod.azure.azurelib.common.render.armor;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;
import java.util.Set;

/**
 * Thread-local context for armor rendering.
 * Stores the current entity being rendered and tracks which slots have custom 3D rendering active.
 * This is used to coordinate between MixinHumanoidArmorLayer (which renders 3D models) and
 * MixinEquipmentLayerRenderer (which needs to cancel vanilla 2D layer rendering).
 */
public class AzArmorRenderContext {
    
    private static final ThreadLocal<LivingEntity> CURRENT_ENTITY = new ThreadLocal<>();
    private static final ThreadLocal<Set<EquipmentSlot>> CUSTOM_RENDERED_SLOTS = 
            ThreadLocal.withInitial(() -> EnumSet.noneOf(EquipmentSlot.class));
    private static final ThreadLocal<ItemStack> CURRENT_CUSTOM_STACK = new ThreadLocal<>();
    
    /**
     * Set the current entity being rendered.
     * Called from LivingEntityRenderer mixin when extracting render state.
     */
    public static void setCurrentEntity(@Nullable LivingEntity entity) {
        CURRENT_ENTITY.set(entity);
        // Clear custom rendered slots when starting a new entity render
        CUSTOM_RENDERED_SLOTS.get().clear();
        CURRENT_CUSTOM_STACK.remove();
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
        CUSTOM_RENDERED_SLOTS.get().clear();
        CURRENT_CUSTOM_STACK.remove();
    }
    
    /**
     * Mark a slot as having custom 3D rendering.
     * Called from MixinHumanoidArmorLayer when rendering a custom armor piece.
     */
    public static void markSlotCustomRendered(EquipmentSlot slot, ItemStack stack) {
        CUSTOM_RENDERED_SLOTS.get().add(slot);
        CURRENT_CUSTOM_STACK.set(stack);
        mod.azure.azurelib.AzureLib.LOGGER.debug(
            "AzArmorRenderContext: Marked slot {} as custom rendered ({})", 
            slot, stack.getItem()
        );
    }
    
    /**
     * Check if a slot has been marked as having custom 3D rendering.
     */
    public static boolean isSlotCustomRendered(EquipmentSlot slot) {
        return CUSTOM_RENDERED_SLOTS.get().contains(slot);
    }
    
    /**
     * Check if an ItemStack should have its vanilla layer rendering suppressed.
     * This is called from MixinEquipmentLayerRenderer.
     */
    public static boolean shouldSuppressVanillaLayer(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return false;
        }
        
        // Check if this item has a registered custom renderer
        var renderer = AzArmorRendererRegistry.getOrNull(stack);
        return renderer != null;
    }
    
    /**
     * Get the current stack being custom rendered (if any).
     */
    @Nullable
    public static ItemStack getCurrentCustomStack() {
        return CURRENT_CUSTOM_STACK.get();
    }
}
