package nl.grapjeje.opengrinding.jobs.mining.timers;

import nl.grapjeje.opengrinding.OpenGrinding;
import nl.grapjeje.opengrinding.jobs.mining.objects.MiningOres;
import nl.grapjeje.opengrinding.jobs.mining.MiningModule;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;

import java.util.Iterator;
import java.util.List;

public class OreTimer {
    private final long respawnTimeMillis;

    public OreTimer(long respawnTimeMinutes) {
        this.respawnTimeMillis = respawnTimeMinutes * 60_000L;
        this.startTimer();
    }

    private void startTimer() {
        Bukkit.getScheduler().runTaskTimerAsynchronously(OpenGrinding.getInstance(), () -> {
            List<MiningOres> ores = MiningModule.getOres();
            long now = System.currentTimeMillis();

            Iterator<MiningOres> iterator = ores.iterator();
            while (iterator.hasNext()) {
                MiningOres ore = iterator.next();

                if (now - ore.time() >= respawnTimeMillis) {
                    iterator.remove();

                    Bukkit.getScheduler().runTask(OpenGrinding.getInstance(), () -> {
                        Block block = ore.location().getBlock();
                        block.setType(ore.material());
                    });
                }
            }
        }, 0L, 20L);
    }
}
