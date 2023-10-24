package boat.boattravel.Handlers;

import boat.boattravel.Storage.SignObject;
import boat.boattravel.Storage.StorageObjectUtil;
import boat.boattravel.Storage.TravelHandler;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

public class SignClickEventHandler implements Listener {

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK || event.getAction() == Action.LEFT_CLICK_BLOCK) {
            Block clickedBlock = event.getClickedBlock();
            Player player = event.getPlayer();

            // Check if the clicked block is a sign
            if (clickedBlock != null && clickedBlock.getState() instanceof Sign) {
                Sign sign = (Sign) clickedBlock.getState();

                // Implement your logic for signs here
                player.sendMessage("You clicked a sign!");

                SignObject clickedSignObject = StorageObjectUtil.findSignObjectByLocation(clickedBlock.getLocation());

                if(clickedSignObject == null){
                    player.sendMessage("Error with sign creation!");
                }else{
                    if(clickedSignObject.getDestination() != null){
                        player.sendMessage("Prepare for travel!");
                        TravelHandler.doTravel(player,clickedBlock.getLocation());
                    }
                }



            }
        }
    }






}
