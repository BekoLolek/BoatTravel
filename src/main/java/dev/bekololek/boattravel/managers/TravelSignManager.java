package dev.bekololek.boattravel.managers;

import dev.bekololek.boattravel.Main;
import dev.bekololek.boattravel.model.TravelSign;
import dev.bekololek.boattravel.utils.ColorUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.block.sign.Side;
import org.bukkit.block.sign.SignSide;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TravelSignManager {

    private final Main plugin;
    private final File dataFile;
    // keyed by "world,x,y,z"
    private final Map<String, TravelSign> signs = new HashMap<>();

    public TravelSignManager(Main plugin) {
        this.plugin   = plugin;
        this.dataFile = new File(plugin.getDataFolder(), "travel-signs.yml");
        load();
    }

    // ── Persistence ──────────────────────────────────────────────────────────

    public void load() {
        signs.clear();
        if (!dataFile.exists()) return;
        FileConfiguration cfg = YamlConfiguration.loadConfiguration(dataFile);
        var section = cfg.getConfigurationSection("signs");
        if (section == null) return;
        for (String key : section.getKeys(false)) {
            TravelSign ts = TravelSign.load(section.getConfigurationSection(key));
            signs.put(ts.key(), ts);
        }
        plugin.getLogger().info("Loaded " + signs.size() + " travel signs.");
    }

    public void save() {
        FileConfiguration cfg = new YamlConfiguration();
        int i = 0;
        for (TravelSign ts : signs.values()) {
            ts.save(cfg.createSection("signs.s" + i++));
        }
        try { cfg.save(dataFile); }
        catch (IOException e) { plugin.getLogger().severe("Could not save travel-signs.yml: " + e.getMessage()); }
    }

    // ── API ───────────────────────────────────────────────────────────────────

    public void register(TravelSign ts) {
        signs.put(ts.key(), ts);
        save();
    }

    public void unregister(TravelSign ts) {
        signs.remove(ts.key());
        save();
    }

    public TravelSign getByKey(String key) {
        return signs.get(key);
    }

    public Collection<TravelSign> getAll() {
        return Collections.unmodifiableCollection(signs.values());
    }

    /** All travel signs pointing at a given dock name. */
    public List<TravelSign> getPointingAt(String dockName) {
        List<TravelSign> result = new ArrayList<>();
        for (TravelSign ts : signs.values()) {
            if (ts.getDestinationDock().equalsIgnoreCase(dockName)) result.add(ts);
        }
        return result;
    }

    // ── Sign visual update ────────────────────────────────────────────────────

    /**
     * Recolor an existing travel sign block according to whether its destination is valid.
     * The sign content (lines 2–4) is also refreshed from the TravelSign data.
     */
    public void recolorSign(TravelSign ts, boolean valid, double cost) {
        var loc = ts.getBlockLocation();
        if (loc.getWorld() == null) return;
        Block block = loc.getWorld().getBlockAt(loc);
        if (!(block.getState() instanceof Sign sign)) return;

        TextColor color = valid
                ? ColorUtils.fromName(plugin.getConfig().getString("sign-valid-color",   "DARK_GREEN"))
                : ColorUtils.fromName(plugin.getConfig().getString("sign-invalid-color", "DARK_RED"));

        SignSide front = sign.getSide(Side.FRONT);
        front.line(0, Component.text("[BoatTravel]").color(color).decorate(TextDecoration.BOLD));
        front.line(1, Component.text(ts.getDestinationDock()).color(color));
        if (valid) {
            front.line(2, Component.text("~" + Math.round(ts.getDistance()) + " blocks").color(color));
            front.line(3, cost <= 0 ? Component.text("Free").color(color)
                                    : Component.text("$" + String.format("%.2f", cost)).color(color));
        } else {
            front.line(2, Component.text("(invalid)").color(color));
            front.line(3, Component.empty());
        }
        sign.update(true);
    }
}
