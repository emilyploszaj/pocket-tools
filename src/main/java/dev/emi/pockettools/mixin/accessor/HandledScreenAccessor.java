package dev.emi.pockettools.mixin.accessor;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.client.gui.screen.ingame.HandledScreen;

@Mixin(HandledScreen.class)
public interface HandledScreenAccessor {
	
	@Accessor("x")
	int getX();
	
	@Accessor("y")
	int getY();
}
