package com.nexora.client.gui;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public final class NexoraClientSettingsScreen extends Screen {
    private final Screen parent;

    public NexoraClientSettingsScreen(Screen parent) {
        super(Text.literal("Client Settings"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        addDrawableChild(ButtonWidget.builder(Text.translatable("gui.done"), button -> close())
                .dimensions(width / 2 - 100, height / 2 + 36, 200, 20)
                .build());
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        context.fillGradient(0, 0, width, height, 0xFF03040B, 0xFF08051C);
        context.drawCenteredTextWithShadow(textRenderer, "Client Settings", width / 2, height / 2 - 34,
                0xFFF4F1FF);
        context.drawCenteredTextWithShadow(textRenderer, "Nexora client settings will appear here.",
                width / 2, height / 2 - 10, 0xFFBCA9FF);
        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public void close() {
        if (client != null) {
            client.setScreen(parent);
        }
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
