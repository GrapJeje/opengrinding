package nl.grapjeje.opengrinding.api;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public enum ToolType {
    HAND,
    HOE,
    SHOVEL,
    AXE,
    PICKAXE,
    NONE;

    // TODO: Change all of the jobs to this

    public static ToolType fromItem(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) return HAND;
        Material type = item.getType();

        String name = type.name().toLowerCase();
        if (name.contains("hoe")) return HOE;
        if (name.contains("shovel")) return SHOVEL;
        if (name.contains("axe")) return AXE;
        if (name.contains("pickaxe")) return PICKAXE;
        return NONE;
    }

    public static boolean isValidTool(ToolType given, ToolType required) {
        if (required == NONE) return true;
        return given == required;
    }
}
