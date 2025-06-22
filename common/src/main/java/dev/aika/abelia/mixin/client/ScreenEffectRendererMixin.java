package dev.aika.abelia.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.aika.abelia.Abelia;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.ScreenEffectRenderer;
import net.minecraft.world.effect.MobEffects;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ScreenEffectRenderer.class)
public abstract class ScreenEffectRendererMixin {
    @Inject(method = "renderFire", at = @At("HEAD"), cancellable = true)
    private static void renderFire(Minecraft minecraft, PoseStack poseStack, CallbackInfo ci) {
        boolean hideFireOverlayWhenFireResistance = Abelia.CONFIG.get().isHideFireOverlayWhenFireResistance();
        if (!hideFireOverlayWhenFireResistance) return;
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return;
        if (player.isCreative()) {
            ci.cancel();
            return;
        }
        if (player.getEffect(MobEffects.FIRE_RESISTANCE) != null) ci.cancel();
    }
}
