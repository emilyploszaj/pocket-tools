package dev.emi.pockettools.item;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.emi.pockettools.item.PocketFurnace.PocketFurnaceTooltip;
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
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.inventory.StackReference;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.recipe.AbstractCookingRecipe;
import net.minecraft.recipe.RecipeType;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.ClickType;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

import java.util.Optional;

public class PocketFurnace<T extends AbstractCookingRecipe> extends Item {
	private RecipeType<T> type;

	public PocketFurnace(RecipeType<T> type, Settings settings) {
		super(settings);
		this.type = type;
	}

	@Override
	public boolean isItemBarVisible(ItemStack stack) {
		NbtCompound nbt = stack.getOrCreateNbt();
		if (nbt.contains("cookTime") && nbt.contains("fuelTime")) {
			int cookTime = nbt.getInt("cookTime");
			int fuelTime = nbt.getInt("fuelTime");
			return cookTime > 0 && fuelTime > 0;
		}
		return false;
	}

	@Override
	public int getItemBarStep(ItemStack stack) {
		NbtCompound nbt = stack.getOrCreateNbt();
		if (nbt.contains("cookTime") && nbt.contains("maxCookTime")) {
			int cookTime = nbt.getInt("cookTime");
			int maxCookTime = nbt.getInt("maxCookTime");
			return Math.round((maxCookTime - cookTime) / ((float) (maxCookTime)) * 13f);
		}
		return 0;
	}

