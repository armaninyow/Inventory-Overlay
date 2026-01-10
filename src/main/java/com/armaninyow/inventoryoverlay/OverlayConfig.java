package com.armaninyow.inventoryoverlay;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;

@Config(name = "inventoryoverlay")
public class OverlayConfig implements ConfigData {

    // Enums for Anchors
    public enum HAnchor { LEFT, CENTER, RIGHT }
    public enum VAnchor { TOP, CENTER, BOTTOM }

    public HAnchor horizontalAnchor = HAnchor.RIGHT;
    public VAnchor verticalAnchor = VAnchor.CENTER;

    public int xOffset = -10;
    public int yOffset = 0;

    // Alpha 0.0 to 1.0 (float), but user asked for 0-100% representation in GUI
    public int containerAlphaPercent = 75;
    
    // Overlay visibility toggle
    public boolean overlayVisible = true;

    // Helper to get static instance
    public static OverlayConfig get() {
        return AutoConfig.getConfigHolder(OverlayConfig.class).getConfig();
    }

    public static void register() {
        AutoConfig.register(OverlayConfig.class, GsonConfigSerializer::new);
    }
}