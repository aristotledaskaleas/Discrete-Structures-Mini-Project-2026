import java.nio.file.Path;

//*/
// Aristotle Daskaleas - 2026 : "User Recommendation Database With Bipartite Graphing" 
// This class stores a single instance of a PRODUCT-PRODUCT relationship from the database.
//*/

//*/
// User(int,String) is the only valid constructor because
//   if all of these fields are not known at the time of construction, the
//   database was incorrectly accessed or the record is invalid. Additionally,
//   usernames and ID's are identifying information and should not be changed later.
//   to make a field unchangeable, it MUST be declared upfront.
//
// Omitting (some) mutators allow these records to become (partially) immutable.
// NOTE: We only want to be able to add used or recommended products
//   NAMES AND ID's SHOULD NOT BE CHANGEABLE!
//*/

public final class Constants {
    // Main program information (Author(s), Year, Title)
    final static String[] AUTHORS = {"Aristotle Daskaleas"};
    final static int YEAR = 2026;
    final static String PROG_NAME = "User Recommendation Database With Bipartite Graphing";

    // Database information
    private final static Path DATABASE_PATH = Path.of("database.db"); // hardcoded 'database.db'
    public final static String DATABASE_LOC = "jdbc:sqlite:" + DATABASE_PATH.toAbsolutePath(); // specify JBDC driver

    final static String[] CATEGORIES = { // Only allow these categories to be searched for
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
    final static int MAX_PRODUCT_LENGTH = 35; // what is exceeds 35 characters? woe betide whatever it may be

    // Define a thoughtful string for the menu
    final static String menu = """
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

    final static String allowedCharacters = "ABCDEFGHIJKLMQ"; // The allowed characters (must be capital) [based on menu]

    private Constants(){} // purely static constants, no need for instantiation
}