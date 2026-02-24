package dev.bekololek.boattravel.listeners;

import dev.bekololek.boattravel.Main;
import dev.bekololek.boattravel.managers.DockManager;
import dev.bekololek.boattravel.managers.EconomyManager;
import dev.bekololek.boattravel.managers.TravelSignManager;
import dev.bekololek.boattravel.managers.VoyageManager;
import dev.bekololek.boattravel.model.Dock;
import dev.bekololek.boattravel.model.TravelSign;
import dev.bekololek.boattravel.model.Voyage;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.vehicle.VehicleExitEvent;

public class VoyageListener implements Listener {

    private final Main plugin;
    private final DockManager dockManager;
    private final TravelSignManager travelSignManager;
    private final VoyageManager voyageManager;
    private final EconomyManager economyManager;

    public VoyageListener(Main plugin, DockManager dockManager,
                          TravelSignManager travelSignManager,
                          VoyageManager voyageManager, EconomyManager economyManager) {
        this.plugin            = plugin;
        this.dockManager       = dockManager;
        this.travelSignManager = travelSignManager;
        this.voyageManager     = voyageManager;
        this.economyManager    = economyManager;
    }

    // ── Travel sign click ─────────────────────────────────────────────────────

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent e) {
        if (e.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        Block block = e.getClickedBlock();
        if (block == null) return;
        if (!(block.getState() instanceof Sign)) return;

        String key = TravelSign.keyFor(block.getLocation());
        TravelSign ts = travelSignManager.getByKey(key);
        if (ts == null) return;

        e.setCancelled(true);
        Player player = e.getPlayer();

        if (!player.hasPermission("boattravel.use")) {
            player.sendMessage(Component.text("You don't have permission to board ferries.", NamedTextColor.RED));
            return;
        }
        if (voyageManager.isOnVoyage(player.getUniqueId())) {
            player.sendMessage(Component.text("You are already on a voyage. Use /boattravel cancel to abort.", NamedTextColor.YELLOW));
            return;
        }

        Dock destination = dockManager.getByName(ts.getDestinationDock());
        if (destination == null) {
            player.sendMessage(Component.text("Destination dock '" + ts.getDestinationDock() + "' not found.", NamedTextColor.RED));
            return;
        }

        // Find origin dock (nearest registered dock, or any dock whose sign is this block — fallback: unknown)
        // For cancel behaviour, we use the nearest dock to the player as "origin"
        Dock origin = nearestDock(player);
        if (origin == null) {
            // Create a synthetic unnamed origin at the player's current location
            // Use destination world as reference
            origin = new Dock("(departure)",
                    player.getWorld().getName(),
                    player.getLocation().getBlockX(), player.getLocation().getBlockY(), player.getLocation().getBlockZ(),
                    player.getLocation().getX(), player.getLocation().getY(), player.getLocation().getZ(),
                    player.getLocation().getYaw());
        }

        double cost = economyManager.calculateCost(ts.getDistance());
        boolean bypassCost = player.hasPermission("boattravel.admin")
                          || player.hasPermission("boattravel.bypass.cost");
        if (!bypassCost && !economyManager.canAfford(player, cost)) {
            player.sendMessage(Component.text("You need " + economyManager.format(cost)
                    + " to travel to " + destination.getName() + ".", NamedTextColor.RED));
            return;
        }

        if (!bypassCost) economyManager.charge(player, cost);
        if (cost > 0 && !bypassCost) {
            player.sendMessage(Component.text("Charged " + economyManager.format(cost) + " for travel.", NamedTextColor.GOLD));
        }

        double chargedCost = bypassCost ? 0 : cost;
        voyageManager.startVoyage(player, origin, destination, ts.getDistance(), chargedCost);
    }

    // ── Boat exit prevention ──────────────────────────────────────────────────

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onVehicleExit(VehicleExitEvent e) {
        if (!(e.getExited() instanceof Player player)) return;
        if (!voyageManager.isOnVoyage(player.getUniqueId())) return;
        e.setCancelled(true);
        // Re-seat on next tick in case the client briefly desyncs
        Voyage voyage = voyageManager.getVoyage(player.getUniqueId());
        if (voyage != null) {
            plugin.getServer().getScheduler().runTask(plugin,
                    () -> voyage.getBoat().addPassenger(player));
        }
    }

    // ── Teleport lock ─────────────────────────────────────────────────────────

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerTeleport(PlayerTeleportEvent e) {
        Player player = e.getPlayer();
        if (!voyageManager.isOnVoyage(player.getUniqueId())) return;
        if (voyageManager.isBypassing(player.getUniqueId())) return;
        e.setCancelled(true);
        player.sendMessage(Component.text("You cannot teleport during a voyage. Use /boattravel cancel to abort.", NamedTextColor.RED));
    }

    // ── Movement lock (safety net while seated) ───────────────────────────────

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerMove(PlayerMoveEvent e) {
        Player player = e.getPlayer();
        if (!voyageManager.isOnVoyage(player.getUniqueId())) return;
        if (voyageManager.isBypassing(player.getUniqueId())) return;
        // Only block positional changes — allow head rotation
        if (e.getFrom().getBlockX() == e.getTo().getBlockX()
         && e.getFrom().getBlockY() == e.getTo().getBlockY()
         && e.getFrom().getBlockZ() == e.getTo().getBlockZ()) return;
        // Player is moving (not just looking); if they somehow left the boat, re-seat
        Voyage voyage = voyageManager.getVoyage(player.getUniqueId());
        if (voyage != null && !voyage.getBoat().getPassengers().contains(player)) {
            plugin.getServer().getScheduler().runTask(plugin,
                    () -> voyage.getBoat().addPassenger(player));
        }
    }

    // ── Helper ────────────────────────────────────────────────────────────────

    private Dock nearestDock(Player player) {
        Dock nearest = null;
        double best = Double.MAX_VALUE;
        for (Dock dock : dockManager.getAll()) {
            if (!dock.getSignWorld().equals(player.getWorld().getName())) continue;
            double dist = player.getLocation().distanceSquared(
                    new org.bukkit.Location(player.getWorld(), dock.getArrX(), dock.getArrY(), dock.getArrZ()));
            if (dist < best) { best = dist; nearest = dock; }
        }
        return nearest;
    }
}
