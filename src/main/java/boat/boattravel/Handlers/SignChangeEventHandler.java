package boat.boattravel.Handlers;

import boat.boattravel.BoatTravel;
import boat.boattravel.Storage.SignObject;
import boat.boattravel.Storage.StorageObjectUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.plugin.PluginManager;

import java.text.MessageFormat;
import java.util.Objects;

public class SignChangeEventHandler implements Listener {


    @EventHandler
    public void onSignChange(SignChangeEvent event) {
        String[] lines = event.getLines();

        if (lines.length < 3) {
            return; // Ensure at least 3 lines are available
        }

        String firstLine = lines[0].trim();
        String costString = lines[1].trim();
        String id = lines[2].trim();

        if (!"[Transport]".equalsIgnoreCase(firstLine) || costString.isEmpty()) {
            return; // The sign doesn't meet the required format
        }

        OfflinePlayer owner = event.getPlayer();
        Location location = event.getBlock().getLocation();

        // Try to parse the cost as a double, handle errors gracefully
        double cost = 0.0;
        try {
            cost = Double.parseDouble(costString);
        } catch (NumberFormatException e) {
            owner.getPlayer().sendMessage("Invalid cost value.");
            return;
        }

        if (id.isEmpty()) {
            // If the 'id' line is empty, generate an ID using the method from StorageObjectUtil
            id = StorageObjectUtil.generateId();
        }

        SignObject object = new SignObject(id, owner, location, 0, cost, null);

        // Check if this sign is being linked to another sign
        if (lines.length > 3 && !lines[3].isEmpty()) {
            String linkedSignId = lines[3].trim();
            SignObject linkedObject = StorageObjectUtil.findSignObjectById(linkedSignId);

            if (linkedObject != null) {
                // Try to link the signs
                if (!StorageObjectUtil.tryLink(object)) {
                    owner.getPlayer().sendMessage("Failed to link to the other sign, invalid ID.");
                    return;
                }
            } else {
                owner.getPlayer().sendMessage("The linked sign ID doesn't exist.");
                return;
            }
        }

        StorageObjectUtil.create(object);

        // Format and set the sign lines
        event.setLine(0, "§b[§dTransport§b]");
        event.setLine(1, "§a" + costString);
        event.setLine(2, "§4" + id);


        owner.getPlayer().sendMessage("Transport sign created successfully!");
    }


}
