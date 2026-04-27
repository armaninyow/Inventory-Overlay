package com.armaninyow.inventoryoverlay;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;
import me.shedaniel.autoconfig.AutoConfig;
import com.mojang.blaze3d.systems.RenderSystem;

import java.util.HashMap;
import java.util.Map;

// 1.21_1.21.1
public class OverlayRenderer implements ClientModInitializer {

	private static final Identifier CONTAINER_TEXTURE = Identifier.of(InventoryOverlay.MOD_ID, "textures/gui/inventory_overlay.png");

	private static final int TEX_WIDTH = 180;
	private static final int TEX_HEIGHT = 60;
	private static final int SLOT_SIZE = 16;
	private static final int GAP = 4;
	private static final int SLOT_SPACING = SLOT_SIZE + GAP;

	private static KeyBinding toggleKey;

	// Track item counts per slot to detect new items
	private final Map<Integer, Integer> previousSlotCounts = new HashMap<>();

	// Shine effect renderer
	private final ShineEffectRenderer shineRenderer = new ShineEffectRenderer();

	@Override
	public void onInitializeClient() {
		OverlayConfig.register();

		// Register keybinding with string category (1.21 / 1.21.1 API)
		toggleKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
				"key.inventoryoverlay.toggle",
				InputUtil.Type.KEYSYM,
				GLFW.GLFW_KEY_I,
				"key.category.inventoryoverlay.inventory_overlay"
		));

		// Handle key press
		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			while (toggleKey.wasPressed()) {
				OverlayConfig config = OverlayConfig.get();
				config.overlayVisible = !config.overlayVisible;
				AutoConfig.getConfigHolder(OverlayConfig.class).save();
			}

			// Check for new items
			checkForNewItems(client);
		});

		HudRenderCallback.EVENT.register((drawContext, renderTickCounter) -> {
			renderOverlay(drawContext);
		});
	}

	private void checkForNewItems(MinecraftClient client) {
		ClientPlayerEntity player = client.player;
		if (player == null) return;

		// Check slots 9-35 (inventory rows, not hotbar)
		for (int slotIndex = 9; slotIndex < 36; slotIndex++) {
			ItemStack stack = player.getInventory().getStack(slotIndex);
			int currentCount = stack.isEmpty() ? 0 : stack.getCount();
			int previousCount = previousSlotCounts.getOrDefault(slotIndex, 0);

			// New item detected: slot was empty and now has items
			if (previousCount == 0 && currentCount > 0) {
				OverlayConfig config = OverlayConfig.get();
				if (config.shineEffectEnabled) {
					shineRenderer.startShineAnimation(slotIndex);
				}
			}

			// Update tracking
			previousSlotCounts.put(slotIndex, currentCount);
		}
	}

	private void renderOverlay(DrawContext context) {
		MinecraftClient client = MinecraftClient.getInstance();
		ClientPlayerEntity player = client.player;

		if (player == null || client.options.hudHidden || player.isSpectator()) return;

		OverlayConfig config = OverlayConfig.get();

		if (!config.overlayVisible) return;

		int screenWidth = client.getWindow().getScaledWidth();
		int screenHeight = client.getWindow().getScaledHeight();

		int x = calculateX(screenWidth, config);
		int y = calculateY(screenHeight, config);

		int alphaInt = (int) (config.containerAlphaPercent * 2.55f);

		if (alphaInt > 0) {
			// 1.21 / 1.21.1: must enable blending via RenderSystem before drawing with alpha
			float alpha = config.containerAlphaPercent / 100.0f;
			RenderSystem.enableBlend();
			RenderSystem.defaultBlendFunc();
			context.setShaderColor(1.0f, 1.0f, 1.0f, alpha);
			context.drawTexture(
				CONTAINER_TEXTURE,
				x, y,
				0.0f, 0.0f,
				TEX_WIDTH, TEX_HEIGHT,
				TEX_WIDTH, TEX_HEIGHT
			);
			context.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f); // reset
			RenderSystem.disableBlend();
		}

		// Render Items
		int startX = x + 2;
		int startY = y + 2;

		for (int row = 0; row < 3; row++) {
			for (int col = 0; col < 9; col++) {
				int slotIndex = 9 + (row * 9) + col;
				ItemStack stack = player.getInventory().getStack(slotIndex);

				if (!stack.isEmpty()) {
					int itemX = startX + (col * SLOT_SPACING);
					int itemY = startY + (row * SLOT_SPACING);

					context.drawItem(stack, itemX, itemY);
					// 1.21 / 1.21.1: method is drawItemInSlot
					context.drawItemInSlot(client.textRenderer, stack, itemX, itemY);
				}
			}
		}

		// Render shine effects on top using an elevated Z to clear item/count depth
		MatrixStack matrices = context.getMatrices();
		matrices.push();
		matrices.translate(0, 0, 300);
		for (int row = 0; row < 3; row++) {
			for (int col = 0; col < 9; col++) {
				int slotIndex = 9 + (row * 9) + col;
				int itemX = startX + (col * SLOT_SPACING);
				int itemY = startY + (row * SLOT_SPACING);

				shineRenderer.renderShine(context, slotIndex, itemX, itemY);
			}
		}
		matrices.pop();

		// Clean up completed animations
		shineRenderer.cleanupCompletedAnimations();
	}

	private int calculateX(int screenWidth, OverlayConfig config) {
		int base = switch (config.horizontalAnchor) {
			case LEFT -> 0;
			case CENTER -> (screenWidth / 2) - (TEX_WIDTH / 2);
			case RIGHT -> screenWidth - TEX_WIDTH;
		};
		return base + config.xOffset;
	}

	private int calculateY(int screenHeight, OverlayConfig config) {
		int base = switch (config.verticalAnchor) {
			case TOP -> 0;
			case CENTER -> (screenHeight / 2) - (TEX_HEIGHT / 2);
			case BOTTOM -> screenHeight - TEX_HEIGHT;
		};
		return base + config.yOffset;
	}
}
