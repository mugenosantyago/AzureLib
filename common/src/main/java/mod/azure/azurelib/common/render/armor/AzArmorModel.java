package mod.azure.azurelib.common.render.armor;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("rawtypes")
public class AzArmorModel<E extends LivingEntity> extends HumanoidModel {

    private final AzArmorRendererPipeline rendererPipeline;

    public AzArmorModel(AzArmorRendererPipeline rendererPipeline) {
        super(Minecraft.getInstance().getEntityModels().bakeLayer(ModelLayers.PLAYER_INNER_ARMOR));
        this.rendererPipeline = rendererPipeline;
        // 1.21.8: young field moved to RenderState
        // this.young = false;
    }

    // renderToBuffer is now final in 1.21.8, cannot override
    // @Override
    public void azRenderToBuffer(
        @NotNull PoseStack poseStack,
        @Nullable VertexConsumer buffer,
        int packedLight,
        int packedOverlay,
        int color
    ) {
        var mc = Minecraft.getInstance();
        var context = rendererPipeline.context();
        var currentEntity = context.currentEntity();
        var currentStack = context.currentStack();
        
        if (currentStack == null) {
            return; // Cannot render without item stack context
        }
        
        MultiBufferSource bufferSource = Minecraft.getInstance().levelRenderer.renderBuffers.bufferSource();

        // Only check for outline if we have entity context (1.21.8+ may not provide it)
        var shouldOutline = currentEntity != null 
            && Minecraft.getInstance().levelRenderer.shouldShowEntityOutlines() 
            && mc.shouldEntityAppearGlowing(currentEntity);

        if (shouldOutline) {
            bufferSource = Minecraft.getInstance().levelRenderer.renderBuffers.outlineBufferSource();
        }

        var config = rendererPipeline.config();
        var animatable = context.animatable();
        var partialTick = mc.getDeltaTracker().getGameTimeDeltaTicks();
        var textureLocation = config.textureLocation(currentEntity, animatable);
        var renderType = context.getDefaultRenderType(
            animatable,
            textureLocation,
            bufferSource,
            partialTick,
            config.getRenderType(currentEntity, animatable),
            config.alpha(animatable)
        );
        buffer = ItemRenderer.getArmorFoilBuffer(bufferSource, renderType, currentStack.hasFoil());

        var model = rendererPipeline.renderer().provider().provideBakedModel(currentEntity, animatable);
        rendererPipeline.render(poseStack, model, animatable, bufferSource, null, buffer, 0, partialTick, packedLight);
    }

    /**
     * Applies settings and transformations pre-render based on the default model
     */
    public void applyBaseModel(HumanoidModel<?> baseModel) {
        // TODO: Fix for 1.21.8 - HumanoidModel fields moved to RenderState
        // Temporarily disabled - these fields are now in HumanoidRenderState
        // this.young = baseModel.young;
        // this.crouching = baseModel.crouching;
        // this.riding = baseModel.riding;
        // this.rightArmPose = baseModel.rightArmPose;
        // this.leftArmPose = baseModel.leftArmPose;
    }

    @Override
    public void setAllVisible(boolean pVisible) {
        super.setAllVisible(pVisible);
        var boneContext = rendererPipeline.context().boneContext();
        boneContext.setAllVisible(pVisible);
    }
}
