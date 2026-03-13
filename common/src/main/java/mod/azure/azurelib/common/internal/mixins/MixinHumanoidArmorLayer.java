/**
 * This class is a fork of the matching class found in the Geckolib repository. Original source:
 * https://github.com/bernie-g/geckolib Copyright © 2024 Bernie-G. Licensed under the MIT License.
 * https://github.com/bernie-g/geckolib/blob/main/LICENSE
 */
package mod.azure.azurelib.common.internal.mixins;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import org.spongepowered.asm.mixin.Mixin;

/**
 * Mixin for HumanoidArmorLayer in 1.21.8.
 *
 * renderArmorPiece is intentionally NOT intercepted here. Letting it run fully ensures
 * getParentModel().copyPropertiesTo() and setPartVisibility() are applied to the armor model
 * before equipmentRenderer.renderLayers() is called. MixinEquipmentLayerRenderer then
 * intercepts renderLayers(), renders the 3D AzureLib geo model with the already-posed model,
 * and cancels the vanilla 2D equipment layer.
 */
@Mixin(HumanoidArmorLayer.class)
@SuppressWarnings("rawtypes")
public abstract class MixinHumanoidArmorLayer<S extends HumanoidRenderState, A extends HumanoidModel<S>> {
    // Intentionally empty — all custom armor rendering is handled in MixinEquipmentLayerRenderer.
}
