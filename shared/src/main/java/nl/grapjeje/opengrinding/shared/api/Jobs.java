package nl.grapjeje.opengrinding.shared.api;

import nl.grapjeje.core.modules.Module;

public enum Jobs {
    MINING,
    FISHING,
    LUMBER,
    MAILMAN,
    FARMING;

    public static Jobs getByModule(Module module) {
        return switch (module.getName()) {
            case "mining" -> Jobs.MINING;
            case "fishing" -> Jobs.FISHING;
            case "lumber" -> Jobs.LUMBER;
            case "mailman" -> Jobs.MAILMAN;
            case "farming" -> Jobs.FARMING;
            default -> null;
        };
    }

}
