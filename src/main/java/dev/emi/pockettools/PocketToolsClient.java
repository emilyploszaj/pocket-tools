package dev.emi.pockettools;

import dev.emi.pockettools.tooltip.ConvertibleTooltipData;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.TooltipComponentCallback;
import net.minecraft.item.*;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.MathHelper;

import java.util.HashMap;
import java.util.Map;

public class PocketToolsClient implements ClientModInitializer {
	// TODO Make this stuff data driven so I can feel better about hard coding it instead of generating
	public static final Map<ArmorMaterial, Integer> ARMOR_COLORS = new HashMap<ArmorMaterial, Integer>();
	public static final Map<Item, Integer> ITEM_COLORS = new HashMap<Item, Integer>();

	@Override
	public void onInitializeClient() {
		ARMOR_COLORS.put(ArmorMaterials.LEATHER, ColorHelper.Argb.getArgb(255, 112, 71, 45));
		ARMOR_COLORS.put(ArmorMaterials.IRON, ColorHelper.Argb.getArgb(255, 198, 198, 198));
		ARMOR_COLORS.put(ArmorMaterials.CHAIN, ColorHelper.Argb.getArgb(255, 150, 150, 150));
		ARMOR_COLORS.put(ArmorMaterials.GOLD, ColorHelper.Argb.getArgb(255, 234, 237, 87));
		ARMOR_COLORS.put(ArmorMaterials.DIAMOND, ColorHelper.Argb.getArgb(255, 74, 237, 217));
		ARMOR_COLORS.put(ArmorMaterials.NETHERITE, ColorHelper.Argb.getArgb(255, 77, 73, 77));
		ARMOR_COLORS.put(ArmorMaterials.TURTLE, ColorHelper.Argb.getArgb(255, 49, 118, 63));
		ITEM_COLORS.put(Items.CARVED_PUMPKIN, ColorHelper.Argb.getArgb(255, 212, 114, 17));
		ITEM_COLORS.put(Items.ELYTRA, ColorHelper.Argb.getArgb(255, 127, 127, 152));
		ITEM_COLORS.put(Items.WITHER_SKELETON_SKULL, ColorHelper.Argb.getArgb(255, 41, 41, 41));
		ITEM_COLORS.put(Items.DRAGON_HEAD, ColorHelper.Argb.getArgb(255, 41, 33, 41));
		ITEM_COLORS.put(Items.CREEPER_HEAD, ColorHelper.Argb.getArgb(255, 19, 169, 16));
		ITEM_COLORS.put(Items.PLAYER_HEAD, ColorHelper.Argb.getArgb(255, 200, 150, 128));
		ITEM_COLORS.put(Items.SKELETON_SKULL, ColorHelper.Argb.getArgb(255, 188, 188, 188));
		ITEM_COLORS.put(Items.ZOMBIE_HEAD, ColorHelper.Argb.getArgb(255, 62, 105, 45));
		ColorProviderRegistry.ITEM.register((stack, tintIndex) -> {
			var nbt = stack.getOrCreateNbt();
			ItemStack armor = null;
			if (tintIndex == 1) {
				if (nbt.contains("feet")) {
					armor = ItemStack.fromNbt(nbt.getCompound("feet"));
				}
			} else if (tintIndex == 2) {
				if (nbt.contains("legs")) {
					armor = ItemStack.fromNbt(nbt.getCompound("legs"));
				}
			} else if (tintIndex == 3) {
				if (nbt.contains("chest")) {
					armor = ItemStack.fromNbt(nbt.getCompound("chest"));
				}
			} else if (tintIndex == 4) {
				if (nbt.contains("head")) {
					armor = ItemStack.fromNbt(nbt.getCompound("head"));
				}
			}
			if (armor != null) {
				if (ITEM_COLORS.containsKey(armor.getItem())) {
					return ITEM_COLORS.get(armor.getItem());
				}
				if (armor.getItem() instanceof ArmorItem item) {
					ArmorMaterial material = item.getMaterial();
					if (item instanceof DyeableArmorItem dye) {
						if (dye.hasColor(armor)) {
							return dye.getColor(armor);
						}
					}
					return ARMOR_COLORS.getOrDefault(material, -1);
				}
			}
			return -1;
		}, PocketToolsMain.POCKET_ARMOR_STAND);

		TooltipComponentCallback.EVENT.register(data -> {
			if (data instanceof ConvertibleTooltipData convertible) {
				return convertible.getComponent();
			}

			return null;
		});
	}
}
