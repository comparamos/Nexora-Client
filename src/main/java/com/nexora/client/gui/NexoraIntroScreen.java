package com.nexora.client.gui;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.SharedConstants;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

public final class NexoraIntroScreen extends Screen {
    private static final Identifier NEXORA_LOGO = Identifier.of("nexora", "textures/gui/nexora_logo.png");
    private static final Identifier MENU_FONT = Identifier.of("nexora", "menu");
    private static final String CLIENT_VERSION = FabricLoader.getInstance().getModContainer("nexora")
            .orElseThrow(() -> new IllegalStateException("Nexora mod metadata is unavailable"))
            .getMetadata().getVersion().getFriendlyString();
    private static final String GAME_VERSION = SharedConstants.getGameVersion().getName();
    private final long openedAt = System.currentTimeMillis();

    public NexoraIntroScreen() {
        super(Text.literal("Nexora Client"));
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
        NexoraTheme.Palette palette = NexoraTheme.current();
        context.fillGradient(0, 0, width, height, palette.backgroundTop(), palette.backgroundBottom());

        long elapsed = System.currentTimeMillis() - openedAt;
        int logoSize = Math.min(170, Math.max(110, height / 5));
        int logoWidth = logoSize * 631 / 568;
        int groupHeight = logoSize + 48;
        int logoY = height / 2 - groupHeight / 2;
        int centerX = width / 2;
        context.drawTexture(NEXORA_LOGO, centerX - logoWidth / 2, logoY, logoWidth, logoSize,
                0, 0, 1262, 1136, 1262, 1136);
        context.drawCenteredTextWithShadow(textRenderer, menuText("Nexora"), centerX, logoY + logoSize + 8,
                palette.text());
        context.drawCenteredTextWithShadow(textRenderer, menuText("C L I E N T"), centerX, logoY + logoSize + 27,
                palette.accent());

        int promptColor = (elapsed / 650) % 2 == 0 ? palette.text() : palette.accentSoft();
        context.drawCenteredTextWithShadow(textRenderer, menuText("Press to Enter"), centerX, height * 3 / 4,
                promptColor);
        String versionText = "Nexora Client " + CLIENT_VERSION + " [" + GAME_VERSION + "]";
        context.drawText(textRenderer, menuText(versionText), 2, height - 8, palette.accentSoft(), false);
    }

    private static Text menuText(String value) {
        return Text.literal(value).setStyle(Style.EMPTY.withFont(MENU_FONT));
    }

    private void enterMenu() {
        if (client != null) {
            client.setScreen(new NexoraTitleScreen(true));
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            enterMenu();
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER
                || keyCode == GLFW.GLFW_KEY_SPACE) {
            enterMenu();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
