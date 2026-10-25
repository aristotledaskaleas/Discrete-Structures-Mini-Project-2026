import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.ResultSet;

public final class Database {
    private static final Path DATABASE_PATH = Path.of("database.db");
    private static final String DATABASE_LOC = "jdbc:sqlite:" + DATABASE_PATH.toAbsolutePath();

    private Database(){} // make uninstantiable

    // Return a connection to the sql database found at 'DATABASE_PATH'
    public static Connection connect() throws SQLException {
        Connection connection = DriverManager.getConnection(DATABASE_LOC);

        try (Statement statement = connection.createStatement()) {
            // this prevents existing data checks from being skipped
            statement.execute("PRAGMA foreign_keys = ON");
            return connection; // database successfully connected
        } catch (SQLException e) {
            connection.close();
            throw e;
        }
    }

    // In case we need to create a new database
    public static void initialize() throws SQLException, IOException {
       String schema;

        try (InputStream input = Database.class.getResourceAsStream("schema.sql")) {
            if (input == null) {
                throw new IOException("Could not find schema.sql.");
            }
            schema = new String(input.readAllBytes(), StandardCharsets.UTF_8);
        }

        try (Connection connection = connect();
             Statement statement = connection.createStatement()) 
        {
            for (String sql : schema.split(";")) { // execute all statements needed to initialize database
                if (!sql.isBlank()) {
                    statement.execute(sql);
                }
            }
        }
    }

    // Seed the database with sample data
    public static void seedDemoData() throws SQLException {
        try (Connection connection = connect()) {
            connection.setAutoCommit(false);

            try (Statement statement = connection.createStatement()) {
                // Add demo users
                statement.executeUpdate("""
                    INSERT OR IGNORE INTO users (user_id, username) VALUES
                    (1, 'Alice'),
                    (2, 'Bob'),
                    (3, 'Charlie')
                """);

                // Add demo products
                statement.executeUpdate("""
                    INSERT OR IGNORE INTO products (product_id, product_name, manufacturer, category) VALUES
                    (101, 'Mechanical Keyboard', 'KeyWorks', 'Peripherals'),
                    (102, 'Gaming Mouse', 'PointerTech', 'Peripherals'),
                    (103, 'Studio Headphones', 'SoundLab', 'Audio'),
                    (104, 'USB Microphone', 'SoundLab', 'Audio'),
                    (105, 'Webcam', 'VisionTech', 'Video'),
                    (106, 'DSLR Camera', 'Canon', 'Video'),
                    (107, 'French Fries', NULL, 'Food')
                """);

                // Add demo product - user relationships
                statement.executeUpdate("""
                    INSERT OR IGNORE INTO user_products (user_id, product_id) VALUES
                    (1, 101),
                    (1, 102),
                    (1, 103),
                    (2, 106),
                    (2, 104),
                    (3, 107),
                    (3, 105)
                """);

                // Add demo product - product relationships
                statement.executeUpdate("""
                    INSERT OR IGNORE INTO product_relationships (src_product_id, dst_product_id, weight) VALUES
                    (101, 102, 0.95),
                    (102, 101, 0.95),
                    (101, 103, 0.65),
                    (103, 101, 0.65),
                    (101, 104, 0.25),
                    (104, 101, 0.25),
                    (102, 103, 0.70),
                    (103, 102, 0.70),
                    (102, 104, 0.20),
                    (104, 102, 0.20),
                    (103, 104, 0.90),
                    (104, 103, 0.90),
                    (101, 105, 0.60),
                    (105, 101, 0.60),
                    (102, 105, 0.35),
                    (105, 102, 0.35),
                    (103, 105, 0.10),
                    (105, 103, 0.10),
                    (105, 106, 0.92),
                    (106, 105, 0.92),
                    (103, 105, 0.65),
                    (105, 103, 0.65)
                """);

                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                throw e;
            }
        }
    }

    // PRE: take a username string
    // POST: return true if user exists, false otherwise
    public static boolean doesUserExist(String username) throws SQLException {
        if (getUserID(username) != -1) return true; // has a userID -> automatically exists

        try (Connection connection = connect()) {
            PreparedStatement statement = connection.prepareStatement("""
                SELECT * FROM users WHERE (username) LIKE (?)
            """); // 'LIKE' makes query case-insensitive

            statement.setString(1, username);

            try (ResultSet results = statement.executeQuery()) {
                if (!results.next()) return false; // no results from query -> user not found
                return true; // query returned a result
            }
        }
    }

