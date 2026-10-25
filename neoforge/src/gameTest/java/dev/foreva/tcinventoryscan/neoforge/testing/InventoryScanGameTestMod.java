package dev.foreva.tcinventoryscan.neoforge.testing;

import dev.foreva.tcinventoryscan.network.ScanReply;
import dev.foreva.tcinventoryscan.network.ScanRequest;
import dev.foreva.tcinventoryscan.testing.InventoryScanRuntimeChecks;
import io.netty.buffer.Unpooled;
import java.io.File;
import net.minecraft.gametest.framework.GameTestInfo;
import net.minecraft.gametest.framework.GlobalTestReporter;
import net.minecraft.gametest.framework.JUnitLikeTestReporter;
import net.minecraft.gametest.framework.TestReporter;
import org.slf4j.LoggerFactory;
import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.network.CommonListenerCookie;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.network.connection.ConnectionType;
import net.neoforged.neoforge.network.registration.NetworkRegistry;

/** Native negotiated NeoForge sessions; never included in the production addon JAR. */
@Mod("tcinventoryscan_gametest")
public final class InventoryScanGameTestMod {
    public InventoryScanGameTestMod(IEventBus bus) {
        configureReporter();
        bus.addListener((RegisterGameTestsEvent event) -> event.register(InventoryScanGameTests.class));
        InventoryScanRuntimeChecks.installPlatform((player, connection) -> {
            NetworkRegistry.configureMockConnection(connection);
            var cookie = new CommonListenerCookie(player.getGameProfile(), 0, ClientInformation.createDefault(), false, ConnectionType.NEOFORGE);
            player.server.getPlayerList().placeNewPlayer(connection, player, cookie);
            if (!player.connection.hasChannel(ScanReply.TYPE)) throw new AssertionError("native mock negotiation includes the registered reply channel");
        }, InventoryScanGameTestMod::dispatch, helper -> helper.testInfo);
    }

    private static void configureReporter() {
        String destination = System.getProperty("tcinventoryscan.gametest.report-file");
        if (destination == null) return;
        try {
            var delegate = new JUnitLikeTestReporter(new File(destination));
            GlobalTestReporter.replaceWith(new TestReporter() {
                @Override public void onTestFailed(GameTestInfo info) {
                    LoggerFactory.getLogger("TCInventoryScanGameTest").error("Native scenario {} failed", info.getTestName(), info.getError());
                    delegate.onTestFailed(info);
                }
                @Override public void onTestSuccess(GameTestInfo info) { delegate.onTestSuccess(info); }
                @Override public void finish() { delegate.finish(); }
            });
        } catch (javax.xml.parsers.ParserConfigurationException exception) {
            throw new IllegalStateException("Cannot create native addon GameTest report", exception);
        }
    }

    @SuppressWarnings("unchecked")
    private static void dispatch(net.minecraft.server.level.ServerPlayer player, ScanRequest request) {
        var codec = (StreamCodec<RegistryFriendlyByteBuf, ScanRequest>) (StreamCodec<?, ?>)
                NetworkRegistry.getCodec(ScanRequest.TYPE.id(), ConnectionProtocol.PLAY, PacketFlow.SERVERBOUND);
        if (codec == null) throw new AssertionError("native NeoForge serverbound play codec was not registered");
        var wire = new RegistryFriendlyByteBuf(Unpooled.buffer(), player.registryAccess());
        try {
            codec.encode(wire, request); var decoded = codec.decode(wire);
            if (wire.isReadable() || !request.equals(decoded)) throw new AssertionError("request consumes its exact registered NeoForge payload bytes");
            NetworkRegistry.handleModdedPayload(player.connection, new ServerboundCustomPayloadPacket(decoded));
        } finally { wire.release(); }
    }
}
