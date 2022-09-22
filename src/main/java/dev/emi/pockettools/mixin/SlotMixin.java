package dev.emi.pockettools.mixin;

import dev.emi.pockettools.PocketToolsMain;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.screen.slot.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Makes certain pocket portal blocks immovable in the inventory for the a e s t h e t i c
 */
@Mixin(Slot.class)
public abstract class SlotMixin {

	@Shadow
	public abstract ItemStack getStack();
	
	@Inject(at = @At("HEAD"), method = "canTakeItems", cancellable = true)
	public void canTakeItems(PlayerEntity player, CallbackInfoReturnable<Boolean> info) {
		ItemStack stack = this.getStack();
		if (stack.getItem() == PocketToolsMain.POCKET_END_PORTAL && stack.hasNbt()) {
			NbtCompound nbt = stack.getNbt();
			if ((nbt.contains("portal") && nbt.getBoolean("portal")) || (nbt.contains("filled") && nbt.getBoolean("filled"))) {
				if (!player.isCreative()) {
					info.setReturnValue(false);
				}
			}
		}
	}
}
