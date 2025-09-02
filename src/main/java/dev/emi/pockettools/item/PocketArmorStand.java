package dev.emi.pockettools.item;

import java.util.List;
import java.util.Optional;

import com.google.common.collect.Lists;

import dev.emi.pockettools.tooltip.ConvertibleTooltipData;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.client.item.TooltipData;
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
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

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
				dumpArmor(self, nbt, player.getInventory());
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

	private void dumpArmor(ItemStack stack, NbtCompound tag, PlayerInventory playerInventory) {
		List<ItemStack> stacks = getArmorInventory(stack);
		tag.remove("head");
		tag.remove("chest");
		tag.remove("legs");
		tag.remove("feet");
		tag.remove("CustomModelData");
		for (Object s : stacks) {
			playerInventory.offerOrDrop((ItemStack) s);
		}
	}

	public static List<ItemStack> getArmorInventory(ItemStack stack) {
		List<ItemStack> list = Lists.newArrayList();
		list.add(ItemStack.EMPTY);
		list.add(ItemStack.EMPTY);
		list.add(ItemStack.EMPTY);
		list.add(ItemStack.EMPTY);
		if (!stack.hasNbt()) {
			return list;
		}
		NbtCompound tag = stack.getNbt();
		if (tag.contains("head")) {
			list.set(0, ItemStack.fromNbt(tag.getCompound("head")));
		}
		if (tag.contains("chest")) {
			list.set(1, ItemStack.fromNbt(tag.getCompound("chest")));
		}
		if (tag.contains("legs")) {
			list.set(2, ItemStack.fromNbt(tag.getCompound("legs")));
		}
		if (tag.contains("feet")) {
			list.set(3, ItemStack.fromNbt(tag.getCompound("feet")));
		}
		return list;
	}

	static class PocketArmorStandTooltip implements ConvertibleTooltipData, TooltipComponent {
		private final Identifier TOOLTIP = new Identifier("pockettools", "textures/gui/component/tooltip.png");
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
			context.setShaderColor(1.f, 1.f, 1.f, 1.f);
			List<ItemStack> stacks = getArmorInventory(stack);
			for (int i = 0; i < 4; i++) {
				if (stacks.get(i).isEmpty()) {
					context.drawTexture(TOOLTIP, x + 1 + (18 * i), y + 1, 18 * (2 + i), 0, 18, 18, 256, 256);
				} else {
					this.renderGuiItem(context, textRenderer, stacks.get(i), x + 2 + (18 * i), y + 2);
					context.drawTexture(TOOLTIP, x + 1 + (18 * i), y + 1, 0, 0, 18, 18, 256, 256);
				}
			}
		}

		private void renderGuiItem(DrawContext context, TextRenderer textRenderer, ItemStack stack, int x, int y) {
			context.drawItem(stack, x, y);
			context.drawItemInSlot(textRenderer, stack, x, y);
		}
	}
}
