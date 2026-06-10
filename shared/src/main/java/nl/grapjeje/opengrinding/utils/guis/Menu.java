package nl.grapjeje.opengrinding.utils.guis;

import nl.grapjeje.core.gui.Gui;
import nl.grapjeje.opengrinding.OpenGrinding;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

public abstract class Menu {
    protected void registerGui(Gui gui) {
        if (gui instanceof Listener listener)
            Bukkit.getServer().getPluginManager().registerEvents(listener, OpenGrinding.getInstance());
    }

    public abstract void open(Player player);
}
