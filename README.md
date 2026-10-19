# Unplug (Fabric 1.21.11, client-only)

`/unplug` -> window hides to the system tray, rendering stops, render distance drops to 2,
particles minimal, sound muted. Ticking and networking keep running, so AFK farms keep working.
Double-click the tray icon (or right-click > Restore) to come back. Disconnects restore the window automatically.

## Build
Option A (no setup): push this folder to a GitHub repo -> Actions tab -> "build" -> download the `unplug-jar` artifact.
Option B (local): install JDK 21 + Gradle 9.2+, run `gradle build`, jar is `build/libs/unplug-1.0.0.jar`.

## Install
Drop the jar in `.minecraft/mods` next to Fabric API (0.141.1+1.21.11).
