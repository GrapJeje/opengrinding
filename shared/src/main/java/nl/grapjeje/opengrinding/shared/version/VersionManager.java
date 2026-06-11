package nl.grapjeje.opengrinding.shared.version;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;

public class VersionManager {

    @Getter
    private static final MinecraftVersion detectedVersion;

    static {
        detectedVersion = detectVersion();
    }

    private static MinecraftVersion detectVersion() {
        String serverVersion = Bukkit.getVersion();

        if (serverVersion.contains("MC: 1.21.10")) {
            return MinecraftVersion.V1_21_10;
        } else if (serverVersion.contains("MC: 1.21.9")) {
            return MinecraftVersion.V1_21_9;
        } else if (serverVersion.contains("MC: 1.21.8")) {
            return MinecraftVersion.V1_21_8;
        } else if (serverVersion.contains("MC: 1.21.7")) {
            return MinecraftVersion.V1_21_7;
        } else if (serverVersion.contains("MC: 1.21.6")) {
            return MinecraftVersion.V1_21_6;
        } else if (serverVersion.contains("MC: 1.21.5")) {
            return MinecraftVersion.V1_21_5;
        } else if (serverVersion.contains("MC: 1.21.4")) {
            return MinecraftVersion.V1_21_4;
        } else if (serverVersion.contains("MC: 1.21.3")) {
            return MinecraftVersion.V1_21_3;
        } else if (serverVersion.contains("MC: 1.21.2")) {
            return MinecraftVersion.V1_21_2;
        } else if (serverVersion.contains("MC: 1.21.1")) {
            return MinecraftVersion.V1_21_1;
        } else if (serverVersion.contains("MC: 1.21")) {
            return MinecraftVersion.V1_21;
        }

        return MinecraftVersion.UNKNOWN;
    }

    public static boolean isSupportedVersion() {
        return detectedVersion != MinecraftVersion.UNKNOWN;
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
        UNKNOWN("unknown");

        @Getter
        final String versionString;
    }
}

