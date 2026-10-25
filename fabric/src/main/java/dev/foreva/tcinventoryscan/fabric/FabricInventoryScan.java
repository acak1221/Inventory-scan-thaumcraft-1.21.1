package dev.foreva.tcinventoryscan.fabric;

import dev.foreva.tcinventoryscan.InventoryScanServer;
import dev.foreva.tcinventoryscan.network.ScanReply;
import dev.foreva.tcinventoryscan.network.ScanRequest;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

/** Native loader wiring; gameplay and validation belong to the shared server. */
public final class FabricInventoryScan implements ModInitializer {
    @Override public void onInitialize() {
        PayloadTypeRegistry.playC2S().register(ScanRequest.TYPE, ScanRequest.STREAM_CODEC);
        PayloadTypeRegistry.playS2C().register(ScanReply.TYPE, ScanReply.STREAM_CODEC);
        // Fabric's payload-object receiver already executes on the server thread.
        ServerPlayNetworking.registerGlobalReceiver(ScanRequest.TYPE, (request, context) -> {
            if (ServerPlayNetworking.canSend(context.player(), ScanReply.TYPE))
                InventoryScanServer.receive(context.player(), request);
        });
        InventoryScanServer.installTransport((player, reply) -> {
            if (ServerPlayNetworking.canSend(player, ScanReply.TYPE)) ServerPlayNetworking.send(player, reply);
        });
        ServerTickEvents.END_SERVER_TICK.register(InventoryScanServer::tick);
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> InventoryScanServer.disconnect(handler.player));
        ServerLifecycleEvents.SERVER_STOPPED.register(InventoryScanServer::stopped);
    }
}
