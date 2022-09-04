package io.github.friedkeenan.chronopyre;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;

public class RestStatus {
    public int active_players = 0;
    public int resting_players = 0;

    public Map<BlockPos, Integer> rest_positions = new HashMap<BlockPos, Integer>();

    public boolean areEnoughResting(int percentage) {
        return this.resting_players >= this.restersNeeded(percentage);
    }

    public int restersNeeded(int percentage) {
        return Math.max(1, Mth.ceil((float)(this.active_players * percentage) / 100.f));
    }

    public boolean decrementRestersAt(BlockPos rest_pos) {
        if (!this.rest_positions.containsKey(rest_pos)) {
            return false;
        }

        final var prev_resters = this.rest_positions.get(rest_pos).intValue();

        if (prev_resters == 1) {
            this.rest_positions.remove(rest_pos);

            return true;
        }

        this.rest_positions.put(rest_pos, Integer.valueOf(prev_resters - 1));

        return false;
    }

    public void removeAllResters() {
        this.resting_players = 0;
        this.rest_positions.clear();
    }

    public int amountResting() {
        return this.resting_players;
    }

    public boolean update(List<ServerPlayer> players) {
        final var prev_active  = this.active_players;
        final var prev_resting = this.resting_players;

        this.active_players  = 0;
        this.resting_players = 0;
        this.rest_positions.clear();

        for (final var player : players) {
            final var rester = (Rester) player;

            if (player.isSpectator()) {
                continue;
            }

            this.active_players += 1;

            if (!rester.isResting()) {
                continue;
            }

            this.resting_players += 1;

            final var rest_pos = rester.getRestingPos().get();
            if (!this.rest_positions.containsKey(rest_pos)) {
                this.rest_positions.put(rest_pos, Integer.valueOf(1));
            } else {
                final var prev_resters = this.rest_positions.get(rest_pos).intValue();
                this.rest_positions.put(rest_pos, Integer.valueOf(prev_resters + 1));
            }
        }

        /* Return whether we should announce the status of resting players. */
        return !(
            (prev_resting <= 0 && this.resting_players <= 0) ||

            (
                prev_active  == this.active_players  &&
                prev_resting == this.resting_players
            )
        );
    }
}
