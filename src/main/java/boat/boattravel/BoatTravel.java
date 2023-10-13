package boat.boattravel;

import boat.boattravel.Commands.CommandManager;
import boat.boattravel.Handlers.SignChangeEventHandler;
import boat.boattravel.Handlers.SignClickEventHandler;
import boat.boattravel.Storage.StorageObjectUtil;
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
            StorageObjectUtil.save();
        } catch (IOException e) {
            //oopsie
        }

        getCommand("boat").setExecutor(new CommandManager());

        getServer().getPluginManager().registerEvents(new SignChangeEventHandler(),this);
        getServer().getPluginManager().registerEvents(new SignClickEventHandler(),this);


    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
