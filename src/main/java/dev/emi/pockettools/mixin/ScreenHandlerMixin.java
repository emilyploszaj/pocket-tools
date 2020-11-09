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
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.ClickType;
import net.minecraft.util.collection.DefaultedList;

/**
 * Remember SlotMixin?
 * Well that makes it so the item click actions don't occur so you can't interact with them.
 * Oops, well, this reimplements that behavior only for portal items
 */
@Mixin(ScreenHandler.class)
public class ScreenHandlerMixin {
	@Shadow
	public final DefaultedList<Slot> slots = DefaultedList.of();

	private boolean doAction;

	// Marked at head so that a changing state won't cause double fires.
	// Imagine placing an eye, it then becomes invalid, and at the end of the method it sees it's invalid and fires again, taking it out
	@Inject(at = @At("HEAD"), method = "method_30010")
	private void head_method_30010(int i, int clickData, SlotActionType slotActionType, PlayerEntity player, CallbackInfoReturnable<ItemStack> info) {
		doAction = false;
		if (i < 0 || i >= slots.size()) return;
		ItemStack stack = slots.get(i).getStack();
		if (stack.getItem() == PocketToolsMain.POCKET_END_PORTAL && stack.hasTag()) {
			CompoundTag tag = stack.getTag();
			if ((tag.contains("portal") && tag.getBoolean("portal")) || (tag.contains("filled") && tag.getBoolean("filled"))) {
				doAction = true;
			}
		}
	}
	
	@Inject(at = @At("RETURN"), method = "method_30010")
	private void tail_method_30010(int i, int clickData, SlotActionType slotActionType, PlayerEntity player, CallbackInfoReturnable<ItemStack> info) {
		if (i < 0 || i >= slots.size()) return;
		if (!doAction) return;
		ItemStack stack = slots.get(i).getStack();
		if (stack.getItem() == PocketToolsMain.POCKET_END_PORTAL && stack.hasTag()) {
			CompoundTag tag = stack.getTag();
			if ((tag.contains("portal") && tag.getBoolean("portal")) || (tag.contains("filled") && tag.getBoolean("filled"))) {
				if (slotActionType == SlotActionType.PICKUP) {
					ClickType clickType = clickData == 0 ? ClickType.LEFT : ClickType.RIGHT;
					stack.onClicked(player.getInventory().getCursorStack(), clickType, player.getInventory());
				}
			}
		}
	}
}
