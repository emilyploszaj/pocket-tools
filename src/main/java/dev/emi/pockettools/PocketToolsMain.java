package dev.emi.pockettools;

import java.util.HashMap;
import java.util.Map;

import dev.emi.pockettools.item.PocketArmorStand;
import dev.emi.pockettools.item.PocketCactus;
import dev.emi.pockettools.item.PocketComposter;
import dev.emi.pockettools.item.PocketEndPortal;
import dev.emi.pockettools.item.PocketFurnace;
import dev.emi.pockettools.item.PocketGrindstone;
import dev.emi.pockettools.item.PocketJukebox;
import dev.emi.pockettools.item.PocketNoteBlock;
import dev.emi.pockettools.item.PocketStonecutter;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.minecraft.client.color.item.ItemColorProvider;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ArmorMaterial;
import net.minecraft.item.ArmorMaterials;
import net.minecraft.item.DyeableArmorItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.recipe.BlastingRecipe;
import net.minecraft.recipe.RecipeType;
import net.minecraft.recipe.SmeltingRecipe;
import net.minecraft.recipe.SmokingRecipe;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.registry.Registry;

public class PocketToolsMain implements ModInitializer {
	// TODO Make this stuff data driven so I can feel better about hard coding it instead of generating
	public static final Map<ArmorMaterial, Integer> ARMOR_COLORS = new HashMap<ArmorMaterial, Integer>();
	public static final Map<Item, Integer> ITEM_COLORS = new HashMap<Item, Integer>();

	public static final Item POCKET_FURNACE = new PocketFurnace<SmeltingRecipe>(RecipeType.SMELTING, new Item.Settings().maxCount(1));
	public static final Item POCKET_BLAST_FURNACE = new PocketFurnace<BlastingRecipe>(RecipeType.BLASTING, new Item.Settings().maxCount(1));
	public static final Item POCKET_SMOKER = new PocketFurnace<SmokingRecipe>(RecipeType.SMOKING, new Item.Settings().maxCount(1));
	public static final Item POCKET_GRINDSTONE = new PocketGrindstone(new Item.Settings().maxCount(1));
	public static final Item POCKET_COMPOSTER = new PocketComposter(new Item.Settings().maxCount(1));
	public static final Item POCKET_END_PORTAL = new PocketEndPortal(new Item.Settings().maxCount(1));
	public static final Item POCKET_NOTE_BLOCK = new PocketNoteBlock(new Item.Settings().maxCount(1));
	public static final Item POCKET_JUKEBOX = new PocketJukebox(new Item.Settings().maxCount(1));
	public static final Item POCKET_ARMOR_STAND = new PocketArmorStand(new Item.Settings().maxCount(1));
	public static final Item POCKET_CACTUS = new PocketCactus(new Item.Settings().maxCount(1));
	public static final Item POCKET_STONECUTTER = new PocketStonecutter(new Item.Settings().maxCount(1));

