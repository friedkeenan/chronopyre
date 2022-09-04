package io.github.friedkeenan.chronopyre.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import com.mojang.datafixers.util.Either;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Player.BedSleepingProblem;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;

@Mixin(BedBlock.class)
public class DisableSleeping {
    @Redirect(
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/entity/player/Player;startSleepInBed(Lnet/minecraft/core/BlockPos;)Lcom/mojang/datafixers/util/Either;"
        ),

        method      = "use"
    )
    private Either<BedSleepingProblem, Unit> disallowSleepingUsingBeds(Player player, BlockPos pos) {
        /* We still allow setting the respawn point, just not sleeping. */

        if (!(player instanceof ServerPlayer)) {
            return Either.right(Unit.INSTANCE);
        }

        final var server_player = (ServerPlayer) player;

        Direction direction = server_player.level.getBlockState(pos).getValue(HorizontalDirectionalBlock.FACING);

        if (server_player.isSleeping() || !server_player.isAlive()) {
            return Either.left(Player.BedSleepingProblem.OTHER_PROBLEM);
        }

        if (!server_player.level.dimensionType().natural()) {
            return Either.left(Player.BedSleepingProblem.NOT_POSSIBLE_HERE);
        }

        final var bed_accessor = (ServerPlayerBedAccessor) server_player;

        if (!bed_accessor.invokeBedInRange(pos, direction)) {
            return Either.left(Player.BedSleepingProblem.TOO_FAR_AWAY);
        }

        if (bed_accessor.invokeBedBlocked(pos, direction)) {
            return Either.left(Player.BedSleepingProblem.OBSTRUCTED);
        }

        server_player.setRespawnPosition(server_player.level.dimension(), pos, server_player.getYRot(), false, true);

        return Either.right(Unit.INSTANCE);
    }
}
