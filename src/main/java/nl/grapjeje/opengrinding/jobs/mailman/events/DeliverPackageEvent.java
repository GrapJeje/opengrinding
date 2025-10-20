package nl.grapjeje.opengrinding.jobs.mailman.events;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import nl.grapjeje.opengrinding.jobs.core.objects.GrindingPlayer;
import nl.grapjeje.opengrinding.jobs.core.objects.GrindingRegion;
import nl.grapjeje.opengrinding.jobs.mailman.objects.MailmanJob;
import nl.grapjeje.opengrinding.utils.event.GrindingPlayerEvent;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

@RequiredArgsConstructor
@Getter
public class DeliverPackageEvent extends Event implements GrindingPlayerEvent {
    @Getter
    private static final HandlerList HANDLERS = new HandlerList();

    private final GrindingPlayer player;
    private final GrindingRegion region;

    public MailmanJob getJob() {
        return MailmanJob.getJobs().get(player.getPlayer().getUuid());
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
