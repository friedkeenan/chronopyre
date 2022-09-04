package io.github.friedkeenan.chronopyre.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;

@Mixin(ServerPlayer.class)
public interface ServerPlayerBedAccessor {
    @Invoker
    boolean invokeBedInRange(BlockPos pos, Direction direction);

    @Invoker
    boolean invokeBedBlocked(BlockPos pos, Direction direction);
}
