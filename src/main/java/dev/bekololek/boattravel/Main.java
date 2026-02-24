package dev.bekololek.boattravel;

import dev.bekololek.boattravel.commands.BoatTravelCommand;
import dev.bekololek.boattravel.listeners.SessionListener;
import dev.bekololek.boattravel.listeners.SignListener;
import dev.bekololek.boattravel.listeners.VoyageListener;
import dev.bekololek.boattravel.managers.DockManager;
import dev.bekololek.boattravel.managers.EconomyManager;
import dev.bekololek.boattravel.managers.StatsManager;
import dev.bekololek.boattravel.managers.TravelSignManager;
import dev.bekololek.boattravel.managers.VoyageManager;
import dev.bekololek.boattravel.managers.WorldManager;
import dev.bekololek.boattravel.stats.BoatTravelExpansion;
import dev.bekololek.boattravel.world.WaterChunkGenerator;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {

    private DockManager dockManager;
    private TravelSignManager travelSignManager;
    private VoyageManager voyageManager;
    private WorldManager worldManager;
    private EconomyManager economyManager;
    private StatsManager statsManager;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        worldManager      = new WorldManager(this);
        dockManager       = new DockManager(this);
        travelSignManager = new TravelSignManager(this);
        economyManager    = new EconomyManager(this);
        statsManager      = new StatsManager(this);
        statsManager.load();
        statsManager.startAutoSave();
        voyageManager     = new VoyageManager(this, worldManager, statsManager);

        // Ensure boat world exists on startup
        worldManager.ensureBoatWorld();

        // Listeners
        var pm = getServer().getPluginManager();
        pm.registerEvents(new SignListener(this, dockManager, travelSignManager, economyManager), this);
        pm.registerEvents(new VoyageListener(this, dockManager, travelSignManager, voyageManager, economyManager), this);
        pm.registerEvents(new SessionListener(this, voyageManager, statsManager), this);

        // Commands
        var cmd = getCommand("boattravel");
        var handler = new BoatTravelCommand(this, dockManager, travelSignManager, voyageManager, statsManager);
        if (cmd != null) {
            cmd.setExecutor(handler);
            cmd.setTabCompleter(handler);
        }

        // PlaceholderAPI expansion
        if (getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new BoatTravelExpansion(this, statsManager).register();
            getLogger().info("PlaceholderAPI expansion registered.");
        }

        getLogger().info("BoatTravel.v1 - BL enabled.");
    }

    @Override
    public void onDisable() {
        // Complete all active voyages so no one is stranded in the boat world
        if (voyageManager != null) voyageManager.abortAll();
        if (statsManager != null) statsManager.saveSync();
        getLogger().info("BoatTravel.v1 - BL disabled.");
    }

    @Override
    public ChunkGenerator getDefaultWorldGenerator(String worldName, String id) {
        return new WaterChunkGenerator();
    }
}
