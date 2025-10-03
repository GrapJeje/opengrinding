package nl.grapjeje.opengrinding.jobs.core.commands;

import com.craftmend.storm.api.enums.Where;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import net.kyori.adventure.text.Component;
import nl.grapjeje.core.command.Command;
import nl.grapjeje.core.command.CommandSourceStack;
import nl.grapjeje.core.text.MessageUtil;
import nl.grapjeje.opengrinding.OpenGrinding;
import nl.grapjeje.opengrinding.jobs.Jobs;
import nl.grapjeje.opengrinding.jobs.core.objects.GrindingRegion;
import nl.grapjeje.opengrinding.models.GrindingRegionModel;
import nl.openminetopia.modules.data.storm.StormDatabase;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Stream;

public class GrindingRegionCommand implements Command {

    @Getter
    private static final Map<UUID, RegionSelection> selections = new HashMap<>();
    private static final int PAGE_SIZE = 10;

    @Override
    public String getName() {
        return "grindingregion";
    }

    @Override
    public void execute(CommandSourceStack source, String[] args) {
        Player player = source.getPlayer();
        if (player == null) {
            source.getSender().sendRichMessage("<warning>⚠ Alleen spelers kunnen dit commando gebruiken!");
            return;
        }

        if (args.length == 0 || args[0].equalsIgnoreCase("help")) {
            if (!hasPermissionForSubCommand(player, "help")) return;
            this.sendHelp(player);
            return;
        }

        String sub = args[0].toLowerCase();

        if (!this.hasPermissionForSubCommand(player, sub)) {
            player.sendMessage(MessageUtil.filterMessage("<warning>⚠ Jij hebt hier geen permissie voor!"));
            return;
        }

        switch (sub) {
            case "create", "cancel", "delete", "list" -> this.handleRegionCommands(player, args);
            case "addjob", "removejob", "setvalue", "removevalue" -> this.handleJobCommands(player, args);
            default -> this.sendHelp(player);
        }
    }

    private boolean hasPermissionForSubCommand(Player player, String subCommand) {
        if (player.hasPermission("opengrinding.*")) return true;
        return switch (subCommand) {
            case "addjob", "removejob", "setvalue", "removevalue" -> player.hasPermission("opengrinding.region.jobs");
            default -> player.hasPermission("opengrinding.region");
        };
    }

    private void handleRegionCommands(Player player, String[] args) {
        switch (args[0].toLowerCase()) {
            case "create" -> this.handleCreate(player, args);
            case "cancel" -> this.handleCancel(player);
            case "delete" -> this.handleDelete(player, args);
            case "list" -> this.handleList(player, args);
            default -> this.sendHelp(player);
        }
    }

    private void handleJobCommands(Player player, String[] args) {
        switch (args[0].toLowerCase()) {
            case "addjob" -> this.handleAddJob(player, args);
            case "removejob" -> this.handleRemoveJob(player, args);
            case "setvalue" -> this.handelSetValue(player, args);
            case "removevalue" -> this.handleRemoveValue(player, args);
            default -> this.sendHelp(player);
        }
    }

    private void sendHelp(Player player) {
        player.sendMessage(MessageUtil.filterMessage("<red><bold>--- GrindingRegion Help ---</bold>"));
        player.sendMessage(MessageUtil.filterMessage("<gray>/grindingregion help <dark_gray>- <white>Toon dit menu"));
        player.sendMessage(MessageUtil.filterMessage("<gray>/grindingregion create <name> <dark_gray>- <white>Krijg selection tool om een regio te maken"));
        player.sendMessage(MessageUtil.filterMessage("<gray>/grindingregion cancel <dark_gray>- <white>Stop met het maken van een region"));
        player.sendMessage(MessageUtil.filterMessage("<gray>/grindingregion delete <name> <dark_gray>- <white>Verwijder een region"));
        player.sendMessage(MessageUtil.filterMessage("<gray>/grindingregion addjob <name> <job> <dark_gray>- <white>Voeg een job toe"));
        player.sendMessage(MessageUtil.filterMessage("<gray>/grindingregion removejob <name> <job> <dark_gray>- <white>Verwijder een job"));
        player.sendMessage(MessageUtil.filterMessage("<gray>/grindingregion setvalue <name> <value> <dark_gray>- <white>Voeg een value toe"));
        player.sendMessage(MessageUtil.filterMessage("<gray>/grindingregion removevalue <name> <dark_gray>- <white>Verwijder een value"));
        player.sendMessage(MessageUtil.filterMessage("<gray>/grindingregion list <page> <dark_gray>- <white>Lijst van alle regions"));
    }

