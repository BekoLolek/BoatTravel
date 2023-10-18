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
        if (Objects.equals(event.getLine(0), "[Transport]") && event.getLine(1) != null) {
            OfflinePlayer owner = event.getPlayer();
            Location location = event.getBlock().getLocation();
            double income = 0;
            String costString = event.getLine(1);
            double cost = Double.parseDouble(costString);
            String id = StorageObjectUtil.generateId();
            if (!Objects.equals(event.getLine(2), "")) {
                id = event.getLine(2);
            }

            event.setLine(0, "§b[" + "§dTransport" + "§b]");
            event.setLine(1, "§a" + costString);
            event.setLine(2, "§4" + id);

            SignObject object = new SignObject(location, owner, income, cost, id);
            StorageObjectUtil.create(object);
            owner.getPlayer().sendMessage("Transport sign created successfully!");
        }

    }
}
