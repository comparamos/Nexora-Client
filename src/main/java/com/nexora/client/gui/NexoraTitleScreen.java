package com.nexora.client.gui;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.screen.option.OptionsScreen;
import net.minecraft.client.gui.screen.world.SelectWorldScreen;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.DiffuseLighting;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.texture.PlayerSkinProvider;
import net.minecraft.client.util.SkinTextures;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;

import java.util.concurrent.CompletableFuture;

public final class NexoraTitleScreen extends Screen {
    private static final int PURPLE = 0xFF9B38FF;
    private static final Identifier NEXORA_LOGO = Identifier.of("nexora", "textures/gui/nexora_logo.png");
    private static final Identifier MENU_FONT = Identifier.of("nexora", "menu");
    private static final Identifier DEFAULT_SKIN = Identifier.ofVanilla("textures/entity/player/wide/steve.png");
    private static final int MENU_ITEM_COUNT = 5;
    private static final String[] MENU_LABELS = {
            "Singleplayer", "Multiplayer", "Settings", "Client Settings", "Quit Game"
    };
    private static final String MOD_VERSION = FabricLoader.getInstance().getModContainer("nexora")
            .orElseThrow(() -> new IllegalStateException("Nexora mod metadata is unavailable"))
            .getMetadata().getVersion().getFriendlyString();

    private final long openedAt = System.currentTimeMillis();
    private final float[] hover = new float[MENU_ITEM_COUNT];
    private Identifier skinTexture;
    private PlayerEntityModel<LivingEntity> characterModel;
    private boolean skinRequested;
    private boolean slimSkin;
    private boolean accountDropdown;
    private boolean dragging;
    private double dragStartX;
    private float dragStartRotation;
    private float rotation;
    private float lookYaw;
    private float lookPitch;

    public NexoraTitleScreen() {
        super(Text.literal("Nexora Client"));
    }

    @Override
    protected void init() {
        super.init();
        updateCharacterModel();
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
        lookup.thenAccept(textures -> client.execute(() -> updateSkin(textures)));
    }

    private void updateSkin(SkinTextures textures) {
        skinTexture = textures.texture();
        slimSkin = textures.model() == SkinTextures.Model.SLIM;
        updateCharacterModel();
    }

    private void updateCharacterModel() {
        if (client == null) {
            return;
        }
        ModelPart modelPart = client.getEntityModelLoader().getModelPart(
                slimSkin ? EntityModelLayers.PLAYER_SLIM : EntityModelLayers.PLAYER);
        characterModel = new PlayerEntityModel<>(modelPart, slimSkin);
        characterModel.setVisible(true);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        drawBackground(context);
        drawBrand(context);
        drawMenu(context, mouseX, mouseY, delta);
        renderCharacter(context, mouseX, mouseY, delta);
        drawAccount(context, mouseX, mouseY);
        context.drawText(textRenderer, "NEXORA CLIENT  /  " + MOD_VERSION,
                22, height - 24, 0xFFBCA9FF, false);
        context.drawText(textRenderer, "PLAY BETTER. FASTER.", width - textRenderer.getWidth("PLAY BETTER. FASTER.") - 22,
                height - 24, 0xFFBCA9FF, false);
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
        return width * 39 / 100;
    }

