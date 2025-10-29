package nl.grapjeje.opengrinding.utils.guis;

import nl.grapjeje.core.text.MessageUtil;
import nl.grapjeje.opengrinding.OpenGrinding;
import nl.grapjeje.opengrinding.utils.JobModule;
import nl.grapjeje.opengrinding.utils.configuration.JobConfig;
import org.bukkit.entity.Player;

public abstract class ShopMenu extends Menu {

    public void open(Player player, Class<? extends JobModule> moduleClass) {
        try {
            JobModule module = OpenGrinding.getFramework().getModuleLoader()
                    .getModules().stream()
                    .filter(moduleClass::isInstance)
                    .map(m -> (JobModule) m)
                    .findFirst()
                    .orElse(null);
            if (module == null) {
                player.sendMessage(MessageUtil.filterMessage("<warning>⚠ Deze module is niet gevonden!"));
                return;
            }
            if (module.isDisabled()) {
                player.sendMessage(MessageUtil.filterMessage("<warning>⚠ Deze module is momenteel uitgeschakeld!"));
                return;
            }
            JobConfig config = module.getJobConfig();
            if (!config.isEnabled()) {
                player.sendMessage(MessageUtil.filterMessage("<warning>⚠ Deze module is uitgeschakeld in de config!"));
                return;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        this.open(player);
    }

    protected boolean contains(int[] arr, int val) {
        for (int i : arr) if (i == val) return true;
        return false;
    }
}
