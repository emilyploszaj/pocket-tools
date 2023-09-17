package dev.emi.pockettools;

import dev.emi.pockettools.item.*;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.RecipeType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class PocketToolsMain implements ModInitializer {
	public static final String NAMESPACE = "pockettools";

	private static final List<Item> ITEMS = new ArrayList<>();

	public static final Item POCKET_FURNACE = item("pocket_furnace", settings -> new PocketFurnace<>(RecipeType.SMELTING, settings));
	public static final Item POCKET_BLAST_FURNACE = item("pocket_blast_furnace", settings -> new PocketFurnace<>(RecipeType.BLASTING, settings));
	public static final Item POCKET_SMOKER = item("pocket_smoker", settings -> new PocketFurnace<>(RecipeType.SMOKING, settings));
	public static final Item POCKET_GRINDSTONE = item("pocket_grindstone", PocketGrindstone::new);
	public static final Item POCKET_COMPOSTER = item("pocket_composter", PocketComposter::new);
	public static final Item POCKET_END_PORTAL = item("pocket_end_portal", PocketEndPortal::new);
	public static final Item POCKET_NOTE_BLOCK = item("pocket_note_block", PocketNoteBlock::new);
	public static final Item POCKET_JUKEBOX = item("pocket_jukebox", PocketJukebox::new);
	public static final Item POCKET_ARMOR_STAND = item("pocket_armor_stand", PocketArmorStand::new);
	public static final Item POCKET_CACTUS = item("pocket_cactus", PocketCactus::new);
	public static final Item POCKET_STONECUTTER = item("pocket_stonecutter", PocketStonecutter::new);
	public static final Item POCKET_ENDER_CHEST = item("pocket_ender_chest", PocketEnderChest::new);

	public static final ItemGroup POCKET_GROUP = Registry.register(Registries.ITEM_GROUP, new Identifier(NAMESPACE, "pockettools"),
			FabricItemGroup.builder()
					.displayName(Text.translatable("itemGroup.pockettools.pockettools"))
					.icon(() -> new ItemStack(PocketToolsMain.POCKET_CACTUS))
					.entries((displayContext, entries) -> entries.addAll(ITEMS.stream().map(ItemStack::new).toList()))
					.build()
	);

	@Override
	public void onInitialize() {
	}

	private static <T extends Item> T item(String name, Function<Item.Settings, T> itemCreator) {
		T item = itemCreator.apply(new Item.Settings().maxCount(1));
		ITEMS.add(item);
		return Registry.register(Registries.ITEM, new Identifier(NAMESPACE, name), item);
	}
}