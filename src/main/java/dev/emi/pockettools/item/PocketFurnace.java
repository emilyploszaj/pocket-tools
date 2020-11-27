package dev.emi.pockettools.item;

import java.util.Optional;

import dev.emi.pockettools.tooltip.ConvertibleTooltipData;
import net.minecraft.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.client.item.TooltipData;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.recipe.AbstractCookingRecipe;
import net.minecraft.recipe.RecipeType;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.ClickType;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

public class PocketFurnace<T extends AbstractCookingRecipe> extends Item {
	private RecipeType<T> type;

	public PocketFurnace(RecipeType<T> type, Settings settings) {
		super(settings);
		this.type = type;
	}

	@Override
	public boolean isItemBarVisible(ItemStack stack) {
		CompoundTag tag = stack.getOrCreateTag();
		if (tag.contains("cookTime") && tag.contains("fuelTime")) {
			int cookTime = tag.getInt("cookTime");
			int fuelTime = tag.getInt("fuelTime");
			return cookTime > 0 && fuelTime > 0;
		}
		return false;
	}

	@Override
	public int getItemBarStep(ItemStack stack) {
		CompoundTag tag = stack.getOrCreateTag();
		if (tag.contains("cookTime") && tag.contains("maxCookTime")) {
			int cookTime = tag.getInt("cookTime");
			int maxCookTime = tag.getInt("maxCookTime");
			return Math.round((maxCookTime - cookTime) / ((float) (maxCookTime)) * 13f);
		}
		return 0;
	}

	@Override
	public int getItemBarColor(ItemStack stack) {
		CompoundTag tag = stack.getOrCreateTag();
		if (tag.contains("cookTime")) {
			return MathHelper.packRgb(0, 150, 150);
		}
		return MathHelper.packRgb(0, 150, 150);
	}

