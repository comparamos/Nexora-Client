package com.nexora.client.mixin;

import com.nexora.client.gui.NexoraTitleScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.TitleScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TitleScreen.class)
public abstract class TitleScreenMixin {
    @Inject(method = "init", at = @At("HEAD"), cancellable = true)
    private void nexora$replaceTitleScreen(CallbackInfo callback) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (!(client.currentScreen instanceof NexoraTitleScreen)) {
            callback.cancel();
            client.setScreen(new NexoraTitleScreen());
        }
    }
}
