package nl.grapjeje.opengrinding.jobs.lumber.timers;

import nl.grapjeje.opengrinding.OpenGrinding;
import nl.grapjeje.opengrinding.jobs.lumber.LumberModule;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;

public class WoodTimer {
    private final long respawnTimeMillis;

    public WoodTimer(long respawnTimeMinutes) {
        this.respawnTimeMillis = respawnTimeMinutes * 60_000L;
        this.startTimer();
    }

    private void startTimer() {
        Bukkit.getScheduler().runTaskTimerAsynchronously(OpenGrinding.getInstance(), () -> {
            long now = System.currentTimeMillis();
            for (LumberModule.LumberWood wood : LumberModule.getWoods()) {
                long elapsed = now - wood.time();

                if (elapsed >= respawnTimeMillis) {
                    Bukkit.getScheduler().runTask(OpenGrinding.getInstance(), () -> {
                        Block block = wood.location().getBlock();
                        Material original = wood.material();

                        if (this.isStrippedBlock(block.getType(), original)) {
                            block.setType(original);
                            LumberModule.getWoods().remove(wood);
                        } else if (this.isAirOrOther(block.getType())) {
                            Material stripped = this.getStrippedFromBark(original);
                            block.setType(stripped);
                        } else LumberModule.getWoods().remove(wood);
                    });
                }
            }
        }, 0L, 20L);
    }

    private boolean isStrippedBlock(Material current, Material bark) {
        return current == this.getStrippedFromBark(bark);
    }

    private boolean isAirOrOther(Material current) {
        return current.isAir();
    }

    private Material getStrippedFromBark(Material bark) {
        return switch (bark) {
            case OAK_WOOD -> Material.STRIPPED_OAK_WOOD;
            case SPRUCE_WOOD -> Material.STRIPPED_SPRUCE_WOOD;
            case BIRCH_WOOD -> Material.STRIPPED_BIRCH_WOOD;
            case JUNGLE_WOOD -> Material.STRIPPED_JUNGLE_WOOD;
            case ACACIA_WOOD -> Material.STRIPPED_ACACIA_WOOD;
            case DARK_OAK_WOOD -> Material.STRIPPED_DARK_OAK_WOOD;
            case MANGROVE_WOOD -> Material.STRIPPED_MANGROVE_WOOD;
            case CHERRY_WOOD -> Material.STRIPPED_CHERRY_WOOD;
            case CRIMSON_HYPHAE -> Material.STRIPPED_CRIMSON_HYPHAE;
            case WARPED_HYPHAE -> Material.STRIPPED_WARPED_HYPHAE;
            default -> {
                try {
                    if (bark.name().equals("PALE_OAK_WOOD")) {
                        yield Material.valueOf("STRIPPED_PALE_OAK_WOOD");
                    }
                } catch (IllegalArgumentException ignored) {}
                yield bark;
            }
        };
    }
}
