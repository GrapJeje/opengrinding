package nl.grapjeje.opengrinding.jobs.fishing.base.games;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import nl.grapjeje.core.gui.Gui;
import nl.grapjeje.core.gui.GuiButton;
import nl.grapjeje.core.gui.GuiImpl;
import nl.grapjeje.opengrinding.OpenGrinding;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Stack;
import java.util.concurrent.ThreadLocalRandom;

public class MazeGame extends FishingGame {
    private static final int MAZE_WIDTH = 9;
    private static final int MAZE_HEIGHT = 6;

    private Gui gui;
    private boolean[][] maze; // true = wall, false = path
    private int fishX, fishY;
    private int endX, endY;
    private boolean gameWon = false;
    private int moves = 0;
    private long startTime;

    @Override
    public void start(@NotNull Player player) {
        this.startTime = System.currentTimeMillis();

        Bukkit.getScheduler().runTaskAsynchronously(OpenGrinding.getInstance(), () -> {
            this.generateMaze();
            Bukkit.getScheduler().runTask(OpenGrinding.getInstance(), () -> {
                this.updateGui();
                super.start(player);
            });
        });
    }

    private void generateMaze() {
        maze = new boolean[MAZE_WIDTH][MAZE_HEIGHT];
        for (int x = 0; x < MAZE_WIDTH; x++) {
            for (int y = 0; y < MAZE_HEIGHT; y++) {
                maze[x][y] = true;
            }
        }
        Stack<int[]> stack = new Stack<>();
        Random random = ThreadLocalRandom.current();

        int startX = 1;
        int startY = 1;
        maze[startX][startY] = false;
        stack.push(new int[]{startX, startY});

        int[][] directions = {{0, 2}, {2, 0}, {0, -2}, {-2, 0}};
        while (!stack.isEmpty()) {
            int[] current = stack.peek();
            int x = current[0];
            int y = current[1];

            List<int[]> neighbors = new ArrayList<>();
            for (int[] dir : directions) {
                int newX = x + dir[0];
                int newY = y + dir[1];

                if (newX > 0 && newX < MAZE_WIDTH - 1 &&
                        newY > 0 && newY < MAZE_HEIGHT - 1 &&
                        maze[newX][newY]) {
                    neighbors.add(new int[]{newX, newY});
                }
            }
            if (!neighbors.isEmpty()) {
                int[] next = neighbors.get(random.nextInt(neighbors.size()));
                int nextX = next[0];
                int nextY = next[1];

                maze[nextX][nextY] = false;
                maze[x + (nextX - x) / 2][y + (nextY - y) / 2] = false;

                stack.push(next);
            } else stack.pop();
        }

        fishX = 1;
        fishY = 1;
        maze[fishX][fishY] = false;
        List<int[]> possibleEnds = new ArrayList<>();
        for (int x = 1; x < MAZE_WIDTH - 1; x++) {
            for (int y = 1; y < MAZE_HEIGHT - 1; y++) {
                if (!maze[x][y] && (Math.abs(x - fishX) + Math.abs(y - fishY)) > 4)
                    possibleEnds.add(new int[]{x, y});
            }
        }
        if (!possibleEnds.isEmpty()) {
            int[] end = possibleEnds.get(random.nextInt(possibleEnds.size()));
            endX = end[0];
            endY = end[1];
        } else {
            endX = MAZE_WIDTH - 2;
            endY = MAZE_HEIGHT - 2;
            maze[endX][endY] = false;
        }
    }

