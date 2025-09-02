package dev.emi.pockettools.item;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.datafixers.util.Pair;

import dev.emi.pockettools.particle.GuiParticleHolder;
import dev.emi.pockettools.tooltip.ConvertibleTooltipData;
import net.minecraft.block.FlowerBlock;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.client.item.TooltipData;
import net.minecraft.entity.Entity;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.StackReference;
import net.minecraft.item.BlockItem;
import net.minecraft.item.DyeItem;
import net.minecraft.item.FoodComponent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionUtil;
import net.minecraft.screen.slot.Slot;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ClickType;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

public class PocketCactus extends Item {
	public static final List<Tuoy> TUOYS = Lists.newArrayList();
	public static final int FRIEND_PRICKS = 1024;

	static {
		Map<Item, SoundEvent> CLICKY_TUOYS = Maps.newHashMap();
		CLICKY_TUOYS.put(Items.LEVER, SoundEvents.BLOCK_LEVER_CLICK);
		CLICKY_TUOYS.put(Items.STONE_BUTTON, SoundEvents.BLOCK_STONE_BUTTON_CLICK_ON);
		CLICKY_TUOYS.put(Items.SPYGLASS, SoundEvents.ITEM_SPYGLASS_USE);
		CLICKY_TUOYS.put(Items.CHAIN, SoundEvents.BLOCK_CHAIN_PLACE);
		TUOYS.add(new Tuoy(s -> CLICKY_TUOYS.containsKey(s.getItem()), 49, (player, cactus, ref) -> {
			player.getWorld().playSound(null, player.getBlockPos(), CLICKY_TUOYS.get(ref.get().getItem()), SoundCategory.PLAYERS, 0.6f, 1f + player.getRandom().nextFloat() * 0.2f);
		}));
		TUOYS.add(new Tuoy(s -> s.getItem() == Items.FLINT_AND_STEEL, 33, (player, cactus, ref) -> {
			ref.get().damage(1, player.getRandom(), player);
			player.getWorld().playSound(null, player.getBlockPos(), SoundEvents.ITEM_FLINTANDSTEEL_USE, SoundCategory.PLAYERS, 0.6f, 1f + player.getRandom().nextFloat() * 0.2f);
		}));
		TUOYS.add(new Tuoy(s -> s.getItem() == Items.SHEARS, 33, (player, cactus, ref) -> {
			ref.get().damage(1, player.getRandom(), player);
			player.getWorld().playSound(null, player.getBlockPos(), SoundEvents.ENTITY_SHEEP_SHEAR, SoundCategory.PLAYERS, 0.6f, 1f + player.getRandom().nextFloat() * 0.2f);
		}));
		TUOYS.add(new Tuoy(s -> s.getItem() == Items.MILK_BUCKET, 7, (player, cactus, ref) -> {
			if (!player.getStatusEffects().stream().anyMatch(i -> i.getEffectType().getCategory() == StatusEffectCategory.HARMFUL)) {
				return;
			}
			player.clearStatusEffects();
			ref.set(ItemStack.EMPTY);
			player.getWorld().playSound(null, player.getBlockPos(), SoundEvents.ENTITY_GENERIC_DRINK, SoundCategory.PLAYERS, 0.6f, 1.1f + player.getRandom().nextFloat() * 0.2f);
		}));
		TUOYS.add(new Tuoy(s -> s.getItem().isFood() || (s.getItem() instanceof BlockItem bi && bi.getBlock() instanceof FlowerBlock), 7, (player, cactus, ref) -> {
			FoodComponent food = ref.get().getItem().getFoodComponent();
			if (food != null) {
				if (!player.getHungerManager().isNotFull()) {
					if (food.getStatusEffects().isEmpty()) {
						if (player.getRandom().nextInt(7) != 0) {
							return;
						}
					} else {
						if (player.getRandom().nextInt(100) != 0) {
							return;
						}
					}
				}
				List<StatusEffectInstance> effects = Lists.newArrayList();
				for (Pair<StatusEffectInstance, Float> pair : food.getStatusEffects()) {
					if (pair.getSecond() == 1 || player.getRandom().nextFloat() <= pair.getSecond()) {
						effects.add(pair.getFirst());
					}
				}
				player.getHungerManager().add(food.getHunger() / 2, food.getSaturationModifier() * 0.8f);
				if (effects.size() > 0) {
					applyEffects(player, cactus, effects);
				}
				if (ref.get().getItem() == Items.HONEY_BOTTLE) {
					player.removeStatusEffect(StatusEffects.POISON);
				}
			}
			ref.get().decrement(1);
			player.getWorld().playSound(null, player.getBlockPos(), SoundEvents.ENTITY_PLAYER_BURP, SoundCategory.PLAYERS, 0.6f, 1.1f + player.getRandom().nextFloat() * 0.2f);
		}));
		TUOYS.add(new Tuoy(s -> s.getItem() == Items.POTION, 7, (player, cactus, ref) -> {
			Potion potion = PotionUtil.getPotion(ref.get());
			List<StatusEffectInstance> effects = potion.getEffects();
			boolean drinkable = false;
			for (StatusEffectInstance inst : effects) {
				if (!player.hasStatusEffect(inst.getEffectType())) {
					drinkable = true;
					break;
				}
			}
			if (!drinkable && effects.isEmpty() && player.getRandom().nextInt(4) == 0) {
				drinkable = true;
			}
			if (drinkable) {
				applyEffects(player, cactus, effects);
				player.getWorld().playSound(null, player.getBlockPos(), SoundEvents.ENTITY_GENERIC_DRINK, SoundCategory.PLAYERS, 0.6f, 1.1f + player.getRandom().nextFloat() * 0.2f);
				ref.set(ItemStack.EMPTY);
			}
		}));
	}

