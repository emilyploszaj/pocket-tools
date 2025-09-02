package dev.emi.pockettools.item;

import dev.emi.pockettools.particle.GuiParticleHolder;
import dev.emi.pockettools.tooltip.ConvertibleTooltipData;
import net.minecraft.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.client.item.TooltipData;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.inventory.StackReference;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
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
		NbtCompound nbt = stack.getOrCreateNbt();
		if (world.isClient) {
			if (nbt.getInt("fuelTime") > 0 && world.getRandom().nextInt(33) == 0) {
				GuiParticleHolder.addFlame(stack);
			}
		}
		if (!world.isClient()) {
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
			if (fuelTime > 0) {
				fuelTime--;
				nbt.putInt("fuelTime", fuelTime);
				if (world.isClient && world.getRandom().nextInt(33) == 0) {
					System.out.println("e");
					GuiParticleHolder.addFlame(stack);
				}
			}
			if (cookTime > 0) {
				if (input.isEmpty()) {
					nbt.putInt("cookTime", 0);
					return;
				}
				if (fuelTime == 0) {
					if (fuel.getCount() > 0) {
						fuelTime = AbstractFurnaceBlockEntity.createFuelTimeMap().getOrDefault(fuel.getItem(), 0);
						nbt.putInt("fuelTime", fuelTime);
						nbt.putInt("maxFuelTime", fuelTime);
						if (fuel.getItem() == Items.LAVA_BUCKET) {
							fuel = Items.BUCKET.getDefaultStack();
						} else {
							fuel.decrement(1);
						}
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
						ItemStack recipeOutput = recipe.get().getOutput(world.getRegistryManager());

						if (output.isEmpty()) {
							output = recipeOutput.copy();
						} else if (ItemStack.areItemsEqual(output, recipeOutput)) {
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
				if (output.getCount() < output.getMaxCount() && input.getCount() > 0) {
					Optional<T> recipe = world.getRecipeManager().getFirstMatch(type,
							new SimpleInventory(input), world);
					if (recipe.isPresent() && (output.isEmpty() || (ItemStack.areItemsEqual(output, recipe.get().getOutput(world.getRegistryManager()))
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
					if (!output.isEmpty()) {
						cursor.set(output.copy());
						nbt.remove("output");
						return true;
					}
				}
				if (nbt.contains("input")) {
					ItemStack input = ItemStack.fromNbt(nbt.getCompound("input"));
					if (!input.isEmpty()) {
						cursor.set(input.copy());
						nbt.remove("input");
						return true;
					}
				}
				if (nbt.contains("fuel")) {
					ItemStack fuel = ItemStack.fromNbt(nbt.getCompound("fuel"));
					if (!fuel.isEmpty()) {
						cursor.set(fuel.copy());
						nbt.remove("fuel");
						return true;
					}
				}
			}
			ItemStack input = ItemStack.EMPTY;
			if (nbt.contains("input")) {
				input = ItemStack.fromNbt(nbt.getCompound("input"));
			}
			if ((!input.isEmpty() && ItemStack.areItemsEqual(input, applied))
					|| (input.isEmpty() && isSmeltable(player.getWorld(), applied))) {
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
			if ((!fuel.isEmpty() && ItemStack.areItemsEqual(fuel, applied))
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
		private final Identifier TOOLTIP = new Identifier("pockettools", "textures/gui/component/tooltip.png");
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
			return 73;
		}

		@Override
		public void drawItems(TextRenderer textRenderer, int x, int y, DrawContext context) {
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
			context.setShaderColor(1.f, 1.f, 1.f, 1.f);
			context.drawTexture(TOOLTIP, x + 1, y, 0, 0, 18, 18, 256, 256); // Input
			context.drawTexture(TOOLTIP, x + 47, y, 0, 18, 26, 26, 256, 256); // Output
			context.drawTexture(TOOLTIP, x + 1, y + 19, 18, 0, 18, 18, 256, 256); // Fuel

			context.drawTexture(TOOLTIP, x + 22, y + 3, 0, 44, 22, 15, 256, 256); // Arrow
			if (maxFuelTime > 0) {
				int fuelProgress = fuelTime * 13 / maxFuelTime;
				context.drawTexture(FURNACE, x + 26, y + 36 - fuelProgress, 0, 13 - fuelProgress, 13, fuelProgress + 1, 256, 256);
			}
			if (cookTime > 0 && maxCookTime > 0) {
				int cookProgress = 22 - (cookTime * 22 / maxCookTime);
				context.drawTexture(FURNACE, x + 22, y + 3, 0, 13, cookProgress, 15, 256, 256);
			}
			this.renderGuiItem(context, textRenderer, input, x + 2, y + 1);
			this.renderGuiItem(context, textRenderer, output, x + 52, y + 5);
			this.renderGuiItem(context, textRenderer, fuel, x + 2, y + 20);
		}

		private void renderGuiItem(DrawContext context, TextRenderer textRenderer, ItemStack stack, int x, int y) {
			context.drawItem(stack, x, y);
			context.drawItemInSlot(textRenderer, stack, x, y);
		}
	}
}
