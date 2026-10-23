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
    private static final int PURPLE = 0xFF9B38FF;
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

    public NexoraTitleScreen() {
        super(Text.literal("Nexora Client"));
    }

    @Override
    protected void init() {
        super.init();
        if (client != null) {
            client.getTextureManager().getTexture(NEXORA_LOGO).setFilter(true, true);
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        drawBackground(context);
        drawBrand(context);
        drawMenu(context, mouseX, mouseY, delta);
        context.drawText(textRenderer,
                menuText("Nexora Client " + MOD_VERSION + "  |  Minecraft " + GAME_VERSION),
                4, height - 14, 0xFFBCA9FF, false);
    }

    private void drawBackground(DrawContext context) {
        context.fillGradient(0, 0, width, height, 0xFF03040B, 0xFF08051C);
        long time = System.currentTimeMillis() - openedAt;
        for (int i = 0; i < 24; i++) {
            float phase = (time * (0.012f + (i % 5) * 0.002f) + i * 67.0f) % (height + 24);
            int x = Math.floorMod(i * 193 + (int) (Math.sin(time * 0.00035 + i) * 48), Math.max(1, width));
            int y = height - (int) phase;
            int color = i % 4 == 0 ? 0x77723BFF : 0x445149FF;
            int size = i % 9 == 0 ? 2 : 1;
            context.fill(x, y, x + size, y + size, color);
        }
    }

    private void drawBrand(DrawContext context) {
        float progress = MathHelper.clamp((System.currentTimeMillis() - openedAt) / 1100.0f, 0, 1);
        float eased = 1 - (float) Math.pow(1 - progress, 3);
        int center = menuCenterX() + Math.round((float) Math.sin(Math.PI * progress)
                * Math.min(width * 0.14f, 150));
        int logoSize = logoSize();
        int logoWidth = logoSize * 631 / 568;
        int logoY = Math.round(MathHelper.lerp(eased, height * 0.32f, logoY()));
        context.drawTexture(NEXORA_LOGO, center - logoWidth / 2, logoY, logoWidth, logoSize,
                0, 0, 631, 568, 631, 568);
        context.drawCenteredTextWithShadow(textRenderer, menuText("Nexora"), center, logoY + logoSize + 8,
                0xFFF4F1FF);
        context.drawCenteredTextWithShadow(textRenderer, menuText("C L I E N T"), center, logoY + logoSize + 25,
                PURPLE);
    }

    private void drawMenu(DrawContext context, int mouseX, int mouseY, float delta) {
        if (System.currentTimeMillis() - openedAt < 750) {
            return;
        }
        int buttonWidth = buttonWidth();
        int buttonHeight = buttonHeight();
        int x = menuCenterX() - buttonWidth / 2;
        int firstY = firstButtonY();
        int gap = buttonGap();
        for (int i = 0; i < MENU_LABELS.length; i++) {
            int y = firstY + i * (buttonHeight + gap);
            boolean isHovered = inside(mouseX, mouseY, x, y, buttonWidth, buttonHeight);
            hover[i] = MathHelper.clamp(hover[i] + (isHovered ? 1 : -1) * delta * 0.12f, 0, 1);
            Text label = menuText(MENU_LABELS[i]);
            int textColor = mixColor(hover[i], 0xFFF2EEFF, 0xFFFFFFFF);
            context.drawCenteredTextWithShadow(textRenderer, label, menuCenterX(),
                    y + (buttonHeight - 8) / 2 - 3, textColor);
            int underlineWidth = Math.round((textRenderer.getWidth(label) + 16) * hover[i]);
            if (underlineWidth > 0) {
                int underlineX = menuCenterX() - underlineWidth / 2;
                int underlineY = y + buttonHeight - 2;
                context.fill(underlineX, underlineY, underlineX + underlineWidth, underlineY + 1,
                        mixColor(hover[i], 0x009B38FF, 0xFFBCA9FF));
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

    private static boolean inside(double mouseX, double mouseY, int x, int y, int width, int height) {
        return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button != 0) {
            return super.mouseClicked(mouseX, mouseY, button);
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
