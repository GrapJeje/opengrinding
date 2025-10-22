package nl.grapjeje.opengrinding.jobs.mailman.guis;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.kyori.adventure.text.Component;
import nl.grapjeje.core.gui.Gui;
import nl.grapjeje.core.gui.GuiButton;
import nl.grapjeje.core.text.MessageUtil;
import nl.grapjeje.opengrinding.OpenGrinding;
import nl.grapjeje.opengrinding.jobs.Jobs;
import nl.grapjeje.opengrinding.jobs.core.CoreModule;
import nl.grapjeje.opengrinding.jobs.core.objects.GrindingPlayer;
import nl.grapjeje.opengrinding.jobs.core.objects.GrindingRegion;
import nl.grapjeje.opengrinding.jobs.mailman.MailmanModule;
import nl.grapjeje.opengrinding.jobs.mailman.configuration.MailmanJobConfiguration;
import nl.grapjeje.opengrinding.jobs.mailman.objects.MailmanJob;
import nl.grapjeje.opengrinding.models.GrindingRegionModel;
import nl.grapjeje.opengrinding.models.PlayerGrindingModel;
import nl.grapjeje.opengrinding.utils.guis.ShopMenu;
import nl.openminetopia.modules.data.storm.StormDatabase;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

@NoArgsConstructor
public class MailmanGui extends ShopMenu {
    private String customName;
    @Setter
    @Getter
    private Integer level = 0;

    public MailmanGui(String customName) {
        this.customName = customName;
    }

    @Override
    public void open(Player player) {
        PlayerGrindingModel model = GrindingPlayer.loadOrCreatePlayerModel(player, Jobs.MAILMAN);
        int playerLevel = model.getLevel();

        Gui.Builder builder;
        if (customName != null)
            builder = Gui.builder(InventoryType.CHEST, MessageUtil.filterMessage(customName));
        else builder = Gui.builder(InventoryType.CHEST, Component.text("Mailman"));
        builder.withSize(27);

        GuiButton startBtn = GuiButton.builder()
                .withHead(MailmanModule.getPackageUrl())
                .withName(MessageUtil.filterMessage("<gold>Start"))
                .withLore(MessageUtil.filterMessage("<gray>Klik om te starten"))
                .withClickEvent((gui, p, type) -> {
                    gui.close(p);
                    this.start(p, playerLevel);
                })
                .build();
        builder.withButton(11, startBtn);

        Map<Integer, MailmanJobConfiguration.Package> packages = MailmanModule.getConfig().getPackages();
        int maxLevel = packages.keySet().stream().max(Integer::compareTo).orElse(0);
        int minLevel = packages.keySet().stream().min(Integer::compareTo).orElse(0);

        if (!packages.containsKey(level)) level = minLevel;
        MailmanJobConfiguration.Package currentPackage = packages.get(level);
        if (currentPackage == null) return;

        boolean sellInTokens = CoreModule.getConfig().isSellInTokens();
        String currencyType = sellInTokens ? " tokens" : "";

        MailmanJobConfiguration.Package nextPackage = packages.get(level + 1);

        double currentPrice = sellInTokens ? currentPackage.price().grindToken() : currentPackage.price().cash();
        double nextPrice = nextPackage != null ? (sellInTokens ? nextPackage.price().grindToken() : nextPackage.price().cash()) : 0;

        GuiButton lvlBtn = GuiButton.builder()
                .withMaterial(Material.DARK_OAK_DOOR)
                .withName(MessageUtil.filterMessage("<gold>Jij bent momenteel level <bold>" + playerLevel))
                .withLore(
                        MessageUtil.filterMessage("<gold>" + level + " -> " + (level + 1) + ":"),
                        MessageUtil.filterMessage("<gray>Wachttijd: <white>" + currentPackage.waitTime() + " minuten -> " +
                                (nextPackage != null ? nextPackage.waitTime() : currentPackage.waitTime()) + " minuten"),
                        MessageUtil.filterMessage("<gray>Prijs: <white>" + currentPrice + currencyType + " -> " +
                                (nextPackage != null ? nextPrice : currentPrice) + currencyType),
                        MessageUtil.filterMessage("<gray>Aantal: <white>" + currentPackage.amount().min() + "-" + currentPackage.amount().max() +
                                " -> " + (nextPackage != null ? nextPackage.amount().min() + "-" + nextPackage.amount().max() : currentPackage.amount().min() + "-" + currentPackage.amount().max())),
                        MessageUtil.filterMessage(" "),
                        MessageUtil.filterMessage("<green>Klik rechts om level omhoog!"),
                        MessageUtil.filterMessage("<red>Klik links om level omlaag!")
                )
                .withClickEvent((gui, p, type) -> {
                    if (type.isRightClick() && nextPackage != null && level < maxLevel) level++;
                    else if (type.isLeftClick() && level > minLevel) level--;
                    this.open(player);
                })
                .build();

        builder.withButton(15, lvlBtn);

        for (int i = 0; i < 27; i++) {
            if (i != 11 && i != 15) builder.withButton(i, GuiButton.getFiller());
        }

        Gui gui = builder.build();
        this.registerGui(gui);
        gui.open(player);
    }

