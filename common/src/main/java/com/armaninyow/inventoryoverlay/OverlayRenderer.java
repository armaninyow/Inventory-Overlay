package com.armaninyow.inventoryoverlay;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.item.ItemStack;
import com.mojang.blaze3d.platform.InputConstants;
import org.lwjgl.glfw.GLFW;


import java.util.HashMap;
import java.util.Map;

public class OverlayRenderer implements ClientModInitializer {

	private static final Identifier OVERLAY_ID = Identifier.fromNamespaceAndPath(InventoryOverlay.MOD_ID, "inventory_overlay");
	private static final Identifier RELOAD_LISTENER_ID = Identifier.fromNamespaceAndPath(InventoryOverlay.MOD_ID, "hotbar_texture_reload");

	private static final int TEX_WIDTH = HotbarOverlayTexture.TEX_WIDTH;
	private static final int TEX_HEIGHT = HotbarOverlayTexture.TEX_HEIGHT;
	private static final int SLOT_SIZE = 16;
	private static final int GAP = 4;
	private static final int SLOT_SPACING = SLOT_SIZE + GAP;

	private static KeyMapping toggleKey;

	// Track item counts per slot to detect new items
	private final Map<Integer, Integer> previousSlotCounts = new HashMap<>();

	// Shine effect renderer
	private final ShineEffectRenderer shineRenderer = new ShineEffectRenderer();

	@Override
	public void onInitializeClient() {
		OverlayConfig.register();

		// Register key mapping
		KeyMapping.Category category = KeyMapping.Category.register(Identifier.fromNamespaceAndPath(InventoryOverlay.MOD_ID, "inventory_overlay"));
		toggleKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
				"key.inventoryoverlay.toggle",
				InputConstants.Type.KEYSYM,
				GLFW.GLFW_KEY_I,
				category
		));

		// Register resource reload listener so texture updates when resource packs change
		ResourceManagerHelper.get(PackType.CLIENT_RESOURCES).registerReloadListener(
				new SimpleSynchronousResourceReloadListener() {
					@Override
					public Identifier getFabricId() {
						return RELOAD_LISTENER_ID;
					}

					@Override
					public void onResourceManagerReload(ResourceManager resourceManager) {
						HotbarOverlayTexture.load(OverlayConfig.get().textureMode);
					}
				}
		);

		// Handle key press
		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			while (toggleKey.consumeClick()) {
				OverlayConfig config = OverlayConfig.get();
				config.overlayVisible = !config.overlayVisible;
				OverlayConfig.get().save();
			}

			// Check for new items
			checkForNewItems(client);
		});

		// Register HUD element as lambda (HudElement is a functional interface)
		HudElementRegistry.attachElementBefore(VanillaHudElements.HOTBAR, OVERLAY_ID,
				(GuiGraphicsExtractor context, DeltaTracker deltaTracker) -> renderOverlay(context));
	}

	private void checkForNewItems(Minecraft client) {
		if (client.player == null) return;

		// Check slots 9-35 (inventory rows, not hotbar)
		for (int slotIndex = 9; slotIndex < 36; slotIndex++) {
			ItemStack stack = client.player.getInventory().getItem(slotIndex);
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

	private void renderOverlay(GuiGraphicsExtractor context) {
		Minecraft client = Minecraft.getInstance();

		if (client.player == null || client.options.hideGui || client.player.isSpectator()) return;

		OverlayConfig config = OverlayConfig.get();

		if (!config.overlayVisible) return;

		int screenWidth = client.getWindow().getGuiScaledWidth();
		int screenHeight = client.getWindow().getGuiScaledHeight();

		int x = calculateX(screenWidth, config);
		int y = calculateY(screenHeight, config);

		int alphaInt = (int) (config.containerAlphaPercent * 2.55f);
		int color = (alphaInt << 24) | 0xFFFFFF;

		// Use dynamic texture if loaded, otherwise skip background
		if (alphaInt > 0 && HotbarOverlayTexture.isLoaded()) {
			context.blit(
				RenderPipelines.GUI_TEXTURED,
				HotbarOverlayTexture.TEXTURE_ID,
				x, y,
				0.0f, 0.0f,
				TEX_WIDTH, TEX_HEIGHT,
				TEX_WIDTH, TEX_HEIGHT,
				color
			);
		}

		// Render Items
		int startX = x + 2;
		int startY = y + 2;

		for (int row = 0; row < 3; row++) {
			for (int col = 0; col < 9; col++) {
				int slotIndex = 9 + (row * 9) + col;
				ItemStack stack = client.player.getInventory().getItem(slotIndex);

				if (!stack.isEmpty()) {
					int itemX = startX + (col * SLOT_SPACING);
					int itemY = startY + (row * SLOT_SPACING);

					context.item(stack, itemX, itemY);
					context.itemDecorations(client.font, stack, itemX, itemY);
				}
			}
		}

		// Render shine effects AFTER items (so they appear on top)
		for (int row = 0; row < 3; row++) {
			for (int col = 0; col < 9; col++) {
				int slotIndex = 9 + (row * 9) + col;
				int itemX = startX + (col * SLOT_SPACING);
				int itemY = startY + (row * SLOT_SPACING);

				shineRenderer.renderShine(context, slotIndex, itemX, itemY);
			}
		}

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