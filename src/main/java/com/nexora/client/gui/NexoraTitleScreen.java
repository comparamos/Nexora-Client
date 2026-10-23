package com.nexora.client.gui;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.SharedConstants;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.screen.option.OptionsScreen;
import net.minecraft.client.gui.screen.world.SelectWorldScreen;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

public final class NexoraTitleScreen extends Screen {
    private static final Identifier NEXORA_LOGO = Identifier.of("nexora", "textures/gui/nexora_logo.png");
    private static final Identifier MENU_FONT = Identifier.of("nexora", "menu");
    private static final int MENU_ITEM_COUNT = 5;
    private static final String[] MENU_LABELS = {
            "Singleplayer", "Multiplayer", "Settings", "Client Settings", "Quit Game"
    };
    private static final String MOD_VERSION = FabricLoader.getInstance().getModContainer("nexora")
            .orElseThrow(() -> new IllegalStateException("Nexora mod metadata is unavailable"))
            .getMetadata().getVersion().getFriendlyString();
    private static final String GAME_VERSION = SharedConstants.getGameVersion().getName();

    private final long openedAt = System.currentTimeMillis();
    private final float[] hover = new float[MENU_ITEM_COUNT];
    private final boolean animateEntrance;

    public NexoraTitleScreen() {
        this(true);
    }

    public NexoraTitleScreen(boolean animateEntrance) {
        super(Text.literal("Nexora Client"));
        this.animateEntrance = animateEntrance;
    }

