/**
 * Mixin to add custom armor layer to living entity renderers.
 * This allows us to render 3D armor by accessing the entity directly.
 */
package mod.azure.azurelib.common.internal.mixins;

import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.model.EntityModel;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import mod.azure.azurelib.AzureLib;

/**
 * Mixin to capture the living entity during render state extraction.
 * This allows us to access the entity's equipment for custom armor rendering.
 */
@Mixin(LivingEntityRenderer.class)
public class MixinLivingEntityRenderer<T extends LivingEntity, S extends LivingEntityRenderState, M extends EntityModel<? super S>> {

    /**
     * Capture the entity when extracting render state.
     * This gives us access to the entity's equipment slots.
     */
    @Inject(
        method = "extractRenderState(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;F)V",
        at = @At("HEAD"),
        require = 0
    )
    private void azurelib$captureEntity(T entity, S state, float partialTick, CallbackInfo ci) {
        // Store the entity reference in a thread-local for later access during armor rendering
        mod.azure.azurelib.common.render.armor.AzArmorRenderContext.setCurrentEntity(entity);
        AzureLib.LOGGER.debug("AzureLib: Captured entity for armor render: {}", entity.getType().getDescriptionId());
    }
}
