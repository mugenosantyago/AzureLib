/**
 * This class is a fork of the matching class found in the Geckolib repository. Original source:
 * https://github.com/bernie-g/geckolib Copyright © 2024 Bernie-G. Licensed under the MIT License.
 * https://github.com/bernie-g/geckolib/blob/main/LICENSE
 */
package mod.azure.azurelib.common.internal.mixins;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.ARGB;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.DyedItemColor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import mod.azure.azurelib.common.render.armor.AzArmorRendererRegistry;

/**
 * Mixin for intercepting armor rendering in 1.21.8.
 * In 1.21.8, the renderArmorPiece method signature changed:
 * - Third parameter is now ItemStack (was LivingEntity in older versions)
 * - The entity data comes from HumanoidRenderState
 */
@Mixin(HumanoidArmorLayer.class)
@SuppressWarnings("rawtypes")
public abstract class MixinHumanoidArmorLayer<S extends HumanoidRenderState, A extends HumanoidModel<S>> {

    /**
     * Inject at the start of renderArmorPiece to intercept and use AzureLib armor rendering.
     * In 1.21.8, the method signature is:
     * renderArmorPiece(PoseStack, MultiBufferSource, ItemStack, EquipmentSlot, int, HumanoidModel)
     */
    @Inject(
        method = "renderArmorPiece",
        at = @At("HEAD"),
        cancellable = true,
        require = 0
    )
    public void azurelib$renderAzurelibModel(
        PoseStack poseStack,
        MultiBufferSource bufferSource,
        ItemStack stack,
        EquipmentSlot equipmentSlot,
        int packedLight,
        A baseModel,
        CallbackInfo ci
    ) {
        // Debug: Log every call to see if mixin is working
        mod.azure.azurelib.AzureLib.LOGGER.debug("AzureLib Mixin: renderArmorPiece called for item: {}", stack.getItem());
        
        var renderer = AzArmorRendererRegistry.getOrNull(stack);
        mod.azure.azurelib.AzureLib.LOGGER.debug("AzureLib Mixin: renderer lookup result: {}", renderer != null ? "FOUND" : "NOT FOUND");

        if (renderer != null) {
            mod.azure.azurelib.AzureLib.LOGGER.info("AzureLib: Rendering custom armor for item: {} in slot: {}", stack.getItem(), equipmentSlot);
            // Get dye color if applicable
            var dyeColor = stack.is(ItemTags.DYEABLE)
                ? ARGB.opaque(DyedItemColor.getOrDefault(stack, -6265536))
                : -1;

            var rendererPipeline = renderer.rendererPipeline();
            var armorModel = rendererPipeline.armorModel();
            @SuppressWarnings({"unchecked", "rawtypes"})
            var typedHumanoidModel = (HumanoidModel) armorModel;

            // In 1.21.8, entity is not directly available in renderArmorPiece
            // We prepare with minimal context - the armor renderer should handle this gracefully
            renderer.prepForRenderWithoutEntity(stack, equipmentSlot, baseModel);
            baseModel.copyPropertiesTo(typedHumanoidModel);

            // Render the custom armor model
            // Pass null for the VertexConsumer - azRenderToBuffer creates its own from the buffer source
            armorModel.azRenderToBuffer(poseStack, null, packedLight, OverlayTexture.NO_OVERLAY, dyeColor);
            ci.cancel();
        }
    }
}
