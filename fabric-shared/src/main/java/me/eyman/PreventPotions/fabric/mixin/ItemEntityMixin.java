package me.eyman.PreventPotions.fabric.mixin;

import me.eyman.PreventPotions.fabric.PotionRules;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemEntity.class)
abstract class ItemEntityMixin {

    @Shadow
    public abstract ItemStack getItem();

    @Inject(method = "playerTouch", at = @At("HEAD"), cancellable = true)
    private void preventpotions$blockTippedArrowPickup(Player player, CallbackInfo ci) {
        if (getItem().is(Items.TIPPED_ARROW) && PotionRules.blocks(PotionRules.TIPPED_ARROWS)) ci.cancel();
    }
}
