package com.nexora.client.gui;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.screen.option.OptionsScreen;
import net.minecraft.client.gui.screen.world.SelectWorldScreen;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.texture.PlayerSkinProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.util.SkinTextures;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.client.render.OverlayTexture;

import java.util.concurrent.CompletableFuture;

public final class NexoraTitleScreen extends Screen {
    private static final int PURPLE = 0xFF9B38FF;
    private static final int PANEL = 0xD9080B1B;
    private static final Identifier NEXORA_LOGO = Identifier.of("nexora", "textures/gui/nexora_logo.png");
    private static final Identifier DEFAULT_SKIN = Identifier.ofVanilla("textures/entity/player/wide/steve.png");

    private final long openedAt = System.currentTimeMillis();
    private final float[] hover = new float[3];
    private PlayerEntityModel<?> playerModel;
    private Identifier skinTexture;
    private boolean skinRequested;
    private boolean accountDropdown;
    private boolean dragging;
    private double dragStartX;
    private float dragStartRotation;
    private float rotation;
    private float headYaw;
    private float headPitch;

    public NexoraTitleScreen() {
        super(Text.literal("Nexora Client"));
    }

    @Override
    protected void init() {
        super.init();
        requestSkin();
    }

    private void requestSkin() {
        if (skinRequested || client == null) {
            return;
        }
        skinRequested = true;
        skinTexture = DEFAULT_SKIN;
        PlayerSkinProvider provider = client.getSkinProvider();
        CompletableFuture<SkinTextures> lookup = provider.fetchSkinTextures(client.getGameProfile());
        lookup.thenAccept(textures -> client.execute(() -> {
            skinTexture = textures.texture();
        }));
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        drawBackground(context);
        drawBrand(context);
        drawMenu(context, mouseX, mouseY, delta);
        drawAccount(context, mouseX, mouseY);
        renderCharacter(context, mouseX, mouseY);
        context.drawText(textRenderer, "Nexora Client 1.0.1", 22, height - 24, 0xFFBCA9FF, false);
        context.drawText(textRenderer, "PLAY BETTER. FASTER.", width - textRenderer.getWidth("PLAY BETTER. FASTER.") - 22,
                height - 24, 0xFFBCA9FF, false);
    }

    private void drawBackground(DrawContext context) {
        context.fillGradient(0, 0, width, height, 0xFF03040B, 0xFF08051C);
        long time = System.currentTimeMillis() - openedAt;
        for (int i = 0; i < 64; i++) {
            float phase = (time * (0.012f + (i % 5) * 0.002f) + i * 67.0f) % (height + 24);
            int x = Math.floorMod(i * 193 + (int) (Math.sin(time * 0.00035 + i) * 48), Math.max(1, width));
            int y = height - (int) phase;
            int color = i % 4 == 0 ? 0xAA723BFF : 0x665149FF;
            int size = i % 9 == 0 ? 3 : 1;
            context.fill(x, y, x + size, y + size, color);
        }
        int trailWidth = Math.max(1, width / 3);
        context.fillGradient(0, height - 150, trailWidth, height - 148, 0x003B22FF, 0xAA813BFF);
        context.fillGradient(width - trailWidth, height - 130, width, height - 128, 0xAA4538FF, 0x003B22FF);
    }

    private void drawBrand(DrawContext context) {
        int center = menuCenterX();
        int logoSize = Math.min(76, Math.max(52, height / 9));
        int logoY = Math.max(28, height / 8);
        context.drawTexture(NEXORA_LOGO, center - logoSize / 2, logoY, logoSize, logoSize,
                0, 0, 1254, 1254, 1254, 1254);
        context.drawCenteredTextWithShadow(textRenderer, Text.literal("Nexora"), center, logoY + logoSize + 10,
                0xFFF4F1FF);
        context.drawCenteredTextWithShadow(textRenderer, Text.literal("C L I E N T"), center, logoY + logoSize + 34,
                PURPLE);
    }

