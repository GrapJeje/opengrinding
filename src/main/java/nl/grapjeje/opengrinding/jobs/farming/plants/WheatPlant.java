package nl.grapjeje.opengrinding.jobs.farming.plants;

import lombok.Getter;
import nl.grapjeje.opengrinding.api.ToolType;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;

@Getter
public class WheatPlant extends GrowablePlant {
    private final UUID id;

    protected WheatPlant(UUID id) {
        super(7, 60000);
        this.id = id;
    }

    @Override
    public List<ToolType> whitelistedToolTypes() {
        return List.of(ToolType.HOE);
    }

    @Override
    public void grow() {
        super.grow();
    }

    @Override
    public void onHarvest(Player player, ToolType tool) {

    }

    @Override
    public void onInteract(Player player, ToolType tool, Block block) {

    }
}
