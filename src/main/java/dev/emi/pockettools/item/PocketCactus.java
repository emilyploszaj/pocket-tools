package dev.emi.pockettools.item;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.StackReference;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.ClickType;

public class PocketCactus extends Item {

	public PocketCactus(Settings settings) {
		super(settings);
	}

	@Override
	public boolean onClicked(ItemStack self, ItemStack stack, Slot slot, ClickType clickType, PlayerEntity player, StackReference cursor) {
		if (clickType == ClickType.RIGHT) {
			if (stack.isEmpty()) {
				player.damage(player.getDamageSources().cactus(), 1f);
			} else {
				cursor.set(ItemStack.EMPTY);
			}
			return true;
		}
		return false;
	}

	@Override
	public boolean onStackClicked(ItemStack self, Slot slot, ClickType clickType, PlayerEntity player) {
		ItemStack stack = slot.getStack();
		if (clickType == ClickType.RIGHT) {
			if (!stack.isEmpty()) {
				stack.setCount(0);
			}
			return true;
		}
		return false;
	}
}
