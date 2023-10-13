package boat.boattravel.Storage;

import boat.boattravel.BoatTravel;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitScheduler;

import java.text.MessageFormat;

public class TravelHandler extends BukkitRunnable implements Listener {

    private Plugin plugin;
    private double counter;
    private Player player;
    private  SignObject object;

    public TravelHandler(Player player, SignObject signObject, double totalTime) {

        this.plugin = BoatTravel.getPlugin();
        this.counter = totalTime;
        this.player = player;
        this.object = signObject;

        plugin.getServer().getPluginManager().registerEvents(this,plugin);


        //tp
        World world = Bukkit.getWorld((String) plugin.getConfig().get("limbo.world"));
        if(world == null){
            Bukkit.getLogger().severe("You have to set a limbo world!");
        }
        int limboX = plugin.getConfig().getInt("limbo.x");
        int limboY = plugin.getConfig().getInt("limbo.y");
        int limboZ = plugin.getConfig().getInt("limbo.z");
        float limboYaw = (float) plugin.getConfig().getDouble("limbo.yaw");
        float limboPitch = (float) plugin.getConfig().getDouble("limbo.pitch");
        Location limbo = new Location(world,limboX,limboY,limboZ,limboYaw,limboPitch);
        player.teleport(limbo);

        //invisibility
        player.addPotionEffect(PotionEffectType.INVISIBILITY.createEffect(100000,1));


    }

    @Override
    public void run() {
        if(counter > 0){
            if(counter % 10 == 0){
                player.sendMessage(MessageFormat.format("You will arrive in {0} seconds!", Math.floor(counter)));
            }
            counter--;
        }else{
            Location destination = object.getDestination().getLocation();

            player.teleport(destination);
            player.removePotionEffect(PotionEffectType.INVISIBILITY);
            player.sendMessage("You have arrived!");
            this.cancel();
        }
    }

    @EventHandler
    public void onLeave(PlayerQuitEvent event){
        if(player.equals(event.getPlayer())){
            event.getPlayer().teleport(object.getLocation());
            event.getPlayer().removePotionEffect(PotionEffectType.INVISIBILITY);

        }
    }


    public static boolean doTravel(Player player, Location location){
        SignObject signObject = StorageObjectUtil.find(location);
        double totalTime = calculateTime(signObject);
        if(totalTime == -10){
            return false;
        }
        new TravelHandler(player,signObject,totalTime);
        return true;

    }

    public static double calculateTime(SignObject signObject){

        double returnable = -10;

        if(signObject != null){
            int originX = signObject.getLocation().getBlockX();
            int originZ =  signObject.getLocation().getBlockZ();

            int destinationX = signObject.getDestination().getLocation().getBlockX();
            int destinationZ = signObject.getDestination().getLocation().getBlockZ();

            int distX = Math.abs(originX-destinationX);
            int distY = Math.abs(originZ-destinationZ);

            int totalDistance = distX + distY;

            double speed = Double.parseDouble(BoatTravel.getPlugin().getConfig().getString("speed"));

            double totalTime = totalDistance*speed;

            if(speed == -1){
                totalTime = 1;
            }
            returnable = totalTime;
        }
        return returnable;
    }


}
