package dev.foreva.tcinventoryscan.neoforge;

import dev.foreva.tcinventoryscan.InventoryScanServer;
import dev.foreva.tcinventoryscan.TCInventoryScan;
import dev.foreva.tcinventoryscan.network.ScanReply;
import dev.foreva.tcinventoryscan.network.ScanRequest;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.HandlerThread;

/** Both physical sides register the protocol without linking any client classes. */
@Mod(TCInventoryScan.MOD_ID)
public final class NeoForgeInventoryScan {
    public NeoForgeInventoryScan(IEventBus modBus) {
        modBus.addListener(NeoForgeInventoryScan::registerPayloads);
        InventoryScanServer.installTransport((player, reply) -> {
            if (player.connection.hasChannel(ScanReply.TYPE)) PacketDistributor.sendToPlayer(player, reply);
        });
        NeoForge.EVENT_BUS.addListener((ServerTickEvent.Post event) -> InventoryScanServer.tick(event.getServer()));
        NeoForge.EVENT_BUS.addListener((PlayerEvent.PlayerLoggedOutEvent event) -> {
            if (event.getEntity() instanceof ServerPlayer player) InventoryScanServer.disconnect(player);
        });
        NeoForge.EVENT_BUS.addListener((ServerStoppedEvent event) -> InventoryScanServer.stopped(event.getServer()));
    }

    private static void registerPayloads(RegisterPayloadHandlersEvent event) {
        // optional() returns a new registrar: retain it so peers without the addon may still connect.
        var registrar = event.registrar("1").optional().executesOn(HandlerThread.MAIN);
        registrar.playToServer(ScanRequest.TYPE, ScanRequest.STREAM_CODEC, (request, context) -> {
            if (context.player() instanceof ServerPlayer player && player.connection.hasChannel(ScanReply.TYPE))
                InventoryScanServer.receive(player, request);
        });
        registrar.playToClient(ScanReply.TYPE, ScanReply.STREAM_CODEC,
                (reply, context) -> TCInventoryScan.receiveReply(reply));
    }
}
