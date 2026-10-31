package me.eyman.PreventPotions;

import java.lang.reflect.Method;
import java.util.Map;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.event.EventHandler;
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
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
/* loaded from: PreventPotions_1.1.jar:me/eyman/PreventPotions/PreventPotions.class */
public class PreventPotions extends JavaPlugin implements Listener {
    private FileConfiguration config;
    private boolean preventionEnabled = true;

    public void onEnable() {
        saveDefaultConfig();
        this.config = getConfig();
        Bukkit.getPluginManager().registerEvents(this, this);
        try {
            Method getCommandMap = Bukkit.class.getMethod("getCommandMap", new Class[0]);
            CommandMap commandMap = (CommandMap) getCommandMap.invoke(Bukkit.getServer(), new Object[0]);
            commandMap.register("preventpotions", new BukkitCommand("preventpotions") { // from class: me.eyman.PreventPotions.PreventPotions.1
                public boolean execute(@NotNull CommandSender sender, @NotNull String label, String[] args) {
                    return PreventPotions.this.handlePreventPotionsCommand(sender, args);
                }
            });
            getLogger().info("Command /preventpotions registered via CommandMap (Paper mode).");
        } catch (Exception e) {
            getLogger().info("Command /preventpotions registered via plugin.yml (Bukkit/Spigot mode).");
        }
        getLogger().info("PreventPotions has been enabled.");
    }

    public void onDisable() {
        getLogger().info("PreventPotions disabled.");
    }

    private String msg(String path) {
        Map<String, String> defaults = Map.of("drink", "&cYou cannot drink potions on this server!", "splash", "&cSplash potions are disabled!", "lingering", "&cLingering potions are disabled!", "tipped_arrows", "&cTipped arrows are not allowed!", "beacon", "&cBeacon effects are disabled!", "commands", "&cPotion effects via commands are disabled!");
        String raw = this.config.getString("messages." + path, defaults.getOrDefault(path, "&cThat action is blocked!"));
        return raw.replace("&", "§");
    }

    @EventHandler
    public void onPotionDrink(PlayerItemConsumeEvent event) {
        if (this.preventionEnabled) {
            ItemStack item = event.getItem();
            if ((item.getItemMeta() instanceof PotionMeta) && this.config.getBoolean("prevent.drink")) {
                event.setCancelled(true);
                event.getPlayer().sendMessage(msg("drink"));
            }
        }
    }

    @EventHandler
    public void onPotionThrow(ProjectileLaunchEvent event) {
        if (this.preventionEnabled) {
            ThrownPotion entity = event.getEntity();
            if (entity instanceof ThrownPotion) {
                ThrownPotion potion = entity;
                ItemStack item = potion.getItem();
                if (item.getType() == Material.SPLASH_POTION && this.config.getBoolean("prevent.splash")) {
                    event.setCancelled(true);
                    Player shooter = potion.getShooter();
                    if (shooter instanceof Player) {
                        Player player = shooter;
                        player.sendMessage(msg("splash"));
                    }
                }
                if (item.getType() == Material.LINGERING_POTION && this.config.getBoolean("prevent.lingering")) {
                    event.setCancelled(true);
                    Player shooter2 = potion.getShooter();
                    if (shooter2 instanceof Player) {
                        Player player2 = shooter2;
                        player2.sendMessage(msg("lingering"));
                    }
                }
            }
        }
    }

    @EventHandler
    public void onArrowShoot(ProjectileLaunchEvent event) {
        if (this.preventionEnabled) {
            Arrow entity = event.getEntity();
            if (entity instanceof Arrow) {
                Arrow arrow = entity;
                Player shooter = arrow.getShooter();
                if (shooter instanceof Player) {
                    Player player = shooter;
                    ItemStack inHand = player.getInventory().getItemInMainHand();
                    if (inHand.getType() == Material.TIPPED_ARROW && this.config.getBoolean("prevent.tipped_arrows")) {
                        event.setCancelled(true);
                        player.sendMessage(msg("tipped_arrows"));
                    }
                }
            }
        }
    }

