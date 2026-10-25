import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;

public class Utilities {
    final static int START_USER_ID = 1;
    final static int START_PROD_ID = 101;

//#region UTILITY
    // Utility functions for populating database information into class lists
    public static void populateProductList(ArrayList<Product> products) {
        int other = 0;
        try {
            int counter = START_PROD_ID;
            while (counter <= Database.getLastProdID()) { // from start to finish, add all products
                // Name, Manufacturer, Category
                String[] prod_data = Database.getProductFull(counter);

                if (prod_data.length != 3) {
                    ++counter; continue;
                }
                products.add(new Product(counter, prod_data[0], prod_data[1], prod_data[2]));

                ++counter; ++other;
            }
        } catch (SQLException e) { }

        System.out.println("Populated " + other + " product entries into program.");
    }

    public static void populateUserList(ArrayList<User> users) {
        int other = 0;
        try {
            int counter = START_USER_ID;
            while (counter <= Database.getLastUserID()) { // from start to finish, add all users
                String name = Database.getUser(counter);

                if (name == null) {
                    ++counter; continue;
                }
                users.add(new User(counter, name));
                
                ++counter; ++other;
            }
        } catch (SQLException e) { }

        System.out.println("Populated " + other + " user entries into program.");
    }

    public static void populateProductRelationList(ArrayList<ProductRelation> p_relations) {
        int other = 0;
        try {
            for (int i = START_PROD_ID; i < Database.getLastProdID(); i++) {
                for (int j = START_PROD_ID; j < Database.getLastProdID(); j++) {
                    final double weight = Database.getRelationWeight(i, j);
                    if (weight < 0.0) continue;
                    p_relations.add(new ProductRelation(i, j, weight));
                    ++other;
                }
            }
        } catch (SQLException e) {}

        System.out.println("Populated " + other + " product relationships into program.");
    }

    public static void populateUserProductRelations(ArrayList<User> users, ArrayList<Product> products) {
        int other = 0;
        try {
            for (User u : users) {
                final int uid = u.getUserID();
                for (int i = START_PROD_ID; i < Database.getLastProdID(); i++) {
                    if (Database.isUserProductRelated(uid, i)) {
                        Product p = null;
                        for (Product prod : products) {
                            if (prod.product_id() == i) {
                                p = prod;
                                break;
                            }
                        }
                        u.addUsedProduct(p);
                        ++other;
                    }
                }
            }
        } catch (SQLException e) {}

        System.out.println("Populated " + other + " user-product relationships into program.");
    }

    public static void populateUserProductRecommendations(ArrayList<User> users, ArrayList<ProductRelation> relations) {
        for (User u : users) {
            if (u.getUsedProducts().isEmpty()) {
                // add 3 most popular recommendations for those with no data
            } else {
                // get 3 most similar recommendations based on used products
            }
        }
    }
//#endregion

//#region WRAPPER METHODS FOR Database.java
    public static void addUser(String name) {
        try {
            if (Database.addUser(name)) {
                System.out.println("Registered '" + name + "' as a user.");
            } else {
                System.out.println("Failed to register '" + name + "' as a user.");
            }
        } catch (SQLException e) {
            System.out.println("A database error occurred: " + e.getMessage());
        }
    }

    public static void removeUser(String name) {
        try {
            if (Database.removeUser(name)) {
                System.out.println("Deleted user '" + name + "'.");
            } else {
                System.out.println("Failed to delete user '" + name + "'.");
            }
        } catch (SQLException e) {
            System.out.println("A database error occurred: " + e.getMessage());
        }
    }

    public static void addProduct(String product, String manufacturer, String category) {
        try {
            if (Database.addProduct(product, manufacturer, category)) {
                System.out.println("Registered '" + String.join(", ",product,manufacturer,category) + "' as a new product.");
            } else {
                System.out.println("Failed to register '" + String.join(", ",product,manufacturer,category) + "' as a new product.");
                
                // Ensure a proper category was supplied
                boolean found = false;
                for (String c : Main.CATEGORIES) {
                    if (c.equals(category)) {
                        found = true; break;
                    }
                }
                if (!found) { // hint to users the valid categories
                    System.out.println("Try to use a proper category:");
                    for (String c : Main.CATEGORIES) {
                        System.out.println("  "+c);
                    } System.out.println();
                }
            }
        } catch (SQLException e) {
            System.out.println("A database error occurred: " + e.getMessage());
        }
    }

