package org.eyman_.preventpotions.fabric;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Config/preventpotions.json and the switches the mixins consult. Mirrors the Paper config keys.
 * Static because mixins have no other way to reach mod state; only ever written on the server thread.
 */
public final class PotionRules {

    public static final String DRINK = "drink";
    public static final String SPLASH = "splash";
    public static final String LINGERING = "lingering";
    public static final String TIPPED_ARROWS = "tipped_arrows";
    public static final String BEACON = "beacon";
    public static final String COMMANDS = "commands";

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    private static final class Json {
        Map<String, Boolean> prevent = new LinkedHashMap<>(Map.of(
                DRINK, true, SPLASH, true, LINGERING, true, TIPPED_ARROWS, true, BEACON, false, COMMANDS, false));
        Map<String, String> messages = new LinkedHashMap<>(Map.of(
                DRINK, "&cYou cannot drink potions on this server!",
                SPLASH, "&cSplash potions are disabled!",
                LINGERING, "&cLingering potions are disabled!",
                TIPPED_ARROWS, "&cTipped arrows are not allowed!",
                BEACON, "&cBeacon effects are disabled!",
                COMMANDS, "&cPotion effects via commands are disabled!"));
    }

    private static volatile Json config = new Json();
    private static volatile boolean preventing = true;

    private PotionRules() {
    }

    static void load(Path file) throws IOException {
        Json loaded = new Json();
        if (Files.exists(file)) {
            try (Reader reader = Files.newBufferedReader(file)) {
                Json read = GSON.fromJson(reader, Json.class);
                if (read != null) {
                    // Merge so a file missing newer keys still gets their defaults.
                    if (read.prevent != null) loaded.prevent.putAll(read.prevent);
                    if (read.messages != null) loaded.messages.putAll(read.messages);
                }
            } catch (JsonParseException e) {
                throw new IOException("Malformed " + file.getFileName(), e);
            }
        }
        Files.createDirectories(file.getParent());
        try (Writer writer = Files.newBufferedWriter(file)) {
            GSON.toJson(loaded, writer);
        }
        config = loaded;
    }

    static boolean toggle() {
        return preventing = !preventing;
    }

    public static boolean blocks(String rule) {
        return preventing && Boolean.TRUE.equals(config.prevent.get(rule));
    }

    /** Checks the rule and tells the player when it blocks them. */
    public static boolean deny(ServerPlayer player, String rule) {
        if (!blocks(rule)) return false;
        player.sendSystemMessage(Component.literal(config.messages.getOrDefault(rule, "&cThat action is blocked!").replace('&', '§')));
        return true;
    }
}
