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
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import mod.azure.azurelib.common.render.armor.AzArmorRendererRegistry;

import java.lang.reflect.Field;

/**
 * Mixin for intercepting armor rendering in 1.21.8.
 * In 1.21.8, armor items are passed directly to renderArmorPiece.
 * We need to use @Local to capture the ItemStack parameter.
 */
@Mixin(HumanoidArmorLayer.class)
@SuppressWarnings("rawtypes")
public abstract class MixinHumanoidArmorLayer<S extends HumanoidRenderState, A extends HumanoidModel<S>> {

    @Unique
    private PoseStack azurelib$poseStack;
    @Unique
    private MultiBufferSource azurelib$bufferSource;
    @Unique
    private int azurelib$packedLight;
    @Unique
    private HumanoidRenderState azurelib$renderState;
    @Unique
    private A azurelib$baseModel;
    @Unique
    private static boolean azurelib$fieldNamesLogged = false;

    /**
     * Capture render context at the start of the render method.
     */
    @Inject(
        method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/client/renderer/entity/state/HumanoidRenderState;FF)V",
        at = @At("HEAD"),
        require = 0
    )
    public void azurelib$captureRenderContext(
        PoseStack poseStack,
        MultiBufferSource bufferSource,
        int packedLight,
        HumanoidRenderState renderState,
        float limbSwing,
        float limbSwingAmount,
        CallbackInfo ci
    ) {
        this.azurelib$poseStack = poseStack;
        this.azurelib$bufferSource = bufferSource;
        this.azurelib$packedLight = packedLight;
        this.azurelib$renderState = renderState;
        
        // Log all fields of HumanoidRenderState to find the correct ones (once)
        if (!azurelib$fieldNamesLogged) {
            azurelib$fieldNamesLogged = true;
            mod.azure.azurelib.AzureLib.LOGGER.info("AzureLib: HumanoidRenderState fields:");
            for (Field field : renderState.getClass().getFields()) {
                try {
                    Object value = field.get(renderState);
                    mod.azure.azurelib.AzureLib.LOGGER.info("  - {} ({}) = {}", 
                        field.getName(), field.getType().getSimpleName(), value);
                } catch (Exception e) {
                    mod.azure.azurelib.AzureLib.LOGGER.info("  - {} ({}) = <error>", 
                        field.getName(), field.getType().getSimpleName());
                }
            }
            // Also check superclass fields
            Class<?> superClass = renderState.getClass().getSuperclass();
            while (superClass != null && superClass != Object.class) {
                mod.azure.azurelib.AzureLib.LOGGER.info("AzureLib: {} fields:", superClass.getSimpleName());
                for (Field field : superClass.getDeclaredFields()) {
                    try {
                        field.setAccessible(true);
                        Object value = field.get(renderState);
                        mod.azure.azurelib.AzureLib.LOGGER.info("  - {} ({}) = {}", 
                            field.getName(), field.getType().getSimpleName(), value);
                    } catch (Exception e) {
                        mod.azure.azurelib.AzureLib.LOGGER.info("  - {} ({}) = <error>", 
                            field.getName(), field.getType().getSimpleName());
                    }
                }
                superClass = superClass.getSuperclass();
            }
        }
    }

    /**
     * Get the armor ItemStack from the render state based on equipment slot.
     * Falls back to getting from the current entity if render state has empty stacks.
     */
    @Unique
    private ItemStack azurelib$getArmorFromSlot(EquipmentSlot slot) {
        // First try from render state
        if (azurelib$renderState != null) {
            ItemStack result = switch (slot) {
                case HEAD -> azurelib$renderState.headEquipment;
                case CHEST -> azurelib$renderState.chestEquipment;
                case LEGS -> azurelib$renderState.legsEquipment;
                case FEET -> azurelib$renderState.feetEquipment;
                default -> ItemStack.EMPTY;
            };
            if (result != null && !result.isEmpty()) {
                return result;
            }
        }
        
        // Fall back to getting from the current entity via thread-local context
        var entity = mod.azure.azurelib.common.render.armor.AzArmorRenderContext.getCurrentEntity();
        if (entity != null) {
            ItemStack entityStack = entity.getItemBySlot(slot);
            if (entityStack != null && !entityStack.isEmpty()) {
                mod.azure.azurelib.AzureLib.LOGGER.debug("AzureLib: Got armor from entity context: {} for slot {}", 
                    entityStack.getItem(), slot);
                return entityStack;
            }
        }
        
        return ItemStack.EMPTY;
    }

    /**
     * Inject at renderArmorPiece to intercept armor rendering.
     * Try to capture the ItemStack and EquipmentSlot parameters.
     */
    @Inject(
        method = "renderArmorPiece(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/entity/EquipmentSlot;ILnet/minecraft/client/model/HumanoidModel;)V",
        at = @At("HEAD"),
        cancellable = true,
        require = 0
    )
    public void azurelib$renderAzurelibModelWithParams(
        PoseStack poseStack,
        MultiBufferSource bufferSource,
        ItemStack stack,
        EquipmentSlot equipmentSlot,
        int packedLight,
        A baseModel,
        CallbackInfo ci
    ) {
        // Try with passed stack first
        ItemStack armorStack = stack;
        
        mod.azure.azurelib.AzureLib.LOGGER.debug("AzureLib: renderArmorPiece called for slot {} with stack: {}", 
            equipmentSlot, stack != null ? stack.getItem() : "null");
        
        // If passed stack is empty/air, try from render state
        if (armorStack == null || armorStack.isEmpty()) {
            armorStack = azurelib$getArmorFromSlot(equipmentSlot);
            mod.azure.azurelib.AzureLib.LOGGER.debug("AzureLib: Falling back to render state, got: {}", 
                armorStack != null ? armorStack.getItem() : "null");
        }
        
        if (armorStack == null || armorStack.isEmpty()) {
            return;
        }
        
        var renderer = AzArmorRendererRegistry.getOrNull(armorStack);

        if (renderer != null) {
            mod.azure.azurelib.AzureLib.LOGGER.info("AzureLib: Rendering 3D armor for {} in slot {}", 
                armorStack.getItem(), equipmentSlot);
            
            try {
                var dyeColor = armorStack.is(ItemTags.DYEABLE)
                    ? ARGB.opaque(DyedItemColor.getOrDefault(armorStack, -6265536))
                    : -1;

                var rendererPipeline = renderer.rendererPipeline();
                var armorModel = rendererPipeline.armorModel();
                @SuppressWarnings({"unchecked"})
                var typedHumanoidModel = (HumanoidModel) armorModel;

                renderer.prepForRenderWithoutEntity(armorStack, equipmentSlot, baseModel);
                baseModel.copyPropertiesTo(typedHumanoidModel);

                armorModel.azRenderToBuffer(poseStack, null, packedLight, OverlayTexture.NO_OVERLAY, dyeColor);
                ci.cancel();
            } catch (Exception e) {
                mod.azure.azurelib.AzureLib.LOGGER.error("AzureLib: Error rendering 3D armor", e);
            }
        }
    }
}
