package nl.grapjeje.opengrinding.jobs.core.commands;

import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import nl.grapjeje.core.command.Command;
import nl.grapjeje.core.command.CommandSourceStack;
import nl.grapjeje.core.text.MessageUtil;
import nl.grapjeje.opengrinding.OpenGrinding;
import nl.grapjeje.opengrinding.jobs.mining.MiningModule;
import nl.grapjeje.opengrinding.jobs.mining.configuration.MiningJobConfiguration;
import nl.grapjeje.opengrinding.jobs.mining.guis.ShopMenu;
import nl.grapjeje.opengrinding.jobs.mining.objects.Ore;
import nl.grapjeje.opengrinding.utils.currency.Currency;
import nl.grapjeje.opengrinding.utils.currency.CurrencyUtil;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Stream;

public class SellCommand implements Command {

    @Override
    public String getName() {
        return "grindingsell";
    }

    @Override
    public void execute(CommandSourceStack source, String[] args) {
        Player player = source.getPlayer();
        if (player == null) {
            source.getSender().sendRichMessage("<warning>⚠ Alleen spelers kunnen dit commando gebruiken!");
            return;
        }

        if (args == null || args.length == 0) return;

        String sub = args[0].toLowerCase();
        switch (sub) {
            case "mining" -> this.handleMiningCommand(player);
        }
    }

    private void handleMiningCommand(Player player) {
        ItemStack itemInHand = player.getInventory().getItemInMainHand();
        if (itemInHand == null || itemInHand.getType() != Material.PLAYER_HEAD) {
            if (MiningModule.getConfig().isOpenBuyShop()) new ShopMenu().open(player);
            else player.sendMessage(MessageUtil.filterMessage("<warning>⚠ Dit item kun je hier niet verkopen!"));
            return;
        }
        SkullMeta meta = (SkullMeta) itemInHand.getItemMeta();
        if (meta == null || meta.getPlayerProfile() == null) {
            if (MiningModule.getConfig().isOpenBuyShop()) new ShopMenu().open(player);
            else player.sendMessage(MessageUtil.filterMessage("<warning>⚠ Dit item kun je hier niet verkopen!"));
            return;
        }

        UUID skullUuid = meta.getPlayerProfile().getId();

        MiningJobConfiguration config = MiningModule.getConfig();
        if (!config.isSellEnabled()) {
            player.sendMessage(MessageUtil.filterMessage("<warning>⚠ Je kunt momenteel niks verkopen! Contacteer een beheerder als jij denkt dat dit een fout is."));
            return;
        }

        Optional<Ore> enumOre = Arrays.stream(Ore.values())
                .filter(ore -> ore.getUuid().equals(skullUuid))
                .findFirst();
        if (enumOre.isEmpty()) {
            if (MiningModule.getConfig().isOpenBuyShop()) new ShopMenu().open(player);
            else player.sendMessage(MessageUtil.filterMessage("<warning>⚠ Dit item kun je hier niet verkopen!"));
            return;
        }

        var oreRecord = config.getOres().values().stream()
                .filter(record -> record.name().equalsIgnoreCase(enumOre.get().name()))
                .findFirst()
                .orElse(null);

        if (oreRecord == null || (oreRecord.price().cash() <= 0 && oreRecord.price().grindToken() <= 0)) {
            if (MiningModule.getConfig().isOpenBuyShop()) new ShopMenu().open(player);
            else player.sendMessage(MessageUtil.filterMessage("<warning>⚠ Dit item kun je hier niet verkopen!"));
            return;
        }

        int amountInHand = itemInHand.getAmount();
        double cashAmount = oreRecord.price().cash() * amountInHand;
        double tokenAmount = oreRecord.price().grindToken() * amountInHand;

        CurrencyUtil.giveReward(player, cashAmount, tokenAmount, "Sold ores").thenAccept(rewardMap -> {
            int removedAmount = itemInHand.getAmount();
            player.getInventory().removeItem(itemInHand);

            String itemName = PlainTextComponentSerializer.plainText()
                    .serialize(itemInHand.getItemMeta().displayName());

            double receivedAmount = rewardMap.values().stream().mapToDouble(Double::doubleValue).sum();
            player.sendMessage(MessageUtil.filterMessage(
                    "<green>Je hebt succesvol <bold>" + removedAmount + " " + itemName +
                            "<!bold> verkocht voor <bold>" + receivedAmount + "<!bold>!"
            ));
        });
    }

    @Override
    public @NotNull Collection<String> suggest(@NotNull CommandSourceStack source, @NotNull String[] args) {
        Player player = source.getPlayer();
        if (player == null) return Collections.emptyList();
        if (!this.canUse(player)) return Collections.emptyList();

        if (args.length == 1) {
            return Stream.of("mining")
                    .filter(s -> s.toLowerCase().startsWith(args[0].toLowerCase()))
                    .toList();
        }
        return Collections.emptyList();
    }

    @Override
    public @Nullable String permission() {
        return "opengrinding.*";
    }
}
