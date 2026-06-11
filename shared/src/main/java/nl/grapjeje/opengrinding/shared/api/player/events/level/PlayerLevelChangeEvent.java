package nl.grapjeje.opengrinding.shared.api.player.events.level;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import nl.grapjeje.opengrinding.shared.api.Jobs;
import nl.grapjeje.opengrinding.shared.api.player.GrindingPlayer;
import nl.grapjeje.opengrinding.shared.utils.event.GrindingPlayerEvent;
import nl.grapjeje.opengrinding.shared.utils.event.JobEvent;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

@RequiredArgsConstructor
@Getter
public class PlayerLevelChangeEvent extends Event implements GrindingPlayerEvent, JobEvent {
    @Getter
    private static final HandlerList HANDLERS = new HandlerList();

    private final GrindingPlayer player;
    private final Jobs job;
    private final int oldLevel;
    private final int newLevel;

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