    private void drawMenu(DrawContext context, int mouseX, int mouseY, float delta) {
        String[] labels = {"Singleplayer", "Multiplayer", "Settings"};
        int buttonWidth = Math.min(390, Math.max(240, width * 31 / 100));
        int buttonHeight = buttonHeight();
        int x = menuCenterX() - buttonWidth / 2;
        int firstY = firstButtonY();
        int gap = buttonGap();
        for (int i = 0; i < labels.length; i++) {
            int y = firstY + i * (buttonHeight + gap);
            boolean isHovered = inside(mouseX, mouseY, x, y, buttonWidth, buttonHeight);
            hover[i] = MathHelper.clamp(hover[i] + (isHovered ? 1 : -1) * delta * 0.12f, 0, 1);
            int border = hover[i] > 0.01f ? 0xFFB22CFF : 0xFF4422A8;
            roundedPanel(context, x, y, buttonWidth, buttonHeight, PANEL, border);
            context.drawCenteredTextWithShadow(textRenderer, labels[i], menuCenterX(), y + 20, 0xFFF2EEFF);
            int lineWidth = Math.round((buttonWidth - 48) * hover[i]);
            if (lineWidth > 0) {
                int lineX = menuCenterX() - lineWidth / 2;
                int lineY = y + buttonHeight - 4;
                context.fill(lineX - 4, lineY - 2, lineX + lineWidth + 4, lineY + 2, 0x667B69FF);
                context.fill(lineX, lineY - 1, lineX + lineWidth, lineY + 1, 0xFFFFFFFF);
            }
        }
    }

    private int menuCenterX() {
        return width * 40 / 100;
    }

    private int buttonHeight() {
        return Math.min(60, Math.max(44, height / 12));
    }

    private int buttonGap() {
        return Math.max(12, height / 36);
    }

    private int firstButtonY() {
        int logoSize = Math.min(76, Math.max(52, height / 9));
        int preferred = Math.max(height * 47 / 100, height / 8 + logoSize + 108);
        int contentHeight = buttonHeight() * 3 + buttonGap() * 2;
        int latest = Math.max(height * 38 / 100, height - 48 - contentHeight);
        return Math.min(preferred, latest);
    }

    private void drawAccount(DrawContext context, int mouseX, int mouseY) {
        int x = width - Math.min(250, width / 16 + 220);
        int y = Math.max(16, height / 8);
        int w = 220;
        roundedPanel(context, x, y, w, 38, 0xE8080B1D, 0xFF5D2BEA);
        drawAvatar(context, x + 8, y + 6, 26);
        String name = client == null ? "Player" : client.getSession().getUsername();
        context.drawText(textRenderer, name, x + 44, y + 15, 0xFFF1EDFF, false);
        context.drawText(textRenderer, accountDropdown ? "v" : ">", x + w - 23, y + 14, PURPLE, false);
        if (!accountDropdown) {
            return;
        }
        roundedPanel(context, x, y + 43, w, 68, 0xF0080B1D, 0xFF5D2BEA);
        context.drawText(textRenderer, name, x + 14, y + 53, 0xFFF1EDFF, false);
        context.drawText(textRenderer, "Accounts are managed by", x + 14, y + 72, 0xFFB7AFCB, false);
        context.drawText(textRenderer, "your Minecraft launcher.", x + 14, y + 87, 0xFFB7AFCB, false);
    }

    private void drawAvatar(DrawContext context, int x, int y, int size) {
        Identifier texture = skinTexture != null ? skinTexture
                : DEFAULT_SKIN;
        context.drawTexture(texture, x, y, size, size, 8, 8, 8, 8, 64, 64);
        context.drawTexture(texture, x, y, size, size, 40, 8, 8, 8, 64, 64);
    }

