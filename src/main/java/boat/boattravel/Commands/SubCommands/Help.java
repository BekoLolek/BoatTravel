package boat.boattravel.Commands.SubCommands;

import boat.boattravel.Commands.CommandManager;
import boat.boattravel.Commands.SubCommand;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class Help extends SubCommand {
    @Override
    public String getName() {
        return "help";
    }

    @Override
    public String getDescription() {
        return "Shows all the commands for BoatTravel";
    }

    @Override
    public String getSyntax() {
        return "/boat help";
    }

    @Override
    public void perform(Player player, String[] args) {
        CommandManager commandManager = new CommandManager();
        player.sendMessage(ChatColor.RED + "=======" + "Boat Travel Plugin" + "=======");
        player.sendMessage("Guide:");
        player.sendMessage("Create a sign with the following lines:");
        player.sendMessage("First line: [Transport]");
        player.sendMessage("Second line is the cost of the travel: 10");
        player.sendMessage("If you want to link your sign to an already existing ones, put the id here, otherwise it will be generated for you: 0000");
        player.sendMessage(" ");
        player.sendMessage("WARNING: signs can only be linked with 1 other sign!");
        player.sendMessage(" ");
        player.sendMessage("Commands:");
        for(int i = 0; i < commandManager.getSubCommands().size();i++){
            player.sendMessage(commandManager.getSubCommands().get(i).getSyntax() + " - " + commandManager.getSubCommands().get(i).getDescription());

        }
        player.sendMessage(ChatColor.RED + "==========================");
    }
}
