package net.arkonix.luckydrops.config;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import net.arkonix.luckydrops.LuckyDropsForge;
import net.minecraft.resources.ResourceLocation;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class LuckyDropsConfig {
    private static Path configPath;

    public static boolean enabled = true;
    public static double triggerChance = 1.0D;
    public static boolean triggerInCreative = false;
    public static boolean debug = false;
    public static boolean preferLuckyBlockMod = true;
    public static boolean enableFallbackRewards = false;

    public static final Set<ResourceLocation> blacklist = new HashSet<>();

    private static final List<String> DEFAULT_BLACKLIST = List.of(
            "minecraft:bedrock",
            "minecraft:command_block",
            "minecraft:chain_command_block",
            "minecraft:repeating_command_block",
            "minecraft:end_portal",
            "minecraft:end_portal_frame",
            "minecraft:nether_portal",
            "minecraft:spawner",
            "minecraft:barrier",
            "minecraft:structure_block",
            "minecraft:jigsaw"
    );

    private LuckyDropsConfig() {
    }

    public static void load(Path path) {
        configPath = path;

        try {
            if (Files.notExists(path)) {
                writeDefault(path);
            }

            CommentedFileConfig config = CommentedFileConfig.builder(path)
                    .sync()
                    .autosave()
                    .preserveInsertionOrder()
                    .build();

            config.load();

            enabled = getBoolean(config, "enabled", true);
            triggerChance = Math.max(0.0D, Math.min(1.0D, getDouble(config, "triggerChance", 1.0D)));
            triggerInCreative = getBoolean(config, "triggerInCreative", false);
            debug = getBoolean(config, "debug", false);
            preferLuckyBlockMod = getBoolean(config, "preferLuckyBlockMod", true);
            enableFallbackRewards = getBoolean(config, "enableFallbackRewards", false);

            List<String> list = config.getOrElse("blacklist", DEFAULT_BLACKLIST);

            blacklist.clear();

            for (String entry : list) {
                ResourceLocation id = ResourceLocation.tryParse(entry);
                if (id != null) {
                    blacklist.add(id);
                }
            }

            config.close();

            LuckyDropsForge.LOGGER.info(
                    "LuckyDrops config loaded. enabled={}, chance={}, preferLuckyBlockMod={}, fallback={}",
                    enabled,
                    triggerChance,
                    preferLuckyBlockMod,
                    enableFallbackRewards
            );
        } catch (Exception exception) {
            LuckyDropsForge.LOGGER.error("Failed to load LuckyDrops config.", exception);
        }
    }

    public static void reload() {
        if (configPath != null) {
            load(configPath);
        }
    }

    public static void setEnabled(boolean value) {
        enabled = value;
        saveBoolean("enabled", value);
    }

    public static void setDebug(boolean value) {
        debug = value;
        saveBoolean("debug", value);
    }

    private static void saveBoolean(String key, boolean value) {
        if (configPath == null) return;

        try {
            CommentedFileConfig config = CommentedFileConfig.builder(configPath)
                    .sync()
                    .autosave()
                    .preserveInsertionOrder()
                    .build();

            config.load();
            config.set(key, value);
            config.save();
            config.close();
        } catch (Exception exception) {
            LuckyDropsForge.LOGGER.error("Failed to save LuckyDrops config value {}", key, exception);
        }
    }

    private static void writeDefault(Path path) throws Exception {
        Files.createDirectories(path.getParent());

        StringBuilder builder = new StringBuilder();

        builder.append("# LuckyDropsForge config\n");
        builder.append("enabled = true\n");
        builder.append("triggerChance = 1.0\n");
        builder.append("triggerInCreative = false\n");
        builder.append("debug = false\n");
        builder.append("preferLuckyBlockMod = true\n");
        builder.append("enableFallbackRewards = false\n\n");
        builder.append("blacklist = [\n");

        for (int i = 0; i < DEFAULT_BLACKLIST.size(); i++) {
            builder.append("  \"").append(DEFAULT_BLACKLIST.get(i)).append("\"");
            if (i < DEFAULT_BLACKLIST.size() - 1) {
                builder.append(",");
            }
            builder.append("\n");
        }

        builder.append("]\n");

        Files.writeString(path, builder.toString());
    }

    private static boolean getBoolean(CommentedFileConfig config, String key, boolean fallback) {
        Object value = config.get(key);
        return value instanceof Boolean b ? b : fallback;
    }

    private static double getDouble(CommentedFileConfig config, String key, double fallback) {
        Object value = config.get(key);
        return value instanceof Number number ? number.doubleValue() : fallback;
    }
}