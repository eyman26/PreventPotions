package me.eyman.PreventPotions.fabric;

import me.lucko.fabric.api.permissions.v0.Permissions;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.PermissionLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;

/**
 * Drinking and throwing go through Fabric's UseItemCallback; beacon effects, /effect, firing tipped
 * arrows and picking them up are handled by the mixins in {@code fabric.mixin}.
 */
public final class PreventPotionsMod implements ModInitializer {

    // Catches tipped arrows that arrive through crafting, trading, loot or commands.
    private static final int SWEEP_INTERVAL = 20;

    private final Path configFile = FabricLoader.getInstance().getConfigDir().resolve("preventpotions.json");
    private int ticks;

    @Override
    public void onInitialize() {
        try {
            PotionRules.load(configFile);
        } catch (IOException e) {
            throw new UncheckedIOException("Could not load " + configFile, e);
        }

        UseItemCallback.EVENT.register((player, level, hand) -> {
            if (!(player instanceof ServerPlayer serverPlayer)) return InteractionResult.PASS;
            ItemStack stack = player.getItemInHand(hand);
            String rule = stack.is(Items.POTION) ? PotionRules.DRINK
                    : stack.is(Items.SPLASH_POTION) ? PotionRules.SPLASH
                    : stack.is(Items.LINGERING_POTION) ? PotionRules.LINGERING
                    : null;
            return rule != null && PotionRules.deny(serverPlayer, rule) ? InteractionResult.FAIL : InteractionResult.PASS;
        });

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> removeTippedArrows(handler.getPlayer()));
        ServerTickEvents.END_SERVER_TICK.register(this::sweep);
        CommandRegistrationCallback.EVENT.register((dispatcher, registries, environment) -> dispatcher.register(
                Commands.literal("preventpotions")
                        .requires(Permissions.require("preventpotions.admin", PermissionLevel.GAMEMASTERS))
                        .then(Commands.literal("reload").executes(ctx -> reload(ctx.getSource())))
                        .then(Commands.literal("toggle").executes(ctx -> {
                            boolean on = PotionRules.toggle();
                            ctx.getSource().sendSuccess(() -> Component.literal(on ? "§aPotion prevention enabled!" : "§cPotion prevention disabled!"), true);
                            return 1;
                        }))));
    }

    private int reload(CommandSourceStack source) {
        try {
            PotionRules.load(configFile);
            source.sendSuccess(() -> Component.literal("§aPreventPotions config reloaded!"), true);
            return 1;
        } catch (IOException e) {
            source.sendFailure(Component.literal("§cReload failed: " + e.getMessage()));
            return 0;
        }
    }

    private void sweep(MinecraftServer server) {
        if (++ticks < SWEEP_INTERVAL) return;
        ticks = 0;
        if (!PotionRules.blocks(PotionRules.TIPPED_ARROWS)) return;
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            removeTippedArrows(player);
        }
    }

    private static void removeTippedArrows(ServerPlayer player) {
        if (!PotionRules.blocks(PotionRules.TIPPED_ARROWS)) return;
        Inventory inventory = player.getInventory();
        boolean removed = false;
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            if (inventory.getItem(slot).is(Items.TIPPED_ARROW)) {
                inventory.setItem(slot, ItemStack.EMPTY);
                removed = true;
            }
        }
        if (removed) PotionRules.deny(player, PotionRules.TIPPED_ARROWS);
    }
}