    public static void removeProduct(String product) {
        try {
            if (Database.removeProduct(product)) {
                System.out.println("Deleted product '" + product + "'.");
            } else {
                System.out.println("Failed to delete product '" + product + "'.");
            }
        } catch (SQLException e) {
            System.out.println("A database error occurred: " + e.getMessage());
        }
    }

    public static void addUserProductRelation(int user_id, int product_id) {
        try {
            if (Database.addUserProductRelation(user_id, product_id)) {
                System.out.println("Added user-product relation '" + user_id+"->"+product_id + "'.");
            } else {
                System.out.println("Failed to add user-product relation '" + user_id+"->"+product_id + "'.");
            }
        } catch (SQLException e) {
            System.out.println("A database error occurred: " + e.getMessage());
        }
    }

    public static void removeUserProductRelation(int user_id, int product_id) {
        try {
            if (Database.removeUserProductRelation(user_id, product_id)) {
                System.out.println("Removed user-product relation '" + user_id+"->"+product_id + "'.");
            } else {
                System.out.println("Failed to remove user-product relation '" + user_id+"->"+product_id + "'.");
            }
        } catch (SQLException e) {
            System.out.println("A database error occurred: " + e.getMessage());
        }
    }

    public static void addProductRelation(int src_product_id, int dst_product_id, double weight) {
        try {
            if (Database.addProductRelation(src_product_id, dst_product_id, weight)) {
                System.out.println("Added product relation '" + src_product_id+"->"+dst_product_id+":"+weight + "'.");
            } else {
                System.out.println("Failed to add product relation '" + src_product_id+"->"+dst_product_id+":"+weight + "'.");

                if (src_product_id==dst_product_id) {
                    System.out.println("Both product ID's MUST be dissimilar.");
                }
                if (weight <= 0.0 || weight > 1.0) {
                    System.out.println("Weight must be greater than 0.0 and less than 1.0.");
                }
            }
        } catch (SQLException e) {
            System.out.println("A database error occurred: " + e.getMessage());
        }
    }

    public static void removeProductRelation(int src_product_id, int dst_product_id) {
        try {
            if (Database.removeProductRelation(src_product_id, dst_product_id)) {
                System.out.println("Removed user-product relation '" + src_product_id+"->"+dst_product_id + "'.");
            } else {
                System.out.println("Failed to remove user-product relation '" + src_product_id+"->"+dst_product_id + "'.");
            }
        } catch (SQLException e) {
            System.out.println("A database error occurred: " + e.getMessage());
        }    
    }

    public static int getUserID(String username) {
        try {
            final int id = Database.getUserID(username);
            if (id == -1) {
                System.out.println("User '" + username + "' does not exist.");
            }

            return id;
        } catch (SQLException e) {
            System.out.println("A database error occurred: " + e.getMessage());
            return -1;
        }

    }

    public static int getProductID(String product) {
        try {
            final int id = Database.getProductID(product);
            if (id == -1) {
                System.out.println("Product '" + product + "' does not exist.");
            }

            return id;
        } catch (SQLException e) {
            System.out.println("A database error occurred: " + e.getMessage());
            return -1;
        }
    }

    public static double getRelationWeight(int src_product_id, int dst_product_id) {
        try {
            final double weight = Database.getRelationWeight(src_product_id, dst_product_id);
            if (weight < 0.0) {
                System.out.println("Product relation '" + src_product_id+"->"+dst_product_id + "' does not exist.");
            }

            return weight;
        } catch (SQLException e) {
            System.out.println("A database error occurred: " + e.getMessage());
            return -1;
        }
    }

