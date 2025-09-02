package dev.emi.pockettools.particle;

import net.minecraft.client.gui.DrawContext;

public class HeartGuiParticle extends GuiParticle {
	private int x, y;
	private int lifespan = vary(2_500, 500);
	private float horizontalRate = vary(0.0017f, 0.0005f);
	private float verticalRate = vary(0.014f, 0.005f);
	private float width = vary(12f, 4f);
	private int offset = RANDOM.nextInt(1_000_000);

	public HeartGuiParticle(int x, int y) {
		super();
		this.x = vary(x, 4);
		this.y = vary(y, 4);
	}

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float delta) {
		int time = time();
		int yo = y - (int) (verticalRate * time);
		int xo = x + (int) (Math.sin((time + offset) * horizontalRate) * width - Math.sin((offset) * horizontalRate) * width);
		int u = time() > (lifespan - 100) ? 9 : 0;
		context.drawTexture(TEXTURE, xo - 4, yo - 4, u, 0, 9, 9, 256, 256);
	}

	@Override
	public boolean isAlive() {
		return time() < lifespan;
	}
}
