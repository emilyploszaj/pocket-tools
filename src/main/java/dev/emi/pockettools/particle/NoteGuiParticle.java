package dev.emi.pockettools.particle;

import dev.emi.pockettools.item.PocketNoteBlock;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.math.MathHelper;

public class NoteGuiParticle extends GuiParticle {
	private int x, y;
	private int lifespan = vary(600, 300);
	private float xOff = vary(0f, 0.01f);
	private float yOff = vary(-0.03f, 0.01f);
	private int tune;
	private float r, g, b;

	public NoteGuiParticle(int x, int y, int tune, float r, float g, float b) {
		super();
		this.x = vary(x, 4);
		this.y = vary(y, 4);
		this.tune = tune;
		this.r = r;
		this.g = g;
		this.b = b;
	}

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float delta) {
		int time = time();
		int xo = x + (int) (xOff * time);
		int yo = y + (int) (yOff * time);
		if (tune == -1) {
			context.setShaderColor(r, g, b, 1);
			context.drawTexture(TEXTURE, xo - 4, yo - 4, 0, 9 * 3, 9, 9, 256, 256);
			context.setShaderColor(1, 1, 1, 1);
		} else {
			MinecraftClient client = MinecraftClient.getInstance();
			TextRenderer textRenderer = client.textRenderer;
			String name = PocketNoteBlock.NOTE_NAMES[tune];
			int w = textRenderer.getWidth(name);
			int h = textRenderer.fontHeight;
			int c = MathHelper.packRgb(r, g, b);
			context.drawText(textRenderer, name, xo - w / 2, yo - h / 2, c, true);
		}
	}

	@Override
	public boolean isAlive() {
		return time() < lifespan;
	}
}
