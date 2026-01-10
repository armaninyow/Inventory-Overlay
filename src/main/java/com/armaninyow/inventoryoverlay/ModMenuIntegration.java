package com.armaninyow.inventoryoverlay;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.text.Text;

public class ModMenuIntegration implements ModMenuApi {

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent -> {
            ConfigBuilder builder = ConfigBuilder.create()
                    .setParentScreen(parent)
                    .setTitle(Text.literal("Inventory Overlay Config"));

            OverlayConfig config = OverlayConfig.get();
            ConfigEntryBuilder entryBuilder = builder.entryBuilder();
            ConfigCategory general = builder.getOrCreateCategory(Text.literal("General"));

            // Horizontal Anchor
            general.addEntry(entryBuilder.startEnumSelector(
                    Text.literal("Horizontal Anchor"),
                    OverlayConfig.HAnchor.class,
                    config.horizontalAnchor)
                    .setDefaultValue(OverlayConfig.HAnchor.RIGHT)
                    .setSaveConsumer(newValue -> config.horizontalAnchor = newValue)
                    .build());

            // Vertical Anchor
            general.addEntry(entryBuilder.startEnumSelector(
                    Text.literal("Vertical Anchor"),
                    OverlayConfig.VAnchor.class,
                    config.verticalAnchor)
                    .setDefaultValue(OverlayConfig.VAnchor.CENTER)
                    .setSaveConsumer(newValue -> config.verticalAnchor = newValue)
                    .build());

            // X Offset
            general.addEntry(entryBuilder.startIntField(
                    Text.literal("X Offset"),
                    config.xOffset)
                    .setDefaultValue(-10)
                    .setMin(-500).setMax(500)
                    .setSaveConsumer(newValue -> config.xOffset = newValue)
                    .build());

            // Y Offset
            general.addEntry(entryBuilder.startIntField(
                    Text.literal("Y Offset"),
                    config.yOffset)
                    .setDefaultValue(0)
                    .setMin(-500).setMax(500)
                    .setSaveConsumer(newValue -> config.yOffset = newValue)
                    .build());

            // Transparency
            general.addEntry(entryBuilder.startIntSlider(
                    Text.literal("Container Alpha %"),
                    config.containerAlphaPercent, 0, 100)
                    .setDefaultValue(75)
                    .setSaveConsumer(newValue -> config.containerAlphaPercent = newValue)
                    .build());

            builder.setSavingRunnable(() -> AutoConfig.getConfigHolder(OverlayConfig.class).save());

            return builder.build();
        };
    }
}