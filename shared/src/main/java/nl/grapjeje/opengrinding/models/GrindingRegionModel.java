package nl.grapjeje.opengrinding.models;

import com.craftmend.storm.api.StormModel;
import com.craftmend.storm.api.markers.Column;
import com.craftmend.storm.api.markers.Table;
import com.google.gson.Gson;
import lombok.Data;
import lombok.EqualsAndHashCode;
import nl.grapjeje.opengrinding.api.Jobs;
import nl.openminetopia.modules.data.storm.StormDatabase;
import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Data
@EqualsAndHashCode(callSuper = false)
@Table(name = "grinding_regions")
public class GrindingRegionModel extends StormModel {
    private static final Gson GSON = new Gson();

    @Column(name = "name")
    private String name;

    @Column(name = "min_location")
    private String minLocationJson;

    @Column(name = "max_location")
    private String maxLocationJson;

    @Column(name = "jobs")
    private String jobsJson;

    @Column(name = "value")
    private String value;

    public Location getMinLocation() {
        return this.fromJson(minLocationJson);
    }

    public void setMinLocation(Location location) {
        this.minLocationJson = this.toJson(location);
    }

    public Location getMaxLocation() {
        return this.fromJson(maxLocationJson);
    }

    public void setMaxLocation(Location location) {
        this.maxLocationJson = this.toJson(location);
    }

    public List<Jobs> getJobs() {
        if (jobsJson == null || jobsJson.isEmpty()) return List.of();
        String[] arr = GSON.fromJson(jobsJson, String[].class);
        return Arrays.stream(arr).map(Jobs::valueOf).collect(Collectors.toList());
    }

    public void setJobs(List<Jobs> jobs) {
        this.jobsJson = GSON.toJson(jobs.stream().map(Enum::name).toArray());
    }

    private String toJson(Location loc) {
        if (loc == null) return null;
        return GSON.toJson(new LocationData(loc));
    }

    private Location fromJson(String json) {
        if (json == null) return null;
        LocationData data = GSON.fromJson(json, LocationData.class);
        return data.toBukkitLocation();
    }

    public void save() {
        StormDatabase.getInstance().saveStormModel(this);
    }

    @Data
    private static class LocationData {
        private String world;
        private double x, y, z;
        private float yaw, pitch;

        public LocationData(Location loc) {
            this.world = loc.getWorld().getName();
            this.x = loc.getX();
            this.y = loc.getY();
            this.z = loc.getZ();
            this.yaw = loc.getYaw();
            this.pitch = loc.getPitch();
        }

        public Location toBukkitLocation() {
            return Bukkit.getWorld(world) != null ? new Location(Bukkit.getWorld(world), x, y, z, yaw, pitch) : null;
        }
    }
}
