package dev.emi.pockettools.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import dev.emi.pockettools.PocketToolsMain;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.screen.slot.Slot;

/**
 * Makes certain pocket portal blocks immovable in the inventory for the a e s t h e t i c
 */
@Mixin(Slot.class)
public abstract class SlotMixin {

	@Shadow
	public abstract ItemStack getStack();
	
	@Inject(at = @At("HEAD"), method = "canTakeItems", cancellable = true)
	public void canTakeItems(PlayerEntity player, CallbackInfoReturnable<Boolean> info) {
		ItemStack stack = getStack();
		if (stack.getItem() == PocketToolsMain.POCKET_END_PORTAL && stack.hasTag()) {
			CompoundTag tag = stack.getTag();
			if ((tag.contains("portal") && tag.getBoolean("portal")) || (tag.contains("filled") && tag.getBoolean("filled"))) {
				info.setReturnValue(false);
			}
		}
	}
}
