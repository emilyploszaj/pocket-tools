package dev.emi.pockettools.item;

import java.util.List;

import dev.emi.pockettools.particle.GuiParticleHolder;
import net.minecraft.block.enums.Instrument;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.StackReference;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.screen.slot.Slot;
import net.minecraft.sound.SoundCategory;
import net.minecraft.text.LiteralTextContent;
import net.minecraft.text.MutableText;
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
	public boolean onClicked(ItemStack self, ItemStack stack, Slot slot, ClickType clickType, PlayerEntity player, StackReference cursor) {
		NbtCompound nbt = self.getOrCreateNbt();
		if (clickType == ClickType.RIGHT) {
			if (stack.isEmpty()) {
				playNote(player, self, false);
				return true;
			} else if (stack.getItem() instanceof BlockItem) {
				if (nbt.contains("instrument")) {
					Instrument instrument = getInstrument(self);
					Instrument newInstrument = ((BlockItem) stack.getItem()).getBlock().getDefaultState().getInstrument();
					if (!instrument.equals(newInstrument)) {
						setInstrument(self, stack);
					} else {
						pitchUp(self);
					}
				} else {
					setInstrument(self, stack);
				}
				playNote(player, self, true);
				return true;
			}
		}
		return false;
	}

	public void playNote(PlayerEntity player, ItemStack stack, boolean tune) {
		NbtCompound tag = stack.getOrCreateNbt();
		Instrument instrument = getInstrument(stack);
		int pitch = 0;
		if (tag.contains("pitch")) {
			pitch = tag.getInt("pitch");
		}
		if (player.getWorld().isClient) {
			float f = (float) Math.pow(2.0D, (pitch - 12) / 12.0D);
			float red = Math.max(0.0F, MathHelper.sin((f + 0.0F) * 6.2831855F) * 0.65F + 0.35F);
			float green = Math.max(0.0F, MathHelper.sin((f + 0.33333334F) * 6.2831855F) * 0.65F + 0.35F);
			float blue = Math.max(0.0F, MathHelper.sin((f + 0.6666667F) * 6.2831855F) * 0.65F + 0.35F);
			GuiParticleHolder.addNote(stack, tune ? pitch : -1, red, green, blue);
		}
		float f = (float) Math.pow(2.0D, (pitch - 12) / 12.0D);
		player.playSound(instrument.getSound().value(), SoundCategory.RECORDS, 3.0F, f);
	}

	public Instrument getInstrument(ItemStack stack) {
		var nbt = stack.getOrCreateNbt();
		Instrument instrument = Instrument.HARP;
		if (nbt.contains("instrument")) {
			var ins = ItemStack.fromNbt(nbt.getCompound("instrument"));
			if (ins.getItem() instanceof BlockItem blockItem)
				instrument = blockItem.getBlock().getDefaultState().getInstrument();
		}
		return instrument;
	}

	public void setInstrument(ItemStack self, ItemStack instrument) {
		NbtCompound nbt = self.getOrCreateNbt();
		nbt.put("instrument", instrument.writeNbt(new NbtCompound()));
		nbt.putInt("pitch", 0);
	}

	public void pitchUp(ItemStack stack) {
		NbtCompound nbt = stack.getOrCreateNbt();
		int pitch = -1;
		if (nbt.contains("pitch")) {
			pitch = nbt.getInt("pitch");
		}
		pitch++;
		if (pitch >= 24) pitch = 0;
		nbt.putInt("pitch", pitch);
	}

	@Override
	public void appendTooltip(ItemStack stack, World world, List<Text> tooltip, TooltipContext context) {
		super.appendTooltip(stack, world, tooltip, context);
		NbtCompound nbt = stack.getOrCreateNbt();
		if (nbt.contains("pitch")) {
			int pitch = nbt.getInt("pitch");
			float f = (float) Math.pow(2.0D, (pitch - 12) / 12.0D);
			float red = Math.max(0.0F, MathHelper.sin((f + 0.0F) * 6.2831855F) * 0.65F + 0.35F);
			float green = Math.max(0.0F, MathHelper.sin((f + 0.33333334F) * 6.2831855F) * 0.65F + 0.35F);
			float blue = Math.max(0.0F, MathHelper.sin((f + 0.6666667F) * 6.2831855F) * 0.65F + 0.35F);
			tooltip.add(MutableText.of(new LiteralTextContent(NOTE_NAMES[pitch])).setStyle(Style.EMPTY.withColor(TextColor.fromRgb(MathHelper.packRgb(red, green, blue)))));
		}
	}
}
