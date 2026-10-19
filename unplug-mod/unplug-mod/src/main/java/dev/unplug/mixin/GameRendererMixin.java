package dev.unplug.mixin;

import dev.unplug.UnplugManager;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.RenderTickCounter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public class GameRendererMixin {
    // While unplugged: skip all rendering (GPU ~idle) and sleep a bit so the
    // main loop doesn't spin. Ticking + networking keep running normally.
    @Inject(method = "render(Lnet/minecraft/client/render/RenderTickCounter;Z)V",
            at = @At("HEAD"), cancellable = true)
    private void unplug$skipRender(RenderTickCounter tickCounter, boolean tick, CallbackInfo ci) {
        if (UnplugManager.isUnplugged()) {
            try {
                Thread.sleep(25);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            ci.cancel();
        }
    }
}
