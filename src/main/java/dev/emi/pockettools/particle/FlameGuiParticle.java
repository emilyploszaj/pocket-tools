package dev.emi.pockettools.particle;

import net.minecraft.client.gui.DrawContext;

public class FlameGuiParticle extends GuiParticle {
	private int x, y;
	private int lifespan = vary(500, 200);

	public FlameGuiParticle(int x, int y) {
		super();
		this.x = vary(x, 6);
		this.y = vary(y + 2, 4);
	}

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float delta) {
		int time = time();
		int u = (time * 3 / lifespan) * 9;
		context.drawTexture(TEXTURE, x - 4, y - 4, u, 18, 9, 9, 256, 256);
	}

	@Override
	public boolean isAlive() {
		return time() < lifespan;
	}
}
