package dev.unplug;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.option.ParticlesMode;
import net.minecraft.sound.SoundCategory;
import org.lwjgl.glfw.GLFW;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.image.BufferedImage;

public final class UnplugManager {
    private static volatile boolean unplugged = false;
    private static TrayIcon trayIcon;
    private static boolean optionsLowered = false;

    private static int oldViewDistance;
    private static boolean oldPauseOnLostFocus;
    private static ParticlesMode oldParticles;
    private static double oldVolume;

    private UnplugManager() {}

    public static boolean isUnplugged() {
        return unplugged;
    }

    public static void unplug(MinecraftClient client) {
        if (unplugged) return;

        GameOptions o = client.options;
        oldViewDistance = o.getViewDistance().getValue();
        oldPauseOnLostFocus = o.pauseOnLostFocus;
        oldParticles = o.getParticles().getValue();
        oldVolume = o.getSoundVolumeOption(SoundCategory.MASTER).getValue();
        optionsLowered = true;

        // Important for singleplayer: otherwise losing focus pauses the game and stops your farm.
        o.pauseOnLostFocus = false;
        o.getViewDistance().setValue(2);
        o.getParticles().setValue(ParticlesMode.MINIMAL);
        o.getSoundVolumeOption(SoundCategory.MASTER).setValue(0.0);

        unplugged = true;

        long window = client.getWindow().getHandle();
        if (addTray(client)) {
            GLFW.glfwHideWindow(window);
        } else {
            GLFW.glfwIconifyWindow(window); // fallback: just minimize
        }
    }

    public static void plugIn(MinecraftClient client) {
        if (!unplugged) return;
        unplugged = false;
        restoreOptionsOnly(client);
        removeTray();

        long window = client.getWindow().getHandle();
        GLFW.glfwShowWindow(window);
        GLFW.glfwRestoreWindow(window);
        GLFW.glfwFocusWindow(window);
    }

    public static void restoreOptionsOnly(MinecraftClient client) {
        if (!optionsLowered) return;
        optionsLowered = false;
        GameOptions o = client.options;
        o.pauseOnLostFocus = oldPauseOnLostFocus;
        o.getViewDistance().setValue(oldViewDistance);
        o.getParticles().setValue(oldParticles);
        o.getSoundVolumeOption(SoundCategory.MASTER).setValue(oldVolume);
    }

    private static boolean addTray(MinecraftClient client) {
        try {
            if (!SystemTray.isSupported()) return false;

            BufferedImage img = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = img.createGraphics();
            g.setColor(new Color(0x55, 0xAA, 0x55));
            g.fillRect(0, 0, 16, 16);
            g.setColor(new Color(0x2E, 0x6B, 0x2E));
            g.drawRect(0, 0, 15, 15);
            g.dispose();

            PopupMenu menu = new PopupMenu();
            MenuItem restore = new MenuItem("Restore Minecraft");
            restore.addActionListener(e -> client.execute(() -> plugIn(client)));
            MenuItem quit = new MenuItem("Quit Minecraft");
            quit.addActionListener(e -> client.execute(client::scheduleStop));
            menu.add(restore);
            menu.add(quit);

            trayIcon = new TrayIcon(img, "Minecraft (AFK) - double-click to restore", menu);
            trayIcon.setImageAutoSize(true);
            trayIcon.addActionListener(e -> client.execute(() -> plugIn(client)));
            SystemTray.getSystemTray().add(trayIcon);
            return true;
        } catch (Throwable t) {
            trayIcon = null;
            return false;
        }
    }

    private static void removeTray() {
        try {
            if (trayIcon != null) SystemTray.getSystemTray().remove(trayIcon);
        } catch (Throwable ignored) {
        }
        trayIcon = null;
    }
}
