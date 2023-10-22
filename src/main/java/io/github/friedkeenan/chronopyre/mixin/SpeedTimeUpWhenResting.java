package io.github.friedkeenan.chronopyre.mixin;

import java.util.function.BooleanSupplier;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;

import io.github.friedkeenan.chronopyre.RestHandler;
import io.github.friedkeenan.chronopyre.RestStatus;
import io.github.friedkeenan.chronopyre.Rester;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetTimePacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.GameRules;

@Mixin(ServerLevel.class)
public class SpeedTimeUpWhenResting implements RestHandler {
    private static final long DAY_LENGTH      = 24000L;
    private static final long REST_TIME_SPEED = 5L;

    private static final Component RESTING         = Component.translatable("block.minecraft.campfire.rest.resting");
    private static final Component CANCEL_RESTING  = Component.translatable("block.minecraft.campfire.rest.cancel");
    private static final String    PLAYERS_RESTING = "block.minecraft.campfire.rest.players_resting";

    private RestStatus rest_status = new RestStatus();

    private long target_time = -1;

    private ServerLevel asServerLevel() {
        return (ServerLevel) (Object) this;
    }

    @Override
    public RestStatus getRestStatus() {
        return this.rest_status;
    }

    private void announceRestStatus() {
        final var level = this.asServerLevel();

        int percentage_needed = level.getGameRules().getInt(GameRules.RULE_PLAYERS_SLEEPING_PERCENTAGE);

        Component message;
        if (this.rest_status.areEnoughResting(percentage_needed)) {
            message = RESTING;
        } else if (this.rest_status.amountResting() <= 0) {
            message = CANCEL_RESTING;
        } else {
            message = Component.translatable(PLAYERS_RESTING, this.rest_status.amountResting(), this.rest_status.restersNeeded(percentage_needed));
        }

        for (final var player : level.players()) {
            player.displayClientMessage(message, true);
        }
    }

    @Override
    public void updateRestingPlayerList() {
        final var players = this.asServerLevel().players();

        if (!players.isEmpty() && this.rest_status.update(players)) {
            this.announceRestStatus();
        }
    }

    private void rouseAllPlayers() {
        for (final var player : this.asServerLevel().players()) {
            final var rester = (Rester) player;

            if (rester.isResting()) {
                rester.stopResting(false);
            }
        }

        this.rest_status.removeAllResters();
    }

    private void setAndBroadcastDayTime(long time) {
        final var level = this.asServerLevel();

        level.setDayTime(time);

        level.getServer().getPlayerList().broadcastAll(
            new ClientboundSetTimePacket(level.getGameTime(), time, level.getGameRules().getBoolean(GameRules.RULE_DAYLIGHT)),
            level.dimension()
        );
    }

    @ModifyExpressionValue(
        at = @At(
            value  = "INVOKE",
            target = "Lnet/minecraft/world/level/GameRules;getBoolean(Lnet/minecraft/world/level/GameRules$Key;)Z"
        ),
        method = "tickTime"
    )
    private boolean shouldDoNormalTimeAdvance(boolean original) {
        if (this.target_time >= 0) {
            return false;
        }

        return original;
    }

    @Shadow
    private void resetWeatherCycle() {
        throw new AssertionError();
    }

    @Inject(
        at = @At(
            value  = "INVOKE",
            target = "Lnet/minecraft/server/level/ServerLevel;advanceWeatherCycle()V",
            shift  = At.Shift.AFTER
        ),

        method = "tick"
    )
    private void speedTimeUp(BooleanSupplier supplier, CallbackInfo info) {
        final var level = this.asServerLevel();

        final var current_time      = level.getLevelData().getDayTime();
        final var percentage_needed = level.getGameRules().getInt(GameRules.RULE_PLAYERS_SLEEPING_PERCENTAGE);

        if (this.rest_status.areEnoughResting(percentage_needed)) {
            if (this.target_time < 0) {
                this.target_time  = current_time + DAY_LENGTH;
                this.target_time -= this.target_time % DAY_LENGTH;
            }
        } else {
            this.target_time = -1;
        }

        if (current_time < this.target_time) {
            final var new_time = Math.min(this.target_time, current_time + REST_TIME_SPEED);

            /*
                We need to broadcast the time ourselves since
                the server will only do it once per second.
            */
            this.setAndBroadcastDayTime(new_time);

            if (new_time >= this.target_time) {
                this.target_time = -1;

                this.rouseAllPlayers();

                if (level.getGameRules().getBoolean(GameRules.RULE_WEATHER_CYCLE)) {
                    this.resetWeatherCycle();
                }
            }
        }
    }
}
