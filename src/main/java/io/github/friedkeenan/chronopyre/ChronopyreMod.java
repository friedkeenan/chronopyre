package io.github.friedkeenan.chronopyre;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.fabricmc.api.ModInitializer;
import net.minecraft.resources.ResourceLocation;

public class ChronopyreMod implements ModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("chronopyre");

    public static final ResourceLocation DOWSE_CAMPFIRE_ID = new ResourceLocation("chronopyre:dowse_campfire");

    @Override
    public void onInitialize() {
        LOGGER.info("chronopyre initialized!");
    }
}
