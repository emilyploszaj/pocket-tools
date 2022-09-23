package dev.emi.pockettools.mixin;

import dev.emi.pockettools.tooltip.ConvertibleTooltipData;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.client.item.TooltipData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.List;

/**
 * This mixin is only required until FAPI adds ConvertibleTooltip, probably near to an official 1.17 release
 */
@Mixin(Screen.class)
public class ScreenMixin {
	
	@Inject(method = "method_32635", at = @At("HEAD"), cancellable = true, locals = LocalCapture.CAPTURE_FAILHARD)
	private static void onComponentConstruct(List<TooltipComponent> list, TooltipData data, CallbackInfo ci) {
		if (data instanceof ConvertibleTooltipData convertible) {
			list.add(convertible.getComponent());
			ci.cancel();
		}
	}
}