    private void updateGui() {
        if (gui == null) {
            Gui.Builder builder = Gui.builder(InventoryType.CHEST,
                    Component.text("Breng de vis naar het eindpunt!", NamedTextColor.GOLD));
            builder.withSize(54);
            builder.withCloseHandler((closeEvent) -> {
                if (!stopping) this.stop(false);
            });
            gui = builder.build();
            this.registerGui(gui);
        }
        gui.clearButtons();

        for (int x = 0; x < MAZE_WIDTH; x++) {
            for (int y = 0; y < MAZE_HEIGHT; y++) {
                int slot = y * 9 + x;
                if (x == fishX && y == fishY) {
                    gui.setButton(slot, GuiButton.builder()
                            .withMaterial(Material.TROPICAL_FISH)
                            .withName(Component.text("Vis", NamedTextColor.AQUA, TextDecoration.BOLD))
                            .build());
                } else if (x == endX && y == endY) {
                    final int clickX = x, clickY = y;
                    gui.setButton(slot, GuiButton.builder()
                            .withMaterial(Material.EMERALD_BLOCK)
                            .withName(Component.text("Haak", NamedTextColor.GREEN, TextDecoration.BOLD))
                            .withClickEvent((g, p, type) -> this.handleMazeClick(clickX, clickY))
                            .build());
                } else if (maze[x][y]) {
                    gui.setButton(slot, GuiButton.builder()
                            .withMaterial(Material.STONE)
                            .withName(Component.text("Muur", NamedTextColor.DARK_GRAY))
                            .build());
                } else {
                    final int clickX = x, clickY = y;
                    gui.setButton(slot, GuiButton.builder()
                            .withMaterial(Material.LIGHT_BLUE_STAINED_GLASS)
                            .withName(Component.text("Pad", NamedTextColor.BLUE))
                            .withClickEvent((g, p, type) -> this.handleMazeClick(clickX, clickY))
                            .build());
                }
            }
        }

        gui.setButton(45, GuiButton.builder()
                .withMaterial(Material.COMPASS)
                .withName(Component.text("Bewegingen: " + moves, NamedTextColor.YELLOW))
                .build());

        long timeElapsed = (System.currentTimeMillis() - startTime) / 1000;
        gui.setButton(46, GuiButton.builder()
                .withMaterial(Material.CLOCK)
                .withName(Component.text("Tijd: " + timeElapsed + "s", NamedTextColor.YELLOW))
                .build());

        gui.setButton(53, GuiButton.builder()
                .withMaterial(Material.BARRIER)
                .withName(Component.text("Stoppen", NamedTextColor.RED))
                .withClickEvent((g, p, type) -> {
                    p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 1.0F, 1.0F);
                    this.stop(false);
                })
                .build());
    }


    private void handleMazeClick(int clickX, int clickY) {
        if (gameWon) return;

        if (this.isAdjacent(clickX, clickY, fishX, fishY) && !maze[clickX][clickY]) {
            fishX = clickX;
            fishY = clickY;
            moves++;

            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0F, 1.0F);

            if (fishX == endX && fishY == endY) {
                gameWon = true;
                long timeElapsed = (System.currentTimeMillis() - startTime) / 1000;

                player.sendMessage(Component.text("🎉 Gefeliciteerd! Je hebt het doolhof voltooid!", NamedTextColor.GREEN));
                player.sendMessage(Component.text("Bewegingen: " + moves + " | Tijd: " + timeElapsed + "s", NamedTextColor.YELLOW));
                player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0F, 1.0F);

                Bukkit.getScheduler().runTaskLater(OpenGrinding.getInstance(), () -> this.stop(true), 60L);
            }
            this.openGui();
        }
    }

    private boolean isAdjacent(int x1, int y1, int x2, int y2) {
        int dx = Math.abs(x1 - x2);
        int dy = Math.abs(y1 - y2);
        return (dx == 1 && dy == 0) || (dx == 0 && dy == 1);
    }

    @Override
    public void tick() {
        if (gameWon) return;
        super.tick();
        if (player != null && gui != null) {
            this.openGui();
        }
    }

    @Override
    public void visualize() {
        if (player != null && gui != null)
            this.openGui();
    }

    private boolean stopping = false;

    @Override
    public void stop(boolean completed) {
        if (stopping) return;
        stopping = true;
        super.stop(completed);
        if (player != null) {
            if (gui != null) gui.close(player);
            player.closeInventory();
            getPlayersInGame().remove(player.getUniqueId());
        }
        stopping = false;
    }

    private void openGui() {
        Bukkit.getScheduler().runTask(OpenGrinding.getInstance(), () -> {
            this.updateGui();
            gui.update(player);
            if (player.getOpenInventory() == null ||
                    player.getOpenInventory().getTopInventory() != ((GuiImpl) gui).getInventory()) {
                gui.open(player);
            }
        });
    }
}
