package me.eyman.PreventPotions.fabric.mixin;

import me.eyman.PreventPotions.fabric.PotionRules;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ProjectileWeaponItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

/**
 * draw() is where bows and crossbows pull (and consume) their ammo. An empty result means
 * nothing is fired or loaded, and since we bail before useAmmo the arrow stays in the inventory.
 */
@Mixin(ProjectileWeaponItem.class)
abstract class ProjectileWeaponItemMixin {

    @Inject(method = "draw", at = @At("HEAD"), cancellable = true)
    private static void preventpotions$blockTippedArrows(ItemStack weapon, ItemStack ammo, LivingEntity shooter, CallbackInfoReturnable<List<ItemStack>> cir) {
        if (ammo.is(Items.TIPPED_ARROW) && shooter instanceof ServerPlayer player && PotionRules.deny(player, PotionRules.TIPPED_ARROWS)) {
            cir.setReturnValue(List.of());
        }
    }
}
