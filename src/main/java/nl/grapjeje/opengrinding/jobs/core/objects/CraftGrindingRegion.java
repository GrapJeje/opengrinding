package nl.grapjeje.opengrinding.jobs.core.objects;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import nl.grapjeje.core.registry.AutoRegistry;
import nl.grapjeje.core.registry.Registry;
import nl.grapjeje.opengrinding.OpenGrinding;
import nl.grapjeje.opengrinding.api.GrindingRegion;
import nl.grapjeje.opengrinding.api.Jobs;
import nl.grapjeje.opengrinding.api.player.GrindingPlayer;
import nl.grapjeje.opengrinding.models.GrindingRegionModel;
import nl.grapjeje.opengrinding.models.PlayerGrindingModel;
import nl.openminetopia.modules.data.storm.StormDatabase;
import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Predicate;

@AutoRegistry
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class CraftGrindingRegion implements GrindingRegion {
    private final GrindingRegionModel model;

    public static GrindingRegion get(GrindingRegionModel model) {
        return Registry.get(
                GrindingRegion.class,
                (args) -> new CraftGrindingRegion((GrindingRegionModel) args[0]),
                model
        );
    }

    @Override
    public CompletableFuture<Void> save() {
        return CompletableFuture.runAsync(() -> StormDatabase.getInstance().saveStormModel(model));
    }

    @Override
    public String getName() {
        return model.getName();
    }

    @Override
    public void setName(String name) {
        model.setName(name);
    }

    @Override
    public Location getMinLocation() {
        return model.getMinLocation();
    }

    @Override
    public void setMinLocation(Location loc) {
        model.setMinLocation(loc);
    }

    @Override
    public Location getMaxLocation() {
        return model.getMaxLocation();
    }

    @Override
    public void setMaxLocation(Location loc) {
        model.setMaxLocation(loc);
    }

    @Override
    public List<Jobs> getJobs() {
        return model.getJobs();
    }

    @Override
    public void setJobs(List<Jobs> jobs) {
        model.setJobs(jobs);
    }

    @Override
    public String getValue() {
        return model.getValue();
    }

    @Override
    public void setValue(String value) {
        model.setValue(value);
    }

    @Override
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

    @Override
    public void addJob(Jobs job) {
        List<Jobs> jobs = new ArrayList<>(this.getJobs());
        if (!jobs.contains(job)) {
            jobs.add(job);
            this.setJobs(jobs);
        }
    }

    @Override
    public void removeJob(Jobs job) {
        List<Jobs> jobs = new ArrayList<>(this.getJobs());
        jobs.remove(job);
        this.setJobs(jobs);
    }

    @Override
    public boolean allowsJob(Jobs job) {
        return this.getJobs().contains(job);
    }

    @Override
    public boolean hasValue() {
        return this.getValue() != null && !this.getValue().isEmpty();
    }

    public static void isInRegionWithJob(Location loc, Jobs job, Consumer<Boolean> callback) {
        Bukkit.getScheduler().runTaskAsynchronously(OpenGrinding.getInstance(), () -> {
            boolean result = false;
            try {
                List<GrindingRegionModel> allRegions = StormDatabase.getInstance().getStorm()
                        .buildQuery(GrindingRegionModel.class)
                        .execute()
                        .join()
                        .stream()
                        .toList();

                for (GrindingRegionModel model : allRegions) {
                    CraftGrindingRegion region = new CraftGrindingRegion(model);
                    if (region.contains(loc) && region.allowsJob(job)) {
                        result = true;
                        break;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            boolean finalResult = result;
            Bukkit.getScheduler().runTask(OpenGrinding.getInstance(), () -> callback.accept(finalResult));
        });
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
                GrindingRegion region = CraftGrindingRegion.get(model);
                if (region.contains(loc) && filter.test(region))
                    return region;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}