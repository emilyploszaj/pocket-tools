package dev.emi.pockettools.item;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import dev.emi.pockettools.tooltip.ConvertibleTooltipData;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.client.item.TooltipData;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.inventory.StackReference;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.recipe.RecipeType;
import net.minecraft.recipe.StonecuttingRecipe;
import net.minecraft.screen.slot.Slot;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ClickType;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

public class PocketStonecutter extends Item {

	public PocketStonecutter(Settings settings) {
		super(settings);
	}

	@Override
	public boolean onClicked(ItemStack stack, ItemStack otherStack, Slot slot, ClickType clickType, PlayerEntity player, StackReference cursorStackReference) {
		World world = player.getWorld();
		NbtCompound nbt = stack.getOrCreateNbt();
		if (clickType == ClickType.RIGHT) {
			if (otherStack.isEmpty()) {
				if (nbt.contains("base")) {
					ItemStack base = ItemStack.fromNbt(nbt.getCompound("base"));
					List<StonecuttingRecipe> list = world.getRecipeManager().getAllMatches(RecipeType.STONECUTTING, new SimpleInventory(base), world);
					int offset = -1;
					if (nbt.contains("offset")) {
						offset = nbt.getInt("offset");
					}
					offset++;
					if (offset >= list.size()) {
						offset = 0;
					}
					nbt.putInt("offset", offset);
					if (world.isClient) {
						world.playSound(player, player.getBlockPos(), SoundEvents.UI_STONECUTTER_SELECT_RECIPE, SoundCategory.BLOCKS, 1.0F, 1.0F);
					}
					return true;
				}
			} else {
				List<StonecuttingRecipe> list = world.getRecipeManager().getAllMatches(RecipeType.STONECUTTING, new SimpleInventory(otherStack), world);
				if (!list.isEmpty()) {
					nbt.put("base", otherStack.writeNbt(new NbtCompound()));
					nbt.putInt("offset", 0);
					if (world.isClient) {
						world.playSound(player, player.getBlockPos(), SoundEvents.UI_STONECUTTER_SELECT_RECIPE, SoundCategory.BLOCKS, 1.0F, 1.0F);
					}
					return true;
				}
			}
		}
		return super.onClicked(stack, otherStack, slot, clickType, player, cursorStackReference);
	}

	@Override
	public boolean onStackClicked(ItemStack self, Slot slot, ClickType clickType, PlayerEntity player) {
		World world = player.getWorld();
		ItemStack stack = slot.getStack();
		if (clickType == ClickType.RIGHT) {
			NbtCompound tag = self.getOrCreateNbt();
			if (tag.contains("base")) {
				ItemStack base = ItemStack.fromNbt(tag.getCompound("base"));
				if (ItemStack.areItemsEqual(base, stack)) {
					List<StonecuttingRecipe> list = world.getRecipeManager().getAllMatches(RecipeType.STONECUTTING, new SimpleInventory(stack), world);
					int offset = 0;
					if (tag.contains("offset")) {
						offset = tag.getInt("offset");
					}
					if (offset < list.size()) {
						ItemStack output = list.get(offset).getOutput(world.getRegistryManager()).copy();
						int count = output.getCount() * stack.getCount();
						output.setCount(Math.min(count, output.getMaxCount()));
						count -= output.getCount();
						if (slot.canInsert(output)) {
							slot.setStack(output);
							while (count > 0) {
								output = output.copy();
								output.setCount(Math.min(count, output.getMaxCount()));
								count -= output.getCount();
								player.getInventory().offerOrDrop(output);
							}
							if (world.isClient) {
								world.playSound(player, player.getBlockPos(), SoundEvents.UI_STONECUTTER_TAKE_RESULT, SoundCategory.BLOCKS, 1.0F, 1.0F);
							}
							return true;
						}
					}
				}
			}
		}
		return super.onStackClicked(self, slot, clickType, player);
	}

	@Override
	public Optional<TooltipData> getTooltipData(ItemStack stack) {
		return Optional.of(new PocketStonecutterTooltip(stack));
	}

	class PocketStonecutterTooltip implements ConvertibleTooltipData, TooltipComponent {
		private final Identifier TOOLTIP = new Identifier("pockettools", "textures/gui/component/tooltip.png");
		public List<StonecuttingRecipe> list = new ArrayList<>();
		public ItemStack stack;

		public PocketStonecutterTooltip(ItemStack stack) {
			this.stack = stack;
			NbtCompound nbt = stack.getOrCreateNbt();
			if (nbt.contains("base")) {
				ItemStack base = ItemStack.fromNbt(nbt.getCompound("base"));
				MinecraftClient client = MinecraftClient.getInstance();
				ClientWorld world = client.world;
				list = world.getRecipeManager().getAllMatches(RecipeType.STONECUTTING, new SimpleInventory(base), world);
			}
		}

		@Override
		public TooltipComponent getComponent() {
			return this;
		}

		@Override
		public int getHeight() {
			if (!list.isEmpty()) {
				return ((list.size() - 1) / 4 + 1) * 18 + 4;
			}
			return 0;
		}

		@Override
		public int getWidth(TextRenderer textRenderer) {
			return 18 * 4 + 4;
		}

		@Override
		public void drawItems(TextRenderer textRenderer, int x, int y, DrawContext context) {
			NbtCompound nbt = stack.getOrCreateNbt();
			if (!list.isEmpty()) {
				int offset = 0;
				if (nbt.contains("offset")) {
					offset = nbt.getInt("offset");
				}

				final int maxX = x + 4 * 18;
				int sx = x;
				int sy = y;
				int i = 0;
				for (StonecuttingRecipe recipe : list) {
					ItemStack output = recipe.getOutput(MinecraftClient.getInstance().world.getRegistryManager());

					if (offset == i) {
						context.drawTexture(TOOLTIP, sx + 1, sy + 1, 26, 18, 18, 18, 256, 256);
					} else {
						context.drawTexture(TOOLTIP, sx + 1, sy + 1, 0, 0, 18, 18, 256, 256);
					}
					context.drawItem(output, sx + 2, sy + 2);
					context.drawItemInSlot(textRenderer, output, sx + 2, sy + 2);

					context.setShaderColor(1.f, 1.f, 1.f, 1.f);

					sx += 18;
					if (sx >= maxX) {
						sx = x;
						sy += 18;
					}

					i++;
				}
			}
			TooltipComponent.super.drawItems(textRenderer, x, y, context);
		}
	}
}
