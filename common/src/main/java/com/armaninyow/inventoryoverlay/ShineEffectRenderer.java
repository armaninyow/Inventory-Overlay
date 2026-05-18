package com.armaninyow.inventoryoverlay;

import net.minecraft.client.gui.GuiGraphicsExtractor;

import java.util.HashMap;
import java.util.Map;

public class ShineEffectRenderer {

	private static final int SLOT_SIZE = 16;
	private static final int ANIMATION_DURATION_MS = 1000; // 1 second
	private static final int LINE_WIDTH = 4;

	// Track shine animations: slot index -> start time in milliseconds
	private final Map<Integer, Long> activeShines = new HashMap<>();

	public void startShineAnimation(int slotIndex) {
		long currentTime = System.currentTimeMillis();
		activeShines.put(slotIndex, currentTime);
	}

	public void renderShine(GuiGraphicsExtractor context, int slotIndex, int slotX, int slotY) {
		if (!activeShines.containsKey(slotIndex)) return;

		long startTime = activeShines.get(slotIndex);
		long currentTime = System.currentTimeMillis();
		long elapsed = currentTime - startTime;

		if (elapsed > ANIMATION_DURATION_MS) return;

		// Calculate progress (0.0 to 1.0)
		float progress = (float) elapsed / (float) ANIMATION_DURATION_MS;

		// Bright white color
		int shineColor = 0xFFFFFFFF;

		// The line travels diagonally across the slot
		float startPos = -LINE_WIDTH;
		float endPos = (SLOT_SIZE * 2) + LINE_WIDTH;
		float totalDistance = endPos - startPos;
		float currentPosition = startPos + (progress * totalDistance);

		// Draw the diagonal line
		for (int y = 0; y < SLOT_SIZE; y++) {
			for (int x = 0; x < SLOT_SIZE; x++) {
				float diagonalPos = x + y;

				if (Math.abs(diagonalPos - currentPosition) < LINE_WIDTH / 2.0f) {
					context.fill(slotX + x, slotY + y, slotX + x + 1, slotY + y + 1, shineColor);
				}
			}
		}
	}

	public void cleanupCompletedAnimations() {
		long currentTime = System.currentTimeMillis();
		activeShines.entrySet().removeIf(entry ->
			currentTime - entry.getValue() > ANIMATION_DURATION_MS
		);
	}
}