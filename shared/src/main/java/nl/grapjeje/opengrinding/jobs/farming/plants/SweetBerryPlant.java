package nl.grapjeje.opengrinding.jobs.farming.plants;

import lombok.Getter;
import nl.grapjeje.opengrinding.OpenGrinding;
import nl.grapjeje.opengrinding.api.Jobs;
import nl.grapjeje.opengrinding.api.ToolType;
import nl.grapjeje.opengrinding.api.player.GrindingPlayer;
import nl.grapjeje.opengrinding.core.objects.CraftGrindingPlayer;
import nl.grapjeje.opengrinding.jobs.farming.FarmingModule;
import nl.grapjeje.opengrinding.jobs.farming.configuration.FarmingJobConfiguration;
import nl.grapjeje.opengrinding.jobs.farming.objects.GrowthStage;
import nl.grapjeje.opengrinding.jobs.farming.objects.Plant;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;
import java.util.UUID;

public class SweetBerryPlant extends GrowablePlant {
    @Getter
    private final UUID id;

    public SweetBerryPlant(UUID id, Block block, GrowthStage stage) {
        super(block, stage, 7, 60000);
        this.id = id;
    }

    public SweetBerryPlant(UUID id, Block block) {
        this(id, block, GrowthStage.SEED);
    }

    @Override
    public List<ToolType> whitelistedToolTypes() {
        return List.of(ToolType.HOE, ToolType.HAND);
    }

    @Override
    public void onHarvest(Player player, ToolType tool) {
        for (nl.grapjeje.opengrinding.api.Plant plant : FarmingModule.getPlants()) {
            if (plant.getBlock() != this.getBlock()) continue;
            FarmingModule.getPlants().remove(plant);
        }

        ItemStack custom = FarmingModule.getBlockHead(Plant.SWEET_BERRY);
        if (custom != null) player.getInventory().addItem(custom);

        if (this.getBlock().getBlockData() instanceof Ageable ageable) {
            ageable.setAge(0);
            this.getBlock().setBlockData(ageable);
        }

        ItemStack item = player.getInventory().getItemInMainHand();
        if (item != null && item.getType() != Material.AIR) {
            ItemMeta meta = item.getItemMeta();
            if (meta instanceof Damageable damageable) {
                int currentDamage = damageable.getDamage();
                int maxDurability = item.getType().getMaxDurability();

                if (currentDamage + 1 < maxDurability)
                    damageable.setDamage(currentDamage + 1);
                else item.setAmount(item.getAmount() - 1);
                item.setItemMeta(damageable);
            }
        }
        player.giveExp(0);

        this.setStage(GrowthStage.SEED);
        FarmingModule.getPlants().add(this.getPlant(SweetBerryPlant.this));

        GrindingPlayer.loadOrCreatePlayerModelAsync(player, Jobs.FARMING)
                .thenAccept(model -> {
                    FarmingJobConfiguration.PlantRecord plantRecord = FarmingModule.getConfig().getPlants().get(Plant.SWEET_BERRY);
                    GrindingPlayer gp = CraftGrindingPlayer.get(player.getUniqueId(), model);

                    try {
                        gp.addProgress(Jobs.FARMING, plantRecord.points());
                        gp.save(Jobs.FARMING)
                                .exceptionally(ex -> {
                                    OpenGrinding.getInstance().getLogger().severe("Failed to save player model: " + ex.getMessage());
                                    ex.printStackTrace();
                                    return null;
                                });
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }).exceptionally(ex -> {
                    ex.printStackTrace();
                    return null;
                });
    }

    @Override
    public void onInteract(Player player, ToolType tool, Block block) {
        if (block.getLocation().equals(this.getBlock().getLocation())
                && block.getType() == this.getBlock().getType()
                && this.whitelistedToolTypes().contains(tool)
                && this.getStage().isMax())
            this.onHarvest(player, tool);
    }
}
