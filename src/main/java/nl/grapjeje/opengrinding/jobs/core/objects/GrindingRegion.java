package nl.grapjeje.opengrinding.jobs.core.objects;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import nl.grapjeje.opengrinding.jobs.Jobs;
import nl.grapjeje.opengrinding.models.GrindingRegionModel;
import nl.openminetopia.modules.data.storm.StormDatabase;
import org.bukkit.Location;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

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

    public boolean contains(Location loc) {
        Location min = this.getMinLocation();
        Location max = this.getMaxLocation();

        if (min == null || max == null || !loc.getWorld().equals(min.getWorld())) return false;

        return loc.getBlockX() >= min.getBlockX() && loc.getBlockX() <= max.getBlockX()
                && loc.getBlockY() >= min.getBlockY() && loc.getBlockY() <= max.getBlockY()
                && loc.getBlockZ() >= min.getBlockZ() && loc.getBlockZ() <= max.getBlockZ();
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
}
