package dev.emi.pockettools.item;

import java.util.List;

import net.minecraft.block.enums.Instrument;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.screen.slot.Slot;
import net.minecraft.sound.SoundCategory;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.minecraft.util.ClickType;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

public class PocketNoteBlock extends Item {
	public static final String[] NOTE_NAMES = {
		"F#", "G", "G#", "A", "A#", "B", "C", "C#", "D", "D#", "E", "F",
		"F#", "G", "G#", "A", "A#", "B", "C", "C#", "D", "D#", "E", "F"
	};

	public PocketNoteBlock(Settings settings) {
		super(settings);
	}
	
	@Override
	public boolean onClicked(ItemStack self, ItemStack stack, Slot slot, ClickType clickType, PlayerInventory playerInventory) {
		CompoundTag tag = self.getOrCreateTag();
		if (clickType == ClickType.RIGHT) {
			if (stack.isEmpty()) {
				playNote(playerInventory.player, self);
				return true;
			} else if (stack.getItem() instanceof BlockItem) {
				if (tag.contains("instrument")) {
					Instrument instrument = getInstrument(self);
					Instrument newInstrument = Instrument.fromBlockState(((BlockItem) stack.getItem()).getBlock().getDefaultState());
					if (!instrument.equals(newInstrument)) {
						setInstrument(self, stack);
					} else {
						pitchUp(self);
					}
				} else {
					setInstrument(self, stack);
				}
				playNote(playerInventory.player, self);
				return true;
			}
		}
		return false;
	}

	public void playNote(PlayerEntity player, ItemStack stack) {
		CompoundTag tag = stack.getOrCreateTag();
		Instrument instrument = getInstrument(stack);
		int pitch = 0;
		if (tag.contains("pitch")) {
			pitch = tag.getInt("pitch");
		}
		float f = (float) Math.pow(2.0D, (pitch - 12) / 12.0D);
		player.world.playSound(player, player.getBlockPos(), instrument.getSound(), SoundCategory.RECORDS, 3.0F, f);
	}

	public Instrument getInstrument(ItemStack stack) {
		CompoundTag tag = stack.getOrCreateTag();
		Instrument instrument = Instrument.HARP;
		if (tag.contains("instrument")) {
			ItemStack ins = ItemStack.fromTag(tag.getCompound("instrument"));
			if (ins.getItem() instanceof BlockItem)
			instrument = Instrument.fromBlockState(((BlockItem) ins.getItem()).getBlock().getDefaultState());
		}
		return instrument;
	}
	
	public void setInstrument(ItemStack self, ItemStack instrument) {
		CompoundTag tag = self.getOrCreateTag();
		tag.put("instrument", instrument.toTag(new CompoundTag()));
		tag.putInt("pitch", 0);
	}
	
	public void pitchUp(ItemStack stack) {
		CompoundTag tag = stack.getOrCreateTag();
		int pitch = -1;
		if (tag.contains("pitch")) {
			pitch = tag.getInt("pitch");
		}
		pitch++;
		if (pitch >= 24) pitch = 0;
		tag.putInt("pitch", pitch);
	}

	@Override
	public void appendTooltip(ItemStack stack, World world, List<Text> tooltip, TooltipContext context) {
		super.appendTooltip(stack, world, tooltip, context);
		CompoundTag tag = stack.getOrCreateTag();
		if (tag.contains("pitch")) {
			int pitch = tag.getInt("pitch");
			float f = (float) Math.pow(2.0D, (pitch - 12) / 12.0D);
			float red = Math.max(0.0F, MathHelper.sin((f + 0.0F) * 6.2831855F) * 0.65F + 0.35F);
			float green = Math.max(0.0F, MathHelper.sin((f + 0.33333334F) * 6.2831855F) * 0.65F + 0.35F);
			float blue = Math.max(0.0F, MathHelper.sin((f + 0.6666667F) * 6.2831855F) * 0.65F + 0.35F);
			tooltip.add(new LiteralText(NOTE_NAMES[pitch]).setStyle(Style.EMPTY.withColor(TextColor.fromRgb(MathHelper.packRgb(red, green, blue)))));
		}
	}
}
