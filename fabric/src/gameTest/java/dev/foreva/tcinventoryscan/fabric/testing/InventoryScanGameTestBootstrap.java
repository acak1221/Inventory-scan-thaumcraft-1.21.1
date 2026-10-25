package dev.foreva.tcinventoryscan.fabric.testing;

import dev.foreva.tcinventoryscan.network.ScanReply;
import dev.foreva.tcinventoryscan.network.ScanRequest;
import dev.foreva.tcinventoryscan.testing.InventoryScanRuntimeChecks;
import dev.foreva.tcinventoryscan.testing.mixin.GameTestHelperAccess;
import io.netty.buffer.Unpooled;
import java.util.List;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.impl.networking.RegistrationPayload;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;
import net.minecraft.server.network.CommonListenerCookie;

/** A genuine Fabric play handler/registration path, confined to the optional testmod. */
public final class InventoryScanGameTestBootstrap implements ModInitializer {
    @Override public void onInitialize() {
        InventoryScanRuntimeChecks.installPlatform((player, connection) -> {
            player.server.getPlayerList().placeNewPlayer(connection, player,
                    CommonListenerCookie.createInitial(player.getGameProfile(), false));
            player.connection.handleCustomPayload(new ServerboundCustomPayloadPacket(
                    new RegistrationPayload(RegistrationPayload.REGISTER, List.of(ScanReply.TYPE.id()))));
            if (!ServerPlayNetworking.canSend(player, ScanReply.TYPE)) throw new AssertionError("real Fabric registration announces the reply channel");
        }, (player, request) -> {
            if (!ServerPlayNetworking.getGlobalReceivers().contains(ScanRequest.TYPE.id())) throw new AssertionError("native Fabric C2S receiver was not registered");
            var wire = new RegistryFriendlyByteBuf(Unpooled.buffer(), player.registryAccess());
            try {
                ScanRequest.STREAM_CODEC.encode(wire, request);
                var decoded = ScanRequest.STREAM_CODEC.decode(wire);
                if (wire.isReadable() || !request.equals(decoded)) throw new AssertionError("request consumes its exact Fabric payload bytes");
                player.connection.handleCustomPayload(new ServerboundCustomPayloadPacket(decoded));
            } finally { wire.release(); }
        }, helper -> ((GameTestHelperAccess) helper).tcinventoryscan$testInfo());
    }
}