	@Override
	public void onInitialize() {
		ARMOR_COLORS.put(ArmorMaterials.LEATHER, MathHelper.packRgb(112, 71, 45));
		ARMOR_COLORS.put(ArmorMaterials.IRON, MathHelper.packRgb(198, 198, 198));
		ARMOR_COLORS.put(ArmorMaterials.CHAIN, MathHelper.packRgb(150, 150, 150));
		ARMOR_COLORS.put(ArmorMaterials.GOLD, MathHelper.packRgb(234, 237, 87));
		ARMOR_COLORS.put(ArmorMaterials.DIAMOND, MathHelper.packRgb(74, 237, 217));
		ARMOR_COLORS.put(ArmorMaterials.NETHERITE, MathHelper.packRgb(77, 73, 77));
		ARMOR_COLORS.put(ArmorMaterials.TURTLE, MathHelper.packRgb(49, 118, 63));
		ITEM_COLORS.put(Items.CARVED_PUMPKIN, MathHelper.packRgb(212, 114, 17));
		ITEM_COLORS.put(Items.ELYTRA, MathHelper.packRgb(127, 127, 152));
		ITEM_COLORS.put(Items.WITHER_SKELETON_SKULL, MathHelper.packRgb(41, 41, 41));
		ITEM_COLORS.put(Items.DRAGON_HEAD, MathHelper.packRgb(41, 33, 41));
		ITEM_COLORS.put(Items.CREEPER_HEAD, MathHelper.packRgb(19, 169, 16));
		ITEM_COLORS.put(Items.PLAYER_HEAD, MathHelper.packRgb(200, 150, 128));
		ITEM_COLORS.put(Items.SKELETON_SKULL, MathHelper.packRgb(188, 188, 188));
		ITEM_COLORS.put(Items.ZOMBIE_HEAD, MathHelper.packRgb(62, 105, 45));
		Registry.register(Registry.ITEM, new Identifier("pockettools", "pocket_furnace"), POCKET_FURNACE);
		Registry.register(Registry.ITEM, new Identifier("pockettools", "pocket_blast_furnace"), POCKET_BLAST_FURNACE);
		Registry.register(Registry.ITEM, new Identifier("pockettools", "pocket_smoker"), POCKET_SMOKER);
		Registry.register(Registry.ITEM, new Identifier("pockettools", "pocket_grindstone"), POCKET_GRINDSTONE);
		Registry.register(Registry.ITEM, new Identifier("pockettools", "pocket_composter"), POCKET_COMPOSTER);
		Registry.register(Registry.ITEM, new Identifier("pockettools", "pocket_end_portal"), POCKET_END_PORTAL);
		Registry.register(Registry.ITEM, new Identifier("pockettools", "pocket_note_block"), POCKET_NOTE_BLOCK);
		Registry.register(Registry.ITEM, new Identifier("pockettools", "pocket_jukebox"), POCKET_JUKEBOX);
		Registry.register(Registry.ITEM, new Identifier("pockettools", "pocket_armor_stand"), POCKET_ARMOR_STAND);
		Registry.register(Registry.ITEM, new Identifier("pockettools", "pocket_cactus"), POCKET_CACTUS);
		Registry.register(Registry.ITEM, new Identifier("pockettools", "pocket_stonecutter"), POCKET_STONECUTTER);
		ColorProviderRegistry.ITEM.register(new ItemColorProvider() {

			@Override
			public int getColor(ItemStack stack, int tintIndex) {
				CompoundTag tag = stack.getOrCreateTag();
				ItemStack armor = null;
				if (tintIndex == 1) {
					if (tag.contains("feet")) {
						armor = ItemStack.fromTag(tag.getCompound("feet"));
					}
				} else if (tintIndex == 2) {
					if (tag.contains("legs")) {
						armor = ItemStack.fromTag(tag.getCompound("legs"));
					}
				} else if (tintIndex == 3) {
					if (tag.contains("chest")) {
						armor = ItemStack.fromTag(tag.getCompound("chest"));
					}
				} else if (tintIndex == 4) {
					if (tag.contains("head")) {
						armor = ItemStack.fromTag(tag.getCompound("head"));
					}
				}
				if (armor != null) {
					if (ITEM_COLORS.containsKey(armor.getItem())) {
						return ITEM_COLORS.get(armor.getItem());
					}
					if (armor.getItem() instanceof ArmorItem) {
						ArmorItem item = (ArmorItem) armor.getItem();
						ArmorMaterial material = item.getMaterial();
						if (item instanceof DyeableArmorItem) {
							DyeableArmorItem dye = (DyeableArmorItem) item;
							if (dye.hasColor(armor)) {
								return dye.getColor(armor);
							}
						}
						return ARMOR_COLORS.getOrDefault(material, -1);
					}
				}
				return -1;
			}
			
		}, POCKET_ARMOR_STAND);
	}
}