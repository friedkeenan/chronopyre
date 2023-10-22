package io.github.friedkeenan.chronopyre.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import io.github.friedkeenan.chronopyre.RestHandler;
import net.minecraft.server.level.ServerLevel;

@Mixin(targets = {"net/minecraft/server/level/ServerLevel$EntityCallbacks"})
public class UpdateRestersOnTrackingStartAndEnd {
    /*
        NOTE: We could use 'Inject' for these but then we'd
        need to shadow the anonymous 'ServerLevel' field.
    */

    @WrapOperation(
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/server/level/ServerLevel;updateSleepingPlayerList()V"
        ),

        method = "onTrackingStart"
    )
    private void updateRestersOnTrackingStart(ServerLevel level, Operation<Void> original) {
        original.call(level);

        ((RestHandler) level).updateRestingPlayerList();
    }

    @WrapOperation(
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/server/level/ServerLevel;updateSleepingPlayerList()V"
        ),

        method = "onTrackingEnd"
    )
    private void updateRestersOnTrackingEnd(ServerLevel level, Operation<Void> original) {
        original.call(level);

        ((RestHandler) level).updateRestingPlayerList();
    }
}
