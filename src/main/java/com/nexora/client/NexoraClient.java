package com.nexora.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public final class NexoraClient implements ClientModInitializer {
    private static KeyBinding toggleFpsUnlock;

    @Override
    public void onInitializeClient() {
        toggleFpsUnlock = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.nexora.toggle_fps_unlock",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_F8,
                "category.nexora"
        ));
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (toggleFpsUnlock.wasPressed()) {
                FpsUnlocker.toggle();
            }
        });
    }
}
