package io.github.friedkeenan.chronopyre.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import io.github.friedkeenan.chronopyre.Rester;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.world.entity.player.Player;

@Mixin(ClientPacketListener.class)
public class HijackStopSleepPacket {
    @Redirect(
        at = @At(
            value  = "INVOKE",
            target = "Lnet/minecraft/world/entity/player/Player;stopSleepInBed(ZZ)V"
        ),

        method = "handleAnimate"
    )
    private void StopSleepOrRest(Player player, boolean reset_sleep_timer, boolean update_sleeping_list) {
        final var rester = (Rester) player;

        if (player.isSleeping()) {
            player.stopSleepInBed(reset_sleep_timer, update_sleeping_list);
        } else if (rester.isResting()) {
            ((Rester) player).stopResting(update_sleeping_list);
        }
    }
}
