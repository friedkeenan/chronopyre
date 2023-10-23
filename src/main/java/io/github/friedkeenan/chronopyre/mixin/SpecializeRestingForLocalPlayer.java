package io.github.friedkeenan.chronopyre.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

@Mixin(LocalPlayer.class)
public abstract class SpecializeRestingForLocalPlayer extends AddRestingToPlayer {
    protected SpecializeRestingForLocalPlayer(EntityType<? extends LivingEntity> entityType, Level level) {
        super(entityType, level);
    }

    @Inject(at = @At("TAIL"), method = "actuallyHurt")
    private void makeDamageStopResting(DamageSource source, float damage, CallbackInfo info) {
        if (this.isResting()) {
            this.stopResting(true);
        }
    }
}
