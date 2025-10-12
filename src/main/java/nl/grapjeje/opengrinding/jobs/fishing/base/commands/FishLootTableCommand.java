package nl.grapjeje.opengrinding.jobs.fishing.base.commands;

import com.craftmend.storm.api.enums.Where;
import nl.grapjeje.core.command.Command;
import nl.grapjeje.core.command.CommandSourceStack;
import nl.grapjeje.core.text.MessageUtil;
import nl.grapjeje.opengrinding.OpenGrinding;
import nl.grapjeje.opengrinding.jobs.fishing.base.menus.FishLootTableMenu;
import nl.grapjeje.opengrinding.jobs.fishing.base.menus.FishLootTableListMenu;
import nl.grapjeje.opengrinding.jobs.fishing.base.objects.FishLootTable;
import nl.grapjeje.opengrinding.models.FishLootTableModel;
import nl.openminetopia.modules.data.storm.StormDatabase;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Stream;

public class FishLootTableCommand implements Command {

    @Override
    public String getName() {
        return "fishloottable";
    }

    @Override
    public void execute(CommandSourceStack source, String[] args) {
        Player player = source.getPlayer();
        if (player == null) {
            source.getSender().sendRichMessage("<warning>⚠ Alleen spelers kunnen dit commando gebruiken!");
            return;
        }

        if (args.length == 0) {
            this.sendHelp(player);
            return;
        }

        String sub = args[0].toLowerCase();
        switch (sub) {
            case "create" -> this.handleCreate(player, args);
            case "open" -> this.handleOpen(player, args);
            case "list" -> this.handleList(player, args);
            default -> this.sendHelp(player);
        }
    }

    private void sendHelp(Player player) {
        player.sendMessage(MessageUtil.filterMessage("<red><bold>--- FishLootTable Help ---</bold>"));
        player.sendMessage(MessageUtil.filterMessage("<gray>/fishloottable create <value> <chance> <dark_gray>- <white>Maak loot (item in hand verplicht)"));
        player.sendMessage(MessageUtil.filterMessage("<gray>/fishloottable open <value> <dark_gray>- <white>Bekijk loot table voor een value"));
        player.sendMessage(MessageUtil.filterMessage("<gray>/fishloottable list <page> <dark_gray>- <white>Lijst alle values"));
    }

    private void handleCreate(Player player, String[] args) {
        if (args.length < 3) {
            player.sendMessage(MessageUtil.filterMessage("<warning>⚠ Gebruik: /fishloottable create <value> <chance>"));
            return;
        }

        ItemStack inHand = player.getInventory().getItemInMainHand();
        if (inHand == null || inHand.getType().isAir()) {
            player.sendMessage(MessageUtil.filterMessage("<warning>⚠ Je moet een item in je hand hebben!"));
            return;
        }

        String value = args[1];
        double chance;
        try {
            chance = Double.parseDouble(args[2]);
        } catch (NumberFormatException e) {
            player.sendMessage(MessageUtil.filterMessage("<warning>⚠ Ongeldige kans (moet een getal zijn)!"));
            return;
        }

        FishLootTable loot = new FishLootTable(new FishLootTableModel());
        loot.setValue(value);
        loot.setChance(chance);
        loot.setItem(inHand);

        loot.save().thenRun(() ->
                Bukkit.getScheduler().runTask(OpenGrinding.getInstance(), () ->
                        player.sendMessage(MessageUtil.filterMessage("<green>✔ Loot toegevoegd aan value <bold>" + value + "<!bold>"))
                )
        );
    }

    private void handleOpen(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(MessageUtil.filterMessage("<warning>⚠ Gebruik: /fishloottable open <value>"));
            return;
        }

        String value = args[1];
        Bukkit.getScheduler().runTaskAsynchronously(OpenGrinding.getInstance(), () -> {
            List<FishLootTable> items = null;
            try {
                items = StormDatabase.getInstance().getStorm()
                        .buildQuery(FishLootTableModel.class)
                        .where("value", Where.EQUAL, value)
                        .execute()
                        .join()
                        .stream()
                        .map(FishLootTable::new)
                        .toList();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            if (items.isEmpty()) {
                Bukkit.getScheduler().runTask(OpenGrinding.getInstance(), () ->
                        player.sendMessage(MessageUtil.filterMessage("<warning>⚠ Geen items gevonden voor value <bold>" + value + "<!bold>"))
                );
                return;
            }

            List<FishLootTable> finalItems = items;
            Bukkit.getScheduler().runTask(OpenGrinding.getInstance(), () -> {
                new FishLootTableMenu(value, finalItems).open(player);
            });
        });
    }

    private void handleList(Player player, String[] args) {
        int page = 1;
        if (args.length >= 2) {
            try {
                page = Integer.parseInt(args[1]);
            } catch (NumberFormatException ignored) {}
        }
        int finalPage = page;

        Bukkit.getScheduler().runTaskAsynchronously(OpenGrinding.getInstance(), () -> {
            List<String> values = null;
            try {
                values = StormDatabase.getInstance().getStorm()
                        .buildQuery(FishLootTableModel.class)
                        .execute()
                        .join()
                        .stream()
                        .map(FishLootTableModel::getValue)
                        .distinct()
                        .toList();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            List<String> finalValues = values;
            Bukkit.getScheduler().runTask(OpenGrinding.getInstance(), () -> {
                new FishLootTableListMenu(finalValues, finalPage).open(player);
            });
        });
    }

    @Override
    public Collection<String> suggest(CommandSourceStack source, String[] args) {
        Player player = source.getPlayer();
        if (player == null) return Collections.emptyList();
        if (!this.canUse(player)) {
            player.sendRichMessage(MessageUtil.filterMessageString(this.noPermissionMessage()));
            return Collections.emptyList();
        }

        return switch (args.length) {
            case 1 -> Stream.of("create", "open", "list")
                    .filter(s -> s.startsWith(args[0].toLowerCase()))
                    .toList();
            case 2 -> {
                if (args[0].equalsIgnoreCase("open")) {
                    List<String> values;
                    try {
                        values = StormDatabase.getInstance().getStorm()
                                .buildQuery(FishLootTableModel.class)
                                .execute()
                                .join()
                                .stream()
                                .map(FishLootTableModel::getValue)
                                .distinct()
                                .toList();
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                    yield values.stream().filter(v -> v.toLowerCase().startsWith(args[1].toLowerCase())).toList();
                }
                yield Collections.emptyList();
            }
            default -> Collections.emptyList();
        };
    }

    @Override
    public @Nullable String permission() {
        return "opengrinding.fishloottable";
    }
}
