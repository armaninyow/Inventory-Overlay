package com.armaninyow.inventoryoverlay;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import dev.isxander.yacl3.api.*;
import dev.isxander.yacl3.api.controller.*;
import net.minecraft.network.chat.Component;

public class ModMenuIntegration implements ModMenuApi {

	@Override
	public ConfigScreenFactory<?> getModConfigScreenFactory() {
		return parent -> {
			OverlayConfig config = OverlayConfig.get();

			return YetAnotherConfigLib.createBuilder()
					.title(Component.translatable("inventoryoverlay.config.title"))
					.category(ConfigCategory.createBuilder()
							.name(Component.translatable("inventoryoverlay.config.category.general"))

							// Horizontal Anchor
							.option(Option.<OverlayConfig.HAnchor>createBuilder()
									.name(Component.translatable("inventoryoverlay.config.horizontalAnchor"))
									.description(OptionDescription.of(Component.translatable("inventoryoverlay.config.horizontalAnchor.tooltip")))
									.binding(OverlayConfig.HAnchor.RIGHT, () -> config.horizontalAnchor, v -> config.horizontalAnchor = v)
									.controller(opt -> EnumControllerBuilder.create(opt)
											.enumClass(OverlayConfig.HAnchor.class)
											.formatValue(v -> Component.translatable("inventoryoverlay.config.horizontalAnchor." + v.name())))
									.build())

							// Vertical Anchor
							.option(Option.<OverlayConfig.VAnchor>createBuilder()
									.name(Component.translatable("inventoryoverlay.config.verticalAnchor"))
									.description(OptionDescription.of(Component.translatable("inventoryoverlay.config.verticalAnchor.tooltip")))
									.binding(OverlayConfig.VAnchor.CENTER, () -> config.verticalAnchor, v -> config.verticalAnchor = v)
									.controller(opt -> EnumControllerBuilder.create(opt)
											.enumClass(OverlayConfig.VAnchor.class)
											.formatValue(v -> Component.translatable("inventoryoverlay.config.verticalAnchor." + v.name())))
									.build())

							// X Offset
							.option(Option.<Integer>createBuilder()
									.name(Component.translatable("inventoryoverlay.config.xOffset"))
									.description(OptionDescription.of(Component.translatable("inventoryoverlay.config.xOffset.tooltip")))
									.binding(-10, () -> config.xOffset, v -> config.xOffset = v)
									.controller(opt -> IntegerFieldControllerBuilder.create(opt).range(-500, 500))
									.build())

							// Y Offset
							.option(Option.<Integer>createBuilder()
									.name(Component.translatable("inventoryoverlay.config.yOffset"))
									.description(OptionDescription.of(Component.translatable("inventoryoverlay.config.yOffset.tooltip")))
									.binding(0, () -> config.yOffset, v -> config.yOffset = v)
									.controller(opt -> IntegerFieldControllerBuilder.create(opt).range(-500, 500))
									.build())

							// Container Alpha %
							.option(Option.<Integer>createBuilder()
									.name(Component.translatable("inventoryoverlay.config.containerAlphaPercent"))
									.description(OptionDescription.of(Component.translatable("inventoryoverlay.config.containerAlphaPercent.tooltip")))
									.binding(75, () -> config.containerAlphaPercent, v -> config.containerAlphaPercent = v)
									.controller(opt -> IntegerSliderControllerBuilder.create(opt).range(0, 100).step(1))
									.build())

							// Shine Effect
							.option(Option.<Boolean>createBuilder()
									.name(Component.translatable("inventoryoverlay.config.shineEffectEnabled"))
									.description(OptionDescription.of(Component.translatable("inventoryoverlay.config.shineEffectEnabled.tooltip")))
									.binding(true, () -> config.shineEffectEnabled, v -> config.shineEffectEnabled = v)
									.controller(opt -> BooleanControllerBuilder.create(opt).yesNoFormatter())
									.build())

							// Texture Mode
							.option(Option.<OverlayConfig.TextureMode>createBuilder()
									.name(Component.translatable("inventoryoverlay.config.textureMode"))
									.description(OptionDescription.of(Component.translatable("inventoryoverlay.config.textureMode.tooltip")))
									.binding(OverlayConfig.TextureMode.VANILLA, () -> config.textureMode, v -> {
										config.textureMode = v;
										HotbarOverlayTexture.load(v);
									})
									.controller(opt -> EnumControllerBuilder.create(opt)
											.enumClass(OverlayConfig.TextureMode.class)
											.formatValue(v -> Component.translatable("inventoryoverlay.config.textureMode." + v.name())))
									.build())

							.build())
					.save(() -> OverlayConfig.get().save())
					.build().generateScreen(parent);
		};
	}
}