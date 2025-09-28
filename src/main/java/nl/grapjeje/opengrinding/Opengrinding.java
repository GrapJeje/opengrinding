package nl.grapjeje.opengrinding;

import lombok.Getter;
import nl.grapjeje.core.Framework;
import nl.grapjeje.opengrinding.jobs.Job;
import nl.grapjeje.opengrinding.jobs.core.Core;
import nl.grapjeje.opengrinding.jobs.mining.MiningJob;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

public final class Opengrinding extends JavaPlugin {

    @Getter
    private static Opengrinding instance;

    @Getter
    private static List<Job> jobs = new ArrayList<>();

    @Getter
    private static Framework framework;

    @Override
    public void onEnable() {
        instance = this;

        framework = Framework.init(this);

        // Register jobs
        jobs.add(new Core());
        jobs.add(new MiningJob());

        // Enable jobs
        jobs.forEach(Job::enable);
    }

    @Override
    public void onDisable() {
        // Disable jobs
        jobs.forEach(Job::disable);
    }
}
