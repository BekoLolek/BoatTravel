package boat.boattravel;
//commit 
import boat.boattravel.Storage.StorageObjectUtil;
import jdk.internal.event.Event;
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


    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
