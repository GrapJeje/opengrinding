package nl.grapjeje.opengrinding.jobs.fishing.base.events;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import nl.grapjeje.opengrinding.utils.event.GrindingPlayerEvent;
import nl.grapjeje.opengrinding.jobs.core.objects.GrindingPlayer;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

@RequiredArgsConstructor
@Getter
public class PlayerFishCatchEvent extends Event implements GrindingPlayerEvent {
    @Getter
    private static final HandlerList HANDLERS = new HandlerList();

    private final GrindingPlayer player;
    private final ItemStack item;

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
