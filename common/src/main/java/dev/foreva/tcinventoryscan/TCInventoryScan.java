package dev.foreva.tcinventoryscan;

import java.util.Objects;
import java.util.function.Consumer;
import dev.foreva.tcinventoryscan.network.ScanReply;

/** Shared constants and a dedicated-server-safe client reply bridge. */
public final class TCInventoryScan {
    public static final String MOD_ID = "tcinventoryscan";
    // The original increments 50 times across both START and END client events.
    public static final int SCAN_TICKS = 25;
    public static final int HEARTBEAT_TICKS = 5;
    public static final int HEARTBEAT_TIMEOUT = 12;
    private static Consumer<ScanReply> replyReceiver = ignored -> {};

    private TCInventoryScan() {}

    public static void installReplyReceiver(Consumer<ScanReply> receiver) {
        replyReceiver = Objects.requireNonNull(receiver);
    }

    public static void receiveReply(ScanReply reply) { replyReceiver.accept(reply); }
}
