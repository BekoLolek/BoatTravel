package boat.boattravel.Commands.SubCommands;

import boat.boattravel.BoatTravel;
import boat.boattravel.Commands.SubCommand;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class Reload extends SubCommand {
    @Override
    public String getName() {
        return "reload";
    }

    @Override
    public String getDescription() {
        return "Reloads the plugin.";
    }

    @Override
    public String getSyntax() {
        return "/boat reload";
    }

    @Override
    public void perform(Player player, String[] args) {
        BoatTravel.getPlugin().reloadConfig();
        BoatTravel.getPlugin().saveConfig();
        player.sendMessage(ChatColor.GREEN + "Config reloaded successfully!");
    }


    public void performConsole(){
        BoatTravel.getPlugin().reloadConfig();
        BoatTravel.getPlugin().saveConfig();
        Bukkit.getLogger().info("Config reloaded successfully!");
    }
}
