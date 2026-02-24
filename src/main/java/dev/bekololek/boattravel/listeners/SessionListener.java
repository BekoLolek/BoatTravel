package dev.bekololek.boattravel.listeners;

import dev.bekololek.boattravel.Main;
import dev.bekololek.boattravel.managers.StatsManager;
import dev.bekololek.boattravel.managers.VoyageManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class SessionListener implements Listener {

    private final Main plugin;
    private final VoyageManager voyageManager;
    private final StatsManager statsManager;

    public SessionListener(Main plugin, VoyageManager voyageManager, StatsManager statsManager) {
        this.plugin        = plugin;
        this.voyageManager = voyageManager;
        this.statsManager  = statsManager;
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerQuit(PlayerQuitEvent e) {
        Player player = e.getPlayer();
        if (voyageManager.isOnVoyage(player.getUniqueId())) {
            voyageManager.handleDisconnect(player.getUniqueId());
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerJoin(PlayerJoinEvent e) {
        Player player = e.getPlayer();
        statsManager.updateName(player);

        // Deliver players who disconnected mid-voyage (pending stored in memory)
        if (voyageManager.hasPendingDelivery(player.getUniqueId())) {
            Location dest = voyageManager.consumePendingDelivery(player.getUniqueId());
            plugin.getServer().getScheduler().runTask(plugin, () -> {
                player.teleport(dest);
                player.sendMessage(Component.text("You were delivered to your destination after disconnecting.", NamedTextColor.AQUA));
            });
            return;
        }

        // Detect stale boat-world presence (server restart while player was mid-voyage)
        String boatWorldName = plugin.getConfig().getString("boat-world-name", "boattravel_world");
        if (player.getWorld().getName().equals(boatWorldName)) {
            plugin.getServer().getScheduler().runTask(plugin, () -> {
                Location spawn = plugin.getServer().getWorlds().get(0).getSpawnLocation();
                player.teleport(spawn);
                player.sendMessage(Component.text(
                        "The server restarted during your voyage. You have been moved to spawn.",
                        NamedTextColor.YELLOW));
            });
        }
    }
}
