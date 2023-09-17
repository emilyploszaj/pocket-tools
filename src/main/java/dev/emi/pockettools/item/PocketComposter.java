package dev.emi.pockettools.item;

import net.minecraft.block.ComposterBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.StackReference;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.screen.slot.Slot;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ClickType;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

public class PocketComposter extends Item {

	public PocketComposter(Settings settings) {
		super(settings);
	}

	@Override
	public boolean isItemBarVisible(ItemStack stack) {
		NbtCompound nbt = stack.getOrCreateNbt();
		if (nbt.contains("fill")) {
			int fill = nbt.getInt("fill");
			return fill != 0;
		}
		return false;
	}

	@Override
	public int getItemBarStep(ItemStack stack) {
		NbtCompound nbt = stack.getOrCreateNbt();
		if (nbt.contains("fill")) {
			int fill = nbt.getInt("fill");
			if (fill == 8) fill = 7;
			return Math.round(fill / 7f * 13f);
		}
		return 0;
	}

	@Override
	public int getItemBarColor(ItemStack stack) {
		NbtCompound nbt = stack.getOrCreateNbt();
		if (nbt.contains("fill")) {
			int fill = nbt.getInt("fill");
			if (fill == 8) {
				return MathHelper.packRgb(3, 163, 62);
			}
			return MathHelper.packRgb(0, 150, 150);
		}
		return MathHelper.packRgb(0, 150, 150);
	}

	@Override
	public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
		NbtCompound nbt = stack.getOrCreateNbt();
		int fill = 0;
		int compost = 20;
		if (nbt.contains("fill")) {
			fill = nbt.getInt("fill");
		}
		if (nbt.contains("compost")) {
			compost = nbt.getInt("compost");
		}
		if (compost < 20) {
			compost++;
			nbt.putInt("compost", compost);
			if (compost >= 20) {
				if (fill == 7) {
					nbt.putInt("fill", 8);
					world.playSound(null, entity.getBlockPos(), SoundEvents.BLOCK_COMPOSTER_READY, SoundCategory.BLOCKS, 1.0F, 1.0F);
					nbt.putInt("CustomModelData", 2);
				}
			}
		}
	}
	
	@Override
	public boolean onClicked(ItemStack stack, ItemStack applied, Slot slot, ClickType arg, PlayerEntity player, StackReference cursor) {
		if (arg == ClickType.RIGHT) {
			World world = player.getWorld();
			NbtCompound nbt = stack.getOrCreateNbt();
			int fill = 0;
			if (nbt.contains("fill")) {
				fill = nbt.getInt("fill");
			}
			if (fill == 8 && (applied.isEmpty() || (applied.getItem() == Items.BONE_MEAL && applied.getCount() < applied.getMaxCount()))) {
				cursor.set(new ItemStack(Items.BONE_MEAL, applied.getCount() + 1));
				nbt.putInt("fill", 0);
				nbt.putInt("CustomModelData", 0);
				stack.setNbt(nbt);
				world.playSound(null, player.getBlockPos(), SoundEvents.BLOCK_COMPOSTER_EMPTY, SoundCategory.BLOCKS, 1.0F, 1.0F);
				return true;
			} else if (ComposterBlock.ITEM_TO_LEVEL_INCREASE_CHANCE.containsKey(applied.getItem())) {
				if (fill >= 7) return true;
				applied.decrement(1);
				if (!world.isClient) {
					float f = ComposterBlock.ITEM_TO_LEVEL_INCREASE_CHANCE.getFloat(applied.getItem());
					if (f >= world.random.nextFloat()) {
						if (fill == 6) {
							nbt.putInt("CustomModelData", 1);
						} else {
							nbt.putInt("CustomModelData", -fill);
						}
						nbt.putInt("fill", fill + 1);
						nbt.putInt("compost", 0);
						stack.setNbt(nbt);
						world.syncWorldEvent(1500, player.getBlockPos(), 1);
					} else {
						world.syncWorldEvent(1500, player.getBlockPos(), 0);
					}
				}
				return true;
			}
		}
		return false;
	}
}
