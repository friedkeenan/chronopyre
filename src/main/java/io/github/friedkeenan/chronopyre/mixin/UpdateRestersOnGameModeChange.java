package io.github.friedkeenan.chronopyre.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import io.github.friedkeenan.chronopyre.RestHandler;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayerGameMode;
import net.minecraft.world.level.GameType;

@Mixin(ServerPlayerGameMode.class)
public class UpdateRestersOnGameModeChange {
    @Shadow
    private ServerLevel level;

    @Inject(
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/server/level/ServerLevel;updateSleepingPlayerList()V"
        ),

        method = "changeGameModeForPlayer"
    )
    private void updateResters(GameType game_type, CallbackInfoReturnable<Boolean> info) {
        ((RestHandler) this.level).updateRestingPlayerList();
    }
}
