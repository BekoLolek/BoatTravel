package boat.boattravel.Storage;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class TeleportTask extends BukkitRunnable {
    private final Player player;
    private int countdown;
    private final Location location;

    public TeleportTask(Player player, int countdown, Location location) {
        this.player = player;
        this.countdown = countdown;
        this.location = location;
    }

    @Override
    public void run() {

        if (countdown > 0) {
            // Perform countdown logic
            if(countdown % 10 == 0){
                player.sendMessage("Teleporting in " + countdown + " seconds...");
            }

            countdown--;
        } else {
            // Perform teleportation logic
            player.sendMessage("You have arrived!");
            // Replace "destinationLocation" with the actual location to teleport the player
            player.teleport(location);
            this.cancel(); // Stop the task
        }
    }
}
