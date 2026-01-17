/**
 * This class is a fork of the matching class found in the Geckolib repository. Original source:
 * https://github.com/bernie-g/geckolib Copyright © 2024 Bernie-G. Licensed under the MIT License.
 * https://github.com/bernie-g/geckolib/blob/main/LICENSE
 */
package mod.azure.azurelib.common.cache.texture;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.blaze3d.platform.NativeImage;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.server.packs.metadata.MetadataSectionType;
import net.minecraft.util.ARGB;
import net.minecraft.util.GsonHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import mod.azure.azurelib.common.render.layer.AzAutoGlowingLayer;

/**
 * Metadata class that stores the data for AzureLib's {@link AzAutoGlowingLayer emissive texture feature} for a given
 * texture
 */
public class GeoGlowingTextureMeta {

    public static final MetadataSectionType<GeoGlowingTextureMeta> DESERIALIZER =
        new MetadataSectionType<>() {

            @Override
            public @NotNull String getMetadataSectionName() {
                return "glowsections";
            }

            @Override
            public @NotNull GeoGlowingTextureMeta fromJson(@NotNull JsonObject json) {
                List<Pixel> pixels = fromSections(GsonHelper.getAsJsonArray(json, "sections", null));

                if (pixels.isEmpty())
                    throw new JsonParseException(
                        "Empty glowlayer sections file. Must have at least one glow section!"
                    );

                return new GeoGlowingTextureMeta(pixels);
            }

            /**
             * Generate a {@link Pixel} collection from the "sections" array of the mcmeta file
             */
            private List<Pixel> fromSections(@Nullable JsonArray sectionsArray) {
                if (sectionsArray == null)
                    return List.of();

                List<Pixel> pixels = new ObjectArrayList<>();

                for (JsonElement element : sectionsArray) {
                    if (!(element instanceof JsonObject obj))
                        throw new JsonParseException(
                            "Invalid glowsections json format, expected a JsonObject, found: " + element.getClass()
                        );

                    int x1 = GsonHelper.getAsInt(obj, "x1", GsonHelper.getAsInt(obj, "x", 0));
                    int y1 = GsonHelper.getAsInt(obj, "y1", GsonHelper.getAsInt(obj, "y", 0));
                    int x2 = GsonHelper.getAsInt(obj, "x2", GsonHelper.getAsInt(obj, "w", 0) + x1);
                    int y2 = GsonHelper.getAsInt(obj, "y2", GsonHelper.getAsInt(obj, "h", 0) + y1);
                    int alpha = GsonHelper.getAsInt(obj, "alpha", GsonHelper.getAsInt(obj, "a", 0));

                    if (x1 + y1 + x2 + y2 == 0)
                        throw new IllegalArgumentException(
                            "Invalid glowsections section object, section must be at least one pixel in size"
                        );

                    for (int x = x1; x <= x2; x++) {
                        for (int y = y1; y <= y2; y++) {
                            pixels.add(new Pixel(x, y, alpha));
                        }
                    }
                }

                return pixels;
            }
        };

    private final List<Pixel> pixels;

    public GeoGlowingTextureMeta(List<Pixel> pixels) {
        this.pixels = pixels;
    }

    /**
     * Generate the GlowLayer pixels list from an existing image resource, instead of using the .png.mcmeta file
     */
    public static GeoGlowingTextureMeta fromExistingImage(NativeImage glowLayer) {
        List<Pixel> pixels = new ObjectArrayList<>();

        // TODO: Fix for 1.21.8 - NativeImage pixel access API changed
        // Temporarily disabled to allow build
        /*
        for (int x = 0; x < glowLayer.getWidth(); x++) {
            for (int y = 0; y < glowLayer.getHeight(); y++) {
                int color = glowLayer.getPixelABGR(x, y);

                if (color != 0)
                    pixels.add(new Pixel(x, y, ARGB.alpha(color)));
            }
        }
        */

        if (pixels.isEmpty())
            pixels.add(new Pixel(0, 0, 255)); // Add dummy pixel to prevent error

        return new GeoGlowingTextureMeta(pixels);
    }

    /**
     * Create a new mask image based on the pre-determined pixel data
     */
    public void createImageMask(NativeImage originalImage, NativeImage newImage) {
        // TODO: Fix for 1.21.8 - NativeImage pixel access API changed
        // Temporarily disabled to allow build
        /*
        for (Pixel pixel : this.pixels) {
            int color = originalImage.getPixelABGR(pixel.x, pixel.y);

            if (pixel.alpha > 0)
                color = ARGB.color(
                    pixel.alpha,
                    ARGB.blue(color),
                    ARGB.green(color),
                    ARGB.red(color)
                );

            newImage.setPixelABGR(pixel.x, pixel.y, color);
            originalImage.setPixelABGR(pixel.x, pixel.y, 0);
        }
        */
    }

    /**
     * A pixel marker for a glowlayer mask
     *
     * @param x     The X coordinate of the pixel
     * @param y     The Y coordinate of the pixel
     * @param alpha The alpha value of the mask
     */
    private record Pixel(
        int x,
        int y,
        int alpha
    ) {}
}