    private int playerCenterX() {
        return width * 71 / 100;
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
        return Math.min(76, Math.max(32, height / 9));
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

    private int accountWidth() {
        return Math.min(width * 22 / 100, Math.max(150, width * 18 / 100));
    }

    private int accountX() {
        return playerCenterX() - accountWidth() / 2;
    }

    private int accountY() {
        return Math.round(height * 0.13f);
    }

    private void drawAccount(DrawContext context, int mouseX, int mouseY) {
        int x = accountX();
        int y = accountY();
        int w = accountWidth();
        String name = client == null ? "Player" : client.getSession().getUsername();
        context.drawCenteredTextWithShadow(textRenderer, name, x + w / 2 - 6, y + 4, 0xFFF1EDFF);
        context.drawCenteredTextWithShadow(textRenderer, accountDropdown ? "⌃" : "⌄",
                x + w / 2 + textRenderer.getWidth(name) / 2 + 6, y + 4, PURPLE);
        context.fill(x + w / 2 - 14, y + 20, x + w / 2 + 14, y + 21,
                accountDropdown ? 0xFF9B38FF : 0x60433A5A);
        if (!accountDropdown) {
            return;
        }
        int dropdownY = y + 25;
        context.fill(x + 24, dropdownY, x + w - 24, dropdownY + 42, 0xE610111C);
        context.fill(x + 24, dropdownY, x + w - 24, dropdownY + 1, 0xFF9B38FF);
        context.drawCenteredTextWithShadow(textRenderer, name, x + w / 2, dropdownY + 6, 0xFFF1EDFF);
        context.drawCenteredTextWithShadow(textRenderer, "Add accounts in your launcher",
                x + w / 2, dropdownY + 23, 0xFFB7AFCB);
    }

    private void renderCharacter(DrawContext context, int mouseX, int mouseY, float delta) {
        if (client == null || skinTexture == null || characterModel == null) {
            return;
        }
        float entrance = MathHelper.clamp((System.currentTimeMillis() - openedAt) / 900.0f, 0, 1);
        float eased = 1 - (float) Math.pow(1 - entrance, 3);
        long elapsed = System.currentTimeMillis() - openedAt;
        float idle = (float) Math.sin(elapsed * 0.002) * 0.025f;
        float walk = (float) Math.sin(elapsed * 0.012) * 0.75f * (1 - entrance);
        float scale = Math.min(height * 0.62f / 2, width * 0.19f);
        float centerX = playerCenterX() + (1 - eased) * width * 0.25f;
        float top = height * 0.23f + (float) Math.sin(elapsed * 0.002) * 2.0f;
        float headCenterY = top + scale * 0.25f;
        float focalLength = Math.max(1, height * 0.52f);
        float targetLookYaw = dragging ? 0
                : MathHelper.clamp((float) Math.atan2(mouseX - centerX, focalLength), -1.1f, 1.1f);
        float targetLookPitch = dragging ? 0
                : MathHelper.clamp((float) Math.atan2(mouseY - headCenterY, focalLength), -0.75f, 0.75f);
        float lookSmoothing = MathHelper.clamp(delta * 0.2f, 0, 1);
        lookYaw = MathHelper.lerp(lookSmoothing, lookYaw, targetLookYaw);
        lookPitch = MathHelper.lerp(lookSmoothing, lookPitch, targetLookPitch);
        characterModel.head.yaw = lookYaw;
        characterModel.head.pitch = lookPitch;
        characterModel.head.xScale = 0.84f;
        characterModel.head.yScale = 0.84f;
        characterModel.head.zScale = 0.84f;
        characterModel.hat.copyTransform(characterModel.head);
        characterModel.body.yaw = idle;
        characterModel.rightArm.pitch = walk + 0.08f;
        characterModel.leftArm.pitch = -walk + 0.08f;
        characterModel.rightLeg.pitch = -walk;
        characterModel.leftLeg.pitch = walk;
        characterModel.jacket.copyTransform(characterModel.body);
        characterModel.rightSleeve.copyTransform(characterModel.rightArm);
        characterModel.leftSleeve.copyTransform(characterModel.leftArm);
        characterModel.rightPants.copyTransform(characterModel.rightLeg);
        characterModel.leftPants.copyTransform(characterModel.leftLeg);

        MatrixStack matrices = context.getMatrices();
        DiffuseLighting.enableGuiDepthLighting();
        try {
            context.draw(() -> {
                matrices.push();
                try {
                    matrices.translate(centerX, top + scale * 0.5f, 150);
                    matrices.scale(scale, scale, scale);
                    matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(rotation + 180));
                    VertexConsumer vertexConsumer = context.getVertexConsumers()
                            .getBuffer(RenderLayer.getEntityCutoutNoCull(skinTexture));
                    characterModel.render(matrices, vertexConsumer, LightmapTextureManager.MAX_LIGHT_COORDINATE,
                            OverlayTexture.DEFAULT_UV, 0xFFFFFFFF);
                } finally {
                    matrices.pop();
                }
            });
        } finally {
            DiffuseLighting.disableGuiDepthLighting();
        }
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
        int accountX = accountX();
        int accountY = accountY();
        int accountWidth = accountWidth();
        if (inside(mouseX, mouseY, accountX, accountY, accountWidth, 34)) {
            accountDropdown = !accountDropdown;
            return true;
        }
        if (accountDropdown && inside(mouseX, mouseY, accountX + 24, accountY + 25,
                accountWidth - 48, 42)) {
            accountDropdown = false;
            return true;
        }
        if (accountDropdown) {
            accountDropdown = false;
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
