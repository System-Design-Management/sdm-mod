package jp.ac.u_tokyo.sdm.sdm_mod.mixin;

import jp.ac.u_tokyo.sdm.sdm_mod.game.CommandLockState;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerCommandSource.class)
public class ServerCommandSourceMixin {

    @Inject(method = "hasPermissionLevel", at = @At("HEAD"), cancellable = true)
    private void onHasPermissionLevel(int level, CallbackInfoReturnable<Boolean> cir) {
        if (CommandLockState.isLocked()) {
            ServerCommandSource source = (ServerCommandSource) (Object) this;
            if (source.getEntity() instanceof ServerPlayerEntity) {
                cir.setReturnValue(false);
            }
        }
    }
}
