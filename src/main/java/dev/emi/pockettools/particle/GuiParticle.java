package dev.emi.pockettools.particle;

import java.util.Random;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public abstract class GuiParticle {
	protected static final Random RANDOM = new Random();
	public static final Identifier TEXTURE = new Identifier("pockettools", "textures/gui/particle.png");
	protected long start = System.currentTimeMillis();

	protected int time() {
		return (int) (System.currentTimeMillis() - start);
	}

	protected static float vary(float base, float by) {
		return base - by + RANDOM.nextFloat(by * 2);
	}

	protected static int vary(int base, int by) {
		return base - by + RANDOM.nextInt(by * 2);
	}
	
	public abstract void render(DrawContext context, int mouseX, int mouseY, float delta);

	public abstract boolean isAlive();
}
