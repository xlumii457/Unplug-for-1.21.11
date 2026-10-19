package dev.unplug;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

public class UnplugMod implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        // Needed so java.awt.SystemTray works inside the Minecraft JVM.
        System.setProperty("java.awt.headless", "false");

        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) ->
            dispatcher.register(ClientCommandManager.literal("unplug").executes(ctx -> {
                MinecraftClient client = ctx.getSource().getClient();
                if (client.world == null) {
                    ctx.getSource().sendError(Text.literal("Join a world or server first."));
                    return 0;
                }
                ctx.getSource().sendFeedback(Text.literal("Unplugging... restore from the tray icon."));
                UnplugManager.unplug(client);
                return 1;
            })));

        // If you get disconnected/kicked while unplugged, bring the window back.
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            if (UnplugManager.isUnplugged()) {
                UnplugManager.plugIn(client);
            }
        });

        // Never leave lowered settings behind if the game is closed while unplugged.
        ClientLifecycleEvents.CLIENT_STOPPING.register(UnplugManager::restoreOptionsOnly);
    }
}
