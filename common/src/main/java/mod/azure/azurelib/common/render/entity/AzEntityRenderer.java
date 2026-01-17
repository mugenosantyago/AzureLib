package mod.azure.azurelib.common.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

import mod.azure.azurelib.common.animation.impl.AzEntityAnimator;
import mod.azure.azurelib.common.render.AzProvider;

/**
 * AzEntityRenderer is an abstract class responsible for rendering entities in the game. It extends the base
 * functionality of {@link EntityRenderer} to provide additional rendering capabilities specific to animated and custom
 * entities. This class is parameterized with a generic type {@code T}, which must extend {@link Entity}. It integrates
 * several abstractions such as animation management, model caching, and advanced rendering pipelines for handling
 * complex rendering behavior. Users are expected to configure this renderer using an {@link AzEntityRendererConfig}.
 * Key components: - {@link AzEntityRendererConfig}: Defines configuration options such as textures, models, and
 * animator providers. - {@link AzProvider}: Supplies baked models and animators for entities. -
 * {@link AzEntityRendererPipeline}: Manages rendering logic through a custom pipeline.
 */
public abstract class AzEntityRenderer<T extends Entity> extends EntityRenderer<T, mod.azure.azurelib.common.render.entity.state.AzEntityRenderState> {

    protected final AzEntityRendererConfig<T> config;

    protected final AzProvider<UUID, T> provider;

    protected final AzEntityRendererPipeline<T> rendererPipeline;

    @Nullable
    private AzEntityAnimator<T> reusedAzEntityAnimator;

    protected AzEntityRenderer(AzEntityRendererConfig<T> config, EntityRendererProvider.Context context) {
        super(context);
        this.config = config;
        this.provider = new AzProvider<>(config::createAnimator, config::modelLocation, Entity::getUUID);
        this.rendererPipeline = createPipeline(config);
    }

    @Override
    public mod.azure.azurelib.common.render.entity.state.AzEntityRenderState createRenderState() {
        return new mod.azure.azurelib.common.render.entity.state.AzEntityRenderState();
    }

    @Override
    public void extractRenderState(T entity, mod.azure.azurelib.common.render.entity.state.AzEntityRenderState renderState, float partialTick) {
        super.extractRenderState(entity, renderState, partialTick);
        renderState.entity = entity;
        renderState.partialTick = partialTick;
        renderState.entityYaw = entity.getYRot(partialTick);
    }

    public AzEntityRendererPipeline<T> createPipeline(AzEntityRendererConfig<T> config) {
        return new AzEntityRendererPipeline<>(config, this);
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(@NotNull T animatable) {
        return config.textureLocation(animatable, animatable);
    }

    @Override
    public void render(
        @NotNull mod.azure.azurelib.common.render.entity.state.AzEntityRenderState renderState,
        @NotNull PoseStack poseStack,
        @NotNull MultiBufferSource bufferSource,
        int packedLight
    ) {
        // Extract entity from renderState
        @SuppressWarnings("unchecked")
        T entity = (T) renderState.entity;
        float entityYaw = renderState.entityYaw;
        float partialTick = renderState.partialTick;

        var cachedEntityAnimator = (AzEntityAnimator<T>) provider.provideAnimator(entity, entity);
        var azBakedModel = provider.provideBakedModel(entity, entity);

        // Point the renderer's current animator reference to the cached entity animator before rendering.
        reusedAzEntityAnimator = cachedEntityAnimator;

        // Execute the render pipeline.
        rendererPipeline.render(
            poseStack,
            azBakedModel,
            entity,
            bufferSource,
            null,
            null,
            entityYaw,
            partialTick,
            packedLight
        );
    }

    @Override
    protected float getShadowRadius(@NotNull T entity) {
        return config.shadowRadius(entity);
    }

    /**
     * Whether the entity's nametag should be rendered or not.<br>
     * Pretty much exclusively used in {@link EntityRenderer#renderNameTag}
     */
    @Override
    public boolean shouldShowName(@NotNull T entity) {
        return AzEntityNameRenderUtil.shouldShowName(entityRenderDispatcher, entity);
    }

    // Proxy method override for super.getBlockLightLevel external access.
    @Override
    public int getBlockLightLevel(@NotNull T entity, @NotNull BlockPos pos) {
        return super.getBlockLightLevel(entity, pos);
    }

    public AzEntityAnimator<T> getAnimator() {
        return reusedAzEntityAnimator;
    }

    public AzEntityRendererConfig<T> config() {
        return config;
    }
}
