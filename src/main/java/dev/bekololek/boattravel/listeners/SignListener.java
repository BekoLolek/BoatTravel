package dev.bekololek.boattravel.listeners;

import dev.bekololek.boattravel.Main;
import dev.bekololek.boattravel.managers.DockManager;
import dev.bekololek.boattravel.managers.EconomyManager;
import dev.bekololek.boattravel.managers.TravelSignManager;
import dev.bekololek.boattravel.model.Dock;
import dev.bekololek.boattravel.model.TravelSign;
import dev.bekololek.boattravel.utils.ColorUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.block.sign.Side;
import org.bukkit.block.sign.SignSide;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.SignChangeEvent;

public class SignListener implements Listener {

    private final Main plugin;
    private final DockManager dockManager;
    private final TravelSignManager travelSignManager;
    private final EconomyManager economyManager;

    public SignListener(Main plugin, DockManager dockManager,
                        TravelSignManager travelSignManager, EconomyManager economyManager) {
        this.plugin            = plugin;
        this.dockManager       = dockManager;
        this.travelSignManager = travelSignManager;
        this.economyManager    = economyManager;
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onSignChange(SignChangeEvent e) {
        String line0 = plain(e.line(0)).trim();
        if ("[BoatDock]".equalsIgnoreCase(line0)) {
            handleDockSign(e);
        } else if ("[BoatTravel]".equalsIgnoreCase(line0)) {
            handleTravelSign(e);
        }
    }

    private void handleDockSign(SignChangeEvent e) {
        if (!e.getPlayer().hasPermission("boattravel.dock")) {
            e.getPlayer().sendMessage(Component.text("You don't have permission to create dock signs.", net.kyori.adventure.text.format.NamedTextColor.RED));
            e.setCancelled(true);
            return;
        }

        String dockName = plain(e.line(1)).trim();
        if (dockName.isEmpty()) {
            e.getPlayer().sendMessage(Component.text("Line 2 must contain the dock name.", net.kyori.adventure.text.format.NamedTextColor.RED));
            e.setCancelled(true);
            return;
        }
        if (dockManager.isNameTaken(dockName)) {
            e.getPlayer().sendMessage(Component.text("A dock named '" + dockName + "' already exists.", net.kyori.adventure.text.format.NamedTextColor.RED));
            e.setCancelled(true);
            return;
        }

        TextColor color = ColorUtils.fromName(plugin.getConfig().getString("sign-dock-color", "DARK_BLUE"));
        Block block = e.getBlock();
        double ax = block.getX() + 0.5;
        double ay = block.getY();
        double az = block.getZ() + 0.5;
        float  yaw = e.getPlayer().getLocation().getYaw();

        Dock dock = new Dock(dockName,
                block.getWorld().getName(), block.getX(), block.getY(), block.getZ(),
                ax, ay, az, yaw);
        dockManager.register(dock);

        // Colorize sign lines
        e.line(0, Component.text("[BoatDock]").color(color).decorate(TextDecoration.BOLD));
        e.line(1, Component.text(dockName).color(color));
        e.line(2, Component.text("\u2713 Registered").color(color));
        e.line(3, Component.text(block.getX() + "," + block.getY() + "," + block.getZ()).color(color));

        e.getPlayer().sendMessage(Component.text("Dock '" + dockName + "' registered.", net.kyori.adventure.text.format.NamedTextColor.GREEN));

        // Revalidate any travel signs that were pointing at this dock name
        for (TravelSign ts : travelSignManager.getPointingAt(dockName)) {
            double cost = economyManager.calculateCost(ts.getDistance());
            travelSignManager.recolorSign(ts, true, cost);
        }
    }

    private void handleTravelSign(SignChangeEvent e) {
        if (!e.getPlayer().hasPermission("boattravel.travel")) {
            e.getPlayer().sendMessage(Component.text("You don't have permission to create travel signs.", net.kyori.adventure.text.format.NamedTextColor.RED));
            e.setCancelled(true);
            return;
        }

        String destName = plain(e.line(1)).trim();
        if (destName.isEmpty()) {
            e.getPlayer().sendMessage(Component.text("Line 2 must contain the destination dock name.", net.kyori.adventure.text.format.NamedTextColor.RED));
            e.setCancelled(true);
            return;
        }

        Block block = e.getBlock();
        Dock dest = dockManager.getByName(destName);
        boolean valid = dest != null;

        double distance = 0;
        if (valid) {
            distance = block.getLocation().distance(dest.getArrivalLocation() != null
                    ? dest.getArrivalLocation() : block.getLocation());
        }

        TextColor color = valid
                ? ColorUtils.fromName(plugin.getConfig().getString("sign-valid-color",   "DARK_GREEN"))
                : ColorUtils.fromName(plugin.getConfig().getString("sign-invalid-color", "DARK_RED"));

        double cost = valid ? economyManager.calculateCost(distance) : 0;

        e.line(0, Component.text("[BoatTravel]").color(color).decorate(TextDecoration.BOLD));
        e.line(1, Component.text(destName).color(color));
        if (valid) {
            e.line(2, Component.text("~" + Math.round(distance) + " blocks").color(color));
            e.line(3, cost <= 0 ? Component.text("Free").color(color)
                                : Component.text("$" + String.format("%.2f", cost)).color(color));
        } else {
            e.line(2, Component.text("(invalid)").color(color));
            e.line(3, Component.empty());
            e.getPlayer().sendMessage(Component.text("Dock '" + destName + "' not found — sign marked invalid.", net.kyori.adventure.text.format.NamedTextColor.YELLOW));
        }

        TravelSign ts = new TravelSign(
                block.getWorld().getName(), block.getX(), block.getY(), block.getZ(),
                destName, distance);
        travelSignManager.register(ts);

        if (valid) e.getPlayer().sendMessage(Component.text("Travel sign to '" + destName + "' registered.", net.kyori.adventure.text.format.NamedTextColor.GREEN));
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent e) {
        Block block = e.getBlock();
        if (!(block.getState() instanceof Sign)) return;

        // Check dock sign
        Dock dock = dockManager.getBySignLocation(block.getLocation());
        if (dock != null) {
            dockManager.unregister(dock);
            e.getPlayer().sendMessage(Component.text("Dock '" + dock.getName() + "' unregistered.", net.kyori.adventure.text.format.NamedTextColor.YELLOW));
            // Invalidate all travel signs pointing at this dock
            for (TravelSign ts : travelSignManager.getPointingAt(dock.getName())) {
                travelSignManager.recolorSign(ts, false, 0);
            }
            return;
        }

        // Check travel sign
        String key = TravelSign.keyFor(block.getLocation());
        TravelSign ts = travelSignManager.getByKey(key);
        if (ts != null) {
            travelSignManager.unregister(ts);
            e.getPlayer().sendMessage(Component.text("Travel sign to '" + ts.getDestinationDock() + "' removed.", net.kyori.adventure.text.format.NamedTextColor.YELLOW));
        }
    }

    private String plain(Component c) {
        if (c == null) return "";
        return PlainTextComponentSerializer.plainText().serialize(c);
    }
}
