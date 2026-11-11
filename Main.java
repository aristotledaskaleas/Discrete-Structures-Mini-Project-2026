import java.time.ZonedDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.stream.Collectors;
import java.sql.SQLException;
import java.util.Arrays;

//*/
// "User Recommendation Database With Bipartite Graphing"
//   Research Mini Project - Discrete Structures 2026
//
// Bipartite graphing linking users to products and recommended products.
//   All user information and data is persistent in SQL database.
//
// Author(s): Aristotle Daskaleas
// Date: 2026 (October-December)
//*/

//*
// GOALS: [KEY:{-:planned,+:completed,*:in_progress}]
//  - Implement a simple algorithm for recommending products to users 
//  - Seed the database with real world data
//  - Implement a graphing method (graphical?)
//  * Implement a menu allowing for the querying and manipulation of the database 
//  + Partition code in a manageable, understandable manner
//  + Implement database querying and manipulation methods
//  + Complete Utility methods that will be used later
// */

public class Main {
     public static void main(String args[]) {
        //Utilities.testDatabaseMethods(); // Debugging

//#region SETUP
        // Print the beginning information
        Utilities.printStartInit(Constants.AUTHORS, Constants.PROG_NAME, Constants.YEAR);

        // Create variables to store information during runtime
        ArrayList<User> users = new ArrayList<User>();
        ArrayList<Product> products = new ArrayList<Product>();
        ArrayList<ProductRelation> productRelations = new ArrayList<ProductRelation>();

        // LOAD INFORMATION FROM DATABASE
        // Load all products as instances of the Product class into the array
        Utilities.populateProductList(products);

        // Load all registered users into the array list
        Utilities.populateUserList(users);

        // Load all product relationships
        Utilities.populateProductRelationList(productRelations);

        // Load all user-product relationships
        Utilities.populateUserProductRelations(users, products);

        // Print end of initialization hint
        Utilities.printEndInit();
//#endregion

//#region MAIN
        try {
            // Open a scanner for user input
            Scanner input = new Scanner(System.in);

            // When true, next loop will break to exit the program
            boolean SIGQUIT = false;

            String username = null, product = null;
            boolean confirm = false, quit = false;
            int user_id = -1, product_id = -1;

            while (true) {
                if (SIGQUIT) break;

                final char choice = Utilities.serveMenu(Constants.menu, Constants.allowedCharacters, input);
                switch (choice) {
                    // Execute choice based on user input
                    case 'A': { // Register new user
                        username = Utilities.getUserInput("Enter a username to add: ", input);

                        if (username == null) break;

                        if (!Utilities.addUser(username)) {
                            System.out.println("Failed to register user!");
                            break;
                        }

                        users.clear();
                        Utilities.populateUserList(users); // update user list
                        break;
                    }
                    case 'B': { // Remove a user
                        username = Utilities.getUserInput("Enter a username to remove: ", input);
                        
                        if (username == null) break;

                        if (!Utilities.removeUser(username)) {
                            System.out.println("Failed to remove user!");
                            break;
                        }

                        // clear users and rebuild
                        users.clear();
                        Utilities.populateUserList(users);
                        break;
                    }
                    case 'C': { // View all users [w/ opt. to search single name/id]
                        username = null;
                        user_id = -1;
                        confirm = false;
                        quit = false;
                        boolean view_all = false;
                        
                        while (!confirm) {
                            System.out.print("View [a]ll, or view by [u]ser or user_[i]d [q:cancel]? ");
                            try {
                                char ch = input.nextLine().trim().toLowerCase().charAt(0);
                                if (ch == 'a') { // view all
                                    confirm = true;
                                    view_all = true;
                                } else if (ch == 'u') { // by username
                                    username = Utilities.getUserInput("Enter a username: ", input);
                                    if (username == null) {
                                        quit = true;
                                    } confirm = true;
                                } else if (ch == 'i') { // by user_id
                                    while (true) {
                                        try {
                                            String tmp = Utilities.getUserInput("Enter a user ID: ", input);
                                            if (tmp == null) {
                                                quit = true; confirm = true; break;
                                            }

                                            user_id = Integer.parseInt(tmp.trim());
                                            if (!Utilities.validateUserID(user_id)) {
                                                continue;
                                            }

                                            confirm = true;
                                            break;
                                        } catch (NumberFormatException e) {
                                            System.out.println("User ID MUST be an integer!");
                                            continue;
                                        }
                                    }
                                } else if (ch == 'q') { // cancel
                                    System.out.println("Canceled.\n");
                                    quit = true; confirm = true;
                                } else { // neither a, u, i, nor q
                                    System.out.println("trigger re-prompt.");
                                }
                            } catch (IndexOutOfBoundsException e) {
                                System.out.println("trigger re-prompt.");
                            }
                        }

                        if (quit) break;

                        System.out.println("\nUser information:");
                        int entries = 0;

                        if (view_all) { // view all user information
                            for (User u : users) {
                                ++entries;
                                System.out.println(u);
                            }
                        } else {
                            if (user_id != -1) { // search by user-id
                                for (User u : users) {
                                    if (u.getUserID() == user_id) {
                                        ++entries;
                                        System.out.println(u);
                                    }
                                }
                            } else if (username != null) { // search by username
                                for (User u : users) {
                                    if (u.getName().toLowerCase().equals(username.toLowerCase())) {
                                        ++entries;
                                        System.out.println(u);
                                    }
                                }
                            } else {
                                System.out.println("Query error.");
                            }
                        }
                        System.out.println("END [" + entries + " entries]\n");

                        break;
                    }
                    case 'D': { // Register user-product relationship
                        confirm = false;
                        boolean isSearchName = true;
                        while (!confirm) {
                            System.out.print("Add by [n]ames or [i]ds [q:cancel]? ");
                            try {
                                char ch = input.nextLine().trim().toLowerCase().charAt(0);
                                if (ch == 'n') { // by name
                                    username = Utilities.getUserInput("Enter a username: ", input);
                                    product = Utilities.getUserInput("Enter a product name: ", input);
                                    if (username == null || product == null) {
                                        quit = true;
                                    } confirm = true;

                                } else if (ch == 'i') { // by id
                                    isSearchName = false;
                                    while (true) {
                                        try {
                                            String tmp1 = Utilities.getUserInput("Enter a user ID: ", input);
                                            if (tmp1 == null) {
                                                quit = true; confirm = true; break;
                                            }

                                            user_id = Integer.parseInt(tmp1.trim());
                                            if (!Utilities.validateUserID(user_id)) {
                                                continue;
                                            }

                                            String tmp2 = Utilities.getUserInput("Enter a product ID: ", input);
                                            if (tmp2 == null) {
                                                quit = true; confirm = true; break;
                                            }

                                            product_id = Integer.parseInt(tmp2.trim());
                                            if (!Utilities.validateProductID(product_id)) {
                                                continue;
                                            }

                                            confirm = true;
                                            break;
                                        } catch (NumberFormatException e) {
                                            System.out.println("IDs MUST be an integers!");
                                            continue;
                                        }
                                    }
                                } else if (ch == 'q') { // cancel
                                    System.out.println("Canceled.\n");
                                    quit = true; confirm = true;
                                } else { // neither n, i, nor q
                                    System.out.println("trigger re-prompt.");
                                }
                            } catch (IndexOutOfBoundsException e) {
                                System.out.println("trigger re-prompt.");
                            }
                        }

                        if (quit) break;

                        if (isSearchName) {
                            user_id = Utilities.getUserID(username);
                            if (user_id < 0) {
                                System.out.println("Bad username.");
                                break;
                            }

                            product_id = Utilities.getProductID(product);
                            if (product_id < 0) {
                                System.out.println("Bad product name.");
                                break;
                            }
                        }

                        if (!Utilities.addUserProductRelation(user_id, product_id)) {
                            System.out.println("Failed to register user-product relationship.");
                            break;
                        }

                        for (User u : users) { // update user's used products
                            if (u.getUserID() == user_id) {
                                for (Product p : products) {
                                    if (p.product_id() == product_id) {
                                        if (u.isUsingProduct(p)) {
                                            System.out.println("User is already using product.");
                                            break;
                                        }

                                        u.addUsedProduct(p);
                                        //System.out.println("Registered user-product relationship.");
                                        break;
                                    }
                                }
                                break;
                            }
                        }
                        break;
                    }
                    case 'E': { // Remove user-product r'shp
                        confirm = false;
                        boolean isSearchName = true;
                        while (!confirm) {
                            System.out.print("Remove by [n]ames or [i]ds [q:cancel]? ");
                            try {
                                char ch = input.nextLine().trim().toLowerCase().charAt(0);
                                if (ch == 'n') { // by name
                                    username = Utilities.getUserInput("Enter a username: ", input);
                                    product = Utilities.getUserInput("Enter a product name: ", input);
                                    if (username == null || product == null) {
                                        quit = true;
                                    } confirm = true;

                                } else if (ch == 'i') { // by id
                                    isSearchName = false;
                                    while (true) {
                                        try {
                                            String tmp1 = Utilities.getUserInput("Enter a user ID: ", input);
                                            if (tmp1 == null) {
                                                quit = true; confirm = true; break;
                                            }

                                            user_id = Integer.parseInt(tmp1.trim());
                                            if (!Utilities.validateUserID(user_id)) {
                                                continue;
                                            }

                                            String tmp2 = Utilities.getUserInput("Enter a product ID: ", input);
                                            if (tmp2 == null) {
                                                quit = true; confirm = true; break;
                                            }

                                            product_id = Integer.parseInt(tmp2.trim());
                                            if (!Utilities.validateProductID(product_id)) {
                                                continue;
                                            }

                                            confirm = true;
                                            break;
                                        } catch (NumberFormatException e) {
                                            System.out.println("IDs MUST be an integers!");
                                            continue;
                                        }
                                    }
                                } else if (ch == 'q') { // cancel
                                    System.out.println("Canceled.\n");
                                    quit = true; confirm = true;
                                } else { // neither n, i, nor q
                                    System.out.println("trigger re-prompt.");
                                }
                            } catch (IndexOutOfBoundsException e) {
                                System.out.println("trigger re-prompt.");
                            }
                        }

                        if (quit) break;

                        if (isSearchName) {
                            user_id = Utilities.getUserID(username);
                            if (user_id < 0) {
                                System.out.println("Bad username.");
                                break;
                            }

                            product_id = Utilities.getProductID(product);
                            if (product_id < 0) {
                                System.out.println("Bad product name.");
                                break;
                            }
                        }

                        if (!Utilities.removeUserProductRelation(user_id, product_id)) {
                            System.out.println("Failed to remove user-product relationship.");
                            break;
                        }

                        for (User u : users) { // remove from user's used products
                            if (u.getUserID() == user_id) {
                                for (Product p : products) {
                                    if (p.product_id() == product_id) {
                                        u.removeUsedProduct(p);
                                        break;
                                    }
                                }
                                break;
                            }
                        }
                        break;
                    }
                    case 'F': { // View user-product r'shps [w/ opt. to search single name/product/id]
                        username = null;
                        product = null;
                        user_id = -1;
                        product_id = -1;
                        confirm = false;
                        quit = false;
                        boolean view_all = false;
                        boolean isSearchModeUser = true;
                        boolean isSearchModeID = false;
                        
                        while (!confirm) {
                            System.out.print("View [a]ll, or search by [u]ser or [p]roduct [q:cancel]? ");
                            try {
                                char ch = input.nextLine().trim().toLowerCase().charAt(0);
                                if (ch == 'a') { // view all
                                    confirm = true;
                                    view_all = true;
                                } else if (ch == 'u') { // by user
                                    confirm = true;
                                } else if (ch == 'p') { // by product
                                    isSearchModeUser = false;
                                    confirm = true;
                                } else if (ch == 'q') { // cancel
                                    System.out.println("Canceled.\n");
                                    quit = true; confirm = true;
                                } else { // neither a, u, p, nor q
                                    System.out.println("trigger re-prompt.");
                                }
                            } catch (IndexOutOfBoundsException e) {
                                System.out.println("trigger re-prompt.");
                            }
                        }
                        if (quit) break;

                        confirm = false;
                        while (!view_all && !confirm) {
                            System.out.print("View by [i]d or [n]ame [q:cancel]? ");
                            try {
                                char ch = input.nextLine().trim().toLowerCase().charAt(0);
                                if (ch == 'i') { // id
                                    confirm = true;
                                    isSearchModeID = true;
                                } else if (ch == 'n') { // name
                                    confirm = true;
                                } else if (ch == 'q') { // cancel
                                    System.out.println("Canceled.\n");
                                    quit = true; confirm = true;
                                } else { // neither i, n, nor q
                                    System.out.println("trigger re-prompt.");
                                }
                            } catch (IndexOutOfBoundsException e) {
                                System.out.println("trigger re-prompt.");
                            }
                        }
                        if (quit) break;

                        if (!view_all && isSearchModeID) {
                            while (true) {
                                try {
                                    String tmp = Utilities.getUserInput("Enter a " + (isSearchModeUser ? "user" : "product") + " ID: ", input);
                                    if (tmp == null || tmp.trim().equalsIgnoreCase("q")) {
                                        quit = true; break;
                                    }

                                    if (isSearchModeUser) {
                                        user_id = Integer.parseInt(tmp);
                                        if (!Utilities.validateUserID(user_id)) {
                                            System.out.println("User ID is invalid.");
                                            continue;
                                        }
                                    } else {
                                        product_id = Integer.parseInt(tmp);
                                        if (!Utilities.validateProductID(product_id)) {
                                            System.out.println("Product ID is invalid.");
                                            continue;
                                        }
                                    }

                                    break;
                                } catch (NumberFormatException e) {
                                    System.out.println("ID must be numerical, try again.");
                                    continue;
                                }
                            }
                        } else {
                            if (!view_all) {
                                if (isSearchModeUser) {
                                    while (true) {
                                        username = Utilities.getUserInput("Enter a username: ", input);
                                        if (username == null || username.trim().equalsIgnoreCase("q")) {
                                            quit = true; break;
                                        }

                                        user_id = Utilities.getUserID(username);
                                        if (user_id < 0) {
                                            System.out.println("Invalid username!");
                                            continue;
                                        }

                                        break;
                                    }
                                } else {
                                    while (true) {
                                        product = Utilities.getUserInput("Enter a product name: ", input);
                                        if (product == null || product.trim().equalsIgnoreCase("q")) {
                                            quit = true; break;
                                        }

                                        product_id = Utilities.getProductID(product);
                                        if (product_id < 0) {
                                            System.out.println("Invalid product name!");
                                            continue;
                                        }

                                        break;
                                    }
                                }
                            }
                        }
                        if (quit) break;

                        int entries = 0;

                        if (view_all) { // view all user information
                            boolean cancel = false;
                            boolean isUserSearch = false;
                            while (true) {
                                String selection = Utilities.getUserInput("View by [p]roducts or [u]sers: ", input);
                                if (selection == null) {
                                    cancel = true; break;
                                }
                                switch (selection.trim().toLowerCase()) {
                                    case "p" -> {}
                                    case "u" -> { isUserSearch = true; }
                                    case "q" -> { cancel = true; }
                                    default -> {
                                        System.out.println("Bad selection [MUST be in 'PUQ'].\n");
                                        continue;
                                    }
                                }
                                
                                break;
                            }
                            if (cancel) break;

                            System.out.println("\nUser-Product Relationships:");

                            if (isUserSearch) {
                                for (User u : users) {
                                    ++entries;
                                    String csv_used_products = String.join(", ", u.getUsedProducts().stream().map(Product::name).collect(Collectors.toList()));
                                    if (csv_used_products == null || csv_used_products.length() == 0) {
                                        csv_used_products = "NONE";
                                    }
                                    System.out.printf(
                                        """
                                        {
                                            username: %s,
                                            id: %d,
                                            used_products:
                                            {
                                              %s
                                            }
                                        }
                                        """, 
                                        u.getName(),
                                        u.getUserID(),
                                        csv_used_products
                                    );
                                }
                            } else { // product search
                                for (Product p : products) {
                                    ++entries;

                                    // get all usernames of people using the product (comma-separated)
                                    String csv_user_relations = String.join(", ", users.stream().filter(u -> u.isUsingProduct(p)).map(User::getName).collect(Collectors.toList()));
                                    if (csv_user_relations == null || csv_user_relations.length() == 0) {
                                        csv_user_relations = "NONE";
                                    }

                                    System.out.printf(
                                        """
                                        {
                                            product_name: %s,
                                            id: %d,
                                            related_users:
                                            {
                                              %s
                                            }
                                        }
                                        """, 
                                        p.name(),
                                        p.product_id(),
                                        csv_user_relations
                                    );
                                }
                            }
                        } else {
                            System.out.println("\nUser-Product Relationships:");

                            if (isSearchModeUser) { // search by user
                                for (User u : users) {
                                    if (u.getUserID() == user_id) {
                                        ++entries;
                                        String csv_used_products = String.join(", ", u.getUsedProducts().stream().map(Product::name).collect(Collectors.toList()));
                                        if (csv_used_products == null || csv_used_products.length() == 0) {
                                            csv_used_products = "NONE";
                                        }

                                        System.out.printf(
                                            """
                                            {
                                                username: %s,
                                                id: %d,
                                                used_products:
                                                {
                                                  %s
                                                }
                                            }
                                            """, 
                                            u.getName(),
                                            u.getUserID(),
                                            csv_used_products
                                        );
                                    }
                                }
                            } else if (!isSearchModeUser) { // search by product
                                Product target = null;
                                for (Product p : products) { // get exact product
                                    if (p.product_id() == product_id) {
                                        target = p; break;
                                    }
                                }
                                
                                ArrayList<User> matching = new ArrayList<>();
                                for (User u : users) {
                                    if (u.isUsingProduct(target)) {
                                        ++entries;
                                        matching.add(u);
                                    }
                                }

                                if (matching.size() == 0) {
                                    System.out.println("No results were found.");
                                    break;
                                }

                                String csv_related_users = String.join(", ", matching.stream().map(User::getName).collect(Collectors.toList()));
                                if (csv_related_users == null || csv_related_users.length() == 0) {
                                    csv_related_users = "NONE";
                                }

                                System.out.printf(
                                    """
                                    {
                                        Product: %s,
                                        ID: %d,
                                        related_users:
                                        {
                                          %s
                                        }
                                    }
                                    """, 
                                    target.name(),
                                    target.product_id(),
                                    csv_related_users
                                );

                            } else {
                                System.out.println("Query error.");
                            }
                        }
                        System.out.println("END [" + entries + " entries]\n");

                        break;
                    }
                    case 'G': { // Register product
                        product = Utilities.getUserInput("Enter a product name to add: ", input);
                        if (product == null) break;

                        try {
                            if (Database.doesProductExist(product)) {
                                System.out.println("Error: Product already exists!\n");
                                break;
                            }
                        } catch (SQLException e) {}

                        String manufacturer = null;
                        boolean cancel = false;
                        while (true) {
                            System.out.print("Enter a manufacturer (y:yes, n:leave null, q:cancel)? ");
                            String ch = input.nextLine().toLowerCase();
                            System.out.println();

                            if (ch == null || ch.length() == 0) {
                                System.out.println("Selection cannot be empty.");
                                continue;
                            }

                            if (ch.charAt(0) == 'y') {
                                manufacturer = Utilities.getUserInput("Enter a manufacturer: ", input);
                                if (manufacturer == null) { cancel = true; }
                                if (manufacturer.length() == 0) { manufacturer = null; } // make blank string same as null, but do not quit
                                break;
                            } 
                            else if (ch.charAt(0) == 'n') { break; }
                            else if (ch.charAt(0) == 'q') { cancel = true; break; }
                            else { System.out.println("Bad selection."); continue; }
                        }
                        if (cancel) break;

                        String category = null;
                        cancel = false;
                        int invalid_counter = 3;

                        // while category is invalid, keep prompting user
                        do {
                            if (invalid_counter >= 3) { // hint valid categories
                                System.out.println(
                                    "Available Categories:\n" +
                                    String.join("\n", Constants.CATEGORIES)
                                );
                                invalid_counter = 0;
                            }

                            category = Utilities.getUserInput("Enter a (valid) category: ", input);
                            if (category == null) break;

                            // base case: quit
                            if (category.equalsIgnoreCase("q")) {
                                cancel = true; break;
                            }

                            // base case: valid category
                            if (Arrays.stream(Constants.CATEGORIES).anyMatch(category::equalsIgnoreCase)) {
                                break;
                            }

                            // invalid
                            ++invalid_counter;
                            System.out.println("That is NOT a valid category, try again.");
                        } while (true);

                        if (cancel || category == null) break;

                    
                        if (!Utilities.addProduct(product, manufacturer, category)) {
                            System.out.println("Failed to register product!");
                            break;
                        }

                        products.clear();
                        Utilities.populateProductList(products); // update product list

                        break;
                    }
                    case 'H': { // Remove product
                        product = Utilities.getUserInput("Enter a product name to remove: ", input);
                        
                        if (product == null) break;

                        if (!Utilities.removeProduct(product)) {
                            System.out.println("Failed to remove product!");
                            break;
                        }

                        // clear products and product relations and rebuild
                        products.clear();
                        Utilities.populateProductList(products);

                        productRelations.clear();
                        Utilities.populateProductRelationList(productRelations);

                        Utilities.populateUserProductRelations(users, products);

                        break;
                    }
                    case 'I': { // View products [ w/ opt. to search single product/id]
                        product = null;
                        product_id = -1;
                        confirm = false;
                        quit = false;
                        boolean view_all = false;
                        
                        while (!confirm) {
                            System.out.print("View [a]ll, or view by [p]roduct_name or product_[i]d [q:cancel]? ");
                            try {
                                char ch = input.nextLine().trim().toLowerCase().charAt(0);
                                if (ch == 'a') { // view all
                                    confirm = true;
                                    view_all = true;
                                } else if (ch == 'p') { // by product name
                                    product = Utilities.getUserInput("Enter a product name: ", input);
                                    if (product == null) {
                                        quit = true;
                                    } confirm = true;
                                } else if (ch == 'i') { // by user_id
                                    while (true) {
                                        try {
                                            String tmp = Utilities.getUserInput("Enter a product ID: ", input);
                                            if (tmp == null) {
                                                quit = true; confirm = true; break;
                                            }

                                            product_id = Integer.parseInt(tmp.trim());
                                            if (!Utilities.validateProductID(product_id)) {
                                                continue;
                                            }

                                            confirm = true;
                                            break;
                                        } catch (NumberFormatException e) {
                                            System.out.println("Product ID MUST be an integer!");
                                            continue;
                                        }
                                    }
                                } else if (ch == 'q') { // cancel
                                    System.out.println("Canceled.\n");
                                    quit = true; confirm = true;
                                } else { // neither a, p, i, nor q
                                    System.out.println("trigger re-prompt.");
                                }
                            } catch (IndexOutOfBoundsException e) {
                                System.out.println("trigger re-prompt.");
                            }
                        }

                        if (quit) break;

                        System.out.println("\nProduct information:");
                        int entries = 0;

                        if (view_all) { // view all product information
                            for (Product p : products) {
                                ++entries;
                                System.out.println(p);
                            }
                        } else {
                            if (product_id != -1) { // search by product-id
                                for (Product p : products) {
                                    if (p.product_id() == product_id) {
                                        ++entries;
                                        System.out.println(p);
                                    }
                                }
                            } else if (product != null) { // search by product name
                                for (Product p : products) {
                                    if (p.name().equalsIgnoreCase(product.toLowerCase())) {
                                        ++entries;
                                        System.out.println(p);
                                    }
                                }
                            } else {
                                System.out.println("Query error.");
                            }
                        }
                        System.out.println("END [" + entries + " entries]\n");

                        break;
                    }
                    case 'J': { // Register product-product relationship
                        break;
                    }
                    case 'K': { // Remove product-product r'shp
                        break;
                    }
                    case 'L': { // View prod-prod r'shps [w/ opt. to search single product/id]
                        break;
                    }
                    case 'M': { // Generate graph
                        System.out.println("Generating graph...");
                        Utilities.generateGraph(users);
                        System.out.println("Done!\n");
                        break;
                    }
                    case 'Q': { // quit
                        System.out.println("Quitting...\n");
                        SIGQUIT = true;
                        break;
                    }
                    default: { // an internal error
                        System.out.println("Bad character: " + input + "\n");
                        System.out.println("Quitting...\n");
                        SIGQUIT = true;
                        break;
                    }
                }
            }

        } catch (Exception e) {
            System.out.println("Program encountered an error: " + e.getMessage());
        } finally {
            String date = (ZonedDateTime.now(ZoneId.of("America/Chicago"))).format(DateTimeFormatter.ofPattern("EEEE, MMMM d, h:mm:ss a"));
            System.out.println("Program finished executing on " + date + " CST.");
        }
//#endregion
    }
}