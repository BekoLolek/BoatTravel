package dev.bekololek.boattravel.managers;

import dev.bekololek.boattravel.Main;
import dev.bekololek.boattravel.world.WaterChunkGenerator;
import org.bukkit.Bukkit;
import org.bukkit.GameRule;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.WorldType;

public class WorldManager {

    private final Main plugin;
    private final String worldName;

    public WorldManager(Main plugin) {
        this.plugin    = plugin;
        this.worldName = plugin.getConfig().getString("boat-world-name", "boattravel_world");
    }

    public String getWorldName() { return worldName; }

    /**
     * Returns the boat world, creating it if it does not exist.
     * Returns null if creation failed.
     */
    public World ensureBoatWorld() {
        World world = Bukkit.getWorld(worldName);
        if (world != null) return world;

        plugin.getLogger().info("Creating boat world '" + worldName + "'...");
        try {
            WorldCreator creator = new WorldCreator(worldName)
                    .generator(new WaterChunkGenerator())
                    .environment(World.Environment.NORMAL)
                    .type(WorldType.FLAT);
            world = creator.createWorld();
            if (world == null) {
                plugin.getLogger().severe("Failed to create boat world '" + worldName + "'.");
                return null;
            }
            applyWorldSettings(world);
            plugin.getLogger().info("Boat world '" + worldName + "' created.");
        } catch (Exception e) {
            plugin.getLogger().severe("Exception creating boat world: " + e.getMessage());
            return null;
        }
        return world;
    }

    private void applyWorldSettings(World world) {
        world.setSpawnLocation(0, 64, 0);
        world.setGameRule(GameRule.DO_MOB_SPAWNING,   false);
        world.setGameRule(GameRule.DO_WEATHER_CYCLE,  false);
        world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);
        world.setGameRule(GameRule.DO_FIRE_TICK,      false);
        world.setTime(6000); // always midday
        world.setStorm(false);
    }
}
