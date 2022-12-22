package io.github.friedkeenan.chronopyre;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.world.level.block.CampfireBlock;

public class ChronopyreClientMod implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ClientPlayNetworking.registerGlobalReceiver(
            ChronopyreMod.DOWSE_CAMPFIRE_ID,

            (client, handler, buf, response_sender) -> {

                final var rest_pos = buf.readBlockPos();
                final var rest_state = client.level.getBlockState(rest_pos);

                client.execute(() -> CampfireBlock.dowse(null, client.level, rest_pos, rest_state));
            }
        );
    }
}