    // PRE: Take a valid username string
    // POST: add a user to the sql database based on username
    // false->failed, true->success
    public static boolean addUser(String username) throws SQLException {
        if (doesUserExist(username)) return false; // user already exists

        try (Connection connection = connect()) {
            connection.setAutoCommit(false);

            PreparedStatement prepStatement = connection.prepareStatement("""
                SELECT MAX(user_id) FROM users 
            """);

            int new_user_id = 0;
            try (ResultSet results = prepStatement.executeQuery()) {
                if (results.next()) new_user_id = results.getInt("MAX(user_id)") + 1;
                else new_user_id = 1;
            }

            String sql = """
                INSERT OR IGNORE INTO users (user_id, username)
                VALUES (?, ?)
            """;

            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setInt(1, new_user_id);
                statement.setString(2, username);

                if (statement.executeUpdate() != 1) {
                    connection.rollback();
                    return false;
                }
                
                connection.commit();
                return true;
            } catch (SQLException e) {
                connection.rollback();
                throw e;
            }
        }
    }

    // PRE: A valid username string
    // POST: Delete the user from the SQL database (if exists)
    // false->failed, true->success
    public static boolean removeUser(String username) throws SQLException {
        try (Connection connection = connect()) {
            connection.setAutoCommit(false); 
            if (username == null) return false;

            // prevent sql injection (hopefully)
            if (username.indexOf(";") != -1) return false;

            PreparedStatement statement = connection.prepareStatement("""
                DELETE FROM users WHERE username = ?        
            """);
            statement.setString(1, username);

            int success = statement.executeUpdate();
            if (success == 0) {
                connection.rollback();
                return false;
            }

            connection.commit();
            return true;
        }
    }

    // PRE: Product, Manufacturer [OPT.], Category
    // POST: Adds product to database false->failed, true->success
    public static boolean addProduct(String product, String manufacturer, String category) throws SQLException {
        if (getProductID(product) != -1) return false; // product already exists

        if (product == null || category == null) {
            return false;
        } if (manufacturer == null) manufacturer = "NULL";

        boolean cont = false;
        for (String c : Main.CATEGORIES) { // only allow valid categories to be added
            if (c.equals(category)) {
                cont = true; break;
            }
        }

        if (!cont) return false;

        try (Connection connection = connect()) {
            connection.setAutoCommit(false);

            PreparedStatement prepStatement = connection.prepareStatement("""
                SELECT MAX(product_id) FROM products
            """);

            int new_prod_id = 0;
            try (ResultSet results = prepStatement.executeQuery()) {
                if (results.next()) new_prod_id = results.getInt("MAX(product_id)") + 1;
                else new_prod_id = 1;
            }

            String sql = """
                INSERT OR IGNORE INTO products (product_id, product_name, manufacturer, category) VALUES
                (?, ?, ?, ?)
            """;

            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setInt(1, new_prod_id);
                statement.setString(2, product);
                
                if (manufacturer != "NULL") statement.setString(3, manufacturer);
                else statement.setNull(3, java.sql.Types.VARCHAR);

                statement.setString(4, category);

                if (statement.executeUpdate() != 1) {
                    connection.rollback();
                    return false;
                }

                connection.commit();
                return true;
            } catch (SQLException e) {
                connection.rollback();
                throw e;
            }
        }
    }

    // PRE: A valid product string
    // POST: Delete the product from the SQL database (if exists)
    // false->failed, true->success
    public static boolean removeProduct(String product) throws SQLException {
        try (Connection connection = connect()) {
            connection.setAutoCommit(false); 
            if (product == null) return false;

            // prevent sql injection (hopefully)
            if (product.indexOf(";") != -1) return false;

            PreparedStatement statement = connection.prepareStatement("""
                DELETE FROM products WHERE product_name = ?        
            """);
            statement.setString(1, product);

            int success = statement.executeUpdate();
            if (success == 0) {
                connection.rollback();
                return false;
            }

            connection.commit();
            return true;
        }
    }

    // PRE: take a valid userID and productID
    // POST: insert a new relational assertion between that user and that product
    // into the database. false->failed, true->success
    public static boolean addUserProductRelation(int user_id, int product_id) throws SQLException {
        if (user_id < 0 || product_id < 0) return false;

        try (Connection connection = connect()) {
            connection.setAutoCommit(false);

            PreparedStatement statement = connection.prepareStatement("""
                INSERT OR IGNORE INTO user_products (user_id, product_id) VALUES
                (?, ?)
            """);
            statement.setInt(1, user_id);
            statement.setInt(2, product_id);

            if (statement.executeUpdate() == 0) {
                connection.rollback();
                return false;
            }

            connection.commit();
            return true;
        }
    }

    // PRE: take a valid userID and productID
    // POST: remove any relational assertion between the user and the product
    // from the database. false->failed, true->success
    public static boolean removeUserProductRelation(int user_id, int product_id) throws SQLException {
        if (user_id < 0 || product_id < 0) return false;

        try (Connection connection = connect()) {
            connection.setAutoCommit(false);

            PreparedStatement statement = connection.prepareStatement("""
                DELETE FROM user_products WHERE (user_id, product_id) = (?, ?)
            """);

            statement.setInt(1, user_id);
            statement.setInt(2, product_id);

            if (statement.executeUpdate() == 0) {
                connection.rollback();
                return false;
            }

            connection.commit();
            return true;
        }
    }

    // PRE: take a valid userID
    // POST: remove any relational assertion associated with the user
    // from the database. false->failed, true->success
    public static boolean removeAllUserProductRelationsByUserID(int user_id) throws SQLException {
        if (user_id < 0) return false;

        try (Connection connection = connect()) {
            connection.setAutoCommit(false);

            PreparedStatement statement = connection.prepareStatement("""
                DELETE FROM user_products WHERE (user_id) = (?)
            """);

            statement.setInt(1, user_id);

            if (statement.executeUpdate() == 0) {
                connection.rollback();
                return false;
            }

            connection.commit();
            return true;
        }
    }

    // PRE: take a valid productID
    // POST: remove any relational assertion associated with the product
    // from the database. false->failed, true->success
    public static boolean removeAllUserProductRelationsByProductID(int product_id) throws SQLException {
        if (product_id < 0) return false;

        try (Connection connection = connect()) {
            connection.setAutoCommit(false);

            PreparedStatement statement = connection.prepareStatement("""
                DELETE FROM user_products WHERE (product_id) = (?)
            """);

            statement.setInt(1, product_id);

            if (statement.executeUpdate() == 0) {
                connection.rollback();
                return false;
            }

            connection.commit();
            return true;
        }
    }

    // PRE: take 2 valid product IDs and a weight
    // POST: adds a new product relational assertion with `weight` similarity,
    // where 0.0 ≤ weight ≤ 1.0. false->failed, true->success
    public static boolean addProductRelation(int src_product_id, int dst_product_id, double weight) throws SQLException {
        if (src_product_id == dst_product_id) return false;
        if (weight <= 0.0 || weight > 1.0) return false;

        try (Connection connection = connect()) {
            connection.setAutoCommit(false);

            PreparedStatement statement = connection.prepareStatement("""
                INSERT OR IGNORE INTO product_relationships (src_product_id, dst_product_id, weight) VALUES
                (?, ?, ?),
                (?, ?, ?)
            """);

            statement.setInt(1, src_product_id);
            statement.setInt(2, dst_product_id);
            statement.setDouble(3, weight);

            // invert all values (if p1->p2, then p2->p1 (p1<->p2))
            statement.setInt(4, dst_product_id);
            statement.setInt(5, src_product_id);
            statement.setDouble(6, weight);

            if (statement.executeUpdate() == 0) {
                connection.rollback();
                return false;
            }

            connection.commit();
            return true;
        }
    }

    // PRE: take 2 valid product IDs
    // POST: removes an existing product relational assertion from the database.
    // false->failed, true->success
    public static boolean removeProductRelation(int src_product_id, int dst_product_id) throws SQLException {
        if (src_product_id < 1 || dst_product_id < 1) return false;

        try (Connection connection = connect()) {
            connection.setAutoCommit(false);

            PreparedStatement statement1 = connection.prepareStatement("""
                DELETE FROM product_relationships WHERE (src_product_id, dst_product_id) = (?, ?)
            """);

            statement1.setInt(1, src_product_id);
            statement1.setInt(2, dst_product_id);

            if (statement1.executeUpdate() == 0) {
                connection.rollback();
                return false;
            }

            PreparedStatement statement2 = connection.prepareStatement("""
                DELETE FROM product_relationships WHERE (src_product_id, dst_product_id) = (?, ?)
            """);

            statement2.setInt(1, dst_product_id);
            statement2.setInt(2, src_product_id);

            if (statement2.executeUpdate() == 0) {
                connection.rollback();
                return false;
            }

            connection.commit();
            return true;
        } 
    }

    // PRE: take a valid productID
    // POST: remove any product relational assertion associated with the product
    // from the database. false->failed, true->success
    public static boolean removeAllProductRelations(int product_id) throws SQLException {
        if (product_id < 0) return false;

        try (Connection connection = connect()) {
            connection.setAutoCommit(false);

            PreparedStatement statement = connection.prepareStatement("""
                DELETE FROM product_relationships WHERE (src_product_id) = (?) OR (dst_product_id) = (?)
            """);

            statement.setInt(1, product_id);
            statement.setInt(2, product_id);

            if (statement.executeUpdate() == 0) {
                connection.rollback();
                return false;
            }

            connection.commit();
            return true;
        }
    }

    // PRE: take a valid username string
    // POST: return the userID of username in the database
    // returns -1 if user is not found or is invalid
    public static int getUserID(String username) throws SQLException {
        if (username == null) return -1;
        if (username.indexOf(";") != -1) return -1;

        try (Connection connection = connect()) {
            PreparedStatement statement = connection.prepareStatement("""
                SELECT user_id FROM users WHERE username = ?
            """);

            statement.setString(1, username);
            
            try (ResultSet results = statement.executeQuery()) {
                if (results.next()) return results.getInt("user_id");
                return -1; // return -1 if ID is null
            }
        }
    }

    // PRE: take a valid product string
    // POST: return the productID of product in the database
    // returns -1 if product is not found or is invalid
    public static int getProductID(String product) throws SQLException {
        if (product == null) return -1;
        if (product.indexOf(";") != -1) return -1;

        try (Connection connection = connect()) {
            PreparedStatement statement = connection.prepareStatement("""
                SELECT product_id FROM products WHERE product_name = ?
            """);

            statement.setString(1, product);
            
            try (ResultSet results = statement.executeQuery()) {
                if (results.next()) return results.getInt("product_id");
                return -1;
            }
        }
    }

    // PRE: take valid user ID
    // POST: return user name or null if not found
    public static String getUser(int user_id) throws SQLException {
        if (user_id < 0) return null;

        try (Connection connection = connect()) {
            PreparedStatement statement = connection.prepareStatement("""
                SELECT username FROM users WHERE (user_id) = (?)
            """);

            statement.setInt(1, user_id);

            try (ResultSet results = statement.executeQuery()) {
                if (results.next()) {
                    return results.getString("username");
                }

                return null;
            }
        }
    }

    // PRE: take valid product ID
    // POST: return product name or null if not found
    public static String getProduct(int product_id) throws SQLException {
        if (product_id < 0) return null;

        try (Connection connection = connect()) {
            PreparedStatement statement = connection.prepareStatement("""
                SELECT product_name FROM products WHERE (product_id) = (?)
            """);

            statement.setInt(1, product_id);

            try (ResultSet results = statement.executeQuery()) {
                if (results.next()) {
                    return results.getString("product_name");
                }

                return null;
            }
        }
    }

    // PRE: take valid product ID
    // POST: return list of {product name, manufacturer, category} or null if not found
    public static String[] getProductFull(int product_id) throws SQLException {
        if (product_id < 0) return null;

        try (Connection connection = connect()) {
            PreparedStatement statement = connection.prepareStatement("""
                SELECT * FROM products WHERE (product_id) = (?)
            """);

            statement.setInt(1, product_id);

            try (ResultSet results = statement.executeQuery()) {
                if (results.next()) {
                    String[] res = {
                        results.getString("product_name"), 
                        results.getString("manufacturer"), 
                        results.getString("category")
                    };
                    return res;
                }

                return null;
            }
        }
    }

    public static int getLastUserID() throws SQLException {
        try (Connection connection = connect()) {
            PreparedStatement prepStatement = connection.prepareStatement("""
                SELECT MAX(user_id) FROM users
            """);

            try (ResultSet results = prepStatement.executeQuery()) {
                if (results.next()) return results.getInt("MAX(user_id)");
                return 0;
            }
        }
    }

    public static int getLastProdID() throws SQLException {
        try (Connection connection = connect()) {
            PreparedStatement prepStatement = connection.prepareStatement("""
                SELECT MAX(product_id) FROM products
            """);

            try (ResultSet results = prepStatement.executeQuery()) {
                if (results.next()) return results.getInt("MAX(product_id)");
                return 100;
            }
        }
    }

    // PRE: take valid source and destination product ID's
    // POST: return the relational weight between those two products
    // return -1.0 if the relation is not found
    public static double getRelationWeight(int src_product_id, int dst_product_id) throws SQLException {
        if (src_product_id < 1 || dst_product_id < 1) return -1;

        try (Connection connection = connect()) {
            PreparedStatement statement = connection.prepareStatement("""
                SELECT weight FROM product_relationships WHERE (src_product_id, dst_product_id) = (?, ?)
            """);

            statement.setInt(1, src_product_id);
            statement.setInt(2, dst_product_id);

            try (ResultSet results = statement.executeQuery()) {
                if (results.next()) {
                    return results.getDouble("weight");
                }
                return -1.0;
            }
        }
    }

    public static boolean isUserProductRelated(int user_id, int product_id) throws SQLException {
        if (user_id < 0 || product_id < 0) return false;

        try (Connection connection = connect()) {
            PreparedStatement statement = connection.prepareStatement("""
                SELECT * FROM user_products WHERE (user_id, product_id) = (?, ?)
            """);

            statement.setInt(1, user_id);
            statement.setInt(2, product_id);

            try (ResultSet results = statement.executeQuery()) {
                if (!results.next()) return false; // no results -> no relation
                return true;
            }
        }
    }
}