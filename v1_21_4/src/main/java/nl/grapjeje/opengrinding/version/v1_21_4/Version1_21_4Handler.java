package nl.grapjeje.opengrinding.version.v1_21_4;

import nl.grapjeje.opengrinding.shared.version.VersionHandler;
import nl.grapjeje.opengrinding.shared.OpenGrinding;
import nl.grapjeje.opengrinding.version.v1_21_4.jobs.lumber.BlockBreakListener;

public class Version1_21_4Handler implements VersionHandler {
    @Override
    public void initialize(OpenGrinding plugin) {
        OpenGrinding.getFramework().registerListener(BlockBreakListener::new);
    }
}
