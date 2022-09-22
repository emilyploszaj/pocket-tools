package dev.emi.pockettools.sound;

import dev.emi.pockettools.PocketToolsMain;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.EntityTrackingSoundInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;

import java.util.UUID;

@Environment(EnvType.CLIENT)
public class PocketRecordSoundInstance extends EntityTrackingSoundInstance {
	public static PocketRecordSoundInstance inst;
	public static UUID activeUuid;
	public static int lastCheckin;
	private final PlayerEntity e;
	
	public PocketRecordSoundInstance(SoundEvent sound, SoundCategory soundCategory, PlayerEntity entity) {
		super(sound, soundCategory, 1.f, 1.f, entity, 0);
		e = entity;
	}

	public void start() {
		MinecraftClient.getInstance().getSoundManager().play(this);
	}
	
	public void end() {
		setDone();
	}
	
	@Override
	public void tick() {
		super.tick();
		if (lastCheckin < 0) {
			end();
		}
		ItemStack stack = this.e.currentScreenHandler.getCursorStack();
		NbtCompound tag = stack.getOrCreateNbt();
		if (stack.getItem() == PocketToolsMain.POCKET_JUKEBOX && tag.contains("uuid") && tag.getUuid("uuid").equals(activeUuid)) {
			lastCheckin = 5;
		} else {
			lastCheckin--;
		}
	}
}
