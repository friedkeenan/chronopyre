package io.github.friedkeenan.chronopyre.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import io.github.friedkeenan.chronopyre.Rester;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.world.entity.player.Player;

@Mixin(ClientPacketListener.class)
public class HijackStopSleepPacket {
    @WrapOperation(
        at = @At(
            value  = "INVOKE",
            target = "Lnet/minecraft/world/entity/player/Player;stopSleepInBed(ZZ)V"
        ),

        method = "handleAnimate"
    )
    private void stopSleepOrRest(Player player, boolean reset_sleep_timer, boolean update_sleeping_list, Operation<Void> original) {
        final var rester = (Rester) player;

        if (player.isSleeping()) {
            original.call(player, reset_sleep_timer, update_sleeping_list);
        } else if (rester.isResting()) {
            ((Rester) player).stopResting(update_sleeping_list);
        }
    }
}
