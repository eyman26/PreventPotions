package org.eyman_.preventpotions;

import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Collection;
import java.util.List;
import java.util.Locale;

public final class PreventPotions extends JavaPlugin {

    private static final String ADMIN = "preventpotions.admin";

    private volatile PotionSettings settings;
    private volatile boolean preventing = true;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        settings = PotionSettings.from(getConfig());
        getServer().getPluginManager().registerEvents(new PotionListener(this), this);
        getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, event ->
                event.registrar().register("preventpotions", "Reload or toggle PreventPotions", new AdminCommand()));
    }

    PotionSettings settings() {
        return settings;
    }

    boolean isPreventing() {
        return preventing;
    }

    private final class AdminCommand implements BasicCommand {

        @Override
        public void execute(CommandSourceStack source, String[] args) {
            CommandSender sender = source.getSender();
            String sub = args.length > 0 ? args[0].toLowerCase(Locale.ROOT) : "";
            switch (sub) {
                case "reload" -> {
                    reloadConfig();
                    settings = PotionSettings.from(getConfig());
                    sender.sendMessage(Component.text("PreventPotions config reloaded!", NamedTextColor.GREEN));
                }
                case "toggle" -> {
                    preventing = !preventing;
                    sender.sendMessage(preventing
                            ? Component.text("Potion prevention enabled!", NamedTextColor.GREEN)
                            : Component.text("Potion prevention disabled!", NamedTextColor.RED));
                }
                default -> sender.sendMessage(Component.text("Usage: /preventpotions <reload|toggle>", NamedTextColor.RED));
            }
        }

        @Override
        public Collection<String> suggest(CommandSourceStack source, String[] args) {
            if (args.length > 1) return List.of();
            String prefix = args.length == 0 ? "" : args[0].toLowerCase(Locale.ROOT);
            return List.of("reload", "toggle").stream().filter(option -> option.startsWith(prefix)).toList();
        }

        @Override
        public String permission() {
            return ADMIN;
        }
    }
}
