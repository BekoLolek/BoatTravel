package dev.bekololek.boattravel.model;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;

public class TravelSign {

    private final String world;
    private final int bx, by, bz;
    private final String destinationDock;
    private final double distance; // pre-calculated Euclidean blocks, rounded

    public TravelSign(String world, int bx, int by, int bz,
                      String destinationDock, double distance) {
        this.world           = world;
        this.bx              = bx;
        this.by              = by;
        this.bz              = bz;
        this.destinationDock = destinationDock;
        this.distance        = distance;
    }

    public String getWorld()           { return world; }
    public int getBx()                 { return bx; }
    public int getBy()                 { return by; }
    public int getBz()                 { return bz; }
    public String getDestinationDock() { return destinationDock; }
    public double getDistance()        { return distance; }

    public Location getBlockLocation() {
        return new Location(Bukkit.getWorld(world), bx, by, bz);
    }

    /** Map key: "world,x,y,z" */
    public String key() {
        return world + "," + bx + "," + by + "," + bz;
    }

    public static String keyFor(Location loc) {
        return loc.getWorld().getName() + ","
                + loc.getBlockX() + "," + loc.getBlockY() + "," + loc.getBlockZ();
    }

    public void save(ConfigurationSection section) {
        section.set("world",    world);
        section.set("x",        bx);
        section.set("y",        by);
        section.set("z",        bz);
        section.set("dest",     destinationDock);
        section.set("distance", distance);
    }

    public static TravelSign load(ConfigurationSection s) {
        return new TravelSign(
                s.getString("world"),
                s.getInt("x"), s.getInt("y"), s.getInt("z"),
                s.getString("dest"),
                s.getDouble("distance"));
    }
}
