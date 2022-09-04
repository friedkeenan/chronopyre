package io.github.friedkeenan.chronopyre;

import java.util.Optional;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public interface Rester {
    Optional<BlockPos> getRestingPos();
    void setRestingPos(BlockPos pos);
    void clearRestingPos();

    boolean isResting();

    Optional<RestingProblem> startResting(Level level, BlockState state, BlockPos pos);
    void stopResting(boolean should_update_resters);

}
