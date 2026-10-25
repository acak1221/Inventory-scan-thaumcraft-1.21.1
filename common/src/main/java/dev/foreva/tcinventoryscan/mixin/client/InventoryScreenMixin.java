package dev.foreva.tcinventoryscan.mixin.client;

import dev.foreva.tcinventoryscan.client.InventoryScanClient;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Screen.class)
public abstract class InventoryScreenMixin {
    @Inject(method = "renderWithTooltip", at = @At("TAIL"), require = 1, allow = 1)
    private void tcinventoryscan$afterTooltip(GuiGraphics graphics, int mouseX, int mouseY, float partialTick, CallbackInfo callback) {
        InventoryScanClient.render((Screen) (Object) this, graphics, mouseX, mouseY);
    }
}
