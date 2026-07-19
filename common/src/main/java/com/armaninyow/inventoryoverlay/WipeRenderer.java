package com.armaninyow.inventoryoverlay;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.util.Mth;

import java.util.HashMap;
import java.util.Map;

public class WipeRenderer {

	private static final int SLOT_SIZE = 16;
	private static final long ANIMATION_DURATION_MS = 1000;

	private final Map<Integer, Long> activeAnimations = new HashMap<>();

	public void startAnimation(int slotIndex) {
		activeAnimations.put(slotIndex, System.currentTimeMillis());
	}

	public void renderWipe(GuiGraphicsExtractor context, int slotIndex, int slotX, int slotY) {
		Long startTime = activeAnimations.get(slotIndex);
		if (startTime == null) return;

		long elapsed = System.currentTimeMillis() - startTime;
		if (elapsed >= ANIMATION_DURATION_MS) {
			activeAnimations.remove(slotIndex);
			return;
		}

		float cooldown = 1.0F - (float) elapsed / (float) ANIMATION_DURATION_MS;

		int top    = slotY + Mth.floor(SLOT_SIZE * (1.0F - cooldown));
		int bottom = top   + Mth.ceil (SLOT_SIZE * cooldown);

		context.fill(RenderPipelines.GUI, slotX, top, slotX + SLOT_SIZE, bottom, Integer.MAX_VALUE);
	}
}