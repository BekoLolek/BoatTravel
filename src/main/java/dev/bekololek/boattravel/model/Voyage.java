package dev.bekololek.boattravel.model;

import org.bukkit.entity.Boat;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.UUID;

public class Voyage {

    private final UUID playerId;
    private final Dock origin;
    private final Dock destination;
    private final int slot;
    private final Boat boat;
    private final int totalTicks;
    private final double totalDistance;
    private final double cost;
    private int ticksRemaining;
    private BukkitTask task;
    private boolean completing = false; // true while we are doing the completion teleport

    public Voyage(UUID playerId, Dock origin, Dock destination,
                  int slot, Boat boat, int totalTicks, double totalDistance, double cost) {
        this.playerId      = playerId;
        this.origin        = origin;
        this.destination   = destination;
        this.slot          = slot;
        this.boat          = boat;
        this.totalTicks    = totalTicks;
        this.totalDistance  = totalDistance;
        this.cost           = cost;
        this.ticksRemaining = totalTicks;
    }

    public UUID getPlayerId()      { return playerId; }
    public Dock getOrigin()        { return origin; }
    public Dock getDestination()   { return destination; }
    public int getSlot()           { return slot; }
    public Boat getBoat()          { return boat; }
    public int getTotalTicks()     { return totalTicks; }
    public int getTicksRemaining() { return ticksRemaining; }
    public double getTotalDistance() { return totalDistance; }
    public double getCost()          { return cost; }
    public boolean isCompleting()    { return completing; }

    public void setTask(BukkitTask task) { this.task = task; }

    public void tick() { if (ticksRemaining > 0) ticksRemaining--; }

    public boolean isDone() { return ticksRemaining <= 0; }

    /** Blocks remaining (rough estimate based on time fraction). */
    public int blocksRemaining() {
        double fraction = (double) ticksRemaining / Math.max(1, totalTicks);
        return (int) Math.ceil(fraction * totalDistance);
    }

    public void setCompleting(boolean completing) { this.completing = completing; }

    public void cancel() {
        if (task != null) { task.cancel(); task = null; }
        if (boat.isValid()) boat.remove();
    }
}
