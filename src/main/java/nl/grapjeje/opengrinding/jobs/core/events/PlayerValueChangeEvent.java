package nl.grapjeje.opengrinding.jobs.core.events;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import nl.grapjeje.opengrinding.jobs.Jobs;
import nl.grapjeje.opengrinding.jobs.core.objects.GrindingPlayer;
import nl.grapjeje.opengrinding.utils.event.GrindingPlayerEvent;
import nl.grapjeje.opengrinding.utils.event.JobEvent;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

@RequiredArgsConstructor
@Getter
public class PlayerValueChangeEvent extends Event implements GrindingPlayerEvent, JobEvent {
    @Getter
    private static final HandlerList HANDLERS = new HandlerList();

    private final GrindingPlayer player;
    private final Jobs job;
    private final double oldValue;
    private final double newValue;

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
