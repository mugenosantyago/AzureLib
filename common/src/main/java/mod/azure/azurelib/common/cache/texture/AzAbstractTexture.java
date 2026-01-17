package mod.azure.azurelib.common.cache.texture;

// import com.mojang.blaze3d.pipeline.RenderCall; // Removed in 1.21.8
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.platform.TextureUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.SimpleTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.function.BiFunction;
import java.util.function.Consumer;

import mod.azure.azurelib.common.platform.Services;

public abstract class AzAbstractTexture extends SimpleTexture {

    // TODO: Fix for 1.21.8 - RenderStateShard inner classes have protected constructors
    // Temporarily using fallback render types
    protected static final BiFunction<ResourceLocation, Boolean, RenderType> GLOWING_RENDER_TYPE = Util.memoize(
        (texture, isGlowing) -> RenderType.entityTranslucent(texture)
    );

    protected static final String APPENDIX = "_glowmask";

    public AzAbstractTexture(ResourceLocation location) {
        super(location);
    }

    public static void onRenderThread(Runnable renderCall) {
        if (!RenderSystem.isOnRenderThread()) {
            renderCall.run(); // RenderSystem.recordRenderCall removed in 1.21.8;
        } else {
            renderCall.run();
        }
    }

    /**
     * Generates the texture instance for the given path with the given appendix if it hasn't already been generated
     */
    protected static void generateTexture(
        ResourceLocation texturePath,
        Consumer<TextureManager> textureManagerConsumer
    ) {
        if (!RenderSystem.isOnRenderThread())
            throw new IllegalThreadStateException(
                "Texture loading called outside of the render thread! This should DEFINITELY not be happening."
            );

        TextureManager textureManager = Minecraft.getInstance().getTextureManager();

        if (
            !(textureManager.getTexture(texturePath) instanceof AzAbstractTexture)
        )
            textureManagerConsumer.accept(textureManager);
    }

    /**
     * No-frills helper method for uploading {@link NativeImage images} into memory for use
     */
    public static void uploadSimple(int texture, NativeImage image, boolean blur, boolean clamp) {
        // In 1.21.8, upload signature changed: removed clamp parameter
        // image.upload(0, 0, 0, blur); // TODO: Fix upload API;
    }

    public static ResourceLocation appendToPath(ResourceLocation location, String suffix) {
        String path = location.getPath();
        int i = path.lastIndexOf('.');

        return ResourceLocation.fromNamespaceAndPath(
            location.getNamespace(),
            path.substring(0, i) + suffix + path.substring(i)
        );
    }

    // 1.21.8: AbstractTexture.load signature may have changed
    public void load(ResourceManager resourceManager) throws IOException {
        Runnable renderCall = loadTexture(resourceManager, Minecraft.getInstance());

        if (renderCall == null)
            return;

        if (!RenderSystem.isOnRenderThread()) {
            renderCall.run(); // RenderSystem.recordRenderCall removed in 1.21.8;
        } else {
            renderCall.run();
        }
    }

    /**
     * Debugging function to write out the generated glowmap image to disk
     */
    protected void printDebugImageToDisk(ResourceLocation id, NativeImage newImage) {
        try {
            File file = new File(Services.PLATFORM.getGameDir().toFile(), "GeoTexture Debug Printouts");

            if (!file.exists()) {
                file.mkdirs();
            } else if (!file.isDirectory()) {
                file.delete();
                file.mkdirs();
            }

            file = new File(file, id.getPath().replace('/', '.'));

            if (!file.exists())
                file.createNewFile();

            newImage.writeToFile(file);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Called at {@link AbstractTexture#load} time to load this texture for the first time into the render cache.
     * Generate and apply the necessary functions here, then return the RenderCall to submit to the render pipeline.
     *
     * @return The RenderCall to submit to the render pipeline, or null if no further action required
     */
    @Nullable
    protected abstract Runnable loadTexture(ResourceManager resourceManager, Minecraft mc) throws IOException;

    /**
     * Get the emissive resource equivalent of the input resource path.<br>
     * Additionally prepares the texture manager for the missing texture if the resource is not present
     *
     * @return The glowlayer resourcepath for the provided input path
     */
    public static ResourceLocation getEmissiveResource(ResourceLocation baseResource) {
        // 1.21.8: AutoGlowingTexture disabled - return base texture for now
        ResourceLocation path = appendToPath(baseResource, APPENDIX);
        // TODO: Implement emissive texture support for 1.21.8
        return path;
    }

    /**
     * Return a cached instance of the RenderType for the given texture for GeoGlowingLayer rendering.
     *
     * @param texture The texture of the resource to apply a glow layer to
     */
    public static RenderType getRenderType(ResourceLocation texture) {
        return GLOWING_RENDER_TYPE.apply(getEmissiveResource(texture), false);
    }

    /**
     * Return a cached instance of the RenderType for the given texture for AutoGlowingGeoLayer rendering, while the
     * entity has an outline
     *
     * @param texture The texture of the resource to apply a glow layer to
     */
    public static RenderType getOutlineRenderType(ResourceLocation texture) {
        return GLOWING_RENDER_TYPE.apply(getEmissiveResource(texture), true);
    }
}
