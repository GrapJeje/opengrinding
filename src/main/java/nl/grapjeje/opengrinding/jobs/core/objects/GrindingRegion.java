package nl.grapjeje.opengrinding.jobs.core.objects;

import com.craftmend.storm.api.enums.Where;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import nl.grapjeje.opengrinding.jobs.Jobs;
import nl.grapjeje.opengrinding.models.GrindingRegionModel;
import nl.openminetopia.modules.data.storm.StormDatabase;
import org.bukkit.Location;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;

@Getter
@RequiredArgsConstructor
public class GrindingRegion {
    private final GrindingRegionModel model;

    public CompletableFuture<Void> save() {
        return CompletableFuture.runAsync(() -> StormDatabase.getInstance().saveStormModel(model));
    }

    public String getName() {
        return model.getName();
    }

    public void setName(String name) {
        model.setName(name);
    }

    public Location getMinLocation() {
        return model.getMinLocation();
    }

    public void setMinLocation(Location loc) {
        model.setMinLocation(loc);
    }

    public Location getMaxLocation() {
        return model.getMaxLocation();
    }

    public void setMaxLocation(Location loc) {
        model.setMaxLocation(loc);
    }

    public List<Jobs> getJobs() {
        return model.getJobs();
    }

    public void setJobs(List<Jobs> jobs) {
        model.setJobs(jobs);
    }

    public String getValue() {
        return model.getValue();
    }

    public void setValue(String value) {
        model.setValue(value);
    }

    public boolean contains(Location loc) {
        Location min = this.getMinLocation();
        Location max = this.getMaxLocation();

        if (min == null || max == null || !loc.getWorld().equals(min.getWorld())) return false;

        int minX = Math.min(min.getBlockX(), max.getBlockX());
        int maxX = Math.max(min.getBlockX(), max.getBlockX());
        int minY = Math.min(min.getBlockY(), max.getBlockY());
        int maxY = Math.max(min.getBlockY(), max.getBlockY());
        int minZ = Math.min(min.getBlockZ(), max.getBlockZ());
        int maxZ = Math.max(min.getBlockZ(), max.getBlockZ());

        return loc.getBlockX() >= minX && loc.getBlockX() <= maxX
                && loc.getBlockY() >= minY && loc.getBlockY() <= maxY
                && loc.getBlockZ() >= minZ && loc.getBlockZ() <= maxZ;
    }


    public void addJob(Jobs job) {
        List<Jobs> jobs = new ArrayList<>(this.getJobs());
        if (!jobs.contains(job)) {
            jobs.add(job);
            this.setJobs(jobs);
        }
    }

    public void removeJob(Jobs job) {
        List<Jobs> jobs = new ArrayList<>(this.getJobs());
        if (jobs.contains(job)) {
            jobs.remove(job);
            this.setJobs(jobs);
        }
    }

    public boolean allowsJob(Jobs job) {
        return this.getJobs().contains(job);
    }

    public boolean hasValue() {
        return this.getValue() != null || !(this.getValue().isEmpty());
    }

    public static boolean isInRegionWithJob(Location loc, Jobs job) {
        try {
            List<GrindingRegionModel> allRegions = StormDatabase.getInstance().getStorm()
                    .buildQuery(GrindingRegionModel.class)
                    .execute()
                    .join()
                    .stream()
                    .toList();

            for (GrindingRegionModel model : allRegions) {
                GrindingRegion region = new GrindingRegion(model);
                if (region.contains(loc) && region.allowsJob(job))
                    return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static GrindingRegion getRegionAt(Location loc) {
        return getRegionAt(loc, region -> true);
    }

    public static GrindingRegion getRegionAt(Location loc, Jobs job) {
        return getRegionAt(loc, region -> region.getJobs().contains(job));
    }

    private static GrindingRegion getRegionAt(Location loc, Predicate<GrindingRegion> filter) {
        try {
            Collection<GrindingRegionModel> allRegions = StormDatabase.getInstance().getStorm()
                    .buildQuery(GrindingRegionModel.class)
                    .execute()
                    .join();

            for (GrindingRegionModel model : allRegions) {
                GrindingRegion region = new GrindingRegion(model);
                if (region.contains(loc) && filter.test(region))
                    return region;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
