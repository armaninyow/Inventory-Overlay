package com.armaninyow.inventoryoverlay;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceProvider;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

public class HotbarOverlayTexture {

	private static final Identifier HOTBAR_SOURCE = Identifier.fromNamespaceAndPath("minecraft", "textures/gui/sprites/hud/hotbar.png");

	public static final Identifier TEXTURE_ID = Identifier.fromNamespaceAndPath(InventoryOverlay.MOD_ID, "textures/gui/inventory_overlay_dynamic");

	private static final int CROPPED_W = 180;
	private static final int CROPPED_H = 20;

	public static final int TEX_WIDTH = CROPPED_W;
	public static final int TEX_HEIGHT = CROPPED_H * 3;

	private static DynamicTexture dynamicTexture;
	private static boolean registered = false;

	public static void load(OverlayConfig.TextureMode mode) {
		try {
			ResourceProvider provider;
			if (mode == OverlayConfig.TextureMode.VANILLA) {
				provider = Minecraft.getInstance().getVanillaPackResources().asProvider();
			} else {
				provider = Minecraft.getInstance().getResourceManager();
			}

			Optional<Resource> resourceOpt = provider.getResource(HOTBAR_SOURCE);
			if (resourceOpt.isEmpty()) {
				InventoryOverlay.LOGGER.warn("Could not find hotbar.png, skipping texture load.");
				return;
			}

			NativeImage hotbar;
			try (InputStream stream = resourceOpt.get().open()) {
				hotbar = NativeImage.read(stream);
			}

			NativeImage result = new NativeImage(TEX_WIDTH, TEX_HEIGHT, false);

			for (int row = 0; row < 3; row++) {
				for (int y = 0; y < CROPPED_H; y++) {
					for (int x = 0; x < CROPPED_W; x++) {
						int pixel = hotbar.getPixel(x + 1, y + 1);
						result.setPixel(x, (row * CROPPED_H) + y, pixel);
					}
				}
			}

			hotbar.close();

			int transparent = 0x00000000;
			result.setPixel(0, 0, transparent);
			result.setPixel(TEX_WIDTH - 1, 0, transparent);
			result.setPixel(0, TEX_HEIGHT - 1, transparent);
			result.setPixel(TEX_WIDTH - 1, TEX_HEIGHT - 1, transparent);

			if (dynamicTexture != null) {
				dynamicTexture.setPixels(result);
				dynamicTexture.upload();
			} else {
				dynamicTexture = new DynamicTexture(() -> "inventoryoverlay:inventory_overlay", result);
				Minecraft.getInstance().getTextureManager().register(TEXTURE_ID, dynamicTexture);
				registered = true;
			}

		} catch (IOException e) {
			InventoryOverlay.LOGGER.error("Failed to load hotbar texture for Inventory Overlay", e);
		}
	}

	public static void close() {
		if (dynamicTexture != null) {
			dynamicTexture.close();
			dynamicTexture = null;
			registered = false;
		}
	}

	public static boolean isLoaded() {
		return registered && dynamicTexture != null;
	}
}