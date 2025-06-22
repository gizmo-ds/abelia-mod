package dev.aika.abelia.forge.mixin;

import dev.aika.abelia.Abelia;
import dev.aika.abelia.mixin.client.ScreenInvoker;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.contents.TranslatableContents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TitleScreen.class)
public abstract class TitleScreenMixin {
    @Inject(method = "init", at = @At("RETURN"))
    public void fixModButtonWidth(CallbackInfo ci) {
        if (!Abelia.CONFIG.get().isHideRealmsButton()) return;
        for (final Renderable renderable : ((ScreenInvoker) this).getRenderables()) {
            if (renderable instanceof Button button &&
                    button.getMessage().getContents() instanceof TranslatableContents tc &&
                    tc.getKey().equals("fml.menu.mods"))
                button.setWidth(200);
        }
    }
}
