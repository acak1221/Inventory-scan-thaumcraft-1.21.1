package dev.foreva.tcinventoryscan.fabric.testing;

import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import dev.foreva.tcinventoryscan.testing.InventoryScanRuntimeChecks;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;

public final class InventoryScanGameTests implements FabricGameTest {
    @GameTest(template = "tcinventoryscan:empty", batch = "tcis_native_00", timeoutTicks = 200)
    public void twentyFiveNativeTicks(GameTestHelper test) { InventoryScanRuntimeChecks.twentyFiveNativeTicks(test); }

    @GameTest(template = "tcinventoryscan:empty", batch = "tcis_native_01", timeoutTicks = 200)
    public void serverCursorRequired(GameTestHelper test) { InventoryScanRuntimeChecks.serverCursorRequired(test); }

    @GameTest(template = "tcinventoryscan:empty", batch = "tcis_native_02", timeoutTicks = 200)
    public void invalidAddressesAndResultSlots(GameTestHelper test) { InventoryScanRuntimeChecks.invalidAddressesAndResultSlots(test); }

    @GameTest(template = "tcinventoryscan:empty", batch = "tcis_native_03", timeoutTicks = 200)
    public void permissionAndSlotIdentity(GameTestHelper test) { InventoryScanRuntimeChecks.permissionAndSlotIdentity(test); }

    @GameTest(template = "tcinventoryscan:empty", batch = "tcis_native_04", timeoutTicks = 200)
    public void menuIdentityAba(GameTestHelper test) { InventoryScanRuntimeChecks.menuIdentityAba(test); }

    @GameTest(template = "tcinventoryscan:empty", batch = "tcis_native_05", timeoutTicks = 200)
    public void itemIdentityComponentsAndCount(GameTestHelper test) { InventoryScanRuntimeChecks.itemIdentityComponentsAndCount(test); }

    @GameTest(template = "tcinventoryscan:empty", batch = "tcis_native_06", timeoutTicks = 200)
    public void cursorReplacementCancels(GameTestHelper test) { InventoryScanRuntimeChecks.cursorReplacementCancels(test); }

    @GameTest(template = "tcinventoryscan:empty", batch = "tcis_native_07", timeoutTicks = 200)
    public void missingParentIsAtomic(GameTestHelper test) { InventoryScanRuntimeChecks.missingParentIsAtomic(test); }

    @GameTest(template = "tcinventoryscan:empty", batch = "tcis_native_08", timeoutTicks = 200)
    public void repeatAndOldRequestDoNotAward(GameTestHelper test) { InventoryScanRuntimeChecks.repeatAndOldRequestDoNotAward(test); }

    @GameTest(template = "tcinventoryscan:empty", batch = "tcis_native_09", timeoutTicks = 200)
    public void selfScanAndNoContainerSpoof(GameTestHelper test) { InventoryScanRuntimeChecks.selfScanAndNoContainerSpoof(test); }

    @GameTest(template = "tcinventoryscan:empty", batch = "tcis_native_10", timeoutTicks = 200)
    public void cancelAndHeartbeatTimeout(GameTestHelper test) { InventoryScanRuntimeChecks.cancelAndHeartbeatTimeout(test); }

    @GameTest(template = "tcinventoryscan:empty", batch = "tcis_native_11", timeoutTicks = 200)
    public void retiringPermissionCallback(GameTestHelper test) { InventoryScanRuntimeChecks.retiringPermissionCallback(test); }

    @GameTest(template = "tcinventoryscan:empty", batch = "tcis_native_12", timeoutTicks = 200)
    public void disconnectAndReconnect(GameTestHelper test) { InventoryScanRuntimeChecks.disconnectAndReconnect(test); }
}
