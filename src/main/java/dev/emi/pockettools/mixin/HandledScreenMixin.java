package dev.emi.pockettools.mixin;

import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.google.common.collect.Lists;

import dev.emi.pockettools.particle.GuiParticle;
import dev.emi.pockettools.particle.GuiParticleHolder;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.util.math.MatrixStack;

@Mixin(HandledScreen.class)
public class HandledScreenMixin implements GuiParticleHolder {
	@Unique
	private List<GuiParticle> guiParticles = Lists.newArrayList();
	
	@Inject(at = @At("RETURN"), method = "render")
	public void render(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo info) {
		MatrixStack matrices = context.getMatrices();
		matrices.push();
		matrices.translate(0, 0, 400);
		for (int i = 0; i < guiParticles.size(); i++) {
			GuiParticle particle = guiParticles.get(i);
			if (particle.isAlive()) {
				particle.render(context, mouseX, mouseY, delta);
			} else {
				guiParticles.remove(i);
				i--;
			}
		}
		matrices.pop();
	}

	@Override
	public void addGuiParticle(GuiParticle particle) {
		this.guiParticles.add(particle);
	}
}
