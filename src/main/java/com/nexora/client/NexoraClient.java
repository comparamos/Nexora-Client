package com.nexora.client;

import com.nexora.client.gui.NexoraClientSettingsScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public final class NexoraClient implements ClientModInitializer {
    private static final String KEY_CATEGORY = "key.categories.nexora";
    private static final String OPEN_SETTINGS_KEY = "key.nexora.open_client_settings";

    @Override
    public void onInitializeClient() {
        KeyBinding openSettingsKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                OPEN_SETTINGS_KEY,
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_RIGHT_SHIFT,
                KEY_CATEGORY
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (openSettingsKey.wasPressed()) {
                if (client.player != null && !(client.currentScreen instanceof NexoraClientSettingsScreen)) {
                    client.setScreen(new NexoraClientSettingsScreen(client.currentScreen));
                }
            }
        });
    }
}
