package boat.boattravel.Handlers;

import boat.boattravel.Storage.SignObject;
import boat.boattravel.Storage.StorageObjectUtil;
import boat.boattravel.Storage.TravelHandler;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;

public class SignBreakEventHandler implements Listener {

    @EventHandler
    public void onPlayerInteract(BlockBreakEvent event) {
        if(event.getBlock().getState() instanceof Sign){
            Sign sign = (Sign) event.getBlock().getState();
            if(sign.getLine(0).equalsIgnoreCase("[transport]")){
                int id = Integer.parseInt(sign.getLine(2));
                StorageObjectUtil.removeObjectById(String.valueOf(id));
            }
        }


    }
}
