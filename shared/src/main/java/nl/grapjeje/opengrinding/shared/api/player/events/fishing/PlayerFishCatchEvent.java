package nl.grapjeje.opengrinding.shared.api.player.events.fishing;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import nl.grapjeje.opengrinding.shared.api.player.GrindingPlayer;
import nl.grapjeje.opengrinding.shared.utils.event.GrindingPlayerEvent;
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
