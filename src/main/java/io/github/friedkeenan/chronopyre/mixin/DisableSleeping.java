package io.github.friedkeenan.chronopyre.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

@Mixin(BedBlock.class)
public class DisableSleeping {
    @Inject(
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/entity/player/Player;startSleepInBed(Lnet/minecraft/core/BlockPos;)Lcom/mojang/datafixers/util/Either;"
        ),

        method      = "use",
        cancellable = true
    )
    private void disableSleepingUsingBeds(
        BlockState      state,
        Level           level,
        BlockPos        pos,
        Player          player,
        InteractionHand hand,
        BlockHitResult  hit_result,

        CallbackInfoReturnable<InteractionResult> info
    ) {
        info.setReturnValue(InteractionResult.PASS);
    }
}
