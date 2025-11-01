package nl.grapjeje.opengrinding.jobs.mailman.objects;

import lombok.Getter;
import lombok.Setter;
import nl.grapjeje.core.SkullUtil;
import nl.grapjeje.core.text.MessageUtil;
import nl.grapjeje.opengrinding.OpenGrinding;
import nl.grapjeje.opengrinding.api.Jobs;
import nl.grapjeje.opengrinding.api.player.GrindingPlayer;
import nl.grapjeje.opengrinding.jobs.core.CoreModule;
import nl.grapjeje.opengrinding.jobs.core.objects.CraftGrindingPlayer;
import nl.grapjeje.opengrinding.jobs.mailman.MailmanModule;
import nl.grapjeje.opengrinding.utils.currency.CurrencyUtil;
import nl.grapjeje.opengrinding.utils.currency.Price;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.scheduler.BukkitTask;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

@Getter
public class MailmanJob {
    @Getter
    private static final Map<UUID, MailmanJob> jobs = new ConcurrentHashMap<>();

    private final Player player;
    private final int level;

    @Setter
    private int originalAmount;
    @Setter
    private boolean completed = false;

    private final Map<UUID, List<String>> playerRouteValues = new ConcurrentHashMap<>();

    public MailmanJob(Player player, int level) {
        this.player = player;
        this.level = level;
        jobs.put(player.getUniqueId(), this);
    }

    private BukkitTask tickTask;

    private long startTime;

    public void startRunnable() {
        startTime = System.currentTimeMillis();
        tickTask = Bukkit.getScheduler().runTaskTimer(OpenGrinding.getInstance(), () ->
                this.tick(System.currentTimeMillis()), 20L, 20L);
    }

    private boolean sent15 = false;
    private boolean sent20 = false;
    private boolean sent30 = false;

    private final String[] messages = new String[]{
            "<red>Hè, alweer zo langzaam! Kom op, pak die pakketjes!",
            "<dark_red>Ik kan hier echt niet op wachten, ga wat sneller!",
            "<red>Snelheid is niet jouw sterkste kant, hè?",
            "<dark_red>Serieus, wat is er mis met tempo houden?!"
    };

    public void tick(long currentTime) {
        long elapsedSeconds = (currentTime - startTime) / 1000;
        long elapsedMinutes = elapsedSeconds / 60;

        if (elapsedMinutes >= 15 && !sent15) {
            this.sendMessage(this.getRandomMessage());
            sent15 = true;
        }

        if (elapsedMinutes >= 20 && !sent20) {
            this.sendMessage(this.getRandomMessage());
            sent20 = true;
        }

        if (elapsedMinutes >= 30 && !sent30) {
            this.sendMessage(this.getRandomMessage());
            this.sendMessage("<dark_red>Ik doe het zelf wel… Tijd is geld!");
            sent30 = true;
            this.stop(false);
        }
    }

    private String getRandomMessage() {
        int index = (int) (Math.random() * messages.length);
        return messages[index];
    }

    public void stop(boolean completed) {
        tickTask.cancel();
        player.getInventory().setItemInOffHand(ItemStack.of(Material.AIR));

        if (completed) {
            MailmanModule.getPlayerCooldown().put(player.getUniqueId(), System.currentTimeMillis());
            Price price = MailmanModule.getConfig().getPackages().get(level).price();
            double cashAmount = price.cash() * this.getOriginalAmount();
            double tokenAmount = price.grindToken() * this.getOriginalAmount();
            CurrencyUtil.giveReward(player, cashAmount, tokenAmount, "Delivered packages")
                    .thenAccept(rewardMap -> {
                        double receivedAmount = rewardMap.values().stream().mapToDouble(Double::doubleValue).sum();
                        if (receivedAmount > 0)
                            this.sendMessage("Bedankt voor het bezorgen! Als beloning krijg je <bold>" + receivedAmount + (CoreModule.getConfig().isSellInTokens() ? " tokens" : "") + "<!bold>!");
                    });

            GrindingPlayer.loadOrCreatePlayerModelAsync(player, Jobs.MAILMAN)
                    .thenAccept(model -> {
                        GrindingPlayer gp = CraftGrindingPlayer.get(player.getUniqueId(), model);
                        gp.addProgress(Jobs.MAILMAN, 1);
                        CompletableFuture.runAsync(() -> gp.save(Jobs.MAILMAN));
                    });
        }
        jobs.remove(player.getUniqueId());
    }

    public void sendStartMessage() {
        this.sendMessage("Je taak is simpel: bezorg alle pakketjes bij de juiste regio's.");

        Bukkit.getScheduler().runTaskLater(OpenGrinding.getInstance(), () -> {
            if (CoreModule.getConfig().isSellInTokens())
                this.sendMessage("Elk pakketje die je bezorgt levert je <white>ervaring<gray> en <white>grindtokens<gray> op!");
            else
                this.sendMessage("Elk pakketje die je bezorgt levert je <white>ervaring<gray> en <white>geld<gray> op!");
        }, 40L);

        Bukkit.getScheduler().runTaskLater(OpenGrinding.getInstance(), () ->
                this.sendMessage("Succes! En vergeet niet: tijd is geld, postbode!"), 80L);

        Bukkit.getScheduler().runTaskLater(OpenGrinding.getInstance(), this::givePackages, 115L);
        Bukkit.getScheduler().runTaskLater(OpenGrinding.getInstance(), this::sendRegionsList, 120L);
    }

    private void givePackages() {
        Bukkit.getScheduler().runTask(OpenGrinding.getInstance(), () -> {
            UUID id = UUID.nameUUIDFromBytes(MailmanModule.getPackageUrl().getBytes(StandardCharsets.UTF_8));
            ItemStack head = SkullUtil.getCustomHead(MailmanModule.getPackageUrl(), id);
            SkullMeta meta = (SkullMeta) head.getItemMeta();
            if (meta != null) {
                meta.displayName(MessageUtil.filterMessage("<warning>Pakketje"));
                head.setItemMeta(meta);
            }
            head.setAmount(playerRouteValues.get(player.getUniqueId()).size());
            player.getInventory().setItemInOffHand(head);
        });
    }

    public void sendRegionsList() {
        Bukkit.getScheduler().runTask(OpenGrinding.getInstance(), () -> {
            UUID uuid = player.getUniqueId();
            List<String> values = playerRouteValues.get(uuid);
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_AMBIENT, 1.0f, 1.0f);

            if (values == null || values.isEmpty()) {
                this.sendMessage("<warning>⚠ Je hebt op dit moment geen bezorgregio's.");
                return;
            }

            player.sendMessage(" ");
            player.sendMessage(MessageUtil.filterMessage("<red><bold>📦 Dit zijn de regio's waar jij moet bezorgen:"));

            int index = 1;
            for (String value : values) {
                player.sendMessage(MessageUtil.filterMessage("  " + index + ". " + value));
                index++;
            }

            player.sendMessage(" ");
        });
    }

    private void sendMessage(String msg) {
        Bukkit.getScheduler().runTask(OpenGrinding.getInstance(), () -> {
            player.sendMessage(MessageUtil.filterMessage(MailmanModule.getConfig().getName() + "<dark_gray> >> <gray>" + msg));
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_AMBIENT, 1.0f, 1.0f);
        });
    }

    public static boolean isActive(Player player) {
        return jobs.containsKey(player.getUniqueId());
    }
}