    @EventHandler
    public void onCraft(PrepareItemCraftEvent event) {
        if (this.preventionEnabled && this.config.getBoolean("prevent.tipped_arrows") && event.getRecipe() != null && event.getRecipe().getResult().getType() == Material.TIPPED_ARROW) {
            event.getInventory().setResult(new ItemStack(Material.AIR));
        }
    }

    @EventHandler
    public void onVillagerTrade(VillagerAcquireTradeEvent event) {
        if (this.preventionEnabled && this.config.getBoolean("prevent.tipped_arrows") && event.getRecipe().getResult().getType() == Material.TIPPED_ARROW) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPickup(EntityPickupItemEvent event) {
        if (this.preventionEnabled && this.config.getBoolean("prevent.tipped_arrows") && event.getItem().getItemStack().getType() == Material.TIPPED_ARROW) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        ItemStack current;
        if (this.preventionEnabled && this.config.getBoolean("prevent.tipped_arrows") && (current = event.getCurrentItem()) != null && current.getType() == Material.TIPPED_ARROW) {
            event.setCancelled(true);
            event.setCurrentItem(new ItemStack(Material.AIR));
            Player whoClicked = event.getWhoClicked();
            if (whoClicked instanceof Player) {
                Player player = whoClicked;
                player.sendMessage(msg("tipped_arrows"));
            }
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        ItemStack[] contents;
        if (this.preventionEnabled && this.config.getBoolean("prevent.tipped_arrows")) {
            Player player = event.getPlayer();
            for (ItemStack item : player.getInventory().getContents()) {
                if (item != null && item.getType() == Material.TIPPED_ARROW) {
                    player.getInventory().remove(item);
                    player.sendMessage(msg("tipped_arrows"));
                }
            }
        }
    }

    @EventHandler
    public void onPotionEffect(EntityPotionEffectEvent event) {
        if (this.preventionEnabled) {
            Player entity = event.getEntity();
            if (entity instanceof Player) {
                Player player = entity;
                switch (AnonymousClass2.$SwitchMap$org$bukkit$event$entity$EntityPotionEffectEvent$Cause[event.getCause().ordinal()]) {
                    case 1:
                        if (this.config.getBoolean("prevent.commands")) {
                            event.setCancelled(true);
                            player.sendMessage(msg("commands"));
                            return;
                        }
                        return;
                    case 2:
                        if (this.config.getBoolean("prevent.beacon")) {
                            event.setCancelled(true);
                            player.sendMessage(msg("beacon"));
                            return;
                        }
                        return;
                    default:
                        return;
                }
            }
        }
    }

    /* renamed from: me.eyman.PreventPotions.PreventPotions$2  reason: invalid class name */
    /* loaded from: PreventPotions_1.1.jar:me/eyman/PreventPotions/PreventPotions$2.class */
    static /* synthetic */ class AnonymousClass2 {
        static final /* synthetic */ int[] $SwitchMap$org$bukkit$event$entity$EntityPotionEffectEvent$Cause = new int[EntityPotionEffectEvent.Cause.values().length];

        static {
            try {
                $SwitchMap$org$bukkit$event$entity$EntityPotionEffectEvent$Cause[EntityPotionEffectEvent.Cause.COMMAND.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$org$bukkit$event$entity$EntityPotionEffectEvent$Cause[EntityPotionEffectEvent.Cause.BEACON.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
        }
    }

    private boolean handlePreventPotionsCommand(CommandSender sender, String[] args) {
        String str;
        if (args.length > 0) {
            if (args[0].equalsIgnoreCase("reload")) {
                reloadConfig();
                this.config = getConfig();
                sender.sendMessage("§aPreventPotions config reloaded!");
                return true;
            } else if (args[0].equalsIgnoreCase("toggle")) {
                this.preventionEnabled = !this.preventionEnabled;
                if (this.preventionEnabled) {
                    str = "§aPotion prevention enabled!";
                } else {
                    str = "§cPotion prevention disabled!";
                }
                sender.sendMessage(str);
                return true;
            }
        }
        sender.sendMessage("§cUsage: /preventpotions <reload|toggle>");
        return true;
    }

    public boolean onCommand(@NotNull CommandSender sender, Command command, @NotNull String label, @NotNull String[] args) {
        return handlePreventPotionsCommand(sender, args);
    }
}
