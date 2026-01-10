package com.armaninyow.inventoryoverlay;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.option.KeyBinding.Category;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;
import me.shedaniel.autoconfig.AutoConfig;

public class OverlayRenderer implements ClientModInitializer {

    private static final Identifier CONTAINER_TEXTURE = Identifier.of(InventoryOverlay.MOD_ID, "textures/gui/inventory_overlay.png");

    private static final int TEX_WIDTH = 180;
    private static final int TEX_HEIGHT = 60;
    private static final int SLOT_SIZE = 16;
    private static final int GAP = 4;
    private static final int SLOT_SPACING = SLOT_SIZE + GAP;

    private static KeyBinding toggleKey;

    @Override
    public void onInitializeClient() {
        OverlayConfig.register();

        // Create custom category with Identifier
        Category customCategory = new Category(Identifier.of(InventoryOverlay.MOD_ID, "inventory_overlay"));

        // Register keybinding with custom category
        toggleKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.inventoryoverlay.toggle",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_I,
                customCategory
        ));

        // Handle key press
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (toggleKey.wasPressed()) {
                OverlayConfig config = OverlayConfig.get();
                config.overlayVisible = !config.overlayVisible;
                AutoConfig.getConfigHolder(OverlayConfig.class).save();
            }
        });

        HudRenderCallback.EVENT.register((drawContext, renderTickCounter) -> {
            renderOverlay(drawContext);
        });
    }

    private void renderOverlay(DrawContext context) {
        MinecraftClient client = MinecraftClient.getInstance();
        ClientPlayerEntity player = client.player;

        if (player == null || client.options.hudHidden || player.isSpectator()) return;

        OverlayConfig config = OverlayConfig.get();
        
        // Check if overlay is visible
        if (!config.overlayVisible) return;

        int screenWidth = client.getWindow().getScaledWidth();
        int screenHeight = client.getWindow().getScaledHeight();

        int x = calculateX(screenWidth, config);
        int y = calculateY(screenHeight, config);

        int alphaInt = (int) (config.containerAlphaPercent * 2.55f);
        int color = (alphaInt << 24) | 0xFFFFFF;

        if (alphaInt > 0) {
            context.drawTexture(
                RenderPipelines.GUI_TEXTURED, 
                CONTAINER_TEXTURE,
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
                ItemStack stack = player.getInventory().getStack(slotIndex);

                if (!stack.isEmpty()) {
                    int itemX = startX + (col * SLOT_SPACING);
                    int itemY = startY + (row * SLOT_SPACING);

                    context.drawItem(stack, itemX, itemY);
                    context.drawStackOverlay(client.textRenderer, stack, itemX, itemY);
                }
            }
        }
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