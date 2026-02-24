# BoatTravel

A Minecraft Paper plugin that creates a ferry network system. Players place dock signs to register harbors and travel signs to create routes. Clicking a travel sign launches the player on a boat ride through a dedicated water world, with travel time proportional to real-world distance.

**Paper 1.21.4+ | Java 21**

## Features

- **Dock & Travel Signs** — Place `[BoatDock]` signs to register harbors, and `[BoatTravel]` signs to create one-way routes between docks. Signs auto-display distance and cost.
- **Immersive Boat Voyages** — Players ride a real boat through a dedicated water world. Travel time scales with distance and a configurable speed multiplier.
- **Economy Integration** — Charge per-block and/or flat fees via Vault. Admins can bypass costs.
- **Voyage Controls** — Cancel mid-voyage with `/bt cancel`. Disconnect recovery teleports players to destination or origin.
- **Dynamic Sign Validation** — Travel signs auto-recolor (green/red) when docks are created or removed.
- **Statistics & Leaderboards** — Tracks voyages completed, distance traveled, money spent, favorite destination, and more.
- **PlaceholderAPI Support** — Exposes all stats as placeholders for scoreboards and holograms.
- **Hot-Reload** — Reload all config settings without restarting.

## Commands

| Command | Description | Permission |
|---------|-------------|------------|
| `/bt docks` | List all registered docks | `boattravel.list` |
| `/bt info <dock>` | Show dock details and routes | `boattravel.list` |
| `/bt cancel` | Abort current voyage | `boattravel.cancel` |
| `/bt stats [player]` | View travel statistics | `boattravel.stats` |
| `/bt stats top [stat]` | View top 10 leaderboard | `boattravel.stats` |
| `/bt reload` | Reload configuration | `boattravel.admin` |

## Permissions

| Permission | Description | Default |
|------------|-------------|---------|
| `boattravel.use` | Click travel signs to start voyages | Everyone |
| `boattravel.dock` | Create dock signs | OP |
| `boattravel.travel` | Create travel signs | OP |
| `boattravel.cancel` | Cancel mid-voyage | Everyone |
| `boattravel.list` | View dock list and info | Everyone |
| `boattravel.admin` | Reload config, bypass costs | OP |
| `boattravel.bypass.cost` | Travel for free | OP |
| `boattravel.stats` | View own stats | Everyone |
| `boattravel.stats.others` | View other players' stats | OP |

## How It Works

### Creating Docks
Place a sign with `[BoatDock]` on line 1 and the dock name on line 2. The sign auto-formats with location and a "Registered" status.

### Creating Routes
Place a sign with `[BoatTravel]` on line 1 and a destination dock name on line 2. The sign shows the distance and travel cost. Valid routes appear green; invalid ones appear red.

### Traveling
Right-click a travel sign to embark. You'll be placed on a boat in a flat water world and sail for a duration matching the real-world distance. An action bar shows remaining distance. On arrival, you're teleported to the destination dock.

## Configuration

```yaml
travel-speed-multiplier: 10.0    # Multiplier on base boat speed (~43 blocks/sec at 10x)
cost-per-block: 0.0              # Economy cost per block of distance
flat-cost: 0.0                   # Flat fee per voyage
boat-world-name: "boattravel_world"
boat-spacing: 256                # Spacing between concurrent boats
disconnect-behavior: "DESTINATION" # DESTINATION or ORIGIN on disconnect

# Sign colors
sign-dock-color: "DARK_BLUE"
sign-valid-color: "DARK_GREEN"
sign-invalid-color: "DARK_RED"
```

## Placeholders

Requires [PlaceholderAPI](https://www.spigotmc.org/resources/placeholderapi.6245/).

| Placeholder | Description |
|-------------|-------------|
| `%boattravel_stat_voyages_completed%` | Player's completed voyages |
| `%boattravel_stat_distance_traveled%` | Total distance traveled |
| `%boattravel_stat_money_spent%` | Total money spent on travel |
| `%boattravel_stat_favorite_destination%` | Most visited destination |
| `%boattravel_global_total_voyages%` | Server-wide total voyages |
| `%boattravel_global_most_popular_destination%` | Most popular dock |
| `%boattravel_top_<stat>_<pos>%` | Leaderboard player name |
| `%boattravel_topvalue_<stat>_<pos>%` | Leaderboard value |

## Dependencies

| Dependency | Required |
|------------|----------|
| [VersionAdapter](https://github.com/BekoLolek/VersionAdapter) | Yes |
| [Vault](https://www.spigotmc.org/resources/vault.34315/) | No (travel is free without it) |
| [PlaceholderAPI](https://www.spigotmc.org/resources/placeholderapi.6245/) | No |

## Installation

1. Place `BoatTravel.v1-BL.jar` in your server's `plugins/` folder.
2. Ensure `VersionAdapter.jar` is also in the `plugins/` folder.
3. Restart the server. The boat world is created automatically.
4. Edit `plugins/BoatTravel/config.yml` to customize settings.

## Part of the BekoLolek Plugin Ecosystem

Built by **Lolek** for the BekoLolek Minecraft network.
