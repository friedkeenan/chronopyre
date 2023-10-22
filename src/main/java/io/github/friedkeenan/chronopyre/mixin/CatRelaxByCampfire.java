package io.github.friedkeenan.chronopyre.mixin;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.llamalad7.mixinextras.injector.WrapWithCondition;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import io.github.friedkeenan.chronopyre.Rester;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Cat;
import net.minecraft.world.entity.player.Player;

@Mixin(targets = {"net.minecraft.world.entity.animal.Cat$CatRelaxOnOwnerGoal"})
public class CatRelaxByCampfire {
    @Shadow
    @Nullable
    private Player ownerPlayer;

    @Shadow
    @Nullable
    private BlockPos goalPos;

    @WrapOperation(
        at = @At(
            value  = "INVOKE",
            target = "Lnet/minecraft/world/entity/LivingEntity;isSleeping()Z"
        ),

        method = "canUse"
    )
    private boolean isSleepingOrRestingCanUse(LivingEntity entity, Operation<Boolean> original) {
        /* We already know the entity is a player so the cast is safe. */
        return original.call(entity) || ((Rester) entity).isResting();
    }

    @WrapOperation(
        at = @At(
            value  = "INVOKE",
            target = "Lnet/minecraft/world/entity/player/Player;isSleeping()Z"
        ),

        method = "canContinueToUse"
    )
    private boolean isSleepingOrRestingCanContinueToUse(Player player, Operation<Boolean> original) {
        return original.call(player) || ((Rester) player).isResting();
    }

    @Shadow
    private boolean spaceIsOccupied() {
        throw new AssertionError();
    }

    @Inject(
        at = @At(
            value  = "INVOKE",
            target = "Lnet/minecraft/world/entity/player/Player;blockPosition()Lnet/minecraft/core/BlockPos;"
        ),

        method      = "canUse",
        cancellable = true
    )
    private void relaxByFire(CallbackInfoReturnable<Boolean> info) {
        /* We only want to customize logic for resting. */
        if (this.ownerPlayer.isSleeping()) {
            return;
        }

        final var rester = (Rester) this.ownerPlayer;

        /* We know the player is resting. */
        this.goalPos = rester.getRestingPos().get();

        info.setReturnValue(!this.spaceIsOccupied());
    }

    @WrapOperation(
        at = @At(
            value  = "INVOKE",
            target = "Lnet/minecraft/world/entity/player/Player;getSleepTimer()I"
        ),

        method = "stop"
    )
    private int hasAlwaysRestedLongEnough(Player player, Operation<Integer> original) {
        if (player.isSleeping()) {
            return original.call(player);
        }

        /*
            If we're resting, always have rested long enough.
            The other logic in the method makes this work out.
        */
        return 100;
    }

    @WrapWithCondition(
        at = @At(
            value  = "INVOKE",
            target = "Lnet/minecraft/world/entity/animal/Cat;randomTeleport(DDDZ)Z"
        ),

        method = "giveMorningGift"
    )
    private boolean disableTeleportWhenResting(Cat cat, double x, double y, double z, boolean broadcast_event) {
        return this.ownerPlayer.isSleeping();
    }
}
