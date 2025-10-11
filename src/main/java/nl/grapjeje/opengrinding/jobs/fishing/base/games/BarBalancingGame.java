package nl.grapjeje.opengrinding.jobs.fishing.base.games;

import nl.grapjeje.core.KeyPressInput;
import nl.grapjeje.core.text.MessageUtil;
import nl.grapjeje.opengrinding.OpenGrinding;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.util.Random;

public class BarBalancingGame extends FishingGame {

    private Player player;

    private double fishPosition = 0.5;
    private double fishVelocity = 0.0;
    private double playerPosition = 0.5;
    private double playerVelocity = 0.0;
    private double targetZoneCenter = 0.5;

    private int ticksInGame = 0;
    private int totalTicksInSuccessZone = 0;

    private final Random random = new Random();

    private KeyPressInput input;
    private boolean isMovingLeft = false;
    private boolean isMovingRight = false;

    private BukkitRunnable gameTask;

    @Override
    public void start(@NotNull Player player) {
        this.player = player;

        fishPosition = 0.5;
        fishVelocity = (random.nextDouble() - 0.5) * 0.008 * 0.5;
        playerPosition = 0.5;
        playerVelocity = 0.0;
        targetZoneCenter = 0.35 + random.nextDouble() * 0.3;

        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_CHIME, 1.0f, 1.2f);

        input = new KeyPressInput(player, (keyPress, kpi) -> {
            isMovingLeft = keyPress.left();
            isMovingRight = keyPress.right();
        }, (__) -> stop(false), KeyPressInput.CancelType.NONE);

        this.startGameLoop();
    }

    private void startGameLoop() {
        gameTask = new BukkitRunnable() {
            @Override
            public void run() {
                ticksInGame++;
                if (ticksInGame >= 500) {
                    stop(false);
                    return;
                }

                updatePlayerMovement();
                updateFishMovement();

                double zoneStart = targetZoneCenter - 0.4 / 2;
                double zoneEnd = targetZoneCenter + 0.4 / 2;
                boolean inZone = fishPosition >= zoneStart && fishPosition <= zoneEnd;
                if (inZone) totalTicksInSuccessZone++;

                visualize();

                if (ticksInGame % 10 == 0) {
                    if (inZone) {
                        float pitch = 1.0f + (float) (totalTicksInSuccessZone * 0.005);
                        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 0.3f, pitch);
                    } else {
                        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.2f, 0.8f);
                    }
                }

                if (totalTicksInSuccessZone >= 120) stop(true);
            }
        };
        gameTask.runTaskTimer(OpenGrinding.getInstance(), 1L, 1L);
    }

    private void updatePlayerMovement() {
        if (isMovingLeft && isMovingRight)
            playerVelocity *= 0.90;
        else if (isMovingLeft) {
            playerVelocity -= 0.004;
            playerVelocity = Math.max(playerVelocity, -0.035);
        } else if (isMovingRight) {
            playerVelocity += 0.004;
            playerVelocity = Math.min(playerVelocity, 0.035);
        } else {
            playerVelocity *= 0.90;
            if (Math.abs(playerVelocity) < 0.001)
                playerVelocity = 0.0;
        }
        playerPosition += playerVelocity;
        playerPosition = Math.max(0.0, Math.min(1.0, playerPosition));
    }

    private void updateFishMovement() {
        double escapeForce = (random.nextDouble() - 0.5) * 0.008 * 1.2;
        double distanceToPlayer = fishPosition - playerPosition;
        double avoidanceForce = Math.signum(distanceToPlayer) * 0.008 * 0.3;

        fishVelocity += escapeForce + avoidanceForce;
        fishVelocity *= 0.88;
        fishPosition += fishVelocity;

        if (fishPosition <= 0.0) {
            fishPosition = 0.0;
            fishVelocity = Math.abs(fishVelocity) * 0.6;
        } else if (fishPosition >= 1.0) {
            fishPosition = 1.0;
            fishVelocity = -Math.abs(fishVelocity) * 0.6;
        }
    }

    @Override
    public void visualize() {
        StringBuilder barText = new StringBuilder();
        int width = 40;
        int fishPos = (int) (fishPosition * (width - 1));
        int playerPos = (int) (playerPosition * (width - 1));
        int zoneStart = (int) ((targetZoneCenter - 0.4 / 2) * (width - 1));
        int zoneEnd = (int) ((targetZoneCenter + 0.4 / 2) * (width - 1));

        for (int i = 0; i < width; i++) {
            if (i == fishPos) {
                if (i >= zoneStart && i <= zoneEnd)
                    barText.append("<green>🐟");
                else barText.append("<red>🐟");
            } else if (i == playerPos) {
                barText.append("<yellow>▲");
            } else if (i >= zoneStart && i <= zoneEnd)
                barText.append("<blue>█");
            else barText.append("<gray>▒");
        }

        double progressPercent = ((double) totalTicksInSuccessZone / 120) * 100;
        int secondsLeft = (500 - ticksInGame) / 20;
        player.sendActionBar(MessageUtil.filterMessage(
                String.format("<white>%s <white>| Progress: <green>%.1f%% <white>| Time: <red>%ds",
                        barText, progressPercent, secondsLeft)
        ));
    }

    public void stop(boolean completed) {
        if (input != null) input.cancel();
        if (gameTask != null) gameTask.cancel();
        if (completed) {
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
        }
    }
}
