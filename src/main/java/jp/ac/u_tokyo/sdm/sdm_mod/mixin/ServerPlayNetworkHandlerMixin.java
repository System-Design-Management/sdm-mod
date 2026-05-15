package jp.ac.u_tokyo.sdm.sdm_mod.mixin;

import jp.ac.u_tokyo.sdm.sdm_mod.game.CommandLockState;
import net.minecraft.network.packet.c2s.play.ClickSlotC2SPacket;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayNetworkHandler.class)
public class ServerPlayNetworkHandlerMixin {

    @Shadow
    public ServerPlayerEntity player;

    /**
     * Q キーによるアイテムドロップをブロック。
     * クライアントの先読みによる desync を防ぐため、ブロック時にインベントリを強制同期する。
     */
    @Redirect(
        method = "onPlayerAction",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerPlayerEntity;dropSelectedItem(Z)Z")
    )
    private boolean redirectDropSelectedItem(ServerPlayerEntity player, boolean entireStack) {
        if (CommandLockState.isLocked()) {
            player.playerScreenHandler.updateToClient();
            return false;
        }
        return player.dropSelectedItem(entireStack);
    }

    /** インベントリ画面からのスロー操作によるアイテムドロップをブロック */
    @Inject(method = "onClickSlot", at = @At("HEAD"), cancellable = true)
    private void onHandleClickSlot(ClickSlotC2SPacket packet, CallbackInfo ci) {
        if (!CommandLockState.isLocked()) return;
        if (packet.actionType() == SlotActionType.THROW) {
            ci.cancel();
        }
    }
}
