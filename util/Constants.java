package util;

import java.nio.file.Path;
import java.nio.file.Paths;

//*/
// Aristotle Daskaleas - 2026 : "User Recommendation Database With Bipartite Graphing" 
// This class stores all constants used throughout the program.
//*/

//*/
// This class should NOT be instantiated as it just stores static constants.
//*/

public final class Constants {
    // Main program information (Author(s), Year, Title)
    final public static String[] AUTHORS = {"Aristotle Daskaleas"};
    final public static int YEAR = 2026;
    final public static String PROG_NAME = "User Recommendation Database With Bipartite Graphing";

    // Database information
    // acquire absolute path of Main.java, then resolve the sql directory and the needed files therein
    final public static Path SCHEMA_LOC = Paths.get("").toAbsolutePath().resolve("sql").resolve("schema.sql");
    final public static Path DATABASE_PATH = Paths.get("").toAbsolutePath().resolve("sql").resolve("database.db"); // hardcoded 'database.db'
    final public static String DATABASE_LOC = "jdbc:sqlite:" + DATABASE_PATH.toAbsolutePath(); // specify JBDC driver

    final public static String[] CATEGORIES = { // Only allow these categories to be searched for
        "Food",
        "Video",
        "Audio",
        "Peripherals",
        "Office Supplies"
    };

    // Database user/product starting ID constants
    final static int START_USER_ID = 1; // Database schematics (demo data) start users at 1
    final static int START_PROD_ID = 101; // and products at 101

    // Specify how many recommendations should be issued to each user
    final static int MAX_RECOMMENDATIONS = 3;

    // Specify arbitrary username length restrictions
    final static int MIN_USERNAME_LENGTH = 3; // is 3 -> 20 good? I hope so! :3
    final static int MAX_USERNAME_LENGTH = 20;

    // Specify arbitrary product name length restrictions
    final static int MIN_PRODUCT_LENGTH = 3; 
    final static int MAX_PRODUCT_LENGTH = 35; // what exceeds 35 characters? woe betide whatever it may be

    // Define a thoughtful string for the menu
    final public static String menu = """
    OPTIONS:
        USERS:                      USER-PRODUCT RELATIONSHIPS:
          A.] Register user           D.] Register relationship
          B.] Remove user             E.] Remove relationship
          C.] View users              F.] View relationships

        PRODUCTS:                   PRODUCT-PRODUCT RELATIONSHIPS:
          G.] Register product        J.] Register relationship
          H.] Remove product          K.] Remove relationship
          I.] View products           L.] View relationships

        GRAPH:                      PROGRAM:
          M.] Generate graph          Q.] Quit
    """;

    final public static String allowedCharacters = "ABCDEFGHIJKLMQ"; // The allowed characters (must be capital) [coincides with menu]

    private Constants(){} // purely static constants, no need for instantiation
}