	/*
	 * Emi did you really reimplement the furnace logic without copying code why
	 * didn't you just copy code you're gonna have so many edge cases that you
	 * haven't accounted for
	 */
	@Override
	public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
		if (!world.isClient) {
			CompoundTag tag = stack.getOrCreateTag();
			ItemStack input = ItemStack.EMPTY;
			ItemStack fuel = ItemStack.EMPTY;
			ItemStack output = ItemStack.EMPTY;
			int fuelTime = 0;
			int cookTime = 0;
			int customModelData = 0;
			if (tag.contains("input")) {
				input = ItemStack.fromTag(tag.getCompound("input"));
			}
			if (tag.contains("fuel")) {
				fuel = ItemStack.fromTag(tag.getCompound("fuel"));
			}
			if (tag.contains("output")) {
				output = ItemStack.fromTag(tag.getCompound("output"));
			}
			if (tag.contains("fuelTime")) {
				fuelTime = tag.getInt("fuelTime");
			}
			if (tag.contains("cookTime")) {
				cookTime = tag.getInt("cookTime");
			}
			if (tag.contains("CustomModelData")) {
				customModelData = tag.getInt("CustomModelData");
			}
			if (cookTime > 0) {
				if (fuelTime > 0) {
					fuelTime--;
					tag.putInt("fuelTime", fuelTime);
				}
				if (fuelTime == 0) {
					if (fuel.getCount() > 0) {
						fuelTime = AbstractFurnaceBlockEntity.createFuelTimeMap().getOrDefault(fuel.getItem(), 0);
						tag.putInt("fuelTime", fuelTime);
						tag.putInt("maxFuelTime", fuelTime);
						fuel.decrement(1);
						tag.put("fuel", fuel.toTag(new CompoundTag()));
					} else {
						return;
					}
				}
				cookTime--;
				if (cookTime == 0) {
					Optional<T> recipe = world.getRecipeManager().getFirstMatch(type,
							new SimpleInventory(new ItemStack[] { input }), world);
					if (recipe.isPresent()) {
						if (output.isEmpty()) {
							output = recipe.get().getOutput().copy();
						} else if (output.isItemEqual(recipe.get().getOutput())) {
							output.increment(1);
						}
						tag.put("output", output.toTag(new CompoundTag()));
						if (output.getCount() < output.getMaxCount() && input.getCount() > 1) {
							cookTime = recipe.get().getCookTime();
							tag.putInt("maxCookTime", cookTime);
						}
					}
					input.decrement(1);
					tag.put("input", input.toTag(new CompoundTag()));
				}
				tag.putInt("cookTime", cookTime);
			} else {
				if (fuelTime > 0) {
					fuelTime--;
					tag.putInt("fuelTime", fuelTime);
				}
				if (output.getCount() < output.getMaxCount() && input.getCount() > 0) {
					Optional<T> recipe = world.getRecipeManager().getFirstMatch(type,
							new SimpleInventory(new ItemStack[] { input }), world);
					if (recipe.isPresent() && (output.isEmpty() || (output.isItemEqual(recipe.get().getOutput())
							&& output.getCount() < output.getMaxCount()))) {
						cookTime = recipe.get().getCookTime();
						tag.putInt("cookTime", cookTime);
						tag.putInt("maxCookTime", cookTime);
					}
				}
			}
			if ((customModelData == 0) != (fuelTime == 0)) {
				tag.putInt("CustomModelData", 1 - customModelData);
			}
		}
	}

	@Override
	public boolean onClicked(ItemStack stack, ItemStack applied, Slot slot, ClickType arg,
			PlayerInventory playerInventory) {
		CompoundTag tag = stack.getOrCreateTag();
		if (arg == ClickType.RIGHT) {
			if (applied.isEmpty()) {
				if (tag.contains("output")) {
					ItemStack output = ItemStack.fromTag(tag.getCompound("output"));
					playerInventory.setCursorStack(output.copy());
					tag.remove("output");
					return true;
				}
			}
			ItemStack input = ItemStack.EMPTY;
			if (tag.contains("input")) {
				input = ItemStack.fromTag(tag.getCompound("input"));
			}
			if ((!input.isEmpty() && input.isItemEqual(applied))
					|| (input.isEmpty() && isSmeltable(playerInventory.player.world, applied))) {
				if (input.isEmpty()) {
					input = applied.copy();
					applied.setCount(0);
				} else {
					if (input.getCount() + applied.getCount() > input.getMaxCount()) {
						applied.setCount(input.getCount() + applied.getCount() - input.getMaxCount());
						input.setCount(input.getMaxCount());
					} else {
						input.setCount(input.getCount() + applied.getCount());
						applied.setCount(0);
					}
				}
				tag.put("input", input.toTag(new CompoundTag()));
				stack.setTag(tag);
				return true;
			}
			ItemStack fuel = ItemStack.EMPTY;
			if (tag.contains("fuel")) {
				fuel = ItemStack.fromTag(tag.getCompound("fuel"));
			}
			if ((!fuel.isEmpty() && fuel.isItemEqual(applied))
					|| (fuel.isEmpty() && AbstractFurnaceBlockEntity.canUseAsFuel(applied))) {
				if (fuel.isEmpty()) {
					fuel = applied.copy();
					applied.setCount(0);
				} else {
					if (fuel.getCount() + applied.getCount() > fuel.getMaxCount()) {
						applied.setCount(fuel.getCount() + applied.getCount() - fuel.getMaxCount());
						fuel.setCount(fuel.getMaxCount());
					} else {
						fuel.setCount(fuel.getCount() + applied.getCount());
						applied.setCount(0);
					}
				}
				tag.put("fuel", fuel.toTag(new CompoundTag()));
				stack.setTag(tag);
				return true;
			}
		}
		return false;
	}

	@Override
	public Optional<TooltipData> getTooltipData(ItemStack stack) {
		return Optional.of(new PocketFurnaceTooltip(stack));
	}

	protected boolean isSmeltable(World world, ItemStack itemStack) {
		return world.getRecipeManager().getFirstMatch(type, new SimpleInventory(new ItemStack[] { itemStack }), world)
				.isPresent();
	}

	class PocketFurnaceTooltip implements ConvertibleTooltipData, TooltipComponent {
		private final Identifier FURNACE = new Identifier("pockettools", "textures/gui/component/furnace.png");
		public ItemStack stack;

		public PocketFurnaceTooltip(ItemStack stack) {
			this.stack = stack;
		}

		@Override
		public TooltipComponent getComponent() {
			return this;
		}

		@Override
		public int getHeight() {
			return 40;
		}

		@Override
		public int getWidth(TextRenderer textRenderer) {
			return 66;
		}

		@Override
		public void drawItems(TextRenderer textRenderer, int x, int y, MatrixStack matrices, ItemRenderer itemRenderer, int z, TextureManager textureManager) {
			CompoundTag tag = stack.getOrCreateTag();
			ItemStack input = ItemStack.EMPTY;
			ItemStack fuel = ItemStack.EMPTY;
			ItemStack output = ItemStack.EMPTY;
			int fuelTime = 0;
			int cookTime = 0;
			int maxFuelTime = 0;
			int maxCookTime = 0;
			if (tag.contains("input")) {
				input = ItemStack.fromTag(tag.getCompound("input"));
			}
			if (tag.contains("fuel")) {
				fuel = ItemStack.fromTag(tag.getCompound("fuel"));
			}
			if (tag.contains("output")) {
				output = ItemStack.fromTag(tag.getCompound("output"));
			}
			if (tag.contains("fuelTime")) {
				fuelTime = tag.getInt("fuelTime");
			}
			if (tag.contains("cookTime")) {
				cookTime = tag.getInt("cookTime");
			}
			if (tag.contains("maxFuelTime")) {
				maxFuelTime = tag.getInt("maxFuelTime");
			}
			if (tag.contains("maxCookTime")) {
				maxCookTime = tag.getInt("maxCookTime");
			}
			itemRenderer.renderGuiItemIcon(input, x + 2, y + 2);
			itemRenderer.renderGuiItemOverlay(textRenderer, input, x + 2, y + 2);
			itemRenderer.renderGuiItemIcon(output, x + 48, y + 2);
			itemRenderer.renderGuiItemOverlay(textRenderer, output, x + 48, y + 2);
			itemRenderer.renderGuiItemIcon(fuel, x + 2, y + 20);
			itemRenderer.renderGuiItemOverlay(textRenderer, fuel, x + 2, y + 20);
			textureManager.bindTexture(FURNACE);
			if (maxFuelTime > 0) {
				int fuelProgress = fuelTime * 13 / maxFuelTime;
				DrawableHelper.drawTexture(matrices, x + 26, y + 36 - fuelProgress, z, 0, 13 - fuelProgress, 13, fuelProgress + 1, 256, 256);
			}
			if (cookTime > 0 && maxCookTime > 0) {
				int cookProgress = 22 - (cookTime * 22 / maxCookTime);
				DrawableHelper.drawTexture(matrices, x + 22, y + 3, z, 0, 13, cookProgress, 15, 256, 256);
			}
		}
	}
}
