package nl.grapjeje.opengrinding.api;

import nl.grapjeje.opengrinding.jobs.core.objects.CraftGrindingRegion;
import nl.grapjeje.opengrinding.models.GrindingRegionModel;
import org.bukkit.Location;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public interface GrindingRegion {

    CompletableFuture<Void> save();

    String getName();
    void setName(String name);

    Location getMinLocation();
    void setMinLocation(Location loc);

    Location getMaxLocation();
    void setMaxLocation(Location loc);

    List<Jobs> getJobs();
    void setJobs(List<Jobs> jobs);

    String getValue();
    void setValue(String value);

    boolean contains(Location loc);

    void addJob(Jobs job);
    void removeJob(Jobs job);

    boolean allowsJob(Jobs job);
    boolean hasValue();

    static void isInRegionWithJob(Location loc, Jobs job, Consumer<Boolean> callback) {
        CraftGrindingRegion.isInRegionWithJob(loc, job, callback);
    }

    static GrindingRegion getRegionAt(Location loc) {
        return CraftGrindingRegion.getRegionAt(loc);
    }

    static GrindingRegion getRegionAt(Location loc, Jobs job) {
        return CraftGrindingRegion.getRegionAt(loc, job);
    }
}
