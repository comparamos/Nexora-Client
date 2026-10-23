package com.nexora.client.gui;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;

public final class NexoraClientSettingsScreen extends Screen {
    private static final int PANEL_BORDER = 0x704D4565;
    private static final int CARD_COUNT_PER_ROW = 2;
    private final Screen parent;

    public NexoraClientSettingsScreen(Screen parent) {
        super(Text.literal("Client Settings"));
        this.parent = parent;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        NexoraTheme.Palette palette = NexoraTheme.current();
        context.fillGradient(0, 0, width, height, palette.backgroundTop(), palette.backgroundBottom());
        drawBackdrop(context, palette);

        int panelX = panelX();
        int panelY = panelY();
        int panelWidth = panelWidth();
        int panelHeight = panelHeight();
        int railWidth = railWidth();
        int contentX = panelX + railWidth + 18;
        int contentWidth = panelWidth - railWidth - 36;

        context.fillGradient(panelX, panelY, panelX + panelWidth, panelY + panelHeight,
                0xF01A1722, 0xF00D0D14);
        context.drawBorder(panelX, panelY, panelWidth, panelHeight, PANEL_BORDER);
        context.fill(panelX + 1, panelY + 1, panelX + railWidth, panelY + panelHeight - 1, 0xB414121C);
        context.fill(panelX + railWidth, panelY + 1, panelX + railWidth + 1,
                panelY + panelHeight - 1, 0x804D4565);

        drawRail(context, panelX, panelY, railWidth, palette);
        context.drawText(textRenderer, "APPEARANCE", contentX, panelY + 17, palette.mutedText(), false);
        context.drawText(textRenderer, "Themes", contentX, panelY + 39, palette.text(), false);
        context.drawText(textRenderer, "Choose a color palette for the Nexora menu",
                contentX, panelY + 57, palette.mutedText(), false);

        drawThemeCards(context, mouseX, mouseY, contentX, panelY + cardsTopOffset(), contentWidth, palette);
        super.render(context, mouseX, mouseY, delta);
    }

    private void drawBackdrop(DrawContext context, NexoraTheme.Palette palette) {
        for (int i = 0; i < 26; i++) {
            int x = Math.floorMod(i * 193, Math.max(1, width));
            int y = Math.floorMod(i * 97, Math.max(1, height));
            context.fill(x, y, x + 2, y + 2, withAlpha(palette.accent(), i % 4 == 0 ? 105 : 48));
        }
    }

    private void drawRail(DrawContext context, int x, int y, int railWidth, NexoraTheme.Palette palette) {
        context.drawCenteredTextWithShadow(textRenderer, "N", x + railWidth / 2, y + 18, palette.accentSoft());
        context.fill(x + 15, y + 53, x + railWidth - 15, y + 54, 0x504D4565);
        int selectedY = y + (panelHeight() < 320 ? 64 : 77);
        context.fill(x + 7, selectedY, x + railWidth - 7, selectedY + 34,
                withAlpha(palette.accent(), 46));
        context.fill(x + 7, selectedY + 3, x + 9, selectedY + 31, palette.accent());
        context.drawCenteredTextWithShadow(textRenderer, "o", x + railWidth / 2, selectedY + 10,
                palette.accentSoft());
        context.drawCenteredTextWithShadow(textRenderer, "UI", x + railWidth / 2, selectedY + 45,
                palette.mutedText());
    }

