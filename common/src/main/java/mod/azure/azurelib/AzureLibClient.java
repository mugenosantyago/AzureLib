/**
 * This class is a fork of the matching class found in the Configuration repository. Original source:
 * https://github.com/Toma1O6/Configuration Copyright © 2024 Toma1O6. Licensed under the MIT License.
 */
package mod.azure.azurelib;

import net.minecraft.client.gui.screens.Screen;
import org.jetbrains.annotations.Nullable;

import java.util.List;

// Config system disabled for 1.21.8 port
public final class AzureLibClient {

    private AzureLibClient() {
        throw new UnsupportedOperationException();
    }

    @Nullable
    public static Screen getConfigScreen(Class<?> configClass, Screen previous) {
        return null; // Config system disabled for 1.21.8
    }

    @Nullable
    public static Screen getConfigScreen(String configId, Screen previous) {
        return null; // Config system disabled for 1.21.8
    }

    public static Screen getConfigScreenByGroup(String group, Screen previous) {
        return null; // Config system disabled for 1.21.8
    }

    public static Screen getConfigScreenForHolder(Object holder, Screen previous) {
        return null; // Config system disabled for 1.21.8
    }

    public static Screen getConfigScreenByGroup(List<?> group, String groupId, Screen previous) {
        return null; // Config system disabled for 1.21.8
    }
}
