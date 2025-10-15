package nl.grapjeje.opengrinding.jobs.core.commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import nl.grapjeje.core.command.Command;
import nl.grapjeje.core.command.CommandSourceStack;
import nl.grapjeje.core.text.MessageUtil;
import nl.grapjeje.opengrinding.OpenGrinding;
import nl.grapjeje.opengrinding.jobs.fishing.FishingModule;
import nl.grapjeje.opengrinding.jobs.fishing.configuration.FishingJobConfiguration;
import nl.grapjeje.opengrinding.jobs.fishing.guis.FishingRodShopMenu;
import nl.grapjeje.opengrinding.jobs.lumber.LumberModule;
import nl.grapjeje.opengrinding.jobs.lumber.configuration.LumberJobConfiguration;
import nl.grapjeje.opengrinding.jobs.lumber.guis.AxeShopMenu;
import nl.grapjeje.opengrinding.jobs.lumber.objects.Wood;
import nl.grapjeje.opengrinding.jobs.mining.MiningModule;
import nl.grapjeje.opengrinding.jobs.mining.configuration.MiningJobConfiguration;
import nl.grapjeje.opengrinding.jobs.mining.guis.PickaxeShopMenu;
import nl.grapjeje.opengrinding.jobs.mining.objects.Ore;
import nl.grapjeje.opengrinding.utils.currency.Currency;
import nl.grapjeje.opengrinding.utils.currency.CurrencyUtil;
import nl.grapjeje.opengrinding.utils.currency.Price;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
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
            case "fishing" -> this.handleFishingCommand(player);
            case "lumber" -> this.handleLumberCommand(player); // TODO: Can't sell
        }
    }

    private void handleMiningCommand(Player player) {
        ItemStack itemInHand = player.getInventory().getItemInMainHand();
        if (itemInHand == null || itemInHand.getType() != Material.PLAYER_HEAD) {
            if (MiningModule.getConfig().isOpenBuyShop()) new PickaxeShopMenu().open(player);
            else player.sendMessage(MessageUtil.filterMessage("<warning>⚠ Dit item kun je hier niet verkopen!"));
            return;
        }
        SkullMeta meta = (SkullMeta) itemInHand.getItemMeta();
        if (meta == null || meta.getPlayerProfile() == null) {
            if (MiningModule.getConfig().isOpenBuyShop()) new PickaxeShopMenu().open(player);
            else player.sendMessage(MessageUtil.filterMessage("<warning>⚠ Dit item kun je hier niet verkopen!"));
            return;
        }
        UUID skullUuid = meta.getPlayerProfile().getId();

        MiningModule miningModule = OpenGrinding.getFramework().getModuleLoader()
                .getModules().stream()
                .filter(m -> m instanceof MiningModule)
                .map(m -> (MiningModule) m)
                .findFirst()
                .orElse(null);

        if (miningModule == null || miningModule.isDisabled()) {
            player.sendMessage(MessageUtil.filterMessage("<warning>⚠ De mining module is momenteel uitgeschakeld!"));
            return;
        }
        MiningJobConfiguration config = MiningModule.getConfig();
        if (!config.isSellEnabled()) {
            player.sendMessage(MessageUtil.filterMessage("<warning>⚠ Je kunt momenteel niks verkopen! Contacteer een beheerder als jij denkt dat dit een fout is."));
            return;
        }
        Optional<Ore> enumOre = Arrays.stream(Ore.values()).filter(ore -> ore.getUuid().equals(skullUuid)).findFirst();
        if (enumOre.isEmpty()) {
            if (MiningModule.getConfig().isOpenBuyShop()) new PickaxeShopMenu().open(player);
            else player.sendMessage(MessageUtil.filterMessage("<warning>⚠ Dit item kun je hier niet verkopen!"));
            return;
        }
        var oreRecord = config.getOres().values().stream()
                .filter(record -> Objects.equals(record.name(), enumOre.get().name().toLowerCase()))
                .findFirst()
                .orElse(null);

        if (oreRecord == null || oreRecord.price().cash() <= 0 || oreRecord.price().grindToken() <= 0) {
            if (MiningModule.getConfig().isOpenBuyShop()) new PickaxeShopMenu().open(player);
            else player.sendMessage(MessageUtil.filterMessage("<warning>⚠ Dit item kun je hier niet verkopen!"));
            return;
        }
        int amountInHand = itemInHand.getAmount();
        double cashAmount = oreRecord.price().cash() * amountInHand;
        double tokenAmount = oreRecord.price().grindToken() * amountInHand;

        CurrencyUtil.giveReward(player, cashAmount, tokenAmount, "Sold ores").thenAccept(rewardMap -> {
            double receivedAmount = rewardMap.values().stream().mapToDouble(Double::doubleValue).sum();
            if (receivedAmount > 0) {
                int removedAmount = itemInHand.getAmount();
                player.getInventory().removeItem(itemInHand);

                String itemName = PlainTextComponentSerializer.plainText()
                        .serialize(itemInHand.getItemMeta().displayName());
                player.sendMessage(MessageUtil.filterMessage(
                        "<green>Je hebt succesvol <bold>" + removedAmount + " " + itemName +
                                "<!bold> verkocht voor <bold>" + receivedAmount + "<!bold>!"
                ));
            }
        });
    }

    private void handleFishingCommand(Player player) {
        ItemStack itemInHand = player.getInventory().getItemInMainHand();
        FishingJobConfiguration config = FishingModule.getConfig();
        if (itemInHand == null || itemInHand.getType() == Material.AIR) {
            if (config.isOpenBuyShop()) new FishingRodShopMenu().open(player);
            else player.sendMessage(MessageUtil.filterMessage("<warning>⚠ Je hebt niets in je hand!"));
            return;
        }
        FishingModule module = OpenGrinding.getFramework().getModuleLoader()
                .getModules().stream()
                .filter(m -> m instanceof FishingModule)
                .map(m -> (FishingModule) m)
                .findFirst()
                .orElse(null);

        if (module == null || module.isDisabled()) {
            player.sendMessage(MessageUtil.filterMessage("<warning>⚠ De fishing module is momenteel uitgeschakeld!"));
            return;
        }
        if (!config.isSellEnabled()) {
            player.sendMessage(MessageUtil.filterMessage("<warning>⚠ Je kunt momenteel niks verkopen!"));
            return;
        }
        var fish = config.getFish(itemInHand.getType());
        if (fish == null || fish.price().cash() <= 0 || fish.price().grindToken() <= 0) {
            if (config.isOpenBuyShop()) new FishingRodShopMenu().open(player);
            else player.sendMessage(MessageUtil.filterMessage("<warning>⚠ Dit item kun je hier niet verkopen!"));
            return;
        }
        ItemMeta meta = itemInHand.getItemMeta();
        if (meta == null) return;

        NamespacedKey key = new NamespacedKey("opengrinding", "fish_weight");
        if (!meta.getPersistentDataContainer().has(key, PersistentDataType.DOUBLE)) {
            if (config.isOpenBuyShop()) new FishingRodShopMenu().open(player);
            else player.sendMessage(MessageUtil.filterMessage("<warning>⚠ Dit item kun je hier niet verkopen!"));
            return;
        }
        int amountInHand = itemInHand.getAmount();
        double weight = meta.getPersistentDataContainer().get(key, PersistentDataType.DOUBLE);
        double cashAmount = fish.price().cash() * weight * amountInHand;
        double tokenAmount = fish.price().grindToken() * weight * amountInHand;
        int removedAmount = itemInHand.getAmount();

        Component name = itemInHand.getItemMeta().displayName();
        String itemName;
        if (name == null) itemName = itemInHand.getType().name();
        else itemName = PlainTextComponentSerializer.plainText().serialize(name);

        CurrencyUtil.giveReward(player, cashAmount, tokenAmount, "Sold fishes").thenAccept(rewardMap -> {
            double receivedAmount = rewardMap.values().stream().mapToDouble(Double::doubleValue).sum();
            if (receivedAmount > 0) {
                player.getInventory().removeItem(itemInHand);
                player.sendMessage(MessageUtil.filterMessage(
                        "<green>Je hebt succesvol <bold>" + removedAmount + " " + itemName +
                                "<!bold> verkocht voor <bold>" + receivedAmount + "<!bold>!"
                ));
            }
        });
    }

    private void handleLumberCommand(Player player) {
        ItemStack itemInHand = player.getInventory().getItemInMainHand();
        if (itemInHand == null || itemInHand.getType() != Material.PLAYER_HEAD) {
            if (LumberModule.getConfig().isOpenBuyShop()) new AxeShopMenu().open(player);
            else player.sendMessage(MessageUtil.filterMessage("<warning>⚠ Dit item kun je hier niet verkopen!"));
            return;
        }
        SkullMeta meta = (SkullMeta) itemInHand.getItemMeta();
        if (meta == null || meta.getPlayerProfile() == null) {
            if (LumberModule.getConfig().isOpenBuyShop()) new AxeShopMenu().open(player);
            else player.sendMessage(MessageUtil.filterMessage("<warning>⚠ Dit item kun je hier niet verkopen!"));
            return;
        }
        UUID skullUuid = meta.getPlayerProfile().getId();

        LumberModule lumberModule = OpenGrinding.getFramework().getModuleLoader()
                .getModules().stream()
                .filter(m -> m instanceof LumberModule)
                .map(m -> (LumberModule) m)
                .findFirst()
                .orElse(null);

        if (lumberModule == null || lumberModule.isDisabled()) {
            player.sendMessage(MessageUtil.filterMessage("<warning>⚠ De lumber module is momenteel uitgeschakeld!"));
            return;
        }
        LumberJobConfiguration config = LumberModule.getConfig();
        if (!config.isSellEnabled()) {
            player.sendMessage(MessageUtil.filterMessage("<warning>⚠ Je kunt momenteel niks verkopen! Contacteer een beheerder als jij denkt dat dit een fout is."));
            return;
        }
        Optional<Wood> woodEnum = Arrays.stream(Wood.values())
                .filter(ore -> ore.getUuid().equals(skullUuid))
                .findFirst();

        if (woodEnum.isEmpty()) {
            if (LumberModule.getConfig().isOpenBuyShop()) new AxeShopMenu().open(player);
            else player.sendMessage(MessageUtil.filterMessage("<warning>⚠ Dit item kun je hier niet verkopen!"));
            return;
        }
        var woodRecord = LumberModule.getConfig().getWoods().get(woodEnum.get());
        if (woodRecord == null) {
            if (LumberModule.getConfig().isOpenBuyShop()) new AxeShopMenu().open(player);
            else player.sendMessage(MessageUtil.filterMessage("<warning>⚠ Dit item kun je hier niet verkopen!"));
            return;
        }

        String type;
        Material material = itemInHand.getType();
        if (material == woodEnum.get().getBarkMaterial()) type = "bark";
        else if (material == woodEnum.get().getStrippedMaterial()) type = "wood";
        else {
            player.sendMessage(MessageUtil.filterMessage("<warning>⚠ Ongeldig item type!"));
            return;
        }
        Price price = woodRecord.prices().get(type);
        if (price == null || price.cash() <= 0 || price.grindToken() <= 0) {
            if (LumberModule.getConfig().isOpenBuyShop()) new AxeShopMenu().open(player);
            else player.sendMessage(MessageUtil.filterMessage("<warning>⚠ Dit item kun je hier niet verkopen!"));
            return;
        }

        int amountInHand = itemInHand.getAmount();
        double cashAmount = price.cash() * amountInHand;
        double tokenAmount = price.grindToken() * amountInHand;
        CurrencyUtil.giveReward(player, cashAmount, tokenAmount, "Sold wood").thenAccept(rewardMap -> {
            double receivedAmount = rewardMap.values().stream().mapToDouble(Double::doubleValue).sum();
            if (receivedAmount > 0) {
                int removedAmount = itemInHand.getAmount();
                player.getInventory().removeItem(itemInHand);

                String itemName = PlainTextComponentSerializer.plainText()
                        .serialize(itemInHand.getItemMeta().displayName());
                player.sendMessage(MessageUtil.filterMessage(
                        "<green>Je hebt succesvol <bold>" + removedAmount + " " + itemName +
                                "<!bold> verkocht voor <bold>" + receivedAmount + "<!bold>!"
                ));
            }
        });
    }

    @Override
    public @NotNull Collection<String> suggest(@NotNull CommandSourceStack source, @NotNull String[] args) {
        Player player = source.getPlayer();
        if (player == null) return Collections.emptyList();
        if (!this.canUse(player)) return Collections.emptyList();

        if (args.length == 1) {
            return Stream.of("mining", "fishing", "lumber")
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