    private void renderCharacter(DrawContext context, int mouseX, int mouseY) {
        if (client == null) {
            return;
        }
        if (playerModel == null) {
            playerModel = new PlayerEntityModel<>(client.getEntityModelLoader().getModelPart(EntityModelLayers.PLAYER), false);
        }
        if (skinTexture == null) {
            skinTexture = DEFAULT_SKIN;
        }
        float entrance = MathHelper.clamp((System.currentTimeMillis() - openedAt) / 900.0f, 0, 1);
        float eased = 1 - (float) Math.pow(1 - entrance, 3);
        int targetX = width * 79 / 100;
        int x = targetX + (int) ((1 - eased) * width / 4);
        int y = height * 87 / 100;
        float targetYaw = dragging ? headYaw : MathHelper.clamp((mouseX - x) * 0.12f, -32, 32);
        int headScreenY = y - 210;
        float targetPitch = MathHelper.clamp((mouseY - headScreenY) * 0.08f, -18, 18);
        headYaw += (targetYaw - headYaw) * 0.18f;
        headPitch += (targetPitch - headPitch) * 0.18f;
        long elapsed = System.currentTimeMillis() - openedAt;
        float idle = (float) Math.sin(elapsed * 0.002) * 1.5f;
        float walk = (float) Math.sin(elapsed * 0.012) * (1 - entrance) * 0.45f;
        MatrixStack matrices = context.getMatrices();
        matrices.push();
        matrices.translate(x, y + idle, 100);
        matrices.scale(-10.5f, 10.5f, 10.5f);
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(rotation));
        matrices.translate(0, -24, 0);
        playerModel.head.yaw = headYaw * 0.017453292f;
        playerModel.head.pitch = headPitch * 0.017453292f;
        playerModel.rightArm.pitch = walk + (float) Math.sin(elapsed * 0.002) * 0.04f;
        playerModel.leftArm.pitch = -walk;
        playerModel.rightLeg.pitch = -walk;
        playerModel.leftLeg.pitch = walk;
        VertexConsumerProvider.Immediate consumers = client.getBufferBuilders().getEntityVertexConsumers();
        VertexConsumer vertexConsumer = consumers.getBuffer(RenderLayer.getEntityTranslucent(skinTexture));
        playerModel.render(matrices, vertexConsumer, 0xF000F0, OverlayTexture.DEFAULT_UV);
        consumers.draw();
        matrices.pop();
    }

    private static void roundedPanel(DrawContext context, int x, int y, int width, int height, int fill, int border) {
        context.fill(x + 3, y, x + width - 3, y + height, border);
        context.fill(x, y + 3, x + width, y + height - 3, border);
        context.fill(x + 1, y + 1, x + width - 1, y + height - 1, fill);
    }

    private static boolean inside(double mouseX, double mouseY, int x, int y, int width, int height) {
        return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button != 0) {
            return super.mouseClicked(mouseX, mouseY, button);
        }
        int buttonWidth = Math.min(390, Math.max(240, width * 31 / 100));
        int buttonHeight = buttonHeight();
        int x = menuCenterX() - buttonWidth / 2;
        int firstY = firstButtonY();
        int gap = buttonGap();
        for (int i = 0; i < 3; i++) {
            int y = firstY + i * (buttonHeight + gap);
            if (inside(mouseX, mouseY, x, y, buttonWidth, buttonHeight)) {
                if (client != null) {
                    switch (i) {
                        case 0 -> client.setScreen(new SelectWorldScreen(this));
                        case 1 -> client.setScreen(new MultiplayerScreen(this));
                        case 2 -> client.setScreen(new OptionsScreen(this, client.options));
                    }
                }
                return true;
            }
        }
        int accountX = width - Math.min(250, width / 16 + 220);
        int accountY = Math.max(16, height / 8);
        if (inside(mouseX, mouseY, accountX, accountY, 220, 38)) {
            accountDropdown = !accountDropdown;
            return true;
        }
        if (accountDropdown && inside(mouseX, mouseY, accountX, accountY + 43, 220, 68)) {
            accountDropdown = false;
            return true;
        }
        if (mouseX > width * 0.68 && mouseY > height * 0.3 && mouseY < height * 0.95) {
            dragging = true;
            dragStartX = mouseX;
            dragStartRotation = rotation;
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (dragging) {
            rotation = dragStartRotation + (float) ((mouseX - dragStartX) * 0.7);
            headYaw = MathHelper.clamp(rotation, -55, 55);
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        dragging = false;
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
