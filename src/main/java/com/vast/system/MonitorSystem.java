package com.vast.system;

import com.artemis.Aspect;
import com.artemis.systems.IntervalSystem;
import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.extras.FlatInspector;
import com.vast.VastWorld;
import com.vast.monitor.Monitor;

import javax.swing.*;

public class MonitorSystem extends IntervalSystem {
    private VastWorld vastWorld;

    private Monitor monitor;

    public MonitorSystem(VastWorld vastWorld) {
        super(Aspect.all(), 1f / 30);
        this.vastWorld = vastWorld;
    }

    @Override
    protected void initialize() {
        try {
            SwingUtilities.invokeAndWait(() -> {
                FlatDarkLaf.install();
                FlatInspector.install("ctrl shift alt X");
                monitor = new Monitor(vastWorld);
            });
        } catch (Exception ignored) {
        }
    }

    @Override
    protected void dispose() {
        if (monitor != null) {
            monitor.dispose();
        }
    }

    @Override
    protected void processSystem() {
        if (monitor != null) {
            monitor.sync();
        }
    }
}
