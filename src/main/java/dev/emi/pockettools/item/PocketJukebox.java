package dev.emi.pockettools.item;

import dev.emi.pockettools.sound.PocketRecordSoundInstance;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.StackReference;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.MusicDiscItem;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.screen.slot.Slot;
import net.minecraft.sound.SoundCategory;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.ClickType;
import net.minecraft.util.Formatting;
import net.minecraft.world.World;

import java.util.List;
import java.util.UUID;

public class PocketJukebox extends Item {

	public PocketJukebox(Settings settings) {
		super(settings);
	}

	@Override
	public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
		NbtCompound nbt = stack.getOrCreateNbt();
		if (world.isClient()) {
			if (nbt.contains("start") && nbt.getInt("start") >= 1) {
				if (nbt.contains("disc") && nbt.contains("uuid") && entity instanceof PlayerEntity) {
					PocketRecordSoundInstance.activeUuid = nbt.getUuid("uuid");
					if (PocketRecordSoundInstance.inst != null && !PocketRecordSoundInstance.inst.isDone()) {
						PocketRecordSoundInstance.inst.end();
					}
					ItemStack d = ItemStack.fromNbt(nbt.getCompound("disc"));
					MusicDiscItem disc = (MusicDiscItem) d.getItem();
					PocketRecordSoundInstance.inst = new PocketRecordSoundInstance(disc.getSound(), SoundCategory.RECORDS, (PlayerEntity) entity);
					PocketRecordSoundInstance.inst.start();
					PocketRecordSoundInstance.lastCheckin = 5;
				}
			}
			if (nbt.contains("uuid") && nbt.getUuid("uuid").equals(PocketRecordSoundInstance.activeUuid) && !PocketRecordSoundInstance.inst.isDone()) {
				PocketRecordSoundInstance.lastCheckin = 5;
			}
		} else {
			if (nbt.contains("start") && nbt.getInt("start") > 0) {
				nbt.putInt("start", nbt.getInt("start") - 1);
			}
		}
	}

	@Override
	public boolean onClicked(ItemStack self, ItemStack stack, Slot slot, ClickType clickType, PlayerEntity player, StackReference cursor) {
		NbtCompound nbt = self.getOrCreateNbt();
		if (clickType == ClickType.RIGHT) {
			World world = player.world;
			if (nbt.contains("disc")) {
				if (stack.isEmpty()) {
					cursor.set(ItemStack.fromNbt(nbt.getCompound("disc")));
					if (world.isClient) {
						if (nbt.contains("uuid") && nbt.getUuid("uuid").equals(PocketRecordSoundInstance.activeUuid) && PocketRecordSoundInstance.inst != null && !PocketRecordSoundInstance.inst.isDone()) {
							PocketRecordSoundInstance.inst.end();
						}
					}
					nbt.remove("start");
					nbt.remove("uuid");
					nbt.remove("disc");
					return true;
				}
			} else {
				if (stack.getItem() instanceof MusicDiscItem) {
					if (!world.isClient()) {
						UUID uuid = UUID.randomUUID();
						nbt.putUuid("uuid", uuid);
						nbt.putInt("start", 2);
					}
					nbt.put("disc", stack.writeNbt(new NbtCompound()));
					stack.decrement(1);
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public void appendTooltip(ItemStack stack, World world, List<Text> tooltip, TooltipContext context) {
		NbtCompound nbt = stack.getOrCreateNbt();
		//boolean on = false;
		//if (nbt.contains("uuid") && nbt.getUuid("uuid").equals(activeUuid) && inst != null && !inst.isDone()) {
		//	on = true;
		//}
		if (nbt.contains("disc")) {
			ItemStack disc = ItemStack.fromNbt(nbt.getCompound("disc"));
			MutableText text = ((MusicDiscItem) disc.getItem()).getDescription();
			//if (on) {
			//	//text = text.formatted(Formatting.byColorIndex((int) world.getTime() / 12 % 16));
			//	text = new TranslatableText("record.nowPlaying", new Object[]{text}).formatted(Formatting.LIGHT_PURPLE);
			//} else {
				text = text.formatted(Formatting.GRAY);
			//}
			tooltip.add(text);
		}
	}
}
