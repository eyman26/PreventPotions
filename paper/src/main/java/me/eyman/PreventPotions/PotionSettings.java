package me.eyman.PreventPotions;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.HashMap;
import java.util.Map;

/** Immutable config snapshot, safe to read from any region thread. */
record PotionSettings(boolean drink, boolean splash, boolean lingering, boolean tippedArrows,
                      boolean beacon, boolean commands, Map<String, Component> messages) {

    private static final Map<String, String> DEFAULT_MESSAGES = Map.of(
            "drink", "&cYou cannot drink potions on this server!",
            "splash", "&cSplash potions are disabled!",
            "lingering", "&cLingering potions are disabled!",
            "tipped_arrows", "&cTipped arrows are not allowed!",
            "beacon", "&cBeacon effects are disabled!",
            "commands", "&cPotion effects via commands are disabled!"
    );

    static PotionSettings from(FileConfiguration config) {
        LegacyComponentSerializer legacy = LegacyComponentSerializer.legacyAmpersand();
        Map<String, Component> messages = new HashMap<>();
        DEFAULT_MESSAGES.forEach((key, fallback) -> messages.put(key, legacy.deserialize(config.getString("messages." + key, fallback))));

        return new PotionSettings(
                config.getBoolean("prevent.drink"),
                config.getBoolean("prevent.splash"),
                config.getBoolean("prevent.lingering"),
                config.getBoolean("prevent.tipped_arrows"),
                config.getBoolean("prevent.beacon"),
                config.getBoolean("prevent.commands"),
                Map.copyOf(messages)
        );
    }

    Component message(String key) {
        return messages.get(key);
    }
}
