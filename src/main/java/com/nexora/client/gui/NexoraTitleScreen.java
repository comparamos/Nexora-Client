package com.nexora.client.gui;

import net.minecraft.client.MinecraftClient;
import com.nexora.client.FpsUnlocker;
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
import net.minecraft.client.util.DefaultSkinHelper;
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
        skinTexture = DefaultSkinHelper.getTexture();
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
        context.drawText(textRenderer, "Nexora Client 1.0.0", 22, height - 24, 0xFFBCA9FF, false);
        String fpsStatus = "FPS UNLOCKER " + (FpsUnlocker.isEnabled() ? "ON" : "OFF") + " [F8]";
        context.drawText(textRenderer, fpsStatus, 22, height - 42, 0xFFBCA9FF, false);
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
        int center = width / 2;
        int logoY = height / 5;
        MatrixStack matrices = context.getMatrices();
        matrices.push();
        matrices.translate(center, logoY + 9, 0);
        matrices.scale(2.8f, 2.8f, 1);
        context.drawCenteredTextWithShadow(textRenderer, Text.literal("N"), 0, -4, 0xFF7438FF);
        matrices.pop();
        context.drawCenteredTextWithShadow(textRenderer, Text.literal("Nexora"), center, logoY + 35,
                0xFFF4F1FF);
        context.drawCenteredTextWithShadow(textRenderer, Text.literal("C L I E N T"), center, logoY + 63,
                PURPLE);
    }

    private void drawMenu(DrawContext context, int mouseX, int mouseY, float delta) {
        String[] labels = {"Singleplayer", "Multiplayer", "Settings"};
        int buttonWidth = Math.min(360, Math.max(260, width / 3));
        int buttonHeight = 44;
        int x = width / 2 - buttonWidth / 2;
        int firstY = height / 2 - 20;
        for (int i = 0; i < labels.length; i++) {
            int y = firstY + i * (buttonHeight + 13);
            boolean isHovered = inside(mouseX, mouseY, x, y, buttonWidth, buttonHeight);
            hover[i] = MathHelper.clamp(hover[i] + (isHovered ? 1 : -1) * delta * 0.12f, 0, 1);
            int glow = (int) (40 + hover[i] * 90);
            int border = i == 0 || hover[i] > 0.01f ? 0xFFB22CFF : 0xFF4422A8;
            context.fill(x - 1, y - 1, x + buttonWidth + 1, y + buttonHeight + 1, (glow << 24) | 0x007335FF);
            roundedPanel(context, x, y, buttonWidth, buttonHeight, PANEL, border);
            context.drawCenteredTextWithShadow(textRenderer, labels[i], width / 2, y + 17,
                    0xFFF2EEFF);
        }
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
                : DefaultSkinHelper.getTexture();
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
            skinTexture = DefaultSkinHelper.getTexture();
        }
        float entrance = MathHelper.clamp((System.currentTimeMillis() - openedAt) / 900.0f, 0, 1);
        float eased = 1 - (float) Math.pow(1 - entrance, 3);
        int x = width * 3 / 4 + (int) ((1 - eased) * width / 3);
        int y = height * 3 / 4 + 24;
        float targetYaw = dragging ? headYaw : MathHelper.clamp((mouseX - x) * 0.12f, -32, 32);
        float targetPitch = MathHelper.clamp((mouseY - (y - 120)) * 0.08f, -18, 18);
        headYaw += (targetYaw - headYaw) * 0.18f;
        headPitch += (targetPitch - headPitch) * 0.18f;
        long elapsed = System.currentTimeMillis() - openedAt;
        float idle = (float) Math.sin(elapsed * 0.002) * 1.5f;
        float walk = (float) Math.sin(elapsed * 0.012) * (1 - entrance) * 0.45f;
        MatrixStack matrices = context.getMatrices();
        matrices.push();
        matrices.translate(x, y + idle, 100);
        matrices.scale(-5.5f, 5.5f, 5.5f);
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
        int buttonWidth = Math.min(360, Math.max(260, width / 3));
        int x = width / 2 - buttonWidth / 2;
        int firstY = height / 2 - 20;
        for (int i = 0; i < 3; i++) {
            int y = firstY + i * 57;
            if (inside(mouseX, mouseY, x, y, buttonWidth, 44)) {
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
        if (mouseX > width * 0.62 && mouseY > height * 0.35) {
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
