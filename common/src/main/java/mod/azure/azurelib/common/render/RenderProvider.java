package mod.azure.azurelib.common.render;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

/**
 * Interface for providing custom renderers for items.
 * Implement this in your createRenderer method to provide custom 3D models for armor.
 */
public interface RenderProvider {
    
    /**
     * Get the humanoid armor model for rendering armor on entities.
     * This is where you provide your custom 3D armor model.
     * Returning a custom model here will hide the vanilla 2D item icon.
     * 
     * @param livingEntity The entity wearing the armor
     * @param itemStack The armor item stack
     * @param equipmentSlot The equipment slot the armor is in
     * @param original The original/default armor model
     * @return The custom armor model to render, or the original if no custom model
     */
    default HumanoidModel<?> getHumanoidArmorModel(LivingEntity livingEntity, ItemStack itemStack,
                                                    EquipmentSlot equipmentSlot, HumanoidModel<?> original) {
        return original;
    }
}
