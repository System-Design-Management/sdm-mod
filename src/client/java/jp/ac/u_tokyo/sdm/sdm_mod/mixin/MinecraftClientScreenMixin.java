package jp.ac.u_tokyo.sdm.sdm_mod.mixin;

import jp.ac.u_tokyo.sdm.sdm_mod.client.ClientCommandLockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.screen.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public class MinecraftClientScreenMixin {

    @Inject(method = "setScreen", at = @At("HEAD"), cancellable = true)
    private void onSetScreen(Screen screen, CallbackInfo ci) {
        if (!ClientCommandLockState.isLocked() || screen == null) return;

        // バニラのチャット/コマンド入力画面をブロック
        if (screen instanceof ChatScreen) {
            ci.cancel();
            return;
        }

        // Controlify の仮想キーボードオーバーレイをブロック（ハードデペンデンシーなし）
        String className = screen.getClass().getName();
        if (className.equals("dev.isxander.controlify.screenop.keyboard.KeyboardOverlayScreen")) {
            ci.cancel();
        }
    }
}
