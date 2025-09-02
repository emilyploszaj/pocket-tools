package dev.emi.pockettools.particle;

import java.util.function.BiFunction;

import dev.emi.pockettools.mixin.accessor.HandledScreenAccessor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;

public interface GuiParticleHolder {
	
	void addGuiParticle(GuiParticle particle);

	public static GuiParticleHolder getCurrent() {
		MinecraftClient client = MinecraftClient.getInstance();
		if (client.currentScreen instanceof GuiParticleHolder gph) {
			return gph;
		}
		return p -> {};
	}

	public static void addHeart() {
		MinecraftClient client = MinecraftClient.getInstance();
		double x = client.mouse.getX() * client.getWindow().getScaledWidth() / client.getWindow().getWidth();
		double y = client.mouse.getY() * client.getWindow().getScaledHeight() / client.getWindow().getHeight();
		getCurrent().addGuiParticle(new HeartGuiParticle((int) x, (int) y));
	}

	public static void addSwirl(ItemStack stack, int color) {
		GuiParticleHolder.addAtSlot(stack, (x, y) -> new EffectGuiParticle(x, y, color));
	}

	public static void addFlame(ItemStack stack) {
		GuiParticleHolder.addAtSlot(stack, (x, y) -> new FlameGuiParticle(x, y));
	}

	public static void addNote(ItemStack stack, int tune, float red, float green, float blue) {
		GuiParticleHolder.addAtSlot(stack, (x, y) -> new NoteGuiParticle(x, y, tune, red, green, blue));
	}

	private static void addAtSlot(ItemStack stack, BiFunction<Integer, Integer, GuiParticle> func) {
		MinecraftClient client = MinecraftClient.getInstance();
		if (client.currentScreen instanceof HandledScreen handled) {
			for (Slot slot : handled.getScreenHandler().slots) {
				if (slot.getStack() == stack) {
					int x = slot.x + 8 + ((HandledScreenAccessor) handled).getX();
					int y = slot.y + 8 + ((HandledScreenAccessor) handled).getY();
					getCurrent().addGuiParticle(func.apply(x, y));
					return;
				}
			}
		}
	}
}
