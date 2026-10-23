package com.nexora.client.mixin;

import com.nexora.client.FpsUnlocker;
import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MinecraftClient.class)
public abstract class MinecraftClientMixin {
    @Inject(method = "getFramerateLimit", at = @At("HEAD"), cancellable = true)
    private void nexora$unlockFramerate(CallbackInfoReturnable<Integer> callback) {
        if (FpsUnlocker.isEnabled()) {
            callback.setReturnValue(Integer.MAX_VALUE);
        }
    }
}
