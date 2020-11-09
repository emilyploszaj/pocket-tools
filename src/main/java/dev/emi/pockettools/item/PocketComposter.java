package dev.emi.pockettools.item;

import net.minecraft.block.ComposterBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ClickType;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

public class PocketComposter extends Item {

	public PocketComposter(Settings settings) {
		super(settings);
	}

	public boolean isItemBarVisible(ItemStack stack) {
		CompoundTag tag = stack.getOrCreateTag();
		if (tag.contains("fill")) {
			int fill = tag.getInt("fill");
			return fill != 0;
		}
		return false;
	}

	public int getItemBarStep(ItemStack stack) {
		CompoundTag tag = stack.getOrCreateTag();
		if (tag.contains("fill")) {
			int fill = tag.getInt("fill");
			if (fill == 8) fill = 7;
			return Math.round(fill / 7f * 13f);
		}
		return 0;
	}

	public int getItemBarColor(ItemStack stack) {
		CompoundTag tag = stack.getOrCreateTag();
		if (tag.contains("fill")) {
			int fill = tag.getInt("fill");
			if (fill == 8) {
				return MathHelper.packRgb(3, 163, 62);
			}
			return MathHelper.packRgb(0, 150, 150);
		}
		return MathHelper.packRgb(0, 150, 150);
	}

	@Override
	public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
		CompoundTag tag = stack.getOrCreateTag();
		int fill = 0;
		int compost = 20;
		if (tag.contains("fill")) {
			fill = tag.getInt("fill");
		}
		if (tag.contains("compost")) {
			compost = tag.getInt("compost");
		}
		if (compost < 20) {
			compost++;
			tag.putInt("compost", compost);
			if (compost >= 20) {
				if (fill == 7) {
					tag.putInt("fill", 8);
					world.playSound(null, entity.getBlockPos(), SoundEvents.BLOCK_COMPOSTER_READY, SoundCategory.BLOCKS, 1.0F, 1.0F);
					tag.putInt("CustomModelData", 2);
				}
			}
		}
	}
	
	@Override
	public boolean onClicked(ItemStack stack, ItemStack applied, ClickType arg, PlayerInventory playerInventory) {
		if (arg == ClickType.RIGHT) {
			World world = playerInventory.player.world;
			CompoundTag tag = stack.getOrCreateTag();
			int fill = 0;
			if (tag.contains("fill")) {
				fill = tag.getInt("fill");
			}
			if (fill == 8 && (applied.isEmpty() || (applied.getItem() == Items.BONE_MEAL && applied.getCount() < applied.getMaxCount()))) {
				playerInventory.setCursorStack(new ItemStack(Items.BONE_MEAL, applied.getCount() + 1));
				tag.putInt("fill", 0);
				tag.putInt("CustomModelData", 0);
				stack.setTag(tag);
				world.playSound(null, playerInventory.player.getBlockPos(), SoundEvents.BLOCK_COMPOSTER_EMPTY, SoundCategory.BLOCKS, 1.0F, 1.0F);
				return true;
			} else if (ComposterBlock.ITEM_TO_LEVEL_INCREASE_CHANCE.containsKey(applied.getItem())) {
				if (fill >= 7) return true;
				applied.decrement(1);
				if (!world.isClient) {
					float f = ComposterBlock.ITEM_TO_LEVEL_INCREASE_CHANCE.getFloat(applied.getItem());
					if (f >= world.random.nextFloat()) {
						if (fill == 6) {
							tag.putInt("CustomModelData", 1);
						} else {
							tag.putInt("CustomModelData", -fill);
						}
						tag.putInt("fill", fill + 1);
						tag.putInt("compost", 0);
						stack.setTag(tag);
						world.syncWorldEvent(1500, playerInventory.player.getBlockPos(), 1);
					} else {
						world.syncWorldEvent(1500, playerInventory.player.getBlockPos(), 0);
					}
				}
				return true;
			}
		}
		return false;
	}
}
