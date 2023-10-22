package io.github.friedkeenan.chronopyre.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;

import io.github.friedkeenan.chronopyre.Rester;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.state.BlockState;

@Mixin(CampfireBlock.class)
public class RestAtCampfire {
    @ModifyReturnValue(at = @At("RETURN"), method = "use")
    private InteractionResult startRestingOnUse(
        InteractionResult original,

        @Local(argsOnly = true) BlockState      state,
        @Local(argsOnly = true) Level           level,
        @Local(argsOnly = true) BlockPos        pos,
        @Local(argsOnly = true) Player          player,
        @Local(argsOnly = true) InteractionHand hand
    ) {

        if (original != InteractionResult.PASS) {
            return original;
        }

        /* Require an empty main hand to rest. */
        if (!player.getItemInHand(hand).isEmpty() || hand != InteractionHand.MAIN_HAND) {
            return InteractionResult.PASS;
        }

        final var rester = (Rester) player;

        if (rester.isResting() && rester.getRestingPos().get().equals(pos)) {
            rester.stopResting(true);
        } else {
            rester.startResting(level, state, pos).ifPresent(
                problem -> player.displayClientMessage(problem.message, true)
            );
        }

        return InteractionResult.SUCCESS;
    }
}