    @Override
    protected void init() {
        super.init();
        if (client != null) {
            client.getTextureManager().getTexture(NEXORA_LOGO).setFilter(true, false);
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        drawBackground(context);
        drawBrand(context);
        drawMenu(context, mouseX, mouseY, delta);
        NexoraTheme.Palette palette = NexoraTheme.current();
        context.drawText(textRenderer,
                menuText("Nexora Client " + MOD_VERSION + " [" + GAME_VERSION + "]"),
                2, height - 8, palette.accentSoft(), false);
    }

    private void drawBackground(DrawContext context) {
        NexoraTheme.Palette palette = NexoraTheme.current();
        context.fillGradient(0, 0, width, height, palette.backgroundTop(), palette.backgroundBottom());
    }

    private void drawBrand(DrawContext context) {
        float progress = entranceProgress(650);
        float eased = smoothEase(progress);
        int verticalOffset = Math.round((1 - eased) * 9);
        int center = menuCenterX();
        int logoSize = logoSize();
        int logoWidth = logoSize * 631 / 568;
        int logoY = logoY() + verticalOffset;
        context.setShaderColor(1.0F, 1.0F, 1.0F, eased);
        context.drawTexture(NEXORA_LOGO, center - logoWidth / 2, logoY, logoWidth, logoSize,
                0, 0, 1262, 1136, 1262, 1136);
        context.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        NexoraTheme.Palette palette = NexoraTheme.current();
        context.drawCenteredTextWithShadow(textRenderer, menuText("Nexora"), center, logoY + logoSize + 8,
                withAlpha(palette.text(), Math.round(eased * 255)));
        context.drawCenteredTextWithShadow(textRenderer, menuText("C L I E N T"), center, logoY + logoSize + 25,
                withAlpha(palette.accent(), Math.round(eased * 255)));
    }

    private void drawMenu(DrawContext context, int mouseX, int mouseY, float delta) {
        int buttonWidth = buttonWidth();
        int buttonHeight = buttonHeight();
        int x = menuCenterX() - buttonWidth / 2;
        int firstY = firstButtonY();
        int gap = buttonGap();
        float reveal = smoothEase(entranceProgress(650));
        int verticalOffset = Math.round((1 - reveal) * 7);
        for (int i = 0; i < MENU_LABELS.length; i++) {
            int y = firstY + i * (buttonHeight + gap);
            boolean isHovered = inside(mouseX, mouseY, x, y, buttonWidth, buttonHeight);
            hover[i] = MathHelper.clamp(hover[i] + (isHovered ? 1 : -1) * delta * 0.12f, 0, 1);
            Text label = menuText(MENU_LABELS[i]);
            NexoraTheme.Palette palette = NexoraTheme.current();
            int textColor = mixColor(reveal, withAlpha(palette.text(), 0),
                    mixColor(hover[i], palette.text(), 0xFFFFFFFF));
            context.drawCenteredTextWithShadow(textRenderer, label, menuCenterX(),
                    y + (buttonHeight - 8) / 2 - 3 + verticalOffset, textColor);
            int underlineWidth = Math.round((textRenderer.getWidth(label) + 16) * hover[i] * reveal);
            if (underlineWidth > 0) {
                int underlineX = menuCenterX() - underlineWidth / 2;
                int underlineY = y + buttonHeight - 2;
                context.fill(underlineX, underlineY, underlineX + underlineWidth, underlineY + 1,
                        mixColor(hover[i], withAlpha(palette.accentSoft(), 0), palette.accentSoft()));
            }
        }
    }

    private int menuCenterX() {
        return width / 2;
    }

    private int buttonHeight() {
        return Math.min(28, Math.max(22, height / 36));
    }

    private int buttonWidth() {
        int widestLabel = 0;
        for (String label : MENU_LABELS) {
            widestLabel = Math.max(widestLabel, textRenderer.getWidth(menuText(label)));
        }
        return Math.min(width * 30 / 100, widestLabel + 24);
    }

    private int buttonGap() {
        return Math.max(4, height / 48);
    }

    private int logoSize() {
        return Math.min(140, Math.max(80, height / 8));
    }

    private int logoY() {
        return Math.max(12, height / 8);
    }

    private int firstButtonY() {
        int preferred = Math.max(height * 42 / 100, logoY() + logoSize() + 48);
        int contentHeight = buttonHeight() * MENU_ITEM_COUNT + buttonGap() * (MENU_ITEM_COUNT - 1);
        int latest = Math.max(height * 35 / 100, height - 34 - contentHeight);
        return Math.min(preferred, latest);
    }

    private float entranceProgress(int duration) {
        return entranceProgress(0, duration);
    }

    private float entranceProgress(int delay, int duration) {
        if (!animateEntrance) {
            return 1;
        }
        return MathHelper.clamp((System.currentTimeMillis() - openedAt - delay) / (float) duration, 0, 1);
    }

    private static float smoothEase(float value) {
        return value * value * (3 - 2 * value);
    }

    private static Text menuText(String value) {
        return Text.literal(value).setStyle(Style.EMPTY.withFont(MENU_FONT));
    }

    private static int mixColor(float progress, int from, int to) {
        int alpha = Math.round(MathHelper.lerp(progress, from >>> 24, to >>> 24));
        int red = Math.round(MathHelper.lerp(progress, from >> 16 & 0xFF, to >> 16 & 0xFF));
        int green = Math.round(MathHelper.lerp(progress, from >> 8 & 0xFF, to >> 8 & 0xFF));
        int blue = Math.round(MathHelper.lerp(progress, from & 0xFF, to & 0xFF));
        return alpha << 24 | red << 16 | green << 8 | blue;
    }

    private static int withAlpha(int color, int alpha) {
        return color & 0x00FFFFFF | MathHelper.clamp(alpha, 0, 255) << 24;
    }

    private static boolean inside(double mouseX, double mouseY, int x, int y, int width, int height) {
        return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button != 0) {
            return super.mouseClicked(mouseX, mouseY, button);
        }
        if (animateEntrance && entranceProgress(650) < 1) {
            return true;
        }
        int buttonWidth = buttonWidth();
        int buttonHeight = buttonHeight();
        int x = menuCenterX() - buttonWidth / 2;
        int firstY = firstButtonY();
        int gap = buttonGap();
        for (int i = 0; i < MENU_LABELS.length; i++) {
            int y = firstY + i * (buttonHeight + gap);
            if (inside(mouseX, mouseY, x, y, buttonWidth, buttonHeight)) {
                if (client != null) {
                    switch (i) {
                        case 0 -> client.setScreen(new SelectWorldScreen(this));
                        case 1 -> client.setScreen(new MultiplayerScreen(this));
                        case 2 -> client.setScreen(new OptionsScreen(this, client.options));
                        case 3 -> client.setScreen(new NexoraClientSettingsScreen(this));
                        case 4 -> client.scheduleStop();
                    }
                }
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