	public PocketCactus(Settings settings) {
		super(settings);
	}

	public static void applyEffects(ServerPlayerEntity player, ItemStack cactus, List<StatusEffectInstance> effects) {
		State state = new State(cactus);
		state.effectColor(PotionUtil.getColor(effects));
		int dur = 0;
		for (StatusEffectInstance inst : effects) {
			StatusEffectInstance scaled = new StatusEffectInstance(inst.getEffectType(), inst.getDuration() / 2, inst.getAmplifier());
			scaled.mapDuration(i -> i / 4);

			player.addStatusEffect(scaled);
			dur += scaled.getDuration();
		}
		if (dur > 0) {
			dur /= effects.size();
			dur /= 2;
		}
		state.effectDuration(dur);
	}

	@Override
	public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
		super.inventoryTick(stack, world, entity, slot, selected);
		State state = new State(stack);
		if (state.effectDuration() > 0) {
			state.effectDuration(state.effectDuration() - 1);
			if (world.random.nextInt(14) == 0) {
				GuiParticleHolder.addSwirl(stack, state.color());
			}
		}
		if (state.cooldown() > 0) {
			state.cooldown(state.cooldown() - 1);
		}
		if (state.friend() && !world.isClient && entity instanceof ServerPlayerEntity player) {
			ItemStack held = state.held();
			if (!held.isEmpty() && world.getRandom().nextInt(20) == 0) {
				for (Tuoy tuoy : TUOYS) {
					if (tuoy.validator().test(held)) {
						if (world.getRandom().nextInt(tuoy.chance) == 0) {
							StackReference ref = new StackReference() {
								private ItemStack s;

								public ItemStack get() {
									return s;
								}

								public boolean set(ItemStack s) {
									this.s = s;
									return true;
								}
							};
							ref.set(held);
							tuoy.callback.run(player, stack, ref);
							state.held(ref.get());
						}
					}
				}
			}
		}
	}

	@Override
	public boolean onClicked(ItemStack self, ItemStack stack, Slot slot, ClickType clickType, PlayerEntity player, StackReference cursor) {
		State state = new State(self);
		if (clickType == ClickType.RIGHT) {
			if (stack.isEmpty()) {
				if (player.getWorld().isClient) {
					if (state.friend() || (state.pricks() > FRIEND_PRICKS / 3 && player.getRandom().nextInt(16) == 0)) {
						GuiParticleHolder.addHeart();
					}
				}
				if (!state.friend()) {
					if (player.timeUntilRegen < 10f) {
						state.pricks(state.pricks() + 1);
						if (state.cooldown() == 0) {
							state.cooldown(40);
							state.pricks(state.pricks() + 16);
						}
					}
					if (state.pricks() > FRIEND_PRICKS) {
						state.color(DyeColor.RED.getId());
						state.friend(true);
					}
				} else {
					if (!state.held().isEmpty()) {
						cursor.set(state.held());
						state.held(ItemStack.EMPTY);
					}
				}
				player.damage(player.getDamageSources().cactus(), 1f);
			} else {
				if (state.friend()) {
					ItemStack is = cursor.get();
					if (is.getItem() instanceof DyeItem dye) {
						state.color(dye.getColor().getId());
					} else {
						state.held(is);
					}
				}
				cursor.set(ItemStack.EMPTY);
			}
			return true;
		}
		return false;
	}

	@Override
	public boolean onStackClicked(ItemStack self, Slot slot, ClickType clickType, PlayerEntity player) {
		ItemStack stack = slot.getStack();
		if (clickType == ClickType.RIGHT) {
			if (!stack.isEmpty()) {
				stack.setCount(0);
			}
			return true;
		}
		return false;
	}

	@Override
	public Optional<TooltipData> getTooltipData(ItemStack stack) {
		if (new State(stack).friend()) {
			return Optional.of(new PocketCactusTooltip(stack));
		}
		return super.getTooltipData(stack);
	}

	public static class State {
		private NbtCompound tag;

		public State(ItemStack stack) {
			this.tag = stack.getOrCreateNbt();
		}

		public int pricks() {
			return tag.getInt("pricks");
		}

		public void pricks(int pricks) {
			tag.putInt("pricks", pricks);
		}

		public int cooldown() {
			return tag.getInt("cooldown");
		}

		public void cooldown(int cooldown) {
			tag.putInt("cooldown", cooldown);
		}

		public boolean friend() {
			return tag.getBoolean("friend");
		}

		public void friend(boolean friend) {
			tag.putBoolean("friend", friend);
		}

		public ItemStack held() {
			if (tag.contains("held")) {
				return ItemStack.fromNbt(tag.getCompound("held"));
			}
			return ItemStack.EMPTY;
		}

		public void held(ItemStack held) {
			tag.put("held", held.writeNbt(new NbtCompound()));
		}

		public int effectColor() {
			return tag.getInt("effectColor");
		}

		public void effectColor(int effectColor) {
			tag.putInt("effectColor", effectColor);
		}

		public int effectDuration() {
			return tag.getInt("effectDuration");
		}

		public void effectDuration(int effectDuration) {
			tag.putInt("effectDuration", effectDuration);
		}

		public int color() {
			return tag.getInt("color");
		}

		public void color(int color) {
			tag.putInt("color", color);
		}
	}

	static class PocketCactusTooltip implements ConvertibleTooltipData, TooltipComponent {
		private final Identifier TOOLTIP = new Identifier("pockettools", "textures/gui/component/tooltip.png");
		public ItemStack stack;

		public PocketCactusTooltip(ItemStack stack) {
			this.stack = stack;
		}

		@Override
		public TooltipComponent getComponent() {
			return this;
		}

		@Override
		public int getHeight() {
			return 19;
		}

		@Override
		public int getWidth(TextRenderer textRenderer) {
			return 18;
		}

		@Override
		public void drawItems(TextRenderer textRenderer, int x, int y, DrawContext context) {
			context.setShaderColor(1.f, 1.f, 1.f, 1.f);
			State state = new State(stack);
			context.drawTexture(TOOLTIP, x, y, 0, 0, 18, 18, 256, 256);
			renderGuiItem(context, textRenderer, state.held(), x + 1, y + 1);
		}

		private void renderGuiItem(DrawContext context, TextRenderer textRenderer, ItemStack stack, int x, int y) {
			context.drawItem(stack, x, y);
			context.drawItemInSlot(textRenderer, stack, x, y);
		}
	}

	public static record Tuoy(Predicate<ItemStack> validator, int chance, Callback callback) {

		public static interface Callback {
			void run(ServerPlayerEntity player, ItemStack cactus, StackReference stack);
		}
	}
}
