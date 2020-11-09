package dev.emi.pockettools.item;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ArmorMaterial;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ClickType;
import net.minecraft.world.World;

public class PocketArmorStand extends Item {

	public PocketArmorStand(Settings settings) {
		super(settings);
	}
	
	@Override
	public boolean onClicked(ItemStack self, ItemStack stack, ClickType clickType, PlayerInventory playerInventory) {
		PlayerEntity player = playerInventory.player;
		World world = player.world;
		CompoundTag tag = self.getOrCreateTag();
		if (clickType == ClickType.RIGHT) {
			if (stack.isEmpty()) {
				dumpArmor(tag, playerInventory);
				if (world.isClient) {
					world.playSound(player, player.getBlockPos(), SoundEvents.ITEM_ARMOR_EQUIP_GENERIC, SoundCategory.PLAYERS, 1.0f, 1.0f);
				}
				return true;
			} else {
				EquipmentSlot slot = MobEntity.getPreferredEquipmentSlot(stack);
				if (slot != EquipmentSlot.MAINHAND && slot != EquipmentSlot.OFFHAND) {
					String name = slot.getName();
					ItemStack inner = ItemStack.EMPTY;
					if (tag.contains(name)) {
						inner = ItemStack.fromTag(tag.getCompound(name));
					}
					if (world.isClient) {
						if(stack.getItem() instanceof ArmorItem) {
							ArmorMaterial material = ((ArmorItem) stack.getItem()).getMaterial();
							world.playSound(player, player.getBlockPos(), material.getEquipSound(), SoundCategory.PLAYERS, 1.0f, 1.0f);
						} else {
							world.playSound(player, player.getBlockPos(), SoundEvents.ITEM_ARMOR_EQUIP_GENERIC, SoundCategory.PLAYERS, 1.0f, 1.0f);
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
}
