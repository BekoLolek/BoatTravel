package boat.boattravel;


import boat.boattravel.Commands.CommandManager;
import boat.boattravel.Handlers.SignChangeEventHandler;
import boat.boattravel.Handlers.SignClickEventHandler;

//commit 
import boat.boattravel.Storage.StorageObjectUtil;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;

public final class BoatTravel extends JavaPlugin {

    private static BoatTravel plugin;

    public static Plugin getPlugin() {
        return plugin;
    }

    @Override
    public void onEnable() {
        plugin = this;
        saveDefaultConfig();
        try {
            StorageObjectUtil.load();
        } catch (IOException e) {
            Bukkit.getLogger().info(e.getMessage());
        }



        getCommand("boat").setExecutor(new CommandManager());

        Bukkit.getPluginManager().registerEvents(new SignChangeEventHandler(),this);
        Bukkit.getPluginManager().registerEvents(new SignClickEventHandler(),this);


    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
