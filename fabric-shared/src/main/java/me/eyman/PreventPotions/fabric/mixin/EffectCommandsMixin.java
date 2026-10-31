package me.eyman.PreventPotions.fabric.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import me.eyman.PreventPotions.fabric.PotionRules;
import net.minecraft.server.commands.EffectCommands;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/** Returning false makes /effect count the target as failed, so the command reports it like vanilla would. */
@Mixin(EffectCommands.class)
abstract class EffectCommandsMixin {

    @WrapOperation(
            method = "giveEffect",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;addEffect(Lnet/minecraft/world/effect/MobEffectInstance;Lnet/minecraft/world/entity/Entity;)Z")
    )
    private static boolean preventpotions$blockCommand(LivingEntity target, MobEffectInstance effect, Entity source, Operation<Boolean> original) {
        if (target instanceof ServerPlayer player && PotionRules.deny(player, PotionRules.COMMANDS)) return false;
        return original.call(target, effect, source);
    }
}
