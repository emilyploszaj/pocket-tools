package dev.emi.pockettools;

import dev.emi.pockettools.item.PocketArmorStand;
import dev.emi.pockettools.item.PocketCactus;
import dev.emi.pockettools.item.PocketComposter;
import dev.emi.pockettools.item.PocketEndPortal;
import dev.emi.pockettools.item.PocketEnderChest;
import dev.emi.pockettools.item.PocketFurnace;
import dev.emi.pockettools.item.PocketGrindstone;
import dev.emi.pockettools.item.PocketJukebox;
import dev.emi.pockettools.item.PocketNoteBlock;
import dev.emi.pockettools.item.PocketStonecutter;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.BlastingRecipe;
import net.minecraft.recipe.RecipeType;
import net.minecraft.recipe.SmeltingRecipe;
import net.minecraft.recipe.SmokingRecipe;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class PocketToolsMain implements ModInitializer {

	public static final ItemGroup POCKET_GROUP = FabricItemGroupBuilder.build(new Identifier("pockettools", "pockettools"), () -> {
		return new ItemStack(PocketToolsMain.POCKET_CACTUS);
	});
		
	public static final Item POCKET_FURNACE = new PocketFurnace<SmeltingRecipe>(RecipeType.SMELTING, new Item.Settings().maxCount(1).group(POCKET_GROUP));
	public static final Item POCKET_BLAST_FURNACE = new PocketFurnace<BlastingRecipe>(RecipeType.BLASTING, new Item.Settings().maxCount(1).group(POCKET_GROUP));
	public static final Item POCKET_SMOKER = new PocketFurnace<SmokingRecipe>(RecipeType.SMOKING, new Item.Settings().maxCount(1).group(POCKET_GROUP));
	public static final Item POCKET_GRINDSTONE = new PocketGrindstone(new Item.Settings().maxCount(1).group(POCKET_GROUP));
	public static final Item POCKET_COMPOSTER = new PocketComposter(new Item.Settings().maxCount(1).group(POCKET_GROUP));
	public static final Item POCKET_END_PORTAL = new PocketEndPortal(new Item.Settings().maxCount(1).group(POCKET_GROUP));
	public static final Item POCKET_NOTE_BLOCK = new PocketNoteBlock(new Item.Settings().maxCount(1).group(POCKET_GROUP));
	public static final Item POCKET_JUKEBOX = new PocketJukebox(new Item.Settings().maxCount(1).group(POCKET_GROUP));
	public static final Item POCKET_ARMOR_STAND = new PocketArmorStand(new Item.Settings().maxCount(1).group(POCKET_GROUP));
	public static final Item POCKET_CACTUS = new PocketCactus(new Item.Settings().maxCount(1).group(POCKET_GROUP));
	public static final Item POCKET_STONECUTTER = new PocketStonecutter(new Item.Settings().maxCount(1).group(POCKET_GROUP));
	public static final Item POCKET_ENDER_CHEST = new PocketEnderChest(new Item.Settings().maxCount(1).group(POCKET_GROUP));

	@Override
	public void onInitialize() {
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
		Registry.register(Registry.ITEM, new Identifier("pockettools", "pocket_ender_chest"), POCKET_ENDER_CHEST);
	}
}