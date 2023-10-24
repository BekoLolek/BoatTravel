package boat.boattravel.Storage;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;

public class SignObjectTypeAdapter extends TypeAdapter<SignObject> {


    @Override
    public void write(JsonWriter out, SignObject signObject) throws IOException {
        writeSignObject(out, signObject, 1);
    }

    public void writeSignObject(JsonWriter out, SignObject signObject, int maxDepth) throws IOException {
        if (maxDepth <= 0) {
            // Maximum recursion depth reached; return without further nesting
            out.beginObject();
            out.name("id").value(signObject.getId());
            out.name("owner").value(signObject.getOwner().getUniqueId().toString());
            out.name("income").value(signObject.getIncome());

            Location location = signObject.getLocation();
            out.name("location").beginObject();
            out.name("world").value(location.getWorld().getName());
            out.name("x").value(location.getX());
            out.name("y").value(location.getY());
            out.name("z").value(location.getZ());
            out.name("yaw").value(location.getYaw());
            out.name("pitch").value(location.getPitch());
            out.endObject();

            out.name("cost").value(signObject.getCost());
            // Set "destination" to null at this depth
            out.name("destination").value("empty");

            out.endObject();
        } else {
            // Proceed to write the "destination" with a reduced depth
            out.beginObject();
            out.name("id").value(signObject.getId());
            out.name("owner").value(signObject.getOwner().getUniqueId().toString());
            out.name("income").value(signObject.getIncome());

            Location location = signObject.getLocation();
            out.name("location").beginObject();
            out.name("world").value(location.getWorld().getName());
            out.name("x").value(location.getX());
            out.name("y").value(location.getY());
            out.name("z").value(location.getZ());
            out.name("yaw").value(location.getYaw());
            out.name("pitch").value(location.getPitch());
            out.endObject();

            out.name("cost").value(signObject.getCost());

            SignObject destination = signObject.getDestination();
            if (destination != null) {
                out.name("destination");
                writeSignObject(out, destination, maxDepth - 1);
            } else {
                out.name("destination").value("empty");
            }

            out.endObject();
        }
    }



    @Override
    public SignObject read(JsonReader in) throws IOException {
        return readSignObject(in, 1); // Set an appropriate maximum depth value, e.g., 10
    }

    private SignObject readSignObject(JsonReader in, int maxDepth) throws IOException {
        if (maxDepth <= 0) {
            in.beginObject();
            SignObject signObject = new SignObject();
            // Maximum recursion depth reached; return null or handle appropriately
            while (in.hasNext()) {
                String name = in.nextName();
                if ("id".equals(name)) {
                    signObject.setId(in.nextString());
                } else if ("owner".equals(name)) {
                    // Read the owner's name as a string and convert it to an OfflinePlayer
                    String ownerName = in.nextString();
                    OfflinePlayer owner = Bukkit.getOfflinePlayer(ownerName);
                    signObject.setOwner(owner);
                } else if ("income".equals(name)) {
                    signObject.setIncome(in.nextDouble());
                } else if ("location".equals(name)) {
                    // Deserialize the Location using the custom method
                    signObject.deserializeLocation(in);
                } else if ("cost".equals(name)) {
                    signObject.setCost(in.nextDouble());
                } else if ("destination".equals(name)) {
                    in.nextString();
                    signObject.setDestination(null);
                }
            }

            in.endObject();
            return signObject;
        } else {

            in.beginObject();
            SignObject signObject = new SignObject();

            while (in.hasNext()) {
                String name = in.nextName();
                if ("id".equals(name)) {
                    signObject.setId(in.nextString());
                } else if ("owner".equals(name)) {
                    // Read the owner's name as a string and convert it to an OfflinePlayer
                    String ownerName = in.nextString();
                    OfflinePlayer owner = Bukkit.getOfflinePlayer(ownerName);
                    signObject.setOwner(owner);
                } else if ("income".equals(name)) {
                    signObject.setIncome(in.nextDouble());
                } else if ("location".equals(name)) {
                    // Deserialize the Location using the custom method
                    signObject.deserializeLocation(in);
                } else if ("cost".equals(name)) {
                    signObject.setCost(in.nextDouble());
                } else if ("destination".equals(name)) {
                    SignObject destination = readSignObject(in, maxDepth - 1);
                    signObject.setDestination(destination);
                }
            }

            in.endObject();

            return signObject; // Exit condition: return the fully constructed object
        }


    }


}
