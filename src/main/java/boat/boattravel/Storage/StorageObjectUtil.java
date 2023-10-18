package boat.boattravel.Storage;

import boat.boattravel.BoatTravel;
import com.google.gson.Gson;
import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

public class StorageObjectUtil {

    public static ArrayList<SignObject> signs = new ArrayList<>();
    public static ArrayList<SignObject> routes = new ArrayList<>();

    public static ArrayList<SignObject> getSigns() {
        return signs;
    }

    public static ArrayList<SignObject> getRoutes() {
        return routes;
    }

    public static SignObject create(SignObject signObject) {

        Bukkit.getLogger().info(signObject.toString());

        if (!tryLink(signObject)) {
            signs.add(signObject);
        }

        try {
            save();
        } catch (IOException e) {
            //oops
        }
        return signObject;
    }

    public static SignObject find(Location location) {
        String returnable = "";

        for (SignObject route : routes) {
            if (route.getLocation().equals(location)) {
                return route;
            }
        }
        return null;
    }

    public static boolean tryLink(SignObject object) {
        String id = object.getId();

        for (int i = 0; i < signs.size(); i++) {
            if (signs.get(i).getId().equals(id)) {
                routes.add(object);
                routes.add(signs.get(i));
                signs.remove(signs.get(i));
                return true;
            }
        }
        return false;

    }

    public static boolean removeSign(String id) {
        signs.removeIf(o -> o.getId().equals(id));
        try {
            save();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return true;
    }

    public static boolean removeRoutePartly(SignObject object) {
        SignObject destination = object.getDestination();

        for (int i = 0; i < routes.size(); i++) {
            if (routes.get(i).equals(object)) {
                routes.remove(object);
                Bukkit.getLogger().info("Route origin removed successfully!");
            }
        }

        for (int i = 0; i < routes.size(); i++) {
            if (routes.get(i).equals(destination)) {
                routes.remove(destination);
                Bukkit.getLogger().info("Route destination removed successfully!");
            }
        }

        signs.add(destination);
        try {
            save();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return true;
    }


    public static void saveRoutes() throws IOException {
        Gson gson = new Gson();
        File routesFile = new File(BoatTravel.getPlugin().getDataFolder().getAbsolutePath() + "/paired.json");
        routesFile.getParentFile().mkdir();
        routesFile.createNewFile();
        Writer writer = new FileWriter(routesFile, false);
        gson.toJson(routes, writer);
        writer.flush();
        writer.close();


    }
    public static void save() throws IOException {
        saveRoutes();
        saveSigns();
    }

    public static void saveSigns() throws IOException {
        Gson gson2 = new Gson();
        File signsFile = new File(BoatTravel.getPlugin().getDataFolder().getAbsolutePath() + "/lonely.json");
        signsFile.getParentFile().mkdir();
        signsFile.createNewFile();
        Writer writerSigns = new FileWriter(signsFile, false);
        gson2.toJson(signs, writerSigns);
        writerSigns.flush();
        writerSigns.close();
    }

    public static String generateId() {
        int id = new Random().nextInt(9000) + 1000;
        Bukkit.getLogger().info("Generated id: " + id);
        String stringId = String.valueOf(id);
        ArrayList<SignObject> merged = new ArrayList<>();
        merged.addAll(routes);
        merged.addAll(signs);
        for (int i = 0; i < merged.size(); i++) {
            if(i == 0){
                id = new Random(123).nextInt(9000) + 1000;
                stringId = String.valueOf(id);
            }
            if (merged.get(i).getId().equals(stringId)) {

                i = 0;

            }
        }
        return stringId;
    }

    public static void load() throws IOException {
        Gson gson = new Gson();
        File routesFile = new File(BoatTravel.getPlugin().getDataFolder().getAbsolutePath() + "/paired.json");
        if(routesFile.exists()){
            Reader reader = new FileReader(routesFile);
            SignObject[] signObjects = gson.fromJson(reader, SignObject[].class);
            routes = new ArrayList<>(Arrays.asList(signObjects));
        }

        File signsFile = new File(BoatTravel.getPlugin().getDataFolder().getAbsolutePath() + "/lonely.json");
        if(signsFile.exists()){
            Reader readerSigns = new FileReader(signsFile);
            SignObject[] signObjectsSigns = gson.fromJson(readerSigns, SignObject[].class);
            signs = new ArrayList<>(Arrays.asList(signObjectsSigns));
        }
    }


}
