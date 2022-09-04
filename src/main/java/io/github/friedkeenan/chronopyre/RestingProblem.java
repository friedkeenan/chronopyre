package io.github.friedkeenan.chronopyre;

import net.minecraft.network.chat.Component;

public enum RestingProblem {
    ALREADY_RESTING("block.minecraft.campfire.rest.already_resting"),
    IMPOSSIBLE("block.minecraft.campfire.rest.impossible"),
    NOT_POSSIBLE_NOW("block.minecraft.campfire.rest.not_possible_now"),
    NOT_ALLOWED_IN_DIMENSION("block.minecraft.campfire.rest.not_allowed_in_dimension"),
    UNLIT("block.minecraft.campfire.rest.unlit");

    public final Component message;

    private RestingProblem(String translation_key) {
        this.message = Component.translatable(translation_key);
    }
}
