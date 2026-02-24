package dev.bekololek.boattravel.commands;

import dev.bekololek.boattravel.Main;
import dev.bekololek.boattravel.managers.DockManager;
import dev.bekololek.boattravel.managers.StatsManager;
import dev.bekololek.boattravel.managers.TravelSignManager;
import dev.bekololek.boattravel.managers.VoyageManager;
import dev.bekololek.boattravel.model.Dock;
import dev.bekololek.boattravel.model.TravelSign;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class BoatTravelCommand implements CommandExecutor, TabCompleter {

    private final Main plugin;
    private final DockManager dockManager;
    private final TravelSignManager travelSignManager;
    private final VoyageManager voyageManager;
    private final StatsManager statsManager;

    public BoatTravelCommand(Main plugin, DockManager dockManager,
                              TravelSignManager travelSignManager,
                              VoyageManager voyageManager,
                              StatsManager statsManager) {
        this.plugin            = plugin;
        this.dockManager       = dockManager;
        this.travelSignManager = travelSignManager;
        this.voyageManager     = voyageManager;
        this.statsManager      = statsManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sendUsage(sender, label);
            return true;
        }

        switch (args[0].toLowerCase()) {

            case "reload" -> {
                if (!sender.hasPermission("boattravel.admin")) {
                    sender.sendMessage(Component.text("No permission.", NamedTextColor.RED));
                    return true;
                }
                plugin.reloadConfig();
                sender.sendMessage(Component.text("BoatTravel config reloaded.", NamedTextColor.GREEN));
            }

            case "cancel" -> {
                if (!(sender instanceof Player player)) {
                    sender.sendMessage(Component.text("Only players can use this command.", NamedTextColor.RED));
                    return true;
                }
                if (!player.hasPermission("boattravel.cancel")) {
                    player.sendMessage(Component.text("No permission.", NamedTextColor.RED));
                    return true;
                }
                if (!voyageManager.isOnVoyage(player.getUniqueId())) {
                    player.sendMessage(Component.text("You are not on a voyage.", NamedTextColor.YELLOW));
                    return true;
                }
                voyageManager.cancelVoyage(player.getUniqueId());
            }

            case "docks" -> {
                if (!sender.hasPermission("boattravel.list")) {
                    sender.sendMessage(Component.text("No permission.", NamedTextColor.RED));
                    return true;
                }
                var docks = dockManager.getAll();
                if (docks.isEmpty()) {
                    sender.sendMessage(Component.text("No docks registered.", NamedTextColor.GRAY));
                    return true;
                }
                sender.sendMessage(Component.text("--- Registered Docks ---", NamedTextColor.GOLD));
                for (Dock d : docks) {
                    sender.sendMessage(Component.text("  " + d.getName()
                            + " (" + d.getSignWorld() + " "
                            + Math.round(d.getArrX()) + ","
                            + Math.round(d.getArrY()) + ","
                            + Math.round(d.getArrZ()) + ")", NamedTextColor.AQUA));
                }
            }

            case "info" -> {
                if (!sender.hasPermission("boattravel.list")) {
                    sender.sendMessage(Component.text("No permission.", NamedTextColor.RED));
                    return true;
                }
                if (args.length < 2) {
                    sender.sendMessage(Component.text("Usage: /" + label + " info <dock>", NamedTextColor.YELLOW));
                    return true;
                }
                Dock dock = dockManager.getByName(args[1]);
                if (dock == null) {
                    sender.sendMessage(Component.text("Dock '" + args[1] + "' not found.", NamedTextColor.RED));
                    return true;
                }
                sender.sendMessage(Component.text("--- Dock: " + dock.getName() + " ---", NamedTextColor.GOLD));
                sender.sendMessage(Component.text("  World: " + dock.getSignWorld(), NamedTextColor.AQUA));
                sender.sendMessage(Component.text("  Location: "
                        + Math.round(dock.getArrX()) + ", "
                        + Math.round(dock.getArrY()) + ", "
                        + Math.round(dock.getArrZ()), NamedTextColor.AQUA));
                List<TravelSign> routes = travelSignManager.getPointingAt(dock.getName());
                if (routes.isEmpty()) {
                    sender.sendMessage(Component.text("  No travel signs lead here.", NamedTextColor.GRAY));
                } else {
                    sender.sendMessage(Component.text("  Incoming routes: " + routes.size(), NamedTextColor.AQUA));
                }
                // Outgoing routes (travel signs near this dock pointing elsewhere)
                List<TravelSign> outgoing = travelSignManager.getAll().stream()
                        .filter(ts -> ts.getWorld().equals(dock.getSignWorld())
                                && Math.abs(ts.getBx() - dock.getSignX()) < 10
                                && Math.abs(ts.getBz() - dock.getSignZ()) < 10
                                && !ts.getDestinationDock().equalsIgnoreCase(dock.getName()))
                        .collect(Collectors.toList());
                if (!outgoing.isEmpty()) {
                    sender.sendMessage(Component.text("  Outgoing routes:", NamedTextColor.AQUA));
                    for (TravelSign ts : outgoing) {
                        sender.sendMessage(Component.text("    -> " + ts.getDestinationDock()
                                + " (~" + Math.round(ts.getDistance()) + " blocks)", NamedTextColor.GREEN));
                    }
                }
            }

            case "stats" -> handleStats(sender, label, args);

            default -> sendUsage(sender, label);
        }
        return true;
    }

    // ── Stats subcommand ───────────────────────────────────────────────────────

    private void handleStats(CommandSender sender, String label, String[] args) {
        if (!sender.hasPermission("boattravel.stats")) {
            sender.sendMessage(Component.text("No permission.", NamedTextColor.RED));
            return;
        }

        // /boattravel stats top [stat]
        if (args.length >= 2 && args[1].equalsIgnoreCase("top")) {
            String statName = args.length >= 3 ? args[2].toLowerCase() : "voyages_completed";
            if (!StatsManager.leaderboardStats().contains(statName)) {
                sender.sendMessage(Component.text("Unknown stat. Available: "
                        + String.join(", ", StatsManager.leaderboardStats()), NamedTextColor.RED));
                return;
            }
            List<Map.Entry<String, Number>> top = statsManager.getTopPlayers(statName, 10);
            sender.sendMessage(Component.text("--- Top " + StatsManager.statLabel(statName) + " ---", NamedTextColor.GOLD));
            if (top.isEmpty()) {
                sender.sendMessage(Component.text("  No data yet.", NamedTextColor.GRAY));
                return;
            }
            for (int i = 0; i < top.size(); i++) {
                Map.Entry<String, Number> entry = top.get(i);
                String value;
                if (entry.getValue() instanceof Double d) {
                    value = String.format("%.2f", d);
                } else {
                    value = String.valueOf(entry.getValue());
                }
                sender.sendMessage(Component.text("  " + (i + 1) + ". " + entry.getKey() + " - " + value, NamedTextColor.AQUA));
            }
            return;
        }

        // /boattravel stats <player>
        if (args.length >= 2) {
            if (!sender.hasPermission("boattravel.stats.others")) {
                sender.sendMessage(Component.text("No permission to view other players' stats.", NamedTextColor.RED));
                return;
            }
            @SuppressWarnings("deprecation")
            OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
            if (!target.hasPlayedBefore() && !target.isOnline()) {
                sender.sendMessage(Component.text("Player '" + args[1] + "' not found.", NamedTextColor.RED));
                return;
            }
            showPlayerStats(sender, target.getUniqueId(), target.getName() != null ? target.getName() : args[1]);
            return;
        }

        // /boattravel stats (own)
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("Usage: /" + label + " stats <player>", NamedTextColor.YELLOW));
            return;
        }
        showPlayerStats(sender, player.getUniqueId(), player.getName());
    }

    private void showPlayerStats(CommandSender sender, java.util.UUID uuid, String name) {
        sender.sendMessage(Component.text("--- " + name + "'s Travel Stats ---", NamedTextColor.GOLD));
        sender.sendMessage(Component.text("  Voyages Completed: " + statsManager.getPlayerStat(uuid, "voyages_completed"), NamedTextColor.AQUA));
        sender.sendMessage(Component.text("  Voyages Cancelled: " + statsManager.getPlayerStat(uuid, "voyages_cancelled"), NamedTextColor.AQUA));
        sender.sendMessage(Component.text("  Distance Traveled: " + statsManager.getPlayerStat(uuid, "distance_traveled") + " blocks", NamedTextColor.AQUA));
        sender.sendMessage(Component.text("  Longest Voyage: " + statsManager.getPlayerStat(uuid, "longest_voyage") + " blocks", NamedTextColor.AQUA));
        sender.sendMessage(Component.text("  Money Spent: " + statsManager.getPlayerStat(uuid, "money_spent"), NamedTextColor.AQUA));
        sender.sendMessage(Component.text("  Favorite Destination: " + statsManager.getPlayerStat(uuid, "favorite_destination"), NamedTextColor.AQUA));

        // Global highlights
        sender.sendMessage(Component.text("--- Global ---", NamedTextColor.GOLD));
        sender.sendMessage(Component.text("  Total Voyages: " + statsManager.getGlobalStat("total_voyages"), NamedTextColor.YELLOW));
        sender.sendMessage(Component.text("  Most Popular: " + statsManager.getGlobalStat("most_popular_destination"), NamedTextColor.YELLOW));
        sender.sendMessage(Component.text("  Biggest Traveler: " + statsManager.getGlobalStat("biggest_traveler"), NamedTextColor.YELLOW));
    }

    private void sendUsage(CommandSender sender, String label) {
        sender.sendMessage(Component.text("BoatTravel commands:", NamedTextColor.GOLD));
        sender.sendMessage(Component.text("  /" + label + " docks         - List all registered docks", NamedTextColor.YELLOW));
        sender.sendMessage(Component.text("  /" + label + " info <dock>   - Show dock details and routes", NamedTextColor.YELLOW));
        sender.sendMessage(Component.text("  /" + label + " cancel        - Abort current voyage", NamedTextColor.YELLOW));
        sender.sendMessage(Component.text("  /" + label + " stats         - View travel statistics", NamedTextColor.YELLOW));
        sender.sendMessage(Component.text("  /" + label + " reload        - Reload config [admin]", NamedTextColor.YELLOW));
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 1) {
            return Arrays.asList("docks", "info", "cancel", "stats", "reload").stream()
                    .filter(s -> s.startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("info")) {
            return dockManager.getAll().stream()
                    .map(Dock::getName)
                    .filter(name -> name.toLowerCase().startsWith(args[1].toLowerCase()))
                    .collect(Collectors.toList());
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("stats")) {
            List<String> options = new java.util.ArrayList<>();
            options.add("top");
            // Online player names for /boattravel stats <player>
            for (Player p : Bukkit.getOnlinePlayers()) {
                options.add(p.getName());
            }
            return options.stream()
                    .filter(s -> s.toLowerCase().startsWith(args[1].toLowerCase()))
                    .collect(Collectors.toList());
        }
        if (args.length == 3 && args[0].equalsIgnoreCase("stats") && args[1].equalsIgnoreCase("top")) {
            return StatsManager.leaderboardStats().stream()
                    .filter(s -> s.startsWith(args[2].toLowerCase()))
                    .collect(Collectors.toList());
        }
        return List.of();
    }
}
