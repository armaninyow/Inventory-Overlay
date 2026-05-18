package com.armaninyow.inventoryoverlay;

import dev.isxander.yacl3.config.v2.api.ConfigClassHandler;
import dev.isxander.yacl3.config.v2.api.SerialEntry;
import dev.isxander.yacl3.config.v2.api.serializer.GsonConfigSerializerBuilder;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.resources.Identifier;

public class OverlayConfig {

	public static final ConfigClassHandler<OverlayConfig> HANDLER = ConfigClassHandler.createBuilder(OverlayConfig.class)
			.id(Identifier.fromNamespaceAndPath(InventoryOverlay.MOD_ID, "config"))
			.serializer(config -> GsonConfigSerializerBuilder.create(config)
					.setPath(FabricLoader.getInstance().getConfigDir().resolve("inventoryoverlay.json"))
					.build())
			.build();

	public enum HAnchor { LEFT, CENTER, RIGHT }
	public enum VAnchor { TOP, CENTER, BOTTOM }
	public enum TextureMode { VANILLA, RESOURCEPACK }

	@SerialEntry public HAnchor horizontalAnchor = HAnchor.RIGHT;
	@SerialEntry public VAnchor verticalAnchor = VAnchor.CENTER;

	@SerialEntry public int xOffset = -10;
	@SerialEntry public int yOffset = 0;

	@SerialEntry public int containerAlphaPercent = 75;

	@SerialEntry public boolean overlayVisible = true;

	@SerialEntry public boolean shineEffectEnabled = true;

	@SerialEntry public TextureMode textureMode = TextureMode.VANILLA;

	public static OverlayConfig get() {
		return HANDLER.instance();
	}

	public static void register() {
		HANDLER.load();
	}

	public void save() {
		HANDLER.save();
	}
}