    public static String getUser(int user_id) {
        try {
            final String user = Database.getUser(user_id);
            if (user == null) {
                System.out.println("No user with ID '" + user_id + "' exists.");
            }

            return user;
        } catch (SQLException e) {
            System.out.println("A database error occurred: " + e.getMessage());
            return null;
        }
    }

    public static String getProduct(int product_id) {
        try {
            final String product = Database.getProduct(product_id);
            if (product == null) {
                System.out.println("No product with ID '" + product_id + "' exists.");
            }

            return product;
        } catch (SQLException e) {
            System.out.println("A database error occurred: " + e.getMessage());
            return null;
        }
    }

//#endregion
//#region DEBUG
    // TEST ALL METHODS DEFINED IN Database.java
    public static void testDatabaseMethods() {
        // test initialization
        try {
            Database.initialize();
            System.out.println("Database was successfully initialized!");
        } catch (SQLException e) {
            System.out.println("A SQL exception occurred: " + e.getMessage());
            return;
        } catch (IOException e) {
            System.out.println("An IO exception occurred: " + e.getMessage());
            return;
        }

        // attempt to seed the initialized database
        try {
            Database.seedDemoData();
            System.out.println("Database was successfully seeded with demo data!");
        } catch (SQLException e) {
            System.out.println("A SQL exception occurred: " + e.getMessage());
            return;
        }
        
        // Attempt to add users to the database
        try {
            String[] names = {"Alicia", "Thomas"};
            for (String name : names) {
                if (Database.addUser(name)) {
                    System.out.println("Added " + name + " to database.");
                } else {
                    System.out.println("Error adding " + name + " to database.");
                }
            }
        } catch (SQLException e) {
            System.out.println("A SQL exception occurred: " + e.getMessage());
            return;
        }

        // Attempt to remove a user from the database
        try {   
            // delete thomas's user data pursuant to california data removal requests
            final String CA_data_removal_requestor = "Thomas";

            if (Database.removeUser(CA_data_removal_requestor)) {
                System.out.println("Removed " + CA_data_removal_requestor + " from database.");
            } 
            else { // we're getting sued
                System.out.println("Error removing " + CA_data_removal_requestor + " from database.");
            }
        } catch (SQLException e) {
            System.out.println("A SQL exception occurred: " + e.getMessage());
            return;
        }

        // Attempt to add products to the database
        try {   
            final String[] prod1 = {"Hamburger", null, "Food"};
            final String[] prod2 = {"Shredder", "Home-EC", "Office Supplies"};

            if (Database.addProduct(prod1[0], prod1[1], prod1[2])) {
                System.out.println("Added product: '" + String.join(", ", prod1) + "' to the database.");
            } else {
                System.out.println("Failed to add product: '" + String.join(", ", prod1) + "' to the database.");
            }

            if (Database.addProduct(prod2[0], prod2[1], prod2[2])) {
                System.out.println("Added product: '" + String.join(", ", prod2) + "' to the database.");
            } else {
                System.out.println("Failed to add product: '" + String.join(", ", prod2) + "' to the database.");
            }

        } catch (SQLException e) {
            System.out.println("A SQL exception occurred: " + e.getMessage());
            return;
        }

        // Attempt to remove a product from the database
        try {   
            final String prod = "Shredder";
            if (Database.removeProduct(prod)) {
                System.out.println("Successfully deleted product: " + prod);
            } else {
                System.out.println("Failed to delete product: " + prod);
            }

            // this should fail because it's already removed
            if (Database.removeProduct(prod)) {
                System.out.println("Successfully deleted product: " + prod);
            } else {
                System.out.println("Failed to delete product: " + prod);
            }

        } catch (SQLException e) {
            System.out.println("A SQL exception occurred: " + e.getMessage());
            return;
        }

        // Attempt to add user-product relations to the database
        try {   
            final int uid1 = 2, uid2 = 3;
            final int pid1 = 105, pid2 = 101;

            if (Database.addUserProductRelation(uid1, pid1)) {
                System.out.println("Added user-product relation: " + uid1 + "->" + pid1);
            } else {
                System.out.println("Failed to add user-product relation: " + uid1 + "->" + pid1);
            }

            if (Database.addUserProductRelation(uid2, pid2)) {
                System.out.println("Added user-product relation: " + uid2 + "->" + pid2);
            } else {
                System.out.println("Failed to add user-product relation: " + uid2 + "->" + pid2);
            }

            // this should fail
            if (Database.addUserProductRelation(uid2, pid2)) {
                System.out.println("Added user-product relation: " + uid2 + "->" + pid2);
            } else {
                System.out.println("Failed to add user-product relation: " + uid2 + "->" + pid2);
            }

        } catch (SQLException e) {
            System.out.println("A SQL exception occurred: " + e.getMessage());
            return;
        }

        // Attempt to remove a user-product from the database
        try {   
            final int uid = 3, pid = 101;
            if (Database.removeUserProductRelation(uid, pid)) {
                System.out.println("Removed user-product relation: " + uid + "->" + pid);
            } else {
                System.out.println("Failed to remove user-product relation: " + uid + "->" + pid);
            }
        } catch (SQLException e) {
            System.out.println("A SQL exception occurred: " + e.getMessage());
            return;
        }

        // Attempt to add product relations to the database
        try {   
            final int src1 = 105, dst1 = 103;
            final int src2 = 107, dst2 = 108;
            final double weight1 = 0.42, weight2 = 0.91;

            if (Database.addProductRelation(src1, dst1, weight1)) {
                System.out.println("Added product relation: " + src1+"->"+dst1+":"+weight1);
            } else {
                System.out.println("Failed to add product relation: " + src1+"->"+dst1+":"+weight1);
            }

            if (Database.addProductRelation(src2, dst2, weight2)) {
                System.out.println("Added product relation: " + src2+"->"+dst2+":"+weight2);
            } else {
                System.out.println("Failed to add product relation: " + src2+"->"+dst2+":"+weight2);
            }
        } catch (SQLException e) {
            System.out.println("A SQL exception occurred: " + e.getMessage());
            return;
        }

        // Attempt to remove a product relation from the database
        try {   
            final int src = 105, dst = 103;

            if (Database.removeProductRelation(src, dst)) {
                System.out.println("Successfully removed product relation: " + src+"->"+dst);
            } else {
                System.out.println("Failed to remove product relation: " + src+"->"+dst);
            }
        } catch (SQLException e) {
            System.out.println("A SQL exception occurred: " + e.getMessage());
            return;
        }

        // Attempt to get some userIDs from the database
        try {   
            final String[] users = {"Alice", "Charlie"};
            for (String user : users) {
                final int id = Database.getUserID(user);

                if (id == -1) {
                    System.out.println("Username: " + user + ", ID FAILED");
                } else {
                    System.out.println("Username: " + user + ", ID: " + id);
                }
            }
        } catch (SQLException e) {
            System.out.println("A SQL exception occurred: " + e.getMessage());
            return;
        }

        // Attempt to get some productIDs from the database
        try {   
            final String[] products = {"Studio Headphones", "Webcam"};
            for (String product : products) {
                final int id = Database.getProductID(product);
                
                if (id == -1) {
                    System.out.println("Product Name: " + product + ", ID FAILED");
                } else {
                    System.out.println("Product Name: " + product + ", ID: " + id);
                }
            }
        } catch (SQLException e) {
            System.out.println("A SQL exception occurred: " + e.getMessage());
            return;
        }

        // Attempt to retrieve the weight of various product relations in the database
        try {   
            final int[] src1 = {107, 108};
            final int[] src2 = {108, 107};
            final int[] src3 = {101, 105};
            final int[][] visit = {src1, src2, src3};
            
            for (int[] v : visit) {
                final double weight = Database.getRelationWeight(v[0], v[1]);
                if (weight < 0.0) {
                    System.out.println("Failed to get relational weight for: " + v[0]+"->"+v[1]);
                } else {
                    System.out.println("Relational weight:" + v[0]+"->"+v[1]+":"+weight);
                }
            }

        } catch (SQLException e) {
            System.out.println("A SQL exception occurred: " + e.getMessage());
            return;
        }
    }
//#endregion
}