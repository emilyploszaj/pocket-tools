package dev.emi.pockettools.particle;

import net.minecraft.client.gui.DrawContext;

public class EffectGuiParticle extends GuiParticle {
	private int x, y;
	private int lifespan = vary(600, 300);
	private float xOff = vary(0f, 0.04f);
	private float yOff = vary(-0.04f, 0.02f);
	private float r, g, b;
	private float a = vary(0.6f, 0.1f);

	public EffectGuiParticle(int x, int y, int color) {
		super();
		this.x = vary(x, 6);
		this.y = vary(y, 6);
		r = ((color >> 16) & 0xFF) / 255f;
		g = ((color >> 8) & 0xFF) / 255f;
		b = ((color >> 0) & 0xFF) / 255f;
	}

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float delta) {
		int time = time();
		int xo = x + (int) (xOff * time);
		int yo = y + (int) (yOff * time);
		int u = (time * 5 / lifespan) * 9;
		context.setShaderColor(r, g, b, a);
		context.drawTexture(TEXTURE, xo - 4, yo - 4, u, 9, 9, 9, 256, 256);
		context.setShaderColor(1, 1, 1, 1);
	}

	@Override
	public boolean isAlive() {
		return time() < lifespan;
	}
}