    private final List<GrindingRegion> mailManRegions = new CopyOnWriteArrayList<>();

    private void start(Player player, int level) {
        long waitTimeMs = TimeUnit.MINUTES.toMillis(
                MailmanModule.getConfig().getPackages().get(level).waitTime()
        );

        if (MailmanModule.getPlayerCooldown().containsKey(player.getUniqueId())) {
            long lastActionTime = MailmanModule.getPlayerCooldown().get(player.getUniqueId());
            long timePassed = System.currentTimeMillis() - lastActionTime;

            if (timePassed < waitTimeMs) {
                long remaining = waitTimeMs - timePassed;
                player.sendMessage(MessageUtil.filterMessage(
                        "<warning>⚠ Je moet nog <bold>" + TimeUnit.MILLISECONDS.toMinutes(remaining) + "<!bold> minuten wachten!"));
                return;
            } else MailmanModule.getPlayerCooldown().remove(player.getUniqueId());
        }

        if (player.getInventory().getItemInOffHand().getType() != Material.AIR) {
            player.sendMessage(MessageUtil.filterMessage("<warning>⚠ Maak je off-hand leeg om dit te starten!"));
            return;
        }
        Bukkit.getScheduler().runTaskAsynchronously(OpenGrinding.getInstance(), () -> {
            this.loadAllMailManRegionsAsync(player);

            int min = MailmanModule.getConfig().getPackages().get(level).amount().min();
            int max = MailmanModule.getConfig().getPackages().get(level).amount().max();

            int amountOfPackages = (min >= max)
                    ? min
                    : ThreadLocalRandom.current().nextInt(min, max + 1);

            MailmanJob job = new MailmanJob(player, level);
            UUID uuid = player.getUniqueId();
            job.getPlayerRouteValues().put(uuid, new CopyOnWriteArrayList<>());

            for (int i = 0; i < amountOfPackages && i < mailManRegions.size(); i++) {
                job.getPlayerRouteValues().get(uuid).add(mailManRegions.get(i).getValue());
            }

            job.setOriginalAmount(job.getPlayerRouteValues().get(uuid).size());
            job.sendStartMessage();
            job.startRunnable();
        });
    }

    private void loadAllMailManRegionsAsync(Player player) {
        try {
            mailManRegions.clear();
            List<GrindingRegionModel> allRegions = StormDatabase.getInstance().getStorm()
                    .buildQuery(GrindingRegionModel.class)
                    .execute()
                    .join()
                    .stream()
                    .toList();

            for (GrindingRegionModel model : allRegions) {
                GrindingRegion region = new GrindingRegion(model);
                if (!region.allowsJob(Jobs.MAILMAN)) continue;
                if (!region.hasValue()) continue;
                mailManRegions.add(region);
            }
            Collections.shuffle(mailManRegions);
        } catch (Exception e) {
            e.printStackTrace();
            Bukkit.getScheduler().runTask(OpenGrinding.getInstance(), () -> player.sendMessage(MessageUtil.filterMessage("<warning>⚠ Er is een fout opgetreden met alle regios op te halen")));
        }
    }
}
