package dev.foreva.tcinventoryscan.fabric;

import dev.foreva.tcinventoryscan.client.InventoryScanClient;
import dev.foreva.tcinventoryscan.network.ScanReply;
import dev.foreva.tcinventoryscan.network.ScanRequest;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;

/** Loaded only by Fabric's client entrypoint, never by a dedicated server. */
public final class FabricInventoryScanClient implements ClientModInitializer {
    @Override public void onInitializeClient() {
        InventoryScanClient.installTransport(request -> {
            if (!ClientPlayNetworking.canSend(ScanRequest.TYPE)) return false;
            ClientPlayNetworking.send(request);
            return true;
        });
        // Fabric's payload-object receiver already executes on the render thread.
        ClientPlayNetworking.registerGlobalReceiver(ScanReply.TYPE,
                (reply, context) -> InventoryScanClient.accept(reply));
        ItemTooltipCallback.EVENT.register((stack, context, flags, lines) ->
                InventoryScanClient.appendTooltips(stack, lines, Minecraft.getInstance().isSameThread() && Screen.hasShiftDown()));
        ClientTickEvents.END_CLIENT_TICK.register(client -> InventoryScanClient.tick());
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> InventoryScanClient.disconnect());
    }
}
