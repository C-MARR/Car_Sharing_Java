package carsharing;

import java.util.ArrayList;
import java.util.List;

public class Main {

    public static void main(String[] args) {
        ArrayList<String> options = new ArrayList<>(List.of(args));
        String dbFile = options.contains("-databaseFileName") ? args[options.indexOf("-databaseFileName") + 1] : "db";
        new CarSharingDB(dbFile);
    }

}