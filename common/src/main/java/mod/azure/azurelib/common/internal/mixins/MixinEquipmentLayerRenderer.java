/**
 * Mixin for intercepting equipment layer rendering in 1.21.8+.
 * This is where the actual armor textures are rendered.
 */
package mod.azure.azurelib.common.internal.mixins;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.layers.EquipmentLayerRenderer;
import net.minecraft.client.resources.model.EquipmentClientInfo;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.equipment.EquipmentAsset;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import mod.azure.azurelib.common.render.armor.AzArmorRendererRegistry;
import mod.azure.azurelib.common.render.armor.AzArmorRenderContext;

/**
 * Mixin to intercept EquipmentLayerRenderer for custom 3D armor rendering.
 * In 1.21.8, armor rendering goes through this class instead of directly in HumanoidArmorLayer.
 * This mixin cancels vanilla 2D layer rendering when a custom AzureLib 3D renderer is registered.
 */
@Mixin(EquipmentLayerRenderer.class)
public class MixinEquipmentLayerRenderer {

    @Unique
    private static boolean azurelib$logged = false;

    /**
     * Inject at renderLayers to intercept armor rendering (7 param version).
     * Cancel vanilla rendering when a custom AzureLib renderer is registered for the item.
     * 
     * Method signature in 1.21.8:
     * renderLayers(EquipmentClientInfo$LayerType layerType, ResourceKey<EquipmentAsset> assetKey, 
     *              Model model, ItemStack stack, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight)
     */
    @Inject(
        method = "renderLayers(Lnet/minecraft/client/resources/model/EquipmentClientInfo$LayerType;Lnet/minecraft/resources/ResourceKey;Lnet/minecraft/client/model/Model;Lnet/minecraft/world/item/ItemStack;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
        at = @At("HEAD"),
        cancellable = true,
        require = 0
    )
    private void azurelib$interceptRenderLayers7Params(
            EquipmentClientInfo.LayerType layerType,
            ResourceKey<EquipmentAsset> assetKey,
            Model model,
            ItemStack stack,
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            int packedLight,
            CallbackInfo ci
    ) {
        azurelib$checkAndCancelVanillaRender(stack, layerType, ci);
    }

    /**
     * Inject at renderLayers to intercept armor rendering (8 param version with texture override).
     * 
     * Method signature in 1.21.8:
     * renderLayers(EquipmentClientInfo$LayerType layerType, ResourceKey<EquipmentAsset> assetKey, 
     *              Model model, ItemStack stack, PoseStack poseStack, MultiBufferSource bufferSource, 
     *              int packedLight, ResourceLocation textureOverride)
     */
    @Inject(
        method = "renderLayers(Lnet/minecraft/client/resources/model/EquipmentClientInfo$LayerType;Lnet/minecraft/resources/ResourceKey;Lnet/minecraft/client/model/Model;Lnet/minecraft/world/item/ItemStack;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/resources/ResourceLocation;)V",
        at = @At("HEAD"),
        cancellable = true,
        require = 0
    )
    private void azurelib$interceptRenderLayers8Params(
            EquipmentClientInfo.LayerType layerType,
            ResourceKey<EquipmentAsset> assetKey,
            Model model,
            ItemStack stack,
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            int packedLight,
            ResourceLocation textureOverride,
            CallbackInfo ci
    ) {
        azurelib$checkAndCancelVanillaRender(stack, layerType, ci);
    }

    /**
     * Common method to check if vanilla rendering should be cancelled.
     */
    @Unique
    private void azurelib$checkAndCancelVanillaRender(ItemStack stack, EquipmentClientInfo.LayerType layerType, CallbackInfo ci) {
        if (stack == null || stack.isEmpty()) {
            return;
        }
        
        // Check if this item has a custom AzureLib renderer registered
        var renderer = AzArmorRendererRegistry.getOrNull(stack);
        
        if (renderer != null) {
            if (!azurelib$logged) {
                azurelib$logged = true;
                mod.azure.azurelib.AzureLib.LOGGER.info(
                    "AzureLib: EquipmentLayerRenderer mixin is active - will cancel vanilla armor layer rendering"
                );
            }
            
            mod.azure.azurelib.AzureLib.LOGGER.info(
                "AzureLib: Cancelling vanilla equipment layer for {} (layer type: {})", 
                stack.getItem(), layerType
            );
            
            // Cancel vanilla rendering - our 3D model is already rendered by MixinHumanoidArmorLayer
            ci.cancel();
        }
    }

    /**
     * Fallback injection for alternative method signatures.
     * Some versions may have different overloads.
     */
    @Inject(
        method = "renderLayers*",
        at = @At("HEAD"),
        cancellable = true,
        require = 0
    )
    private void azurelib$interceptRenderLayersFallback(CallbackInfo ci) {
        // Check if we're currently rendering an entity with custom armor
        var entity = AzArmorRenderContext.getCurrentEntity();
        if (entity == null) {
            return;
        }
        
        // Check all armor slots for custom renderers
        for (EquipmentSlot slot : new EquipmentSlot[]{
            EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET
        }) {
            ItemStack armorStack = entity.getItemBySlot(slot);
            if (!armorStack.isEmpty()) {
                var renderer = AzArmorRendererRegistry.getOrNull(armorStack);
                if (renderer != null) {
                    // We have custom armor being rendered - this method shouldn't 
                    // run its vanilla logic. However, we can't determine which specific
                    // piece this call is for without parameters, so we rely on the
                    // more specific injection above.
                    mod.azure.azurelib.AzureLib.LOGGER.debug(
                        "AzureLib: Entity has custom armor in slot {}: {}",
                        slot, armorStack.getItem()
                    );
                }
            }
        }
    }
}
