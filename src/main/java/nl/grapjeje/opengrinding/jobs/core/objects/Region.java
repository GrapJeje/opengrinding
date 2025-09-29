package nl.grapjeje.opengrinding.jobs.core.objects;

import nl.grapjeje.opengrinding.jobs.core.enums.RegionFlags;
import org.bukkit.Location;

import java.util.List;

public class Region {
    private final Location min;
    private final Location max;

    private List<RegionFlags> flags;

    public Region(Location loc1, Location loc2) {
        if (!loc1.getWorld().equals(loc2.getWorld()))
            throw new IllegalArgumentException("Locaties moeten in dezelfde wereld zijn!");

        this.min = new Location(loc1.getWorld(),
                Math.min(loc1.getBlockX(), loc2.getBlockX()),
                Math.min(loc1.getBlockY(), loc2.getBlockY()),
                Math.min(loc1.getBlockZ(), loc2.getBlockZ()));
        this.max = new Location(loc1.getWorld(),
                Math.max(loc1.getBlockX(), loc2.getBlockX()),
                Math.max(loc1.getBlockY(), loc2.getBlockY()),
                Math.max(loc1.getBlockZ(), loc2.getBlockZ()));
    }

    public boolean contains(Location loc) {
        if (!loc.getWorld().equals(min.getWorld())) return false;
        return loc.getBlockX() >= min.getBlockX() && loc.getBlockX() <= max.getBlockX()
                && loc.getBlockY() >= min.getBlockY() && loc.getBlockY() <= max.getBlockY()
                && loc.getBlockZ() >= min.getBlockZ() && loc.getBlockZ() <= max.getBlockZ();
    }
}

