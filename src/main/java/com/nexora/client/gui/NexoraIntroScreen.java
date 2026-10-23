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
    private static final int PURPLE = 0xFF9B38FF;
    private static final Identifier NEXORA_LOGO = Identifier.of("nexora", "textures/gui/nexora_logo.png");
    private static final Identifier MENU_FONT = Identifier.of("nexora", "menu");
    private static final String CLIENT_VERSION = FabricLoader.getInstance().getModContainer("nexora")
            .orElseThrow(() -> new IllegalStateException("Nexora mod metadata is unavailable"))
            .getMetadata().getVersion().getFriendlyString();
    private final long openedAt = System.currentTimeMillis();

    public NexoraIntroScreen() {
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
        context.fillGradient(0, 0, width, height, 0xFF03040B, 0xFF08051C);
        drawParticles(context);

        long elapsed = System.currentTimeMillis() - openedAt;
        int logoSize = Math.min(170, Math.max(110, height / 5));
        int logoWidth = logoSize * 631 / 568;
        int groupHeight = logoSize + 54;
        int logoY = height / 2 - groupHeight / 2 + (int) (Math.sin(elapsed * 0.0018) * 4);
        int centerX = width / 2;
        context.drawTexture(NEXORA_LOGO, centerX - logoWidth / 2, logoY, logoWidth, logoSize,
                0, 0, 631, 568, 631, 568);
        context.drawCenteredTextWithShadow(textRenderer, menuText("Nexora"), centerX, logoY + logoSize + 8,
                0xFFF4F1FF);
        context.drawCenteredTextWithShadow(textRenderer, menuText("C L I E N T"), centerX, logoY + logoSize + 27,
                PURPLE);

        int promptColor = (elapsed / 650) % 2 == 0 ? 0xFFF4F1FF : 0xFFBCA9FF;
        context.drawCenteredTextWithShadow(textRenderer, menuText("Press to Enter"), centerX, height * 2 / 3,
                promptColor);
        String versionText = "Nexora Client " + CLIENT_VERSION + "  |  Minecraft "
                + SharedConstants.getGameVersion().getName();
        context.drawText(textRenderer, menuText(versionText), 4, height - 14, 0xFFBCA9FF, false);
    }

    private void drawParticles(DrawContext context) {
        long elapsed = System.currentTimeMillis() - openedAt;
        for (int i = 0; i < 42; i++) {
            float phase = (elapsed * (0.012f + (i % 5) * 0.002f) + i * 67.0f) % (height + 24);
            int x = Math.floorMod(i * 193 + (int) (Math.sin(elapsed * 0.00035 + i) * 48),
                    Math.max(1, width));
            int y = height - (int) phase;
            int color = i % 4 == 0 ? 0x77723BFF : 0x445149FF;
            int size = i % 9 == 0 ? 2 : 1;
            context.fill(x, y, x + size, y + size, color);
        }
    }

    private static Text menuText(String value) {
        return Text.literal(value).setStyle(Style.EMPTY.withFont(MENU_FONT));
    }

    private void enterMenu() {
        if (client != null) {
            client.setScreen(new NexoraTitleScreen());
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
