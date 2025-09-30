package nl.grapjeje.opengrinding.jobs.mining.objects;

import org.bukkit.Location;
import org.bukkit.Material;

public record MiningOres(Location location, Material material, long time) {
}
