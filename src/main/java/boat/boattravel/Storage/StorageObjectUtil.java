package boat.boattravel.Storage;

import boat.boattravel.BoatTravel;
import boat.boattravel.Storage.SignObject;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class StorageObjectUtil {

    private static final String DATA_FOLDER = BoatTravel.getPlugin().getDataFolder().getAbsolutePath();
    private static final String PAIRED_JSON = "paired.json";
    private static final String LONELY_JSON = "lonely.json";

    private static ArrayList<SignObject> signs = new ArrayList<>();
    private static ArrayList<SignObject> routes = new ArrayList<>();

    public static ArrayList<SignObject> getSigns() {
        return signs;
    }

    public static ArrayList<SignObject> getRoutes() {
        return routes;
    }

    public static SignObject create(SignObject signObject) {


        if (!tryLink(signObject)) {
            signs.add(signObject);
        }

        try {
            save();
        } catch (IOException e) {
            // Handle the exception appropriately, e.g., log it
        }

        return signObject;
    }

    public static SignObject find(Location location) {
        for (SignObject route : routes) {
            if (route.getLocation().equals(location)) {
                return route;
            }
        }
        return null;
    }

    public static boolean tryLink(SignObject object) {
        String id = object.getId();

        // Find the sign with the matching ID in unlinked signs
        SignObject matchingSign = findSignObjectById(id);

        if (matchingSign != null) {
            // Ensure that the matching sign is not already linked
            if (matchingSign.getDestination() == null) {
                // Set the destination of the matching sign to the provided object
                matchingSign.setDestination(object);
                // Set the destination of the provided object to the matching sign
                object.setDestination(matchingSign);

                routes.add(object);
                routes.add(matchingSign);
                signs.remove(matchingSign);
                return true;
            }
        }

        return false;
    }


    public static void save() throws IOException {
        saveDataToFile(PAIRED_JSON, routes);
        saveDataToFile(LONELY_JSON, signs);
    }

    public static String generateId() {
        // Create a list to store all existing IDs
        List<String> existingIds = new ArrayList<>();

        // Add all IDs from signs and routes
        for (SignObject sign : signs) {
            existingIds.add(sign.getId());
        }
        for (SignObject route : routes) {
            existingIds.add(route.getId());
        }

        Random random = new Random();

        while (true) {
            // Generate a 4-digit random integer
            int id = random.nextInt(9000) + 1000;
            String stringId = String.valueOf(id);

            // Check if the generated ID is not in use
            if (!existingIds.contains(stringId)) {
                return stringId;
            }
        }
    }


    public static void removeObjectById(String id){
        List<SignObject> merged = new ArrayList<>(signs);
        merged.addAll(routes);
        SignObject signObj = null;
        for (SignObject signObject : merged) {
            if (signObject.getId().equals(id)) {
                signObj = signObject;
            }
        }
        try {
            signs.remove(signObj);
        }catch (Exception e){
            //oops
        }

        try {
            routes.remove(signObj);
        }catch (Exception e){
            //oops
        }

    }

    public static SignObject findSignObjectById(String id) {
        List<SignObject> merged = new ArrayList<>(signs);

        for (SignObject signObject : merged) {
            if (signObject.getId().equals(id)) {
                return signObject;
            }
        }
        return null; // SignObject not found
    }

    public static SignObject findSignObjectByLocation(Location location) {
        List<SignObject> merged = new ArrayList<>(signs);
        merged.addAll(routes);

        for (SignObject signObject : merged) {
            if (signObject.getLocation().equals(location)) {
                return signObject;
            }
        }
        return null; // SignObject not found
    }


    public static void load() throws IOException {
        routes = loadDataFromFile(PAIRED_JSON);
        signs = loadDataFromFile(LONELY_JSON);
    }

    private static void saveDataToFile(String fileName, List<SignObject> data) throws IOException {
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(SignObject.class, new SignObjectTypeAdapter())
                .create();
        File file = new File(DATA_FOLDER + File.separator + fileName);
        if (!file.exists()) {
            // If the file doesn't exist, create it and write an empty JSON array.
            file.getParentFile().mkdirs();
            file.createNewFile();

            try (Writer writer = new FileWriter(file)) {
                writer.write("[]"); // Write an empty JSON array.
            }

        }

        try (Writer writer = new FileWriter(file, false)) {
            gson.toJson(data, writer);
            writer.flush();
        } catch (IOException e) {
            // Handle the exception appropriately, e.g., log it
        }
    }

    private static ArrayList<SignObject> loadDataFromFile(String fileName) throws IOException {
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(SignObject.class, new SignObjectTypeAdapter())
                .create();
        File file = new File(DATA_FOLDER + File.separator + fileName);

        if (!file.exists()) {
            // If the file doesn't exist, create it and write an empty JSON array.
            file.getParentFile().mkdirs();
            file.createNewFile();

            try (Writer writer = new FileWriter(file)) {
                writer.write("[]"); // Write an empty JSON array.
            }
            return new ArrayList<>(); // Return an empty list, as there's no data to load.
        }

        try (Reader reader = new FileReader(file)) {
            SignObject[] signObjects = gson.fromJson(reader, SignObject[].class);
            return new ArrayList<>(Arrays.asList(signObjects));
        }
    }


}
