package org.eyman_.preventpotions.fabric.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BeaconBlockEntity;
import org.eyman_.preventpotions.fabric.PotionRules;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(BeaconBlockEntity.class)
abstract class BeaconBlockEntityMixin {

    @WrapOperation(
            method = "applyEffects",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;addEffect(Lnet/minecraft/world/effect/MobEffectInstance;)Z")
    )
    private static boolean preventpotions$blockBeacon(Player player, MobEffectInstance effect, Operation<Boolean> original) {
        if (player instanceof ServerPlayer serverPlayer && PotionRules.deny(serverPlayer, PotionRules.BEACON)) return false;
        return original.call(player, effect);
    }
}