    private void drawThemeCards(DrawContext context, int mouseX, int mouseY,
                                int contentX, int contentY, int contentWidth,
                                NexoraTheme.Palette active) {
        int gapX = 10;
        int gapY = cardGapY();
        int cardWidth = (contentWidth - gapX) / CARD_COUNT_PER_ROW;
        int cardHeight = themeCardHeight();
        int swatchHeight = cardHeight < 50 ? 9 : 15;
        var palettes = NexoraTheme.palettes();

        for (int i = 0; i < palettes.size(); i++) {
            NexoraTheme.Palette palette = palettes.get(i);
            int column = i % CARD_COUNT_PER_ROW;
            int row = i / CARD_COUNT_PER_ROW;
            int x = contentX + column * (cardWidth + gapX);
            int y = contentY + row * (cardHeight + gapY);
            boolean selected = palette.id().equals(active.id());
            boolean hovered = inside(mouseX, mouseY, x, y, cardWidth, cardHeight);

            int cardFill = selected ? 0xE92A2335 : hovered ? 0xD9211E2A : 0xC9181620;
            context.fillGradient(x, y, x + cardWidth, y + cardHeight, cardFill, 0xD3111018);
            context.drawBorder(x, y, cardWidth, cardHeight,
                    selected ? palette.accentSoft() : hovered ? 0xA04D4565 : 0x604D4565);
            if (selected) {
                context.fill(x + 1, y + 1, x + cardWidth - 1, y + 3, palette.accent());
            }

            context.drawText(textRenderer, palette.name(), x + 12, y + 8, palette.text(), false);
            if (selected) {
                context.drawText(textRenderer, "ACTIVE", x + cardWidth - 48, y + 8,
                        palette.accentSoft(), false);
            }

            int swatchGap = 5;
            int swatchWidth = Math.min(18,
                    (cardWidth - 24 - swatchGap * (palette.swatches().length - 1)) / palette.swatches().length);
            int swatchY = y + cardHeight - swatchHeight - 8;
            for (int swatch = 0; swatch < palette.swatches().length; swatch++) {
                int swatchX = x + 12 + swatch * (swatchWidth + swatchGap);
                context.fill(swatchX, swatchY, swatchX + swatchWidth, swatchY + swatchHeight,
                        palette.swatches()[swatch]);
                if (swatch == 1) {
                    context.drawBorder(swatchX, swatchY, swatchWidth, swatchHeight, palette.accentSoft());
                }
            }
        }
    }

    private int panelWidth() {
        return Math.min(900, width - 40);
    }

    private int panelHeight() {
        return Math.min(490, height - 32);
    }

    private int panelX() {
        return (width - panelWidth()) / 2;
    }

    private int panelY() {
        return (height - panelHeight()) / 2;
    }

    private int themeCardHeight() {
        int gapY = cardGapY();
        int available = panelHeight() - cardsTopOffset() - 10 - gapY * 3;
        return Math.min(78, Math.max(28, available / 4));
    }

    private int railWidth() {
        return Math.min(58, Math.max(38, panelWidth() / 12));
    }

    private int cardsTopOffset() {
        return panelHeight() < 320 ? 70 : 88;
    }

    private int cardGapY() {
        return panelHeight() < 320 ? 4 : 8;
    }

    private static int withAlpha(int color, int alpha) {
        return color & 0x00FFFFFF | MathHelper.clamp(alpha, 0, 255) << 24;
    }

    private static boolean inside(double mouseX, double mouseY, int x, int y, int width, int height) {
        return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            int contentX = panelX() + railWidth() + 18;
            int contentWidth = panelWidth() - railWidth() - 36;
            int contentY = panelY() + cardsTopOffset();
            int gapX = 10;
            int gapY = cardGapY();
            int cardWidth = (contentWidth - gapX) / CARD_COUNT_PER_ROW;
            int cardHeight = themeCardHeight();

            for (int i = 0; i < NexoraTheme.palettes().size(); i++) {
                int column = i % CARD_COUNT_PER_ROW;
                int row = i / CARD_COUNT_PER_ROW;
                int x = contentX + column * (cardWidth + gapX);
                int y = contentY + row * (cardHeight + gapY);
                if (inside(mouseX, mouseY, x, y, cardWidth, cardHeight)) {
                    NexoraTheme.select(NexoraTheme.palettes().get(i));
                    return true;
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
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