    private void handleCreate(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(MessageUtil.filterMessage("<warning>⚠ Gebruik: /grindingregion create <name>"));
            return;
        }

        String name = args[1];

        Bukkit.getScheduler().runTaskAsynchronously(OpenGrinding.getInstance(), () -> {
            Optional<GrindingRegionModel> existing;
            try {
                existing = StormDatabase.getInstance().getStorm()
                        .buildQuery(GrindingRegionModel.class)
                        .where("name", Where.EQUAL, name)
                        .limit(1)
                        .execute()
                        .join()
                        .stream()
                        .findFirst();
            } catch (Exception ex) {
                ex.printStackTrace();
                Bukkit.getScheduler().runTask(OpenGrinding.getInstance(), () ->
                        player.sendMessage(MessageUtil.filterMessage("<warning>⚠ Er is een fout opgetreden bij het controleren van de region!"))
                );
                return;
            }
            if (existing.isPresent()) {
                Bukkit.getScheduler().runTask(OpenGrinding.getInstance(), () ->
                        player.sendMessage(MessageUtil.filterMessage("<warning>⚠ Region <bold>" + name + " <!bold><warning>bestaat al!"))
                );
                return;
            }
            Bukkit.getScheduler().runTask(OpenGrinding.getInstance(), () -> {
                selections.put(player.getUniqueId(), new RegionSelection(name));

                ItemStack tool = new ItemStack(Material.BLAZE_ROD);
                ItemMeta meta = tool.getItemMeta();
                if (meta != null) {
                    meta.itemName(MessageUtil.filterMessage("<green>Grinding Region Stick"));
                    meta.lore(List.of(
                            MessageUtil.filterMessage("<yellow>Left-click <gray>to select first corner"),
                            MessageUtil.filterMessage("<yellow>Right-click <gray>to select second corner")
                    ));
                    tool.setItemMeta(meta);
                }
                player.getInventory().addItem(tool);
                player.sendMessage(MessageUtil.filterMessage("<green>Je hebt een selection tool gekregen voor region <bold>" + name + "<!bold>"));
                player.sendMessage(MessageUtil.filterMessage("<gray>⬤ Klik links/rechts met de rod om de punten te selecteren en maak de region met /grindingregion finish"));
            });
        });
    }

    private void handleCancel(Player player) {
        if (!GrindingRegionCommand.getSelections().containsKey(player.getUniqueId())) {
            player.sendMessage(MessageUtil.filterMessage("<warning>⚠ Jij bent geen region aan het maken!"));
            return;
        }
        for (ItemStack item : player.getInventory().getContents()) {
            if (item == null) continue;
            if (item.getType() != Material.BLAZE_ROD) continue;
            if (!item.hasItemMeta()) continue;

            Component name = MessageUtil.filterMessage("<green>Grinding Region Stick");
            if (name.equals(item.getItemMeta().itemName())) {
                player.getInventory().remove(item);
                break;
            }
        }
        GrindingRegionCommand.getSelections().remove(player.getUniqueId());
        player.sendMessage(MessageUtil.filterMessage("<red>Region creatie geannuleerd!"));
    }

    private void handleDelete(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(MessageUtil.filterMessage("<warning>⚠ Gebruik: /grindingregion delete <name>"));
            return;
        }

        String name = args[1];
        Bukkit.getScheduler().runTaskAsynchronously(OpenGrinding.getInstance(), () -> {
            Optional<GrindingRegionModel> regionOpt;
            try {
                regionOpt = StormDatabase.getInstance().getStorm()
                        .buildQuery(GrindingRegionModel.class)
                        .where("name", Where.EQUAL, name)
                        .limit(1)
                        .execute()
                        .join()
                        .stream()
                        .findFirst();
            } catch (Exception ex) {
                ex.printStackTrace();
                Bukkit.getScheduler().runTask(OpenGrinding.getInstance(), () ->
                        player.sendMessage(MessageUtil.filterMessage("<warning>⚠ Er is een fout opgetreden bij het controleren van de region!"))
                );
                return;
            }
            if (regionOpt.isEmpty()) {
                Bukkit.getScheduler().runTask(OpenGrinding.getInstance(), () ->
                        player.sendMessage(MessageUtil.filterMessage("<warning>⚠ Region <bold>" + name + " <!bold><warning>bestaat niet!"))
                );
                return;
            }
            try {
                StormDatabase.getInstance().getStorm().delete(regionOpt.get());
            } catch (Exception ex) {
                ex.printStackTrace();
                Bukkit.getScheduler().runTask(OpenGrinding.getInstance(), () ->
                        player.sendMessage(MessageUtil.filterMessage("<warning>⚠ Er is een fout opgetreden bij het verwijderen van de region!"))
                );
                return;
            }
            Bukkit.getScheduler().runTask(OpenGrinding.getInstance(), () ->
                    player.sendMessage(MessageUtil.filterMessage("<red>Region <bold>" + name + "<!bold> is verwijderd."))
            );
        });
    }

    private void handleAddJob(Player player, String[] args) {
        if (args.length < 3) {
            player.sendMessage(MessageUtil.filterMessage("<warning>⚠ Gebruik: /grindingregion addjob <region> <job>"));
            return;
        }

        String regionName = args[1];
        String jobName = args[2].toUpperCase();
        Jobs job = Arrays.stream(Jobs.values())
                .filter(j -> j.name().equalsIgnoreCase(jobName))
                .findFirst()
                .orElse(null);
        if (job == null) {
            player.sendMessage(MessageUtil.filterMessage("<warning>⚠ Onbekende job: <yellow>" + jobName));
            return;
        }

        Bukkit.getScheduler().runTaskAsynchronously(OpenGrinding.getInstance(), () -> {
            Optional<GrindingRegionModel> regionOpt;
            try {
                regionOpt = StormDatabase.getInstance().getStorm()
                        .buildQuery(GrindingRegionModel.class)
                        .where("name", Where.EQUAL, regionName)
                        .limit(1)
                        .execute()
                        .join()
                        .stream()
                        .findFirst();
            } catch (Exception ex) {
                ex.printStackTrace();
                Bukkit.getScheduler().runTask(OpenGrinding.getInstance(), () ->
                        player.sendMessage(MessageUtil.filterMessage("<warning>⚠ Er is een fout opgetreden bij het controleren van de region!"))
                );
                return;
            }
            if (regionOpt.isEmpty()) {
                Bukkit.getScheduler().runTask(OpenGrinding.getInstance(), () ->
                        player.sendMessage(MessageUtil.filterMessage("<warning>⚠ Region <yellow>" + regionName + " <warning>bestaat niet!"))
                );
                return;
            }
            GrindingRegion region = new GrindingRegion(regionOpt.get());
            region.addJob(job);
            region.save();

            Bukkit.getScheduler().runTask(OpenGrinding.getInstance(), () ->
                    player.sendMessage(MessageUtil.filterMessage("<green>Job <bold>" + jobName + "<!bold> toegevoegd aan region <bold>" + regionName + "<!bold>"))
            );
        });
    }

    private void handleRemoveJob(Player player, String[] args) {
        if (args.length < 3) {
            player.sendMessage(MessageUtil.filterMessage("<warning>⚠ Gebruik: /grindingregion removejob <region> <job>"));
            return;
        }

        String regionName = args[1];
        String jobName = args[2].toUpperCase();
        Jobs job = Arrays.stream(Jobs.values())
                .filter(j -> j.name().equalsIgnoreCase(jobName))
                .findFirst()
                .orElse(null);

        if (job == null) {
            player.sendMessage(MessageUtil.filterMessage("<warning>⚠ Onbekende job: <yellow>" + jobName));
            return;
        }
        Bukkit.getScheduler().runTaskAsynchronously(OpenGrinding.getInstance(), () -> {
            Optional<GrindingRegionModel> regionOpt;
            try {
                regionOpt = StormDatabase.getInstance().getStorm()
                        .buildQuery(GrindingRegionModel.class)
                        .where("name", Where.EQUAL, regionName)
                        .limit(1)
                        .execute()
                        .join()
                        .stream()
                        .findFirst();
            } catch (Exception ex) {
                ex.printStackTrace();
                Bukkit.getScheduler().runTask(OpenGrinding.getInstance(), () ->
                        player.sendMessage(MessageUtil.filterMessage("<warning>⚠ Er is een fout opgetreden bij het controleren van de region!"))
                );
                return;
            }
            if (regionOpt.isEmpty()) {
                Bukkit.getScheduler().runTask(OpenGrinding.getInstance(), () ->
                        player.sendMessage(MessageUtil.filterMessage("<warning>⚠ Region <yellow>" + regionName + " <warning>bestaat niet!"))
                );
                return;
            }
            GrindingRegion region = new GrindingRegion(regionOpt.get());
            region.removeJob(job);
            region.save();

            Bukkit.getScheduler().runTask(OpenGrinding.getInstance(), () ->
                    player.sendMessage(MessageUtil.filterMessage("<red>Job <bold>" + jobName + "<!bold> verwijderd van region <bold>" + regionName + "<!bold>"))
            );
        });
    }

    private void handelSetValue(Player player, String[] args) {
        if (args.length < 3) {
            player.sendMessage(MessageUtil.filterMessage("<warning>⚠ Gebruik: /grindingregion setvalue <region> <value>"));
            return;
        }

        String regionName = args[1];
        String value = args[2].toLowerCase();

        Bukkit.getScheduler().runTaskAsynchronously(OpenGrinding.getInstance(), () -> {
            Optional<GrindingRegionModel> regionOpt;
            try {
                regionOpt = StormDatabase.getInstance().getStorm()
                        .buildQuery(GrindingRegionModel.class)
                        .where("name", Where.EQUAL, regionName)
                        .limit(1)
                        .execute()
                        .join()
                        .stream()
                        .findFirst();
            } catch (Exception ex) {
                ex.printStackTrace();
                Bukkit.getScheduler().runTask(OpenGrinding.getInstance(), () ->
                        player.sendMessage(MessageUtil.filterMessage("<warning>⚠ Er is een fout opgetreden bij het controleren van de region!"))
                );
                return;
            }
            if (regionOpt.isEmpty()) {
                Bukkit.getScheduler().runTask(OpenGrinding.getInstance(), () ->
                        player.sendMessage(MessageUtil.filterMessage("<warning>⚠ Region <yellow>" + regionName + " <warning>bestaat niet!"))
                );
                return;
            }
            GrindingRegion region = new GrindingRegion(regionOpt.get());
            region.setValue(value);
            region.save();

            Bukkit.getScheduler().runTask(OpenGrinding.getInstance(), () ->
                    player.sendMessage(MessageUtil.filterMessage("<green>Value <bold>" + value + "<!bold> toegevoegd aan region <bold>" + regionName + "<!bold>"))
            );
        });
    }

    private void handleRemoveValue(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(MessageUtil.filterMessage("<warning>⚠ Gebruik: /grindingregion removevalue <region>"));
            return;
        }

        String regionName = args[1];
        Bukkit.getScheduler().runTaskAsynchronously(OpenGrinding.getInstance(), () -> {
            Optional<GrindingRegionModel> regionOpt;
            try {
                regionOpt = StormDatabase.getInstance().getStorm()
                        .buildQuery(GrindingRegionModel.class)
                        .where("name", Where.EQUAL, regionName)
                        .limit(1)
                        .execute()
                        .join()
                        .stream()
                        .findFirst();
            } catch (Exception ex) {
                ex.printStackTrace();
                Bukkit.getScheduler().runTask(OpenGrinding.getInstance(), () ->
                        player.sendMessage(MessageUtil.filterMessage("<warning>⚠ Er is een fout opgetreden bij het controleren van de region!"))
                );
                return;
            }
            if (regionOpt.isEmpty()) {
                Bukkit.getScheduler().runTask(OpenGrinding.getInstance(), () ->
                        player.sendMessage(MessageUtil.filterMessage("<warning>⚠ Region <yellow>" + regionName + " <warning>bestaat niet!"))
                );
                return;
            }
            GrindingRegion region = new GrindingRegion(regionOpt.get());
            region.setValue(null);
            region.save();

            Bukkit.getScheduler().runTask(OpenGrinding.getInstance(), () ->
                    player.sendMessage(MessageUtil.filterMessage("<red>Value <bold>" + args[1] + "<!bold> verwijderd van region <bold>" + regionName + "<!bold>"))
            );
        });
    }

    private void handleList(Player player, String[] args) {
        int page = 1;
        if (args.length >= 2) {
            try {
                page = Integer.parseInt(args[1]);
            } catch (NumberFormatException ignored) {
            }
        }
        List<GrindingRegionModel> all;
        try {
            all = StormDatabase.getInstance().getStorm().buildQuery(GrindingRegionModel.class).execute().join().stream().toList();
        } catch (Exception ex) {
            ex.printStackTrace();
            player.sendMessage(MessageUtil.filterMessage("<warning>⚠ Er is een fout opgetreden bij het ophalen van de regions!"));
            return;
        }
        if (all.isEmpty()) {
            player.sendMessage(MessageUtil.filterMessage("<warning>⚠ Er zijn nog geen regions."));
            return;
        }
        int totalPages = (int) Math.ceil(all.size() / (double) PAGE_SIZE);
        if (page > totalPages) page = totalPages;
        player.sendMessage(MessageUtil.filterMessage("<red>--- Grinding Regions (Page " + page + "/" + totalPages + ") ---"));
        all.stream().skip((long) (page - 1) * PAGE_SIZE).limit(PAGE_SIZE)
                .forEach(r -> player.sendMessage(MessageUtil.filterMessage("<gray>- <white>" + r.getName() +
                        " <dark_gray>(Jobs: <yellow>" + (r.getJobs() == null || r.getJobs().isEmpty() ? "geen" :
                        String.join(", ", r.getJobs().stream().map(Enum::name).toList())) + "<dark_gray>)" +
                        " <dark_gray>Value: <yellow>" + (r.getValue() == null || r.getValue().isEmpty() ? "geen" :
                        r.getValue()))));
    }

    @Override
    public @NotNull Collection<String> suggest(@NotNull CommandSourceStack source, @NotNull String[] args) {
        Player player = source.getPlayer();
        if (player == null) return Collections.emptyList();

        try {
            switch (args.length) {
                case 1 -> {
                    return Stream.of("create", "cancel", "delete", "addjob", "removejob", "setvalue", "removevalue", "list", "help")
                            .filter(cmd -> this.hasPermissionForSubCommand(player, cmd))
                            .filter(s -> s.toLowerCase().startsWith(args[0].toLowerCase()))
                            .toList();
                }
                case 2 -> {
                    String sub = args[0].toLowerCase();
                    if (!this.hasPermissionForSubCommand(player, sub)) return Collections.emptyList();

                    if (sub.equals("delete") || sub.equals("addjob") || sub.equals("removejob") ||
                    sub.equals("setvalue") || sub.equals("removevalue")) {
                        List<GrindingRegionModel> regions = StormDatabase.getInstance().getStorm()
                                .buildQuery(GrindingRegionModel.class)
                                .execute()
                                .join()
                                .stream()
                                .toList();
                        return regions.stream()
                                .map(GrindingRegionModel::getName)
                                .filter(name -> name.toLowerCase().startsWith(args[1].toLowerCase()))
                                .toList();
                    }
                }
                case 3 -> {
                    String sub = args[0].toLowerCase();
                    if (!this.hasPermissionForSubCommand(player, sub)) return Collections.emptyList();

                    if (sub.equals("addjob") || sub.equals("removejob")) {
                        return Arrays.stream(Jobs.values())
                                .map(Enum::name)
                                .filter(j -> j.toLowerCase().startsWith(args[2].toLowerCase()))
                                .toList();
                    } else if (sub.equals("removevalue")) {
                        List<GrindingRegionModel> regions = StormDatabase.getInstance().getStorm()
                                .buildQuery(GrindingRegionModel.class)
                                .execute()
                                .join()
                                .stream()
                                .toList();
                        return regions.stream()
                                .map(GrindingRegionModel::getValue)
                                .filter(value -> value.toLowerCase().startsWith(args[1].toLowerCase()))
                                .toList();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Collections.emptyList();
    }

    @Getter
    @Setter
    @RequiredArgsConstructor
    public static class RegionSelection {
        private final String name;
        private Location min = null;
        private Location max = null;
        private boolean finished = false;
    }
}
