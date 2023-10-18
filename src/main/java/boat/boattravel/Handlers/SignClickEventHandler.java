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


    @EventHandler
    public void onInteract(PlayerInteractEvent Event) {

        if(Event.getClickedBlock() == null) return;
        if (Event.getClickedBlock().getState() instanceof Sign) {
            Player player = Event.getPlayer();
            Sign sign = (Sign) Event.getClickedBlock().getState();
            String line1 = sign.getLine(0);
            String line2 = sign.getLine(2);
            Location loc = sign.getLocation();
            if (line1.equals("[Transport]")) {
                boolean success = TravelHandler.doTravel(player, loc);
                if (!success) {
                    player.sendMessage("End destination does not exist, travel cancelled!");
                }
            }
        }
        if (Event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {


        }

    }


}
