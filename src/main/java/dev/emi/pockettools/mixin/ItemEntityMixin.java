package dev.emi.pockettools.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import dev.emi.pockettools.PocketToolsMain;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;

@Mixin(ItemEntity.class)
public abstract class ItemEntityMixin {

	@Shadow
	public abstract void setDespawnImmediately();
	@Shadow
	public abstract void setStack(ItemStack stack);


	
	@Inject(at = @At("HEAD"), method = "setStack")
	public void setStack(ItemStack stack, CallbackInfo info) {
		if (stack.getItem() == PocketToolsMain.POCKET_END_PORTAL) {
			if (stack.hasTag()) {
				CompoundTag tag = stack.getTag();
				if (tag.contains("portal") && tag.getBoolean("portal")) {
					setDespawnImmediately();
				} else if (tag.contains("filled") && tag.getBoolean("filled")) {
					stack.setTag(new CompoundTag());
				}
			}
		}
	}
}
