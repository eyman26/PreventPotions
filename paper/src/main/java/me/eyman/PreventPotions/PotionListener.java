package me.eyman.PreventPotions;

import com.destroystokyo.paper.event.player.PlayerLaunchProjectileEvent;
import com.destroystokyo.paper.event.player.PlayerReadyArrowEvent;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.EntityPotionEffectEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.entity.VillagerAcquireTradeEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;

/**
 * Every handler runs on the thread that owns the entity/inventory involved (its region thread on
 * Folia), and only touches that entity plus the immutable {@link PotionSettings}.
 */
final class PotionListener implements Listener {

    private final PreventPotions plugin;

    PotionListener(PreventPotions plugin) {
        this.plugin = plugin;
    }

    /** Settings if prevention is switched on, otherwise null. */
    private PotionSettings active() {
        return plugin.isPreventing() ? plugin.settings() : null;
    }

    @EventHandler(ignoreCancelled = true)
    public void onDrink(PlayerItemConsumeEvent event) {
        PotionSettings settings = active();
        if (settings != null && settings.drink() && event.getItem().getType() == Material.POTION) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(settings.message("drink"));
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerThrow(PlayerLaunchProjectileEvent event) {
        PotionSettings settings = active();
        if (settings == null || !(event.getProjectile() instanceof ThrownPotion)) return;

        String blocked = thrownPotionRule(settings, event.getItemStack().getType());
        if (blocked != null) {
            event.setShouldConsume(false);
            event.setCancelled(true);
            event.getPlayer().sendMessage(settings.message(blocked));
        }
    }

    /** Dispensers and other non-player throwers; players are handled (and refunded) above. */
    @EventHandler(ignoreCancelled = true)
    public void onLaunch(ProjectileLaunchEvent event) {
        PotionSettings settings = active();
        if (settings == null || !(event.getEntity() instanceof ThrownPotion potion) || potion.getShooter() instanceof Player) return;
        if (thrownPotionRule(settings, potion.getItem().getType()) != null) event.setCancelled(true);
    }

    private static String thrownPotionRule(PotionSettings settings, Material type) {
        if (type == Material.SPLASH_POTION && settings.splash()) return "splash";
        if (type == Material.LINGERING_POTION && settings.lingering()) return "lingering";
        return null;
    }

    /**
     * Fires while the server picks ammo for a bow or crossbow. Cancelling makes it skip tipped arrows,
     * so nothing is fired or used up (cancelling EntityShootBowEvent is too late, the arrow is already taken).
     */
    @EventHandler(ignoreCancelled = true)
    public void onReadyArrow(PlayerReadyArrowEvent event) {
        PotionSettings settings = active();
        if (settings != null && settings.tippedArrows() && event.getArrow().getType() == Material.TIPPED_ARROW) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(settings.message("tipped_arrows"));
        }
    }

    @EventHandler
    public void onCraft(PrepareItemCraftEvent event) {
        PotionSettings settings = active();
        Recipe recipe = event.getRecipe();
        if (settings != null && settings.tippedArrows() && recipe != null && recipe.getResult().getType() == Material.TIPPED_ARROW) {
            event.getInventory().setResult(null);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onVillagerTrade(VillagerAcquireTradeEvent event) {
        PotionSettings settings = active();
        if (settings != null && settings.tippedArrows() && event.getRecipe().getResult().getType() == Material.TIPPED_ARROW) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPickup(EntityPickupItemEvent event) {
        PotionSettings settings = active();
        if (settings != null && settings.tippedArrows() && event.getItem().getItemStack().getType() == Material.TIPPED_ARROW) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
        PotionSettings settings = active();
        ItemStack current = event.getCurrentItem();
        if (settings == null || !settings.tippedArrows() || current == null || current.getType() != Material.TIPPED_ARROW) return;

        event.setCancelled(true);
        event.setCurrentItem(null);
        HumanEntity clicker = event.getWhoClicked();
        clicker.sendMessage(settings.message("tipped_arrows"));
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        PotionSettings settings = active();
        if (settings == null || !settings.tippedArrows()) return;

        Player player = event.getPlayer();
        if (player.getInventory().contains(Material.TIPPED_ARROW)) {
            player.getInventory().remove(Material.TIPPED_ARROW);
            player.sendMessage(settings.message("tipped_arrows"));
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPotionEffect(EntityPotionEffectEvent event) {
        PotionSettings settings = active();
        if (settings == null || !(event.getEntity() instanceof Player player)) return;

        switch (event.getCause()) {
            case COMMAND -> {
                if (settings.commands()) {
                    event.setCancelled(true);
                    player.sendMessage(settings.message("commands"));
                }
            }
            case BEACON -> {
                if (settings.beacon()) {
                    event.setCancelled(true);
                    player.sendMessage(settings.message("beacon"));
                }
            }
            default -> {
            }
        }
    }
}
