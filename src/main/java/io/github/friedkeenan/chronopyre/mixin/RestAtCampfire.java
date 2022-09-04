package io.github.friedkeenan.chronopyre.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import io.github.friedkeenan.chronopyre.Rester;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

@Mixin(CampfireBlock.class)
public class RestAtCampfire {
    @Inject(at = @At("TAIL"), method = "use", cancellable = true)
    private void startRestingOnUse(
        BlockState      state,
        Level           level,
        BlockPos        pos,
        Player          player,
        InteractionHand hand,
        BlockHitResult  hit_result,

        CallbackInfoReturnable<InteractionResult> info
    ) {
        final var result = info.getReturnValue();

        if (result != InteractionResult.PASS) {
            return;
        }

        /* Require an empty main hand to rest. */
        if (!player.getItemInHand(hand).isEmpty() || hand != InteractionHand.MAIN_HAND) {
            return;
        }

        final var rester = (Rester) player;

        if (rester.isResting() && rester.getRestingPos().get().equals(pos)) {
            rester.stopResting(true);
        } else {
            rester.startResting(level, state, pos).ifPresent(
                problem -> player.displayClientMessage(problem.message, true)
            );
        }

        info.setReturnValue(InteractionResult.SUCCESS);
    }
}
