package dev.emi.pockettools.item;

import java.util.Iterator;
import java.util.Map;
import java.util.stream.Collectors;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.AnvilScreenHandler;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ClickType;
import net.minecraft.world.World;

public class PocketGrindstone extends Item {

	public PocketGrindstone(Settings settings) {
		super(settings);
	}
	
	@Override
	public boolean onClicked(ItemStack stack, ItemStack applied, ClickType arg, PlayerInventory playerInventory) {
		if (arg == ClickType.RIGHT && (applied.hasEnchantments() || applied.itemMatches(Items.ENCHANTED_BOOK))) {
			World world = playerInventory.player.world;
			if (!world.isClient) {
				ExperienceOrbEntity.method_31493((ServerWorld) world, playerInventory.player.getPos(), getExperience(applied, world));
			}
			System.out.println(getExperience(applied, world));
			playerInventory.setCursorStack(grind(applied));
			world.syncWorldEvent(1042, playerInventory.player.getBlockPos(), 0);
			return true;
		}
		return false;
	}

	private ItemStack grind(ItemStack stack) {
		ItemStack copy = stack.copy();
		stack.removeSubTag("Enchantments");
		stack.removeSubTag("StoredEnchantments");

		Map<Enchantment, Integer> map = EnchantmentHelper.get(copy).entrySet().stream().filter((entry) -> {
			return ((Enchantment)entry.getKey()).isCursed();
		}).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
		EnchantmentHelper.set(map, stack);
		stack.setRepairCost(0);
		if (stack.itemMatches(Items.ENCHANTED_BOOK) && map.size() == 0) {
			stack = new ItemStack(Items.BOOK);
			if (copy.hasCustomName()) {
				stack.setCustomName(copy.getName());
			}
		}

		for(int i = 0; i < map.size(); ++i) {
			stack.setRepairCost(AnvilScreenHandler.getNextCost(stack.getRepairCost()));
		}
		return stack;
	}

	private int getExperience(ItemStack stack, World world) {
		int i = 0;
		Map<Enchantment, Integer> map = EnchantmentHelper.get(stack);
		Iterator<Map.Entry<Enchantment, Integer>> iterator = map.entrySet().iterator();

		while(iterator.hasNext()) {
			Map.Entry<Enchantment, Integer> entry = iterator.next();
			Enchantment enchantment = (Enchantment)entry.getKey();
			Integer integer = (Integer)entry.getValue();
			if (!enchantment.isCursed()) {
				i += enchantment.getMinPower(integer);
			}
		}
		int j = (int)Math.ceil((double)i / 2.0D);
		return j + world.random.nextInt(j);
	}
}