	@Override
	public int getItemBarColor(ItemStack stack) {
		NbtCompound nbt = stack.getOrCreateNbt();
		if (nbt.contains("cookTime")) {
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
		if (!world.isClient()) {
			NbtCompound nbt = stack.getOrCreateNbt();
			ItemStack input = ItemStack.EMPTY;
			ItemStack fuel = ItemStack.EMPTY;
			ItemStack output = ItemStack.EMPTY;
			int fuelTime = 0;
			int cookTime = 0;
			int customModelData = 0;
			if (nbt.contains("input")) {
				input = ItemStack.fromNbt(nbt.getCompound("input"));
			}
			if (nbt.contains("fuel")) {
				fuel = ItemStack.fromNbt(nbt.getCompound("fuel"));
			}
			if (nbt.contains("output")) {
				output = ItemStack.fromNbt(nbt.getCompound("output"));
			}
			if (nbt.contains("fuelTime")) {
				fuelTime = nbt.getInt("fuelTime");
			}
			if (nbt.contains("cookTime")) {
				cookTime = nbt.getInt("cookTime");
			}
			if (nbt.contains("CustomModelData")) {
				customModelData = nbt.getInt("CustomModelData");
			}
			if (cookTime > 0) {
				if (fuelTime > 0) {
					fuelTime--;
					nbt.putInt("fuelTime", fuelTime);
				}
				if (fuelTime == 0) {
					if (fuel.getCount() > 0) {
						fuelTime = AbstractFurnaceBlockEntity.createFuelTimeMap().getOrDefault(fuel.getItem(), 0);
						nbt.putInt("fuelTime", fuelTime);
						nbt.putInt("maxFuelTime", fuelTime);
						fuel.decrement(1);
						nbt.put("fuel", fuel.writeNbt(new NbtCompound()));
					} else {
						return;
					}
				}
				cookTime--;
				if (cookTime == 0) {
					Optional<T> recipe = world.getRecipeManager().getFirstMatch(type,
							new SimpleInventory(input), world);
					if (recipe.isPresent()) {
						if (output.isEmpty()) {
							output = recipe.get().getOutput().copy();
						} else if (output.isItemEqual(recipe.get().getOutput())) {
							output.increment(1);
						}
						nbt.put("output", output.writeNbt(new NbtCompound()));
						if (output.getCount() < output.getMaxCount() && input.getCount() > 1) {
							cookTime = recipe.get().getCookTime();
							nbt.putInt("maxCookTime", cookTime);
						}
					}
					input.decrement(1);
					nbt.put("input", input.writeNbt(new NbtCompound()));
				}
				nbt.putInt("cookTime", cookTime);
			} else {
				if (fuelTime > 0) {
					fuelTime--;
					nbt.putInt("fuelTime", fuelTime);
				}
				if (output.getCount() < output.getMaxCount() && input.getCount() > 0) {
					Optional<T> recipe = world.getRecipeManager().getFirstMatch(type,
							new SimpleInventory(input), world);
					if (recipe.isPresent() && (output.isEmpty() || (output.isItemEqual(recipe.get().getOutput())
							&& output.getCount() < output.getMaxCount()))) {
						cookTime = recipe.get().getCookTime();
						nbt.putInt("cookTime", cookTime);
						nbt.putInt("maxCookTime", cookTime);
					}
				}
			}
			if ((customModelData == 0) != (fuelTime == 0)) {
				nbt.putInt("CustomModelData", 1 - customModelData);
			}
		}
	}

	@Override
	public boolean onClicked(ItemStack stack, ItemStack applied, Slot slot, ClickType clickType,
							 PlayerEntity player, StackReference cursor) {
		NbtCompound nbt = stack.getOrCreateNbt();
		if (clickType == ClickType.RIGHT) {
			if (applied.isEmpty()) {
				if (nbt.contains("output")) {
					ItemStack output = ItemStack.fromNbt(nbt.getCompound("output"));
					cursor.set(output.copy());
					nbt.remove("output");
					return true;
				}
			}
			ItemStack input = ItemStack.EMPTY;
			if (nbt.contains("input")) {
				input = ItemStack.fromNbt(nbt.getCompound("input"));
			}
			if ((!input.isEmpty() && input.isItemEqual(applied))
					|| (input.isEmpty() && isSmeltable(player.world, applied))) {
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
				nbt.put("input", input.writeNbt(new NbtCompound()));
				stack.setNbt(nbt);
				return true;
			}
			ItemStack fuel = ItemStack.EMPTY;
			if (nbt.contains("fuel")) {
				fuel = ItemStack.fromNbt(nbt.getCompound("fuel"));
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
				nbt.put("fuel", fuel.writeNbt(new NbtCompound()));
				stack.setNbt(nbt);
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
		return world.getRecipeManager().getFirstMatch(type, new SimpleInventory(itemStack), world)
				.isPresent();
	}

	static class PocketFurnaceTooltip implements ConvertibleTooltipData, TooltipComponent {
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
		public void drawItems(TextRenderer textRenderer, int x, int y, MatrixStack matrices, ItemRenderer itemRenderer, int z) {
			matrices.push();

			NbtCompound nbt = stack.getOrCreateNbt();
			ItemStack input = ItemStack.EMPTY;
			ItemStack fuel = ItemStack.EMPTY;
			ItemStack output = ItemStack.EMPTY;
			int fuelTime = 0;
			int cookTime = 0;
			int maxFuelTime = 0;
			int maxCookTime = 0;
			if (nbt.contains("input")) {
				input = ItemStack.fromNbt(nbt.getCompound("input"));
			}
			if (nbt.contains("fuel")) {
				fuel = ItemStack.fromNbt(nbt.getCompound("fuel"));
			}
			if (nbt.contains("output")) {
				output = ItemStack.fromNbt(nbt.getCompound("output"));
			}
			if (nbt.contains("fuelTime")) {
				fuelTime = nbt.getInt("fuelTime");
			}
			if (nbt.contains("cookTime")) {
				cookTime = nbt.getInt("cookTime");
			}
			if (nbt.contains("maxFuelTime")) {
				maxFuelTime = nbt.getInt("maxFuelTime");
			}
			if (nbt.contains("maxCookTime")) {
				maxCookTime = nbt.getInt("maxCookTime");
			}
			itemRenderer.renderGuiItemIcon(input, x + 2, y + 2);
			itemRenderer.renderGuiItemOverlay(textRenderer, input, x + 2, y + 2);
			itemRenderer.renderGuiItemIcon(output, x + 48, y + 2);
			itemRenderer.renderGuiItemOverlay(textRenderer, output, x + 48, y + 2);
			itemRenderer.renderGuiItemIcon(fuel, x + 2, y + 20);
			itemRenderer.renderGuiItemOverlay(textRenderer, fuel, x + 2, y + 20);
			RenderSystem.setShaderColor(1.f, 1.f, 1.f, 1.f);
			RenderSystem.setShaderTexture(0, FURNACE);
			if (maxFuelTime > 0) {
				int fuelProgress = fuelTime * 13 / maxFuelTime;
				DrawableHelper.drawTexture(matrices, x + 26, y + 36 - fuelProgress, z, 0, 13 - fuelProgress, 13, fuelProgress + 1, 256, 256);
			}
			if (cookTime > 0 && maxCookTime > 0) {
				int cookProgress = 22 - (cookTime * 22 / maxCookTime);
				DrawableHelper.drawTexture(matrices, x + 22, y + 3, z, 0, 13, cookProgress, 15, 256, 256);
			}

			matrices.pop();
		}
	}
}
