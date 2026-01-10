package com.armaninyow.inventoryoverlay;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InventoryOverlay implements ModInitializer {
    public static final String MOD_ID = "inventoryoverlay";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        // Configuration is loaded lazily when accessed
        LOGGER.info("Inventory Overlay initialized.");
    }
}