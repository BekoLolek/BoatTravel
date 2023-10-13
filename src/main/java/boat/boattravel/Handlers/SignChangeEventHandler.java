package boat.boattravel.Handlers;

import boat.boattravel.BoatTravel;
import boat.boattravel.Storage.SignObject;
import boat.boattravel.Storage.StorageObjectUtil;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.plugin.PluginManager;

import java.util.Objects;

public class SignChangeEventHandler implements Listener {
    public SignChangeEventHandler() {
    }

    @EventHandler
    public void onSignChange(SignChangeEvent event) {
        if (Objects.equals(event.getLine(0), "[Transport]") && event.getLine(1) != null) {
            OfflinePlayer owner = event.getPlayer();
            Location location = event.getBlock().getLocation();
            double income = 0;
            String costString = event.getLine(1);
            double cost = Double.parseDouble(costString);
            String id = StorageObjectUtil.generateId();

            if (event.getLine(2) != null) {
                id = event.getLine(2);
            }

            event.setLine(0, "&b[" + "&dTransport" + "&b]");
            event.setLine(1, "&a" + costString);
            event.setLine(2, "&4" + id);

            SignObject object = new SignObject(location, owner, income, cost, id);
            StorageObjectUtil.create(object);
        } else {
            event.setLine(0, null);
            event.setLine(1, null);
            event.setLine(2, null);
            event.setLine(3, null);

            event.getPlayer().sendMessage("Transport sign could not be created, check /boat help!");
        }

    }
}
