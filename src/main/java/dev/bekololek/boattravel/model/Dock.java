package dev.bekololek.boattravel.model;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;

public class Dock {

    private final String name;
    // Sign block position (used to detect when the sign is broken)
    private final String signWorld;
    private final int signX, signY, signZ;
    // Arrival position (where players land when the voyage completes)
    private final double arrX, arrY, arrZ;
    private final float arrYaw;

    public Dock(String name,
                String signWorld, int signX, int signY, int signZ,
                double arrX, double arrY, double arrZ, float arrYaw) {
        this.name      = name;
        this.signWorld = signWorld;
        this.signX     = signX;
        this.signY     = signY;
        this.signZ     = signZ;
        this.arrX      = arrX;
        this.arrY      = arrY;
        this.arrZ      = arrZ;
        this.arrYaw    = arrYaw;
    }

    public String getName()    { return name; }
    public String getSignWorld() { return signWorld; }
    public int getSignX()      { return signX; }
    public int getSignY()      { return signY; }
    public int getSignZ()      { return signZ; }
    public double getArrX()    { return arrX; }
    public double getArrY()    { return arrY; }
    public double getArrZ()    { return arrZ; }
    public float getArrYaw()   { return arrYaw; }

    /** Returns the block location of this dock's sign. */
    public Location getSignLocation() {
        var world = Bukkit.getWorld(signWorld);
        return new Location(world, signX, signY, signZ);
    }

    /** Returns the arrival location players are teleported to when delivered here. */
    public Location getArrivalLocation() {
        var world = Bukkit.getWorld(signWorld);
        if (world == null) return null;
        return new Location(world, arrX, arrY, arrZ, arrYaw, 0f);
    }

    /** Key used to look up this dock's sign in maps: "world,x,y,z" */
    public String signKey() {
        return signWorld + "," + signX + "," + signY + "," + signZ;
    }

    public void save(ConfigurationSection section) {
        section.set("sign-world", signWorld);
        section.set("sign-x",    signX);
        section.set("sign-y",    signY);
        section.set("sign-z",    signZ);
        section.set("arr-x",     arrX);
        section.set("arr-y",     arrY);
        section.set("arr-z",     arrZ);
        section.set("arr-yaw",   arrYaw);
    }

    public static Dock load(String name, ConfigurationSection s) {
        return new Dock(name,
                s.getString("sign-world"),
                s.getInt("sign-x"), s.getInt("sign-y"), s.getInt("sign-z"),
                s.getDouble("arr-x"), s.getDouble("arr-y"), s.getDouble("arr-z"),
                (float) s.getDouble("arr-yaw"));
    }
}
