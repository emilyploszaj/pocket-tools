package dev.emi.pockettools.item;

import dev.emi.pockettools.tooltip.ConvertibleTooltipData;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.client.item.TooltipData;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.StackReference;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ArmorMaterial;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.screen.slot.Slot;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ClickType;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.Optional;

public class PocketArmorStand extends Item {

	public PocketArmorStand(Settings settings) {
		super(settings);
	}

	@Override
	public boolean onClicked(ItemStack self, ItemStack otherStack, Slot slot, ClickType clickType, PlayerEntity player, StackReference cursor) {
		World world = player.getWorld();
		NbtCompound nbt = self.getOrCreateNbt();
		if (clickType == ClickType.RIGHT) {
			if (otherStack.isEmpty()) {
				dumpArmor(nbt, player.getInventory());
				if (world.isClient) {
					world.playSound(player, player.getBlockPos(), SoundEvents.ITEM_ARMOR_EQUIP_GENERIC, SoundCategory.PLAYERS, 1.0f, 1.0f);
				}
				return true;
			} else {
				EquipmentSlot es = MobEntity.getPreferredEquipmentSlot(otherStack);
				if (es != EquipmentSlot.MAINHAND && es != EquipmentSlot.OFFHAND) {
					ItemStack inner = swapArmor(self, otherStack, es, nbt, player);
					cursor.set(inner);
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public boolean onStackClicked(ItemStack self, Slot slot, ClickType clickType, PlayerEntity player) {
		ItemStack stack = slot.getStack();
		NbtCompound nbt = self.getOrCreateNbt();
		if (slot.canTakeItems(player) && clickType == ClickType.RIGHT) {
			EquipmentSlot es = MobEntity.getPreferredEquipmentSlot(stack);
			if (es != EquipmentSlot.MAINHAND && es != EquipmentSlot.OFFHAND) {
				String name = es.getName();
				ItemStack inner = ItemStack.EMPTY;
				if (nbt.contains(name)) {
					inner = ItemStack.fromNbt(nbt.getCompound(name));
				}
				if (slot.canInsert(inner) || inner.isEmpty()) {
					inner = swapArmor(self, stack, es, nbt, player);
					slot.setStack(inner);
					return true;
				}
			}
		}
		return super.onStackClicked(self, slot, clickType, player);
	}

	@Override
	public Optional<TooltipData> getTooltipData(ItemStack stack) {
		return Optional.of(new PocketArmorStandTooltip(stack));
	}

	private ItemStack swapArmor(ItemStack self, ItemStack stack, EquipmentSlot es, NbtCompound tag, PlayerEntity player) {
		var world = player.getWorld();
		String name = es.getName();
		var inner = ItemStack.EMPTY;
		if (tag.contains(name)) {
			inner = ItemStack.fromNbt(tag.getCompound(name));
		}
		if (world.isClient()) {
			if (stack.getItem() instanceof ArmorItem armorItem) {
				ArmorMaterial material = armorItem.getMaterial();
				world.playSound(player, player.getBlockPos(), material.getEquipSound(),
						SoundCategory.PLAYERS, 1.0f, 1.0f);
			} else {
				world.playSound(player, player.getBlockPos(), SoundEvents.ITEM_ARMOR_EQUIP_GENERIC,
						SoundCategory.PLAYERS, 1.0f, 1.0f);
			}
		}
		tag.put(name, stack.writeNbt(new NbtCompound()));
		int mask = 0;
		if (tag.contains("head")) {
			mask |= 1;
		}
		if (tag.contains("chest")) {
			mask |= 2;
		}
		if (tag.contains("legs")) {
			mask |= 4;
		}
		if (tag.contains("feet")) {
			mask |= 8;
		}
		tag.putInt("CustomModelData", mask);
		return inner;
	}

	private void dumpArmor(NbtCompound tag, PlayerInventory playerInventory) {
		ArrayList stacks = new ArrayList<ItemStack>();
		if (tag.contains("head")) {
			stacks.add(ItemStack.fromNbt(tag.getCompound("head")));
			tag.remove("head");
		}
		if (tag.contains("chest")) {
			stacks.add(ItemStack.fromNbt(tag.getCompound("chest")));
			tag.remove("chest");
		}
		if (tag.contains("legs")) {
			stacks.add(ItemStack.fromNbt(tag.getCompound("legs")));
			tag.remove("legs");
		}
		if (tag.contains("feet")) {
			stacks.add(ItemStack.fromNbt(tag.getCompound("feet")));
			tag.remove("feet");
		}
		tag.remove("CustomModelData");
		for (Object s : stacks) {
			playerInventory.offerOrDrop((ItemStack) s);
		}
	}

	static class PocketArmorStandTooltip implements ConvertibleTooltipData, TooltipComponent {
		public ItemStack stack;

		public PocketArmorStandTooltip(ItemStack stack) {
			this.stack = stack;
		}

		@Override
		public TooltipComponent getComponent() {
			return this;
		}

		@Override
		public int getHeight() {
			return 20;
		}

		@Override
		public int getWidth(TextRenderer textRenderer) {
			return 68;
		}

		@Override
		public void drawItems(TextRenderer textRenderer, int x, int y, DrawContext context) {
			NbtCompound nbt = stack.getOrCreateNbt();
			if (nbt.contains("head")) {
				ItemStack stack = ItemStack.fromNbt(nbt.getCompound("head"));
				this.renderGuiItem(context, textRenderer, stack, x + 2, y + 2);
			}
			if (nbt.contains("chest")) {
				ItemStack stack = ItemStack.fromNbt(nbt.getCompound("chest"));
				this.renderGuiItem(context, textRenderer, stack, x + 18, y + 2);
			}
			if (nbt.contains("legs")) {
				ItemStack stack = ItemStack.fromNbt(nbt.getCompound("legs"));
				this.renderGuiItem(context, textRenderer, stack, x + 34, y + 2);
			}
			if (nbt.contains("feet")) {
				ItemStack stack = ItemStack.fromNbt(nbt.getCompound("feet"));
				this.renderGuiItem(context, textRenderer, stack, x + 50, y + 2);
			}
		}

		private void renderGuiItem(DrawContext context, TextRenderer textRenderer, ItemStack stack, int x, int y) {
			context.drawItem(stack, x, y);
			context.drawItemInSlot(textRenderer, stack, x, y);
		}
	}
}
