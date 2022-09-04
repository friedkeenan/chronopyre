package io.github.friedkeenan.chronopyre.mixin;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

@Mixin(CampfireBlock.class)
public abstract class CampfireUnlitByDefault extends BaseEntityBlock {
    protected CampfireUnlitByDefault(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Inject(at = @At("TAIL"), method = "<init>")
    private void changeDefaultState(boolean spawn_particles, int fire_damage, BlockBehaviour.Properties properties, CallbackInfo info) {
        this.registerDefaultState(this.defaultBlockState().setValue(CampfireBlock.LIT, false));
    }

    @Inject(at = @At("TAIL"), method = "getStateForPlacement", cancellable = true)
    private void changePlacementState(BlockPlaceContext context, CallbackInfoReturnable<@Nullable BlockState> info) {
        final var state = info.getReturnValue();

        info.setReturnValue(state.setValue(CampfireBlock.LIT, false));
    }
}
