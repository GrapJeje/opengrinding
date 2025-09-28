package nl.grapjeje.opengrinding.jobs;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import nl.grapjeje.opengrinding.Opengrinding;

@Getter
@RequiredArgsConstructor
public abstract class Job {
    private final String name;

    public void enable() {
        Opengrinding.getInstance().getLogger().info("Enabeling " + name + "...");
        this.onEnable();
    }

    public void disable() {
        Opengrinding.getInstance().getLogger().info("Disabeling " + name + "...");
        this.onDisable();
    }

    protected abstract void onEnable();

    protected abstract void onDisable();
}
