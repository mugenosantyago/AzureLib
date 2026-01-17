/**
 * Mixin for intercepting equipment layer rendering in 1.21.8+.
 * This is where the actual armor textures are rendered.
 */
package mod.azure.azurelib.common.internal.mixins;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.layers.EquipmentLayerRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.ARGB;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.item.equipment.Equippable;
import net.minecraft.core.component.DataComponents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import mod.azure.azurelib.common.render.armor.AzArmorRenderer;
import mod.azure.azurelib.common.render.armor.AzArmorRendererRegistry;

/**
 * Mixin to intercept EquipmentLayerRenderer for custom 3D armor rendering.
 * In 1.21.8, armor rendering goes through this class instead of directly in HumanoidArmorLayer.
 */
@Mixin(EquipmentLayerRenderer.class)
public class MixinEquipmentLayerRenderer {

    @Unique
    private static boolean azurelib$logged = false;

    /**
     * Inject at renderLayers to intercept armor rendering.
     * This method is called with the actual armor ItemStack.
     */
    @Inject(
        method = "renderLayers*",
        at = @At("HEAD"),
        cancellable = true,
        require = 0
    )
    private void azurelib$interceptRenderLayers(CallbackInfo ci) {
        if (!azurelib$logged) {
            azurelib$logged = true;
            mod.azure.azurelib.AzureLib.LOGGER.info("AzureLib: EquipmentLayerRenderer.renderLayers intercepted!");
        }
    }
}
