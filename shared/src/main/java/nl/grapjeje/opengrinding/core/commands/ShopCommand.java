package nl.grapjeje.opengrinding.core.commands;

import nl.grapjeje.core.command.Command;
import nl.grapjeje.core.command.CommandSourceStack;
import nl.grapjeje.opengrinding.jobs.fishing.FishingModule;
import nl.grapjeje.opengrinding.jobs.fishing.guis.FishingRodShopMenu;
import nl.grapjeje.opengrinding.jobs.lumber.LumberModule;
import nl.grapjeje.opengrinding.jobs.lumber.guis.AxeShopMenu;
import nl.grapjeje.opengrinding.jobs.mining.MiningModule;
import nl.grapjeje.opengrinding.jobs.mining.guis.PickaxeShopMenu;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.stream.Stream;

public class ShopCommand implements Command {

    @Override
    public String getName() {
        return "grindingshop";
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
            case "mining" -> new PickaxeShopMenu().open(player, MiningModule.class);
            case "fishing" -> new FishingRodShopMenu().open(player, FishingModule.class);
            case "lumber" -> new AxeShopMenu().open(player, LumberModule.class);
        }
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
