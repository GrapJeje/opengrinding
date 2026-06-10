package nl.grapjeje.opengrinding.api;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;

public interface Plant {
    UUID getId();

    Block getBlock();

    List<ToolType> whitelistedToolTypes();

    boolean canHarvest();

    void grow();

    void onHarvest(Player player, ToolType tool);

    void onInteract(Player player, ToolType tool, Block block);
}