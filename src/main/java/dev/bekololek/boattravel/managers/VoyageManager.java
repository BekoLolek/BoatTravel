package dev.bekololek.boattravel.managers;

import dev.bekololek.boattravel.Main;
import dev.bekololek.boattravel.model.Dock;
import dev.bekololek.boattravel.model.Voyage;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Boat;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class VoyageManager {

    private final Main plugin;
    private final WorldManager worldManager;
    private final StatsManager statsManager;

    private final Map<UUID, Voyage> active = new HashMap<>();
    private final Set<Integer> usedSlots   = new HashSet<>();
    // Players for whom we are currently running our own teleport (bypass event cancel)
    private final Set<UUID> bypassTeleport = new HashSet<>();
    // Players who disconnected mid-voyage; value = where to send them on next join
    private final Map<UUID, Location> pendingDelivery = new HashMap<>();

    // Walking speed in blocks/second
    private static final double WALK_SPEED = 4.317;

    public VoyageManager(Main plugin, WorldManager worldManager, StatsManager statsManager) {
        this.plugin       = plugin;
        this.worldManager = worldManager;
        this.statsManager = statsManager;
    }

    // ── Public API ────────────────────────────────────────────────────────────

    public boolean isOnVoyage(UUID uuid)  { return active.containsKey(uuid); }
    public Voyage  getVoyage(UUID uuid)   { return active.get(uuid); }
    public boolean isBypassing(UUID uuid) { return bypassTeleport.contains(uuid); }

    public boolean hasPendingDelivery(UUID uuid) { return pendingDelivery.containsKey(uuid); }
    public Location consumePendingDelivery(UUID uuid) { return pendingDelivery.remove(uuid); }

    public Collection<Voyage> allVoyages() { return Collections.unmodifiableCollection(active.values()); }

    /**
     * Starts a voyage for the given player from origin to destination.
     * Distance is the pre-calculated block distance.
     */
    public boolean startVoyage(Player player, Dock origin, Dock destination, double distance, double cost) {
        World boatWorld = worldManager.ensureBoatWorld();
        if (boatWorld == null) {
            player.sendMessage(Component.text("The boat world is unavailable. Contact an admin.", NamedTextColor.RED));
            return false;
        }

        double speedBps = WALK_SPEED * plugin.getConfig().getDouble("travel-speed-multiplier", 10.0);
        int totalTicks  = Math.max(20, (int) ((distance / speedBps) * 20));
        int slot        = allocateSlot();
        double spacing  = plugin.getConfig().getInt("boat-spacing", 256);
        double slotX    = slot * spacing;

        // Spawn boat in the boat world (use EntityType to avoid abstract Boat class issue in 1.21.x)
        Location boatLoc = new Location(boatWorld, slotX + 0.5, 63, 0.5);
        Boat boat;
        try {
            boat = (Boat) boatWorld.spawnEntity(boatLoc, EntityType.OAK_BOAT);
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to spawn boat at " + boatLoc + ": " + e.getMessage());
            freeSlot(slot);
            player.sendMessage(Component.text("Could not spawn boat. Try again.", NamedTextColor.RED));
            return false;
        }
        boat.setInvulnerable(true);

        Voyage voyage = new Voyage(player.getUniqueId(), origin, destination,
                slot, boat, totalTicks, distance, cost);
        active.put(player.getUniqueId(), voyage);

        // Teleport player into the boat world, then seat them
        Location playerStartLoc = boatLoc.clone().add(0, 0.5, 0);
        bypassTeleport.add(player.getUniqueId());
        player.teleport(playerStartLoc);
        bypassTeleport.remove(player.getUniqueId());
        boat.addPassenger(player);

        double speedBpsFinal = speedBps;
        BukkitRunnable task = new BukkitRunnable() {
            int actionBarTick = 0;

            @Override
            public void run() {
                if (!player.isOnline()) {
                    handleDisconnect(player.getUniqueId());
                    this.cancel();
                    return;
                }
                voyage.tick();

                // Push boat forward along +Z every tick
                boat.setVelocity(new Vector(0, -0.05, speedBpsFinal / 20.0));

                // Update action bar every second
                if (actionBarTick++ % 20 == 0) {
                    player.sendActionBar(Component.text(
                            "\u26F5 Sailing to " + destination.getName()
                            + "... " + voyage.blocksRemaining() + " blocks remaining",
                            NamedTextColor.AQUA));
                }

                if (voyage.isDone()) {
                    this.cancel();
                    completeVoyage(player.getUniqueId());
                }
            }
        };

        BukkitRunnable seatTask = new BukkitRunnable() {
            @Override public void run() {
                if (player.isOnline()) boat.addPassenger(player);
            }
        };
        seatTask.runTaskLater(plugin, 2L);

        voyage.setTask(task.runTaskTimer(plugin, 5L, 1L));
        return true;
    }

    /** Cancel a voyage and send the player back to their origin dock. */
    public void cancelVoyage(UUID uuid) {
        Voyage voyage = active.remove(uuid);
        if (voyage == null) return;
        freeSlot(voyage.getSlot());
        voyage.cancel();

        Player player = plugin.getServer().getPlayer(uuid);
        if (player != null && player.isOnline()) {
            statsManager.recordVoyageCancel(player);
            Location origin = voyage.getOrigin().getArrivalLocation();
            if (origin != null) {
                bypassTeleport.add(uuid);
                player.teleport(origin);
                bypassTeleport.remove(uuid);
                player.sendActionBar(Component.empty());
                player.sendMessage(Component.text("Voyage cancelled. Returned to " + voyage.getOrigin().getName() + ".", NamedTextColor.YELLOW));
            }
        }
    }

    /** Complete voyage: deliver player to destination dock. */
    public void completeVoyage(UUID uuid) {
        Voyage voyage = active.remove(uuid);
        if (voyage == null) return;
        freeSlot(voyage.getSlot());
        voyage.setCompleting(true);
        voyage.cancel();

        Player player = plugin.getServer().getPlayer(uuid);
        if (player != null && player.isOnline()) {
            statsManager.recordVoyageComplete(player,
                    voyage.getDestination().getName(),
                    voyage.getTotalDistance(),
                    voyage.getCost());
            Location dest = voyage.getDestination().getArrivalLocation();
            if (dest == null) dest = plugin.getServer().getWorlds().get(0).getSpawnLocation();
            bypassTeleport.add(uuid);
            player.teleport(dest);
            bypassTeleport.remove(uuid);
            player.sendActionBar(Component.empty());
            player.sendMessage(Component.text("Arrived at " + voyage.getDestination().getName() + "!", NamedTextColor.GREEN));
        }
    }

    /** Player disconnected mid-voyage; decide where to send them on rejoin. */
    public void handleDisconnect(UUID uuid) {
        Voyage voyage = active.remove(uuid);
        if (voyage == null) return;
        freeSlot(voyage.getSlot());
        voyage.cancel();

        String behavior = plugin.getConfig().getString("disconnect-behavior", "DESTINATION");
        Location target = behavior.equalsIgnoreCase("ORIGIN")
                ? voyage.getOrigin().getArrivalLocation()
                : voyage.getDestination().getArrivalLocation();
        if (target != null) pendingDelivery.put(uuid, target);
    }

    /** Aborts all active voyages (called on plugin disable). */
    public void abortAll() {
        for (UUID uuid : Set.copyOf(active.keySet())) {
            completeVoyage(uuid);
        }
    }

    // ── Slot management ───────────────────────────────────────────────────────

    private int allocateSlot() {
        int slot = 0;
        while (usedSlots.contains(slot)) slot++;
        usedSlots.add(slot);
        return slot;
    }

    private void freeSlot(int slot) {
        usedSlots.remove(slot);
    }
}
