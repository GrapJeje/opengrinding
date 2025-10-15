package nl.grapjeje.opengrinding.jobs.core.commands;

import nl.grapjeje.core.command.Command;
import nl.grapjeje.core.command.CommandSourceStack;
import nl.grapjeje.core.text.MessageUtil;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class FixSkullCommand implements Command {

    @Override
    public String getName() {
        return "fixskull";
    }

    @Override
    public void execute(CommandSourceStack source, String[] strings) {
        if (!(source.getSender() instanceof Player player)) {
            source.getSender().sendMessage("Dit command kan alleen door een speler uitgevoerd worden.");
            return;
        }

        ItemStack helmet = player.getInventory().getHelmet();
        if (helmet == null || helmet.getType() != Material.PLAYER_HEAD) {
            player.sendMessage(MessageUtil.filterMessage("<warning>⚠ Er zit geen player head op je hoofd!"));
            return;
        }

        if (this.isInventoryFull(player)) {
            player.sendMessage(MessageUtil.filterMessage("<warning>⚠ Je inventory is vol, kan de head niet terugplaatsen!"));
            return;
        }

        player.getInventory().addItem(helmet);
        player.getInventory().setHelmet(null);
    }

    private boolean isInventoryFull(Player player) {
        for (ItemStack item : player.getInventory().getStorageContents()) {
            if (item == null || item.getType() == Material.AIR) return false;
            if (item.getAmount() < item.getMaxStackSize()) return false;
        }
        return true;
    }
}
