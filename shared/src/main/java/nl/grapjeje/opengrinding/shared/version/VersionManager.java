package nl.grapjeje.opengrinding.shared.version;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;

import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class VersionManager {

    @Getter
    private static final MinecraftVersion detectedVersion;

    static {
        detectedVersion = detectVersion();
    }

    private static MinecraftVersion detectVersion() {
        Matcher matcher = Pattern.compile("MC: ([\\d.]+)")
                .matcher(Bukkit.getVersion());

        if (!matcher.find()) return MinecraftVersion.UNKNOWN;
        String version = matcher.group(1);
        return Arrays.stream(MinecraftVersion.values())
                .filter(v -> version.equals(v.getMcVersion()))
                .findFirst()
                .orElse(MinecraftVersion.UNKNOWN);
    }

    public static boolean isSupportedVersion() {
        return detectedVersion != MinecraftVersion.UNKNOWN;
    }

    public static VersionHandler createVersionHandler() {
        MinecraftVersion version = detectedVersion;

        // Groep 1: 1.21, 1.21.1, 1.21.2 -> module v1_21
        if (version == MinecraftVersion.V1_21 ||
                version == MinecraftVersion.V1_21_1 ||
                version == MinecraftVersion.V1_21_2) {
            return instantiateHandler("nl.grapjeje.opengrinding.version.v1_21.Version1_21Handler");
        }

        // Groep 2: 1.21.3 t/m 1.21.10 -> module v1_21_3
        if (version.ordinal() >= MinecraftVersion.V1_21_3.ordinal() &&
                version.ordinal() <= MinecraftVersion.V1_21_10.ordinal()) {
            return instantiateHandler("nl.grapjeje.opengrinding.version.v1_21_3.Version1_21_3Handler");
        }

        return null;
    }

    private static VersionHandler instantiateHandler(String className) {
        try {
            Class<?> clazz = Class.forName(className);
            if (VersionHandler.class.isAssignableFrom(clazz)) {
                return (VersionHandler) clazz.getDeclaredConstructor().newInstance();
            }
        } catch (Exception e) {
            Bukkit.getLogger().severe("Failed to instantiate version handler: " + className);
            e.printStackTrace();
        }
        return null;
    }

    @RequiredArgsConstructor
    public enum MinecraftVersion {
        V1_21("1.21"),
        V1_21_1("1.21.1"),
        V1_21_2("1.21.2"),
        V1_21_3("1.21.3"),
        V1_21_4("1.21.4"),
        V1_21_5("1.21.5"),
        V1_21_6("1.21.6"),
        V1_21_7("1.21.7"),
        V1_21_8("1.21.8"),
        V1_21_9("1.21.9"),
        V1_21_10("1.21.10"),
        UNKNOWN(null);

        @Getter
        final String mcVersion;
    }
}