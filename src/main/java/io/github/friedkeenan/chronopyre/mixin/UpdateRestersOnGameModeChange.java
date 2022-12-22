package io.github.friedkeenan.chronopyre.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import io.github.friedkeenan.chronopyre.RestHandler;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayerGameMode;

@Mixin(ServerPlayerGameMode.class)
public class UpdateRestersOnGameModeChange {
    @Redirect(
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/server/level/ServerLevel;updateSleepingPlayerList()V"
        ),

        method = "changeGameModeForPlayer"
    )
    private void updateResters(ServerLevel level) {
        level.updateSleepingPlayerList();

        ((RestHandler) level).updateRestingPlayerList();
    }
}
