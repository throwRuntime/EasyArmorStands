package me.m56738.easyarmorstands.message;

import me.m56738.easyarmorstands.config.EasConfig;
import me.m56738.easyarmorstands.lib.kyori.adventure.key.Key;
import me.m56738.easyarmorstands.lib.kyori.adventure.text.Component;
import me.m56738.easyarmorstands.lib.kyori.adventure.text.minimessage.MiniMessage;
import me.m56738.easyarmorstands.lib.kyori.adventure.text.minimessage.tag.Tag;
import me.m56738.easyarmorstands.lib.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import me.m56738.easyarmorstands.lib.kyori.adventure.translation.GlobalTranslator;
import me.m56738.easyarmorstands.lib.kyori.adventure.translation.Translator;
import org.bukkit.plugin.Plugin;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class MessageManager {
    private static final Pattern PATTERN = Pattern.compile("(.+)\\.json");
    private static final Key key = Key.key("easyarmorstands", "translation");
    private final Plugin plugin;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();
    private final Map<MessageStyle, String> styleTemplates = new HashMap<>();
    private final Set<Locale> loadedLocales = new HashSet<>();
    private PatternTranslationStore registry = new PatternTranslationStore(key);

    public MessageManager(Plugin plugin) {
        this.plugin = plugin;
        Message.messageManager = this;
    }

    public void load(EasConfig config) {
        styleTemplates.putAll(config.message.format);

        GlobalTranslator.translator().removeSource(registry);

        registry = new PatternTranslationStore(key);
        loadedLocales.clear();

        // Convert old message files
        Path dataPath = plugin.getDataFolder().toPath();
        Path langPath = dataPath.resolve("lang");
        try (Stream<Path> paths = Files.list(dataPath)) {
            paths.forEach(path -> {
                try {
                    if (MessageMigrator.migrate(path, langPath)) {
                        plugin.getLogger().info("Migrated custom messages: " + path.getFileName().toString());
                    }
                } catch (Exception e) {
                    plugin.getLogger().log(Level.SEVERE, "Failed to convert old message file: " + path.getFileName().toString(), e);
                }
            });
        } catch (NoSuchFileException ignored) {
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to convert old message files", e);
        }

        // Export default locales if lang folder is empty or doesn't exist
        try {
            exportDefaultLocalesIfNeeded(langPath);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to export default locale files", e);
        }

        // Load custom locales
        try (Stream<Path> paths = Files.list(langPath)) {
            paths.forEach(this::loadCustomLocale);
        } catch (NoSuchFileException ignored) {
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to load messages", e);
        }

        if (config.message.serverSideTranslation) {
            GlobalTranslator.translator().addSource(registry);
        }
    }

    private void exportDefaultLocalesIfNeeded(Path langPath) throws IOException {
        // Check if lang folder exists and has any .json files
        boolean needsExport = false;
        
        if (!Files.exists(langPath)) {
            Files.createDirectories(langPath);
            needsExport = true;
        } else {
            try (Stream<Path> paths = Files.list(langPath)) {
                needsExport = paths.noneMatch(path -> path.getFileName().toString().endsWith(".json"));
            }
        }

        if (needsExport) {
            exportDefaultLocale(langPath, "en_us");
            exportDefaultLocale(langPath, "de_de");
            exportDefaultLocale(langPath, "ru_ru");
            plugin.getLogger().info("Exported default locale files to lang folder");
        }
    }

    private void exportDefaultLocale(Path langPath, String localeName) throws IOException {
        String resourcePath = "/assets/easyarmorstands/lang/" + localeName + ".json";
        Path targetPath = langPath.resolve(localeName + ".json");
        
        if (Files.exists(targetPath)) {
            return; // Don't overwrite existing files
        }

        try (InputStream input = getClass().getResourceAsStream(resourcePath)) {
            if (input != null) {
                Files.copy(input, targetPath, StandardCopyOption.REPLACE_EXISTING);
            } else {
                plugin.getLogger().warning("Default locale resource not found: " + resourcePath);
            }
        }
    }

    private void loadCustomLocale(Path path) {
        Matcher matcher = PATTERN.matcher(path.getFileName().toString());
        if (!matcher.matches()) {
            return;
        }

        Locale locale = Translator.parseLocale(matcher.group(1));
        if (locale == null) {
            plugin.getLogger().warning("Invalid locale: " + matcher.group(1));
            return;
        }

        if (!loadedLocales.add(locale)) {
            return;
        }

        try {
            registry.readLocale(path, locale);
            plugin.getLogger().info("Loaded custom messages for language: " + locale);
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to load translations from " + path.getFileName(), e);
        }
    }

    public Component format(MessageStyle style, Component message) {
        return format(style, message, TagResolver.empty());
    }

    public Component format(MessageStyle style, Component message, TagResolver resolver) {
        return miniMessage.deserialize(
                styleTemplates.get(style),
                TagResolver.builder()
                        .tag("message", Tag.selfClosingInserting(message))
                        .resolver(resolver)
                        .build());
    }
}
