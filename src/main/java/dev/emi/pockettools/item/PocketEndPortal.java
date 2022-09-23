package dev.emi.pockettools.item;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.StackReference;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.screen.slot.Slot;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ClickType;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;

public class PocketEndPortal extends Item {

	public PocketEndPortal(Settings settings) {
		super(settings);
	}

	@Override
	public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
		NbtCompound nbt = stack.getOrCreateNbt();
		if (nbt.contains("tp") && nbt.getBoolean("tp")) {
			nbt.remove("tp");
			// This has to happen in a tick so that there is no teleportation in the middle of a handled event
			if (world instanceof ServerWorld && !entity.hasVehicle() && !entity.hasPassengers() && entity.canUsePortals()) {
				RegistryKey<World> registryKey = world.getRegistryKey() == World.END ? World.OVERWORLD : World.END;
				ServerWorld serverWorld = ((ServerWorld) world).getServer().getWorld(registryKey);
				if (serverWorld == null) {
					return;
				}
				entity.moveToWorld(serverWorld);
			}
		}
	}
	
	@Override
	public boolean onClicked(ItemStack self, ItemStack stack, Slot slot, ClickType clickType, PlayerEntity player, StackReference cursor) {
		NbtCompound nbt = self.getOrCreateNbt();
		World world = player.world;
		if (clickType == ClickType.RIGHT) {
			if (nbt.contains("portal") && nbt.getBoolean("portal")) {
				if (!world.isClient) {
					// See inventoryTick
					nbt.putBoolean("tp", true);
				}
				return true;
			}
			if (nbt.contains("filled") && nbt.getBoolean("filled")) {
				if (stack.isEmpty() || (stack.getItem() == Items.ENDER_EYE && stack.getCount() < stack.getMaxCount())) {
					boolean brokePortal = breakPortal(self, player.getInventory());
					nbt.remove("filled");
					nbt.remove("CustomModelData");
					if (brokePortal) {
						if (world.isClient) {
							player.playSound(SoundEvents.BLOCK_GLASS_BREAK, SoundCategory.BLOCKS, 1f, player.getRandom().nextFloat() * 0.1f + 0.9f);
						}
					}
					cursor.set(new ItemStack(Items.ENDER_EYE, stack.getCount() + 1));
					if (world.isClient) {
						player.playSound(SoundEvents.ENTITY_ITEM_PICKUP, SoundCategory.BLOCKS, 1f, player.getRandom().nextFloat() * 0.1f + 0.9f);
					}
					return true;
				}
			} else {
				if (stack.getItem() == Items.ENDER_EYE) {
					stack.decrement(1);
					nbt.putBoolean("filled", true);
					nbt.putInt("CustomModelData", 1);
					self.setNbt(nbt);
					if (world.isClient) {
						player.playSound(SoundEvents.BLOCK_END_PORTAL_FRAME_FILL, SoundCategory.BLOCKS, 1f, player.getRandom().nextFloat() * 0.1f + 0.9f);
					}
					PlayerInventory playerInventory = player.getInventory();
					for (int i = 19; i < 26; i++) {
						if (isLitPortal(playerInventory.getStack(i))) continue;
						if (!isFilledPortal(playerInventory.getStack(i - 1))) continue;
						if (!isFilledPortal(playerInventory.getStack(i + 1))) continue;
						if (!isFilledPortal(playerInventory.getStack(i - 9))) continue;
						if (!isFilledPortal(playerInventory.getStack(i + 9))) continue;
						fillPortal(i, playerInventory);
					}
					for (int i = 28; i < 35; i++) {
						if (isLitPortal(playerInventory.getStack(i))) continue;
						if (!isFilledPortal(playerInventory.getStack(i - 1))) continue;
						if (!isFilledPortal(playerInventory.getStack(i + 1))) continue;
						if (!isFilledPortal(playerInventory.getStack(i - 9))) continue;
						if (!isFilledPortal(playerInventory.getStack(i - 27))) continue;
						fillPortal(i, playerInventory);
					}
					return true;
				}
			}
		}
		return false;
	}

	public boolean isLitPortal(ItemStack stack) {
		return stack.getItem() == this && stack.hasNbt() && stack.getNbt().contains("portal") && stack.getNbt().getBoolean("portal");
	}

	public boolean isFilledPortal(ItemStack stack) {
		return stack.getItem() == this && stack.hasNbt() && stack.getNbt().contains("filled") && stack.getNbt().getBoolean("filled");
	}

	public boolean breakPortal(ItemStack self, PlayerInventory playerInventory) {
		boolean brokePortal = false;
		for (int i = 0; i < playerInventory.main.size(); i++) {
			if (playerInventory.getStack(i) == self) {
				if (i % 9 > 0) {
					if (isLitPortal(playerInventory.getStack(i - 1))) {
						playerInventory.setStack(i - 1, ItemStack.EMPTY);
						brokePortal = true;
					}
				}
				if (i % 9 < 8) {
					if (isLitPortal(playerInventory.getStack(i + 1))) {
						playerInventory.setStack(i + 1, ItemStack.EMPTY);
						brokePortal = true;
					}
				}
				if (i < 9) {
					if (isLitPortal(playerInventory.getStack(i + 27))) {
						playerInventory.setStack(i + 27, ItemStack.EMPTY);
						brokePortal = true;
					}
				} else {
					if (i >= 18) {
						if (isLitPortal(playerInventory.getStack(i - 9))) {
							playerInventory.setStack(i - 9, ItemStack.EMPTY);
							brokePortal = true;
						}
					}
					if (isLitPortal(playerInventory.getStack(i + 9))) {
						playerInventory.setStack(i + 9, ItemStack.EMPTY);
						brokePortal = true;
					}
				}
			}
		}
		return brokePortal;
	}

	public void fillPortal(int i, PlayerInventory playerInventory) {
		ItemStack temp = playerInventory.getStack(i);
		ItemStack stack = new ItemStack(this);
		NbtCompound nbt = new NbtCompound();
		nbt.putBoolean("portal", true);
		nbt.putInt("CustomModelData", 2);
		stack.setNbt(nbt);
		playerInventory.setStack(i, stack);
		playerInventory.offerOrDrop(temp);
		if (playerInventory.player.world.isClient) {
			playerInventory.player.playSound(SoundEvents.BLOCK_END_PORTAL_SPAWN, SoundCategory.BLOCKS, 1f, 1f);
		}
	}
}
