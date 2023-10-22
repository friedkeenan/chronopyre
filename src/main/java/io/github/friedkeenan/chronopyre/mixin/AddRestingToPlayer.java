package io.github.friedkeenan.chronopyre.mixin;

import java.util.Optional;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import io.github.friedkeenan.chronopyre.Rester;
import io.github.friedkeenan.chronopyre.RestingProblem;
import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

@Mixin(Player.class)
public abstract class AddRestingToPlayer extends LivingEntity implements Rester {
    protected AddRestingToPlayer(EntityType<? extends LivingEntity> entityType, Level level) {
        super(entityType, level);
    }

    private static final EntityDataAccessor<Optional<BlockPos>> RESTING_POS_ID = SynchedEntityData.defineId(Player.class, EntityDataSerializers.OPTIONAL_BLOCK_POS);

    @Inject(at = @At("TAIL"), method = "defineSynchedData")
    private void defineRestingPos(CallbackInfo info) {
        this.entityData.define(RESTING_POS_ID, Optional.empty());
    }

    @Override
    public Optional<BlockPos> getRestingPos() {
        return this.entityData.get(RESTING_POS_ID);
    }

    @Override
    public void setRestingPos(BlockPos pos) {
        this.entityData.set(RESTING_POS_ID, Optional.of(pos));
    }

    @Override
    public void clearRestingPos() {
        this.entityData.set(RESTING_POS_ID, Optional.empty());
    }

    @Override
    public boolean isResting() {
        return this.getRestingPos().isPresent();
    }

    @Override
    public Optional<RestingProblem> startResting(Level level, BlockState state, BlockPos pos) {
        this.setRestingPos(pos);

        return Optional.empty();
    }

    @Override
    public void stopResting(boolean should_update_resters) {
        this.clearRestingPos();
    }

    private boolean campfireInRange(double radius, BlockPos pos) {
        final var real_pos = Vec3.atCenterOf(pos);

        return (
            Math.abs(this.getX() - real_pos.x()) <= radius &&
            Math.abs(this.getY() - real_pos.y()) <= radius &&
            Math.abs(this.getZ() - real_pos.z()) <= radius
        );
    }

    private boolean checkCampfireIsValid() {
        return this.getRestingPos().map(rest_pos -> {
            final var rest_state = this.level.getBlockState(rest_pos);

            if (!(rest_state.getBlock() instanceof CampfireBlock)) {
                return false;
            }

            if (!rest_state.getValue(CampfireBlock.LIT).booleanValue()) {
                return false;
            }

            return this.campfireInRange(rest_state.getLightEmission() + 0.5d, rest_pos);
        }).orElse(false);
    }

    @Inject(at = @At("HEAD"), method = "tick")
    private void validateResting(CallbackInfo info) {
        if (this.isResting() && !this.checkCampfireIsValid()) {
            this.stopResting(true);
        }
    }

    @WrapOperation(
        at = @At(
            value   = "INVOKE",
            target  = "Lnet/minecraft/world/entity/player/Player;isSleeping()Z",
            ordinal = 1
        ),

        method = "tick"
    )
    private boolean grantTimeSinceLastRestAward(Player player, Operation<Boolean> original) {
        return original.call(player) || ((Rester) player).isResting();
    }
}
