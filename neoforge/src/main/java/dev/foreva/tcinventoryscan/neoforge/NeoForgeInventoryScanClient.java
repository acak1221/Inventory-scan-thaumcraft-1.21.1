package dev.foreva.tcinventoryscan.neoforge;

import dev.foreva.tcinventoryscan.TCInventoryScan;
import dev.foreva.tcinventoryscan.client.InventoryScanClient;
import dev.foreva.tcinventoryscan.network.ScanRequest;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import net.neoforged.neoforge.network.PacketDistributor;

/** FML loads this additional entrypoint only on the physical client. */
@Mod(value = TCInventoryScan.MOD_ID, dist = Dist.CLIENT)
public final class NeoForgeInventoryScanClient {
    public NeoForgeInventoryScanClient() {
        TCInventoryScan.installReplyReceiver(InventoryScanClient::accept);
        InventoryScanClient.installTransport(request -> {
            var connection = Minecraft.getInstance().getConnection();
            if (connection == null || !connection.hasChannel(ScanRequest.TYPE)) return false;
            PacketDistributor.sendToServer(request);
            return true;
        });
        NeoForge.EVENT_BUS.addListener((ItemTooltipEvent event) -> InventoryScanClient.appendTooltips(
                event.getItemStack(), event.getToolTip(), Minecraft.getInstance().isSameThread() && Screen.hasShiftDown()));
        NeoForge.EVENT_BUS.addListener((ClientTickEvent.Post event) -> InventoryScanClient.tick());
        NeoForge.EVENT_BUS.addListener((ClientPlayerNetworkEvent.LoggingOut event) -> InventoryScanClient.disconnect());
    }
}
