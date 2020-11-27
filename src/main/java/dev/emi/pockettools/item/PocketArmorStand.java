package dev.emi.pockettools.item;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import dev.emi.pockettools.tooltip.ConvertibleTooltipData;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.client.item.TooltipData;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ArmorMaterial;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.screen.slot.Slot;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ClickType;
import net.minecraft.world.World;

public class PocketArmorStand extends Item {

	public PocketArmorStand(Settings settings) {
		super(settings);
	}

	@Override
	public boolean onClicked(ItemStack self, ItemStack stack, Slot slot, ClickType clickType,
			PlayerInventory playerInventory) {
		PlayerEntity player = playerInventory.player;
		World world = player.world;
		CompoundTag tag = self.getOrCreateTag();
		if (clickType == ClickType.RIGHT) {
			if (stack.isEmpty()) {
				dumpArmor(tag, playerInventory);
				if (world.isClient) {
					world.playSound(player, player.getBlockPos(), SoundEvents.ITEM_ARMOR_EQUIP_GENERIC,
							SoundCategory.PLAYERS, 1.0f, 1.0f);
				}
				return true;
			} else {
				EquipmentSlot es = MobEntity.method_32326(stack);
				if (es != EquipmentSlot.MAINHAND && es != EquipmentSlot.OFFHAND) {
					String name = es.getName();
					ItemStack inner = ItemStack.EMPTY;
					if (tag.contains(name)) {
						inner = ItemStack.fromTag(tag.getCompound(name));
					}
					if (world.isClient) {
						if (stack.getItem() instanceof ArmorItem) {
							ArmorMaterial material = ((ArmorItem) stack.getItem()).getMaterial();
							world.playSound(player, player.getBlockPos(), material.getEquipSound(),
									SoundCategory.PLAYERS, 1.0f, 1.0f);
						} else {
							world.playSound(player, player.getBlockPos(), SoundEvents.ITEM_ARMOR_EQUIP_GENERIC,
									SoundCategory.PLAYERS, 1.0f, 1.0f);
						}
					}
					tag.put(name, stack.toTag(new CompoundTag()));
					playerInventory.setCursorStack(inner);
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
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public Optional<TooltipData> getTooltipData(ItemStack stack) {
		return Optional.of(new PocketArmorStandTooltip(stack));
	}

	private void dumpArmor(CompoundTag tag, PlayerInventory playerInventory) {
		List<ItemStack> stacks = new ArrayList<ItemStack>();
		if (tag.contains("head")) {
			stacks.add(ItemStack.fromTag(tag.getCompound("head")));
			tag.remove("head");
		}
		if (tag.contains("chest")) {
			stacks.add(ItemStack.fromTag(tag.getCompound("chest")));
			tag.remove("chest");
		}
		if (tag.contains("legs")) {
			stacks.add(ItemStack.fromTag(tag.getCompound("legs")));
			tag.remove("legs");
		}
		if (tag.contains("feet")) {
			stacks.add(ItemStack.fromTag(tag.getCompound("feet")));
			tag.remove("feet");
		}
		tag.remove("CustomModelData");
		for (ItemStack s : stacks) {
			playerInventory.offerOrDrop(s);
		}
	}

	class PocketArmorStandTooltip implements ConvertibleTooltipData, TooltipComponent {
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
		public void drawItems(TextRenderer textRenderer, int x, int y, MatrixStack matrices, ItemRenderer itemRenderer, int z, TextureManager textureManager) {
			CompoundTag tag = stack.getOrCreateTag();
			if (tag.contains("head")) {
				ItemStack stack = ItemStack.fromTag(tag.getCompound("head"));
				itemRenderer.renderGuiItemIcon(stack, x + 2, y + 2);
				itemRenderer.renderGuiItemOverlay(textRenderer, stack, x + 2, y + 2);
			}
			if (tag.contains("chest")) {
				ItemStack stack = ItemStack.fromTag(tag.getCompound("chest"));
				itemRenderer.renderGuiItemIcon(stack, x + 18, y + 2);
				itemRenderer.renderGuiItemOverlay(textRenderer, stack, x + 18, y + 2);
			}
			if (tag.contains("legs")) {
				ItemStack stack = ItemStack.fromTag(tag.getCompound("legs"));
				itemRenderer.renderGuiItemIcon(stack, x + 34, y + 2);
				itemRenderer.renderGuiItemOverlay(textRenderer, stack, x + 34, y + 2);
			}
			if (tag.contains("feet")) {
				ItemStack stack = ItemStack.fromTag(tag.getCompound("feet"));
				itemRenderer.renderGuiItemIcon(stack, x + 50, y + 2);
				itemRenderer.renderGuiItemOverlay(textRenderer, stack, x + 50, y + 2);
			}
		}
	}
}
