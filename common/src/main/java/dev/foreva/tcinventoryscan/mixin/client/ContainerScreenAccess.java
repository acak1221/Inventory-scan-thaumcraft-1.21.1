package dev.foreva.tcinventoryscan.mixin.client;

import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(AbstractContainerScreen.class)
public interface ContainerScreenAccess {
    @Invoker("findSlot") Slot tcinventoryscan$findSlot(double x, double y);
    @Accessor("leftPos") int tcinventoryscan$leftPos();
    @Accessor("topPos") int tcinventoryscan$topPos();
}
