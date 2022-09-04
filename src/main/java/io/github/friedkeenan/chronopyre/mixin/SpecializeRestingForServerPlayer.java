package io.github.friedkeenan.chronopyre.mixin;

import java.util.Optional;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import io.github.friedkeenan.chronopyre.ChronopyreMod;
import io.github.friedkeenan.chronopyre.RestHandler;
import io.github.friedkeenan.chronopyre.RestingProblem;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ClientboundAnimatePacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stat;
import net.minecraft.stats.Stats;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;

@Mixin(ServerPlayer.class)
public abstract class SpecializeRestingForServerPlayer extends AddRestingToPlayer {
    protected SpecializeRestingForServerPlayer(EntityType<? extends LivingEntity> entityType, Level level) {
        super(entityType, level);
    }

    @Shadow
    private void resetStat(Stat<?> stat) {
        throw new AssertionError();
    }

    @Override
    public Optional<RestingProblem> startResting(Level level, BlockState state, BlockPos pos) {
        if (this.isResting()) {
            return Optional.of(RestingProblem.ALREADY_RESTING);
        }

        if (!level.dimensionType().bedWorks() || !level.dimensionType().natural()) {
            return Optional.of(RestingProblem.NOT_ALLOWED_IN_DIMENSION);
        }

        if (!level.getGameRules().getBoolean(GameRules.RULE_DAYLIGHT) || !((ServerLevel) level).canSleepThroughNights()) {
            return Optional.of(RestingProblem.IMPOSSIBLE);
        }

        if (!state.getValue(CampfireBlock.LIT).booleanValue()) {
            return Optional.of(RestingProblem.UNLIT);
        }

        if (level.isDay()) {
            return Optional.of(RestingProblem.NOT_POSSIBLE_NOW);
        }

        final var maybe_problem = super.startResting(level, state, pos);

        this.resetStat(Stats.CUSTOM.get(Stats.TIME_SINCE_REST));

        ((RestHandler) this.level).updateRestingPlayerList();

        return maybe_problem;
    }

    private void extinguishCampfireAt(BlockPos rest_pos) {
        final var rest_state = this.level.getBlockState(rest_pos);

        if (!(rest_state.getBlock() instanceof CampfireBlock)) {
            return;
        }

        if (!rest_state.getValue(CampfireBlock.LIT).booleanValue()) {
            return;
        }

        this.level.levelEvent(null, 1009, rest_pos, 0);

        final var packet_data = PacketByteBufs.create().writeBlockPos(rest_pos);
        for (final var player : PlayerLookup.tracking((ServerLevel) this.level, rest_pos)) {
            ServerPlayNetworking.send(player, ChronopyreMod.DOWSE_CAMPFIRE_ID, packet_data);
        }

        CampfireBlock.dowse(null, this.level, rest_pos, rest_state);

        final var unlit_state = rest_state.setValue(CampfireBlock.LIT, false);
        this.level.setBlock(rest_pos, unlit_state, 11);
        this.level.gameEvent(GameEvent.BLOCK_CHANGE, rest_pos, GameEvent.Context.of(null, unlit_state));
    }

    @Override
    public void stopResting(boolean should_update_resters) {
        final var server_level = (ServerLevel) this.level;
        final var rest_handler = (RestHandler) server_level;

        final var rest_pos = this.getRestingPos().get();

        if (rest_handler.getRestStatus().decrementRestersAt(rest_pos)) {
            this.extinguishCampfireAt(rest_pos);
        }

        server_level.getChunkSource().broadcastAndSend(this, new ClientboundAnimatePacket(this, 2));

        super.stopResting(should_update_resters);

        if (should_update_resters) {
            rest_handler.updateRestingPlayerList();
        }
    }

    @Inject(at = @At("TAIL"), method = "disconnect")
    private void stopRestingOnDisconnect(CallbackInfo info) {
        if (this.isResting()) {
            this.stopResting(false);
        }
    }
}
