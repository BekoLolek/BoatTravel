package boat.boattravel.Storage;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.google.gson.stream.JsonReader;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;

import java.io.IOException;

public class SignObject {
    private String id;
    private OfflinePlayer owner;
    private Location location;
    private double income;
    private double cost;

    public SignObject() {

    }

    public void setId(String id) {
        this.id = id;
    }

    public void setOwner(OfflinePlayer owner) {
        this.owner = owner;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public void setIncome(double income) {
        this.income = income;
    }

    public void setCost(double cost) {
        this.cost = cost;
    }

    private transient SignObject destination;

    public SignObject(String id, OfflinePlayer owner, Location location, double income, double cost, SignObject destination) {
        this.id = id;
        this.owner = owner;
        this.location = location;
        this.income = income;
        this.cost = cost;
        this.destination = destination;
    }

    public String getId() {
        return id;
    }

    public OfflinePlayer getOwner() {
        return owner;
    }

    public Location getLocation() {
        return location;
    }

    public double getIncome() {
        return income;
    }

    public double getCost() {
        return cost;
    }

    public SignObject getDestination() {
        return destination;
    }

    public void setDestination(SignObject destination) {
        this.destination = destination;
    }

    public String toJson() {
        JsonObject json = new JsonObject();
        json.addProperty("id", id);
        json.addProperty("owner", owner.getName());
        json.addProperty("location", locationToString(location));
        json.addProperty("income", income);
        json.addProperty("cost", cost);
        if (destination != null) {
            json.add("destination", destination.toJsonObject());
        }
        return json.toString();
    }

    public static SignObject fromJson(String jsonString) {
        JsonObject jsonObject = new JsonParser().parse(jsonString).getAsJsonObject();
        String id = jsonObject.get("id").getAsString();
        String ownerName = jsonObject.get("owner").getAsString();
        OfflinePlayer owner = Bukkit.getOfflinePlayer(ownerName);
        Location location = locationFromString(jsonObject.get("location").getAsString());
        double income = jsonObject.get("income").getAsDouble();
        double cost = jsonObject.get("cost").getAsDouble();

        SignObject destination = null;
        if (jsonObject.has("destination")) {
            destination = SignObject.fromJsonObject(jsonObject.getAsJsonObject("destination"));
        }

        return new SignObject(id, owner, location, income, cost, destination);
    }

    private JsonObject toJsonObject() {
        JsonObject json = new JsonObject();
        json.addProperty("id", id);
        json.addProperty("owner", owner.getName());
        json.addProperty("location", locationToString(location));
        json.addProperty("income", income);
        json.addProperty("cost", cost);
        if (destination != null) {
            json.add("destination", destination.toJsonObject());
        }
        return json;
    }

    private static SignObject fromJsonObject(JsonObject jsonObject) {
        String id = jsonObject.get("id").getAsString();
        String ownerName = jsonObject.get("owner").getAsString();
        OfflinePlayer owner = Bukkit.getOfflinePlayer(ownerName);
        Location location = locationFromString(jsonObject.get("location").getAsString());
        double income = jsonObject.get("income").getAsDouble();
        double cost = jsonObject.get("cost").getAsDouble();

        SignObject destination = null;
        if (jsonObject.has("destination")) {
            destination = SignObject.fromJsonObject(jsonObject.getAsJsonObject("destination"));
        }

        return new SignObject(id, owner, location, income, cost, destination);
    }

    private static String locationToString(Location location) {
        JsonObject json = new JsonObject();
        json.addProperty("world", location.getWorld().getName());
        json.addProperty("x", location.getX());
        json.addProperty("y", location.getY());
        json.addProperty("z", location.getZ());
        json.addProperty("yaw", location.getYaw());
        json.addProperty("pitch", location.getPitch());
        return json.toString();
    }

    private static Location locationFromString(String locationString) {
        JsonObject json = new JsonParser().parse(locationString).getAsJsonObject();
        String worldName = json.get("world").getAsString();
        double x = json.get("x").getAsDouble();
        double y = json.get("y").getAsDouble();
        double z = json.get("z").getAsDouble();
        float yaw = json.get("yaw").getAsFloat();
        float pitch = json.get("pitch").getAsFloat();
        return new Location(Bukkit.getWorld(worldName), x, y, z, yaw, pitch);
    }

    // Custom deserialization method for Location
    public void deserializeLocation(JsonReader in) throws IOException {
        double x = 0;
        double y = 0;
        double z = 0;
        float yaw = 0;
        float pitch = 0;
        String worldName = "";

        in.beginObject();

        while (in.hasNext()) {
            String name = in.nextName();
            if ("x".equals(name)) {
                x = in.nextDouble();
            } else if ("y".equals(name)) {
                y = in.nextDouble();
            } else if ("z".equals(name)) {
                z = in.nextDouble();
            } else if ("world".equals(name)) {
                worldName = in.nextString();
            } else if ("yaw".equals(name)) {
                yaw = (float) in.nextDouble();
            } else if ("pitch".equals(name)) {
                pitch = (float) in.nextDouble();
            }
        }

        in.endObject();

        this.location = new Location(Bukkit.getWorld(worldName), x, y, z, yaw, pitch);
    }


}
