/**
 * This class is a fork of the matching class found in the Configuration repository. Original source:
 * https://github.com/Toma1O6/Configuration Copyright © 2024 Toma1O6. Licensed under the MIT License.
 */
package mod.azure.azurelib.common.network.packet;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;
import org.jetbrains.annotations.NotNull;

import mod.azure.azurelib.common.network.AbstractPacket;
import mod.azure.azurelib.common.platform.services.AzureLibNetwork;

// Config system disabled for 1.21.8 port
public record SendConfigDataPacket(String config) implements AbstractPacket {

    public static final Marker MARKER = MarkerManager.getMarker("Network");

    public static final Type<SendConfigDataPacket> TYPE = new Type<>(
        AzureLibNetwork.CONFIG_PACKET_ID
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, SendConfigDataPacket> CODEC = StreamCodec.of(
        (buf, packet) -> {
            buf.writeUtf(packet.config);
            // Config system disabled for 1.21.8
        },
        buf -> {
            String config = buf.readUtf();
            // Config system disabled for 1.21.8
            return new SendConfigDataPacket(config);
        }
    );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    @Override
    public void handle() {}
}
