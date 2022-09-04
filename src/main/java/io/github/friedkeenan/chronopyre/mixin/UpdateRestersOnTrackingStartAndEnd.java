package io.github.friedkeenan.chronopyre.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import io.github.friedkeenan.chronopyre.RestHandler;
import net.minecraft.server.level.ServerLevel;

@Mixin(targets = {"net/minecraft/server/level/ServerLevel$EntityCallbacks"})
public class UpdateRestersOnTrackingStartAndEnd {
    @Redirect(
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/server/level/ServerLevel;updateSleepingPlayerList()V"
        ),

        method = "onTrackingStart"
    )
    private void updateRestersOnTrackingStart(ServerLevel level) {
        level.updateSleepingPlayerList();

        ((RestHandler) level).updateRestingPlayerList();
    }

    @Redirect(
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/server/level/ServerLevel;updateSleepingPlayerList()V"
        ),

        method = "onTrackingEnd"
    )
    private void updateRestersOnTrackingEnd(ServerLevel level) {
        level.updateSleepingPlayerList();

        ((RestHandler) level).updateRestingPlayerList();
    }
}
