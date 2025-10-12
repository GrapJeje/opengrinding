package nl.grapjeje.opengrinding.jobs.fishing.games;

import net.kyori.adventure.text.minimessage.MiniMessage;
import nl.grapjeje.core.KeyPressInput;
import nl.grapjeje.opengrinding.OpenGrinding;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.util.Random;

public class ClickGame extends FishingGame {
    private Player player;
    private int fishSpot;
    private int spot = 2;
    private int speed = 1;
    private int direction = -1;
    private boolean autoMove = false;

    private boolean isJumping;
    private KeyPressInput input;
    private int tickTime = 0;
    private int autoMoveTimer = 0;
    private int speedIncreaseTimer = 0;
    private int successTimer = 0;
    private final int requiredSuccessTime = 100;
    private final Random random = new Random();
    private BukkitRunnable gameTask;

    @Override
    public void start(@NotNull Player player) {
        this.player = player;
        this.fishSpot = random.nextInt(11, 14);
        this.spot = 12;
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_CHIME, 0.8f, 1.2f);

        input = new KeyPressInput(player, (key, input) -> {
            isJumping = key.jump();
        },
                (__) -> stop(false),
                KeyPressInput.CancelType.SINGLE_SHIFT);
        gameTask = new BukkitRunnable() {
            @Override
            public void run() {
                tick();
            }
        };
        gameTask.runTaskTimer(OpenGrinding.getInstance(), 1L, 1L);
    }

    private boolean failed = false;
    private boolean completed = false;

    @Override
    public void tick() {
        if (failed || completed) return;
        tickTime++;
        autoMoveTimer++;
        speedIncreaseTimer++;

        if (speedIncreaseTimer >= 300 && speed < 2) {
            speed++;
            speedIncreaseTimer = 0;
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 0.6f, 1.8f);
        }
        if (autoMoveTimer >= 160) autoMove = true;

        if (tickTime % 6 == 0) {
            if (isJumping) spot += speed;
            else spot -= speed;
        }
        if (autoMove && tickTime % 20 == 0) {
            fishSpot += direction;
            if (fishSpot <= 7 || fishSpot >= 18) {
                direction *= -1;
                player.playSound(player.getLocation(), Sound.ENTITY_FISH_SWIM, 0.4f, 1.0f);
            }
        }
        if (spot < 0 || spot >= 25) {
            if (!failed) {
                failed = true;
                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 0.7f, 0.8f);
                stop(false);
            }
            return;
        }
        boolean inTarget = spot >= fishSpot - 2 && spot <= fishSpot + 3;
        if (inTarget) {
            successTimer++;
            if (successTimer % 10 == 0) {
                float pitch = 0.8f + (successTimer / (float) requiredSuccessTime) * 0.8f;
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 0.3f, pitch);
            }
            if (successTimer >= requiredSuccessTime) {
                completed = true;
                player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f);
                player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.8f, 1.2f);
                stop(true);
                return;
            }
        } else if (successTimer > 0) {
            successTimer = 0;
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_HAT, 0.4f, 0.8f);
        }

        if ((spot <= 2 || spot >= 22) && tickTime % 8 == 0)
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 0.3f, 0.5f);

        player.sendActionBar(MiniMessage.miniMessage().deserialize(getBar()));
    }

    @Override
    public void visualize() {
        player.sendActionBar(MiniMessage.miniMessage().deserialize(this.getBar()));
    }

    @Override
    public void stop(boolean completed) {
        failed = true;
        this.completed = true;

        if (input != null) input.cancel();
        if (gameTask != null) gameTask.cancel();
    }

    public String getBar() {
        StringBuilder bar = new StringBuilder();
        for (int i = 0; i < 25; i++) {
            if (i == spot)
                bar.append("<yellow>█");
            else if (i >= fishSpot - 2 && i <= fishSpot + 3)
                bar.append(i == fishSpot ? "<green>█" : "<dark_green>█");
            else if (i <= 1 || i >= 23)
                bar.append("<red>█");
            else
                bar.append("<blue>█");
        }
        String progress = "";
        if (successTimer > 0) {
            int prog = (successTimer * 10) / requiredSuccessTime;
            progress = " <green>VANGEN: " + "█".repeat(prog) + "<gray>" + "█".repeat(10 - prog);
        }
        return "<white>SPRING " + bar + progress;
    }
}