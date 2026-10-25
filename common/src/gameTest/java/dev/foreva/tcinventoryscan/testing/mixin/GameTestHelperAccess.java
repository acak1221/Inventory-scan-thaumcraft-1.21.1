package dev.foreva.tcinventoryscan.testing.mixin;

import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.GameTestInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/** Testmod-only lifetime observation; no production accessor is installed. */
@Mixin(GameTestHelper.class)
public interface GameTestHelperAccess {
    @Accessor("testInfo") GameTestInfo tcinventoryscan$testInfo();
}
