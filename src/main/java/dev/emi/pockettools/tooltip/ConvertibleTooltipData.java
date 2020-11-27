package dev.emi.pockettools.tooltip;

import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.client.item.TooltipData;

public interface ConvertibleTooltipData extends TooltipData {
	
	public TooltipComponent getComponent();
}
