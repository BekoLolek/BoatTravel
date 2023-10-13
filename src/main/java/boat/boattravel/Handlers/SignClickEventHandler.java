package boat.boattravel.Handlers;

import boat.boattravel.BoatTravel;
import boat.boattravel.Storage.SignObject;
import boat.boattravel.Storage.StorageObjectUtil;
import boat.boattravel.Storage.TravelHandler;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

public class SignClickEventHandler implements Listener {
    public SignClickEventHandler() {
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent Event) {
        if (Event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
            Player player = Event.getPlayer();
            Block block = Event.getClickedBlock();
            if (block != null && block.getState() instanceof Sign) {
                Sign sign = (Sign) block.getState();
                String line1 = sign.getLine(0);
                String line2 = sign.getLine(2);
                Location loc = block.getLocation();
                if (line1.equals("[Transport]")) {
                    boolean success = TravelHandler.doTravel(player, loc);
                    if (!success) {
                        player.sendMessage("End destination does not exist, travel cancelled!");
                    }
                }
            }
        }

    }


}
