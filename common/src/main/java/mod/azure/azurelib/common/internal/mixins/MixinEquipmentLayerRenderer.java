/**
 * Mixin for intercepting equipment layer rendering in 1.21.8+.
 * This is where the actual armor textures are rendered.
 */
package mod.azure.azurelib.common.internal.mixins;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.layers.EquipmentLayerRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.EquipmentClientInfo;
import net.minecraft.core.component.DataComponents;
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

import mod.azure.azurelib.AzureLib;
import mod.azure.azurelib.common.render.armor.AzArmorRendererRegistry;

/**
 * Intercepts EquipmentLayerRenderer.renderLayers() — the final step in vanilla armor rendering.
 *
 * By the time renderLayers() is called from HumanoidArmorLayer.renderArmorPiece(), the
 * HumanoidModel parameter has already had:
 *   - getParentModel().copyPropertiesTo(model) applied  → correct pose (crouching, riding, etc.)
 *   - setPartVisibility(model, slot) applied             → only the relevant slot bones are visible
 *
 * We use that correctly-posed model as the base for AzureLib's bone transform application,
 * render the 3D geo model, then cancel the vanilla 2D layer.
 */
@Mixin(EquipmentLayerRenderer.class)
public class MixinEquipmentLayerRenderer {

    /**
     * 7-parameter renderLayers (standard call from HumanoidArmorLayer in 1.21.8).
     */
    @Inject(
        method = "renderLayers(Lnet/minecraft/client/resources/model/EquipmentClientInfo$LayerType;Lnet/minecraft/resources/ResourceKey;Lnet/minecraft/client/model/Model;Lnet/minecraft/world/item/ItemStack;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
        at = @At("HEAD"),
        cancellable = true,
        require = 0
    )
    private void azurelib$renderLayers7(
            EquipmentClientInfo.LayerType layerType,
            ResourceKey<EquipmentAsset> assetKey,
            Model model,
            ItemStack stack,
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            int packedLight,
            CallbackInfo ci
    ) {
        azurelib$tryRender3D(model, stack, poseStack, packedLight, ci);
    }

    /**
     * 8-parameter renderLayers (with texture override, used in some call-sites).
     */
    @Inject(
        method = "renderLayers(Lnet/minecraft/client/resources/model/EquipmentClientInfo$LayerType;Lnet/minecraft/resources/ResourceKey;Lnet/minecraft/client/model/Model;Lnet/minecraft/world/item/ItemStack;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/resources/ResourceLocation;)V",
        at = @At("HEAD"),
        cancellable = true,
        require = 0
    )
    private void azurelib$renderLayers8(
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
        azurelib$tryRender3D(model, stack, poseStack, packedLight, ci);
    }

    /**
     * Core logic: if the item has a registered AzureLib armor renderer, render the 3D geo
     * model using the already-posed HumanoidModel and cancel vanilla 2D layer rendering.
     */
    @Unique
    private void azurelib$tryRender3D(Model model, ItemStack stack, PoseStack poseStack, int packedLight, CallbackInfo ci) {
        if (stack == null || stack.isEmpty()) return;

        var renderer = AzArmorRendererRegistry.getOrNull(stack);
        if (renderer == null) return;

        // Determine the equipment slot from the item's Equippable component.
        var equippable = stack.get(DataComponents.EQUIPPABLE);
        if (equippable == null) {
            ci.cancel();
            return;
        }
        EquipmentSlot slot = equippable.slot();

        if (model instanceof HumanoidModel<?> humanoidModel) {
            try {
                // prepForRenderWithoutEntity sets up the pipeline context:
                //   currentStack, currentSlot, baseModel (the posed HumanoidModel),
                //   and fetches the baked geo model from cache.
                renderer.prepForRenderWithoutEntity(stack, slot, humanoidModel);

                // azRenderToBuffer:
                //   1. Gets bufferSource from the level renderer
                //   2. Applies applyBaseTransformations() to copy head/body rotation from humanoidModel
                //   3. Applies applyBoneVisibilityBySlot() so only the relevant slot bone is visible
                //   4. Renders the geo model bones
                var armorModel = renderer.rendererPipeline().armorModel();
                armorModel.azRenderToBuffer(poseStack, null, packedLight, OverlayTexture.NO_OVERLAY, -1);

                AzureLib.LOGGER.debug("AzureLib: Rendered 3D armor for {} in slot {}", stack.getItem(), slot);
            } catch (Exception e) {
                AzureLib.LOGGER.error("AzureLib: Error rendering 3D armor for {} in slot {}", stack.getItem(), slot, e);
            }
        }

        // Always cancel the vanilla 2D equipment layer for registered items,
        // even if 3D rendering failed (avoids showing the wrong texture).
        ci.cancel();
    }
}
