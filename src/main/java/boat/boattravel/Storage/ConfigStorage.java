package boat.boattravel.Storage;

import boat.boattravel.BoatTravel;
import org.bukkit.Location;

public class ConfigStorage {
    private static double speed;
    private static Location limbo;

    public static void ConfigStorageInit() {


        String world = BoatTravel.getPlugin().getConfig().getString("limbo.world");
        int x = BoatTravel.getPlugin().getConfig().getInt("limbo.x");
        int y = BoatTravel.getPlugin().getConfig().getInt("limbo.y");
        int z = BoatTravel.getPlugin().getConfig().getInt("limbo.z");
        int yaw = BoatTravel.getPlugin().getConfig().getInt("limbo.yaw");
        int pitch = BoatTravel.getPlugin().getConfig().getInt("limbo.pitch");

        limbo = new Location(BoatTravel.getPlugin().getServer().getWorld(world),x,y,z,yaw,pitch);

    }

    public static double getSpeed() {
        return speed;
    }

    public static Location getLimbo() {
        return limbo;
    }
}
