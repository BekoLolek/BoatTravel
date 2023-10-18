package boat.boattravel.Storage;

import org.bukkit.Location;
import org.bukkit.OfflinePlayer;

public class SignObject {
    private Location location;
    private OfflinePlayer owner;
    private double income;
    private String id;
    private SignObject destination;
    private double cost;

    public SignObject(Location location, OfflinePlayer owner, double income, double cost, String id) {
        this.location = location;
        this.owner = owner;
        this.income = income;
        this.id = id;
        this.cost = cost;
    }

    public SignObject getDestination() {
        return destination;
    }

    public void setDestination(SignObject destination) {
        this.destination = destination;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public OfflinePlayer getOwner() {
        return owner;
    }

    public void setOwner(OfflinePlayer owner) {
        this.owner = owner;
    }

    public double getIncome() {
        return income;
    }

    public void setIncome(double income) {
        this.income = income;
    }

}
