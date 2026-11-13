import java.time.ZonedDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;
import java.util.Arrays;
import util.*;
import datastructures.*;

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
//  + Implement a menu allowing for the querying and manipulation of the database 
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

        // Initialize database (if needed)
        // will return false if neither database.db nor schema.sql exist OR it cannot initialize the database
        if (!Utilities.initializeDatabase()) {
            return; // cannot run program without a database
        }

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

            // When true, next loop will break to exit the program (i.e., 'q' is selected)
            boolean SIGQUIT = false;

            while (true) {
                if (SIGQUIT) break;

                final char choice = Utilities.serveMenu(Constants.menu, Constants.allowedCharacters, input);
                switch (choice) {
                    // Execute choice based on user input
                    case 'A': { // Register new user
                        String username = Utilities.getUserInput("Enter a username to add: ", input);

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
                        String username = Utilities.getUserInput("Enter a username to remove: ", input);
                        
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
                        String username = null;
                        int user_id = -1;
                        boolean confirm = false, quit = false, view_all = false;
                        
                        while (!confirm) {
                            System.out.print("View [a]ll, or view by [u]ser or user_[i]d [q:cancel]? ");
                            try {
                                char ch = input.nextLine().trim().toLowerCase().charAt(0);
                                if (ch == 'a') { // view all
                                    confirm = true;
                                    view_all = true;
                                } else if (ch == 'u') { // by username
                                    while (true) {
                                        username = Utilities.getUserInput("Enter a username: ", input);
                                        if (username == null) {
                                            quit = true; break;
                                        }

                                        // set user_id from username
                                        user_id = Utilities.getUserID(username);
                                        if (user_id < 0) {
                                            System.out.println("User does not exist.");
                                            continue;
                                        }

                                        break;
                                    }
                                    if (quit) break;

                                    confirm = true;
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
                            if (user_id != -1) {
                                for (User u : users) {
                                    if (u.getUserID() == user_id) {
                                        ++entries;
                                        System.out.println(u);
                                    }
                                }
                            } else { // an error prevented user_id from being set
                                System.out.println("Query error.");
                            }
                        }
                        System.out.println("END [" + entries + " entries]\n");

                        break;
                    }
                    case 'D': { // Register user-product relationship
                        String username = null, product = null;
                        int user_id = -1, product_id = -1;
                        boolean confirm = false, quit = false;

                        while (!confirm) {
                            System.out.print("Add by [n]ames or [i]ds [q:cancel]? ");
                            try {
                                char ch = input.nextLine().trim().toLowerCase().charAt(0);
                                if (ch == 'n') { // by name
                                    while (true) {
                                        username = Utilities.getUserInput("Enter a username: ", input);
                                        if (username == null) {
                                            quit = true; break;
                                        }

                                        user_id = Utilities.getUserID(username);
                                        if (user_id < 0) {
                                            System.out.println("User does not exist.");
                                            continue;
                                        }

                                        break;
                                    }
                                    if (quit) break;

                                    while (true) {
                                        product = Utilities.getUserInput("Enter a product name: ", input);
                                        if (product == null) {
                                            quit = true; break;
                                        }

                                        product_id = Utilities.getProductID(product);
                                        if (product_id < 0) {
                                            System.out.println("Product does not exist.");
                                            continue;
                                        }

                                        break;
                                    }
                                    if (quit) break;

                                    confirm = true;

                                } else if (ch == 'i') { // by id
                                    while (true) {
                                        while (true) {
                                            String tmp1 = Utilities.getUserInput("Enter a user ID: ", input);
                                            if (tmp1 == null) {
                                                quit = true; confirm = true; break;
                                            }

                                            try {
                                                user_id = Integer.parseInt(tmp1.trim());
                                            } catch (NumberFormatException e) {
                                                System.out.println("User ID MUST be an integer!");
                                                continue;
                                            }
                                            
                                            if (!Utilities.validateUserID(user_id)) {
                                                continue;
                                            }

                                            break;
                                        }
                                        if (quit) break;

                                        while (true) {
                                            String tmp2 = Utilities.getUserInput("Enter a product ID: ", input);
                                            if (tmp2 == null) {
                                                quit = true; confirm = true; break;
                                            }

                                            try {
                                                product_id = Integer.parseInt(tmp2.trim());
                                            } catch (NumberFormatException e) {
                                                System.out.println("Product ID MUST be an integer!");
                                                continue;
                                            }
                                            
                                            if (!Utilities.validateProductID(product_id)) {
                                                continue;
                                            }

                                            break;
                                        }
                                        if (quit) break;

                                        confirm = true;
                                        break;
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
                        String username = null, product = null;
                        int user_id = -1, product_id = -1;
                        boolean confirm = false, quit = false;
                        

                        while (!confirm) {
                            System.out.print("Remove by [n]ames or [i]ds [q:cancel]? ");
                            try {
                                char ch = input.nextLine().trim().toLowerCase().charAt(0);
                                if (ch == 'n') { // by name
                                    while (true) {
                                        username = Utilities.getUserInput("Enter a username: ", input);
                                        if (username == null) {
                                            quit = true; break;
                                        }

                                        user_id = Utilities.getUserID(username);
                                        if (user_id < 0) {
                                            System.out.println("User does not exist.");
                                            continue;
                                        }

                                        break;
                                    }
                                    if (quit) break;

                                    while (true) {
                                        product = Utilities.getUserInput("Enter a product name: ", input);
                                        if (product == null) {
                                            quit = true; break;
                                        }

                                        product_id = Utilities.getProductID(product);
                                        if (product_id < 0) {
                                            System.out.println("Product does not exist.");
                                            continue;
                                        }

                                        break;
                                    }
                                    if (quit) break;
                                    
                                    confirm = true;

                                } else if (ch == 'i') { // by id
                                    while (true) {
                                        while (true) {
                                            String tmp_one = Utilities.getUserInput("Enter a user ID: ", input);
                                            if (tmp_one == null) {
                                                quit = true; confirm = true; break;
                                            }

                                            try {
                                                user_id = Integer.parseInt(tmp_one.trim());
                                            } catch (NumberFormatException e) {
                                                System.out.println("User ID MUST be an integer!");
                                                continue;
                                            }

                                            if (!Utilities.validateUserID(user_id)) {
                                                continue;
                                            }

                                            break;
                                        }
                                        if (quit) break;

                                        while (true) {
                                            String tmp_two = Utilities.getUserInput("Enter a product ID: ", input);
                                            if (tmp_two == null) {
                                                quit = true; confirm = true; break;
                                            }
                                            
                                            try {
                                                product_id = Integer.parseInt(tmp_two.trim());
                                            } catch (NumberFormatException e) {
                                                System.out.println("Product ID MUST be an integer!");
                                                continue;
                                            }

                                            if (!Utilities.validateProductID(product_id)) {
                                                continue;
                                            }

                                            break;
                                        }
                                        if (quit) break;

                                        confirm = true;
                                        break;
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
                        String username = null, product = null;
                        int user_id = -1, product_id = -1;
                        boolean confirm = false, quit = false, view_all = false, 
                          isSearchModeUser = true; // true-search by user, false-search by product
                        
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

                        boolean isSearchModeID = false;
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
                        if (view_all) { // view all user-product relations
                            boolean cancel = false;

                            while (true) { // ask to view in terms of products or users
                                String selection = Utilities.getUserInput("View by [p]roducts or [u]sers: ", input);
                                if (selection == null) {
                                    cancel = true; break;
                                }
                                switch (selection.trim().toLowerCase()) {
                                    case "p" -> { isSearchModeUser = false; }
                                    case "u" -> { isSearchModeUser = true; }
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

                            if (isSearchModeUser) {
                                for (User u : users) {
                                    ++entries;
                                    String csv_recommended_products = String.join(", ", u.getRecommendedProducts().stream().map(Product::name).collect(Collectors.toList()));
                                    if (csv_recommended_products == null || csv_recommended_products.length() == 0) {
                                        csv_recommended_products = "NONE";
                                    }

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
                                            recommended_products:
                                            {
                                              %s
                                            }
                                        }
                                        """, 
                                        u.getName(),
                                        u.getUserID(),
                                        csv_used_products,
                                        csv_recommended_products
                                    );
                                }
                            } else { // product search
                                for (Product p : products) {
                                    ++entries;

                                    // get all usernames who are recommended the product
                                    String csv_user_recommendations = String.join(", ", users.stream().filter(u -> u.isRecommendedProduct(p)).map(User::getName).collect(Collectors.toList()));

                                    // get all usernames of people using the product (comma-separated)
                                    String csv_user_relations = String.join(", ", users.stream().filter(u -> u.isUsingProduct(p)).map(User::getName).collect(Collectors.toList()));

                                    if (csv_user_relations == null || csv_user_relations.length() == 0) {
                                        csv_user_relations = "NONE";
                                    }

                                    if (csv_user_recommendations == null || csv_user_recommendations.length() == 0) {
                                        csv_user_recommendations = "NONE";
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
                                            recommended_to:
                                            {
                                              %s
                                            }
                                        }
                                        """, 
                                        p.name(),
                                        p.product_id(),
                                        csv_user_relations,
                                        csv_user_recommendations
                                    );
                                }
                            }
                        } else { // not view-all
                            System.out.println("\nUser-Product Relationships:");

                            if (isSearchModeUser) { // search by user
                                for (User u : users) {
                                    if (u.getUserID() == user_id) {
                                        ++entries;
                                        String csv_used_products = String.join(", ", u.getUsedProducts().stream().map(Product::name).collect(Collectors.toList()));
                                        String csv_recommended_products = String.join(", ", u.getRecommendedProducts().stream().map(Product::name).collect(Collectors.toList()));

                                        if (csv_used_products == null || csv_used_products.length() == 0) {
                                            csv_used_products = "NONE";
                                        }

                                        if (csv_recommended_products == null || csv_recommended_products.length() == 0) {
                                            csv_recommended_products = "NONE";
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
                                                recommended_products:
                                                {
                                                  %s
                                                }
                                            }
                                            """, 
                                            u.getName(),
                                            u.getUserID(),
                                            csv_used_products,
                                            csv_recommended_products
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
                                
                                ArrayList<User> uses_product = new ArrayList<>();
                                ArrayList<User> recommended_to = new ArrayList<>();
                                for (User u : users) {
                                    boolean add = true;

                                    if (u.isUsingProduct(target)) {
                                        add = false;
                                        ++entries;
                                        uses_product.add(u);
                                    }

                                    if (u.isRecommendedProduct(target)) {
                                        if (add) ++entries;
                                        recommended_to.add(u);
                                    }
                                }

                                if (uses_product.size() == 0 && recommended_to.size() == 0) {
                                    System.out.println("No results were found.");
                                    break;
                                }

                                String csv_related_users = String.join(", ", uses_product.stream().map(User::getName).collect(Collectors.toList()));
                                String csv_recommended_users = String.join(", ", recommended_to.stream().map(User::getName).collect(Collectors.toList()));

                                if (csv_related_users == null || csv_related_users.length() == 0) {
                                    csv_related_users = "NONE";
                                }

                                if (csv_recommended_users == null || csv_recommended_users.length() == 0) {
                                    csv_recommended_users = "NONE";
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
                                        recommended_to:
                                        {
                                          %s
                                        }
                                    }
                                    """, 
                                    target.name(),
                                    target.product_id(),
                                    csv_related_users,
                                    csv_recommended_users
                                );

                            } else {
                                System.out.println("Query error.");
                            }
                        }
                        System.out.println("END [" + entries + " entries]\n");

                        break;
                    }
                    case 'G': { // Register product
                        String product = Utilities.getUserInput("Enter a product name to add: ", input);

                        if (product == null) break;

                        if (Utilities.doesProductExist(product)) {
                            System.out.println("Error: Product already exists!\n");
                            break;
                        }
                        

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
                        int invalid_counter = 3; // keep at 3 to ensure categories are printed the first time

                        // while category is invalid, keep prompting user
                        do {
                            if (invalid_counter >= 3) { // hint valid categories every 3 invalid entries (or first time)
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
                        String product = Utilities.getUserInput("Enter a product name to remove: ", input);
                        
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
                        String product = null;
                        int product_id = -1;
                        boolean confirm = false, quit = false, view_all = false;
                        
                        while (!confirm) {
                            System.out.print("View [a]ll, or view by [p]roduct_name or product_[i]d [q:cancel]? ");
                            try {
                                char ch = input.nextLine().trim().toLowerCase().charAt(0);
                                if (ch == 'a') { // view all
                                    confirm = true;
                                    view_all = true;
                                } else if (ch == 'p') { // by product name
                                    while (true) {
                                        product = Utilities.getUserInput("Enter a product name: ", input);
                                        if (product == null) {
                                            quit = true; break;
                                        }

                                        product_id = Utilities.getProductID(product);
                                        if (product_id < 0) {
                                            System.out.println("Product does not exist.");
                                            continue;
                                        }

                                        break;
                                    }
                                    if (quit) break;

                                    confirm = true;
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
                        String product_one = null, product_two = null;
                        int product_id_one = -1, product_id_two = -1;
                        double weight = -1.0;
                        boolean confirm = false, quit = false;

                        while (!confirm) {
                            System.out.print("Add by [n]ames or [i]ds [q:cancel]? ");
                            try {
                                char ch = input.nextLine().trim().toLowerCase().charAt(0);
                                if (ch == 'n') { // by name
                                    while (true) {
                                        product_one = Utilities.getUserInput("Enter a product name (1/2): ", input);
                                        if (product_one == null) {
                                            quit = true; break;
                                        }
                                        // get product 
                                        product_id_one = Utilities.getProductID(product_one);
                                        if (product_id_one < 0) {
                                            System.out.println("Product does not exist (1/2).");
                                            continue;
                                        }

                                        break;
                                    }
                                    if (quit) break;

                                    while (true) {
                                        product_two = Utilities.getUserInput("Enter a product name (2/2): ", input);
                                        if (product_two == null) {
                                            quit = true; break;
                                        }
                                        product_id_two = Utilities.getProductID(product_two);
                                        if (product_id_two < 0) {
                                            System.out.println("Product does not exist (2/2).");
                                            continue;
                                        } else if (product_id_two == product_id_one) {
                                            System.out.println("Product two may not match product one.");
                                            continue;
                                        }

                                        break;
                                    }
                                    if (quit) break;

                                    confirm = true;

                                } else if (ch == 'i') { // by id
                                    while (true) {
                                        while (true) {
                                            String tmp_one = Utilities.getUserInput("Enter a product ID (1/2): ", input);
                                            if (tmp_one == null) { // user cancelled
                                                quit = true; confirm = true; break;
                                            }

                                            try {
                                                product_id_one = Integer.parseInt(tmp_one.trim());
                                            } catch (NumberFormatException e) {
                                                System.out.println("Product ID MUST be an integer!");
                                                continue;
                                            }

                                            if (!Utilities.validateProductID(product_id_one)) { // invalid
                                                continue;
                                            }

                                            break;
                                        }
                                        if (quit) break;

                                        while (true) {
                                            String tmp_two = Utilities.getUserInput("Enter a product ID (2/2): ", input);
                                            if (tmp_two == null) {
                                                quit = true; confirm = true; break;
                                            }

                                            try {
                                                product_id_two = Integer.parseInt(tmp_two.trim());
                                            } catch (NumberFormatException e) {
                                                System.out.println("Product ID MUST be an integer!");
                                                continue;
                                            }

                                            if (!Utilities.validateProductID(product_id_two)) {
                                                continue;
                                            }

                                            if (product_id_two == product_id_one) {
                                                System.out.println("Product two may not match product one.");
                                                continue;
                                            }

                                            break;
                                        }
                                        if (quit) break;

                                        confirm = true;
                                        break;
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

                        // there is at least one matching relation which already exists
                        final int src_id = product_id_one, dst_id = product_id_two;
                        if (productRelations.stream().filter(pr -> pr.src_id()==src_id && pr.dst_id()==dst_id).collect(Collectors.toList()).size() > 0) {
                            System.out.printf("Could not add product relation '%d:%d' because it is already recorded in the database.\n", src_id, dst_id);
                            break;
                        }

                        // Get weight from user
                        while (true) {
                            try {
                                String tmp = Utilities.getUserInput("Enter product relational weight (0.0, 1.0]: ", input);
                                if (tmp == null) {
                                    quit = true; break;
                                }

                                weight = Double.parseDouble(tmp);
                                if (weight < 0.0) {
                                    System.out.println("Weight MUSTN'T be negative!");
                                    continue;
                                } else if (weight > 1.0 || Double.isNaN(weight)) { // greater than 1 or approaching infinity
                                    System.out.println("Weight MUSTN'T exceed 1.0!");
                                    continue;
                                } else if (Math.abs(weight) < 1e-10) { // weight is just about zero
                                    System.out.println("Weight MUSTN'T be zero (that implies the products are NOT related)!");
                                    continue;
                                } else {
                                    break;
                                }
                            } catch (NumberFormatException e) {
                                System.out.println("Weight MUST be a double (float)!");
                                continue;
                            }
                        }
                        if (quit) break;

                        if (!Utilities.addProductRelation(product_id_one, product_id_two, weight)) {
                            System.out.println("Failed to register product-product relationship.");
                            break;
                        }

                        // update product relations
                        productRelations.clear();
                        Utilities.populateProductRelationList(productRelations);
                        break;
                    }
                    case 'K': { // Remove product-product r'shp
                        String product_one = null, product_two = null;
                        int product_id_one = -1, product_id_two = -1;
                        boolean confirm = false, quit = false;
                        
                        while (!confirm) {
                            System.out.print("Remove by [n]ames or [i]ds [q:cancel]? ");
                            try {
                                char ch = input.nextLine().trim().toLowerCase().charAt(0);
                                if (ch == 'n') { // by name
                                    while (true) {
                                        product_one = Utilities.getUserInput("Enter a product name (1/2): ", input);
                                        if (product_one == null) {
                                            quit = true; break;
                                        }

                                        product_id_one = Utilities.getProductID(product_one);
                                        if (product_id_one < 0) {
                                            System.out.println("Product does not exist (1/2).");
                                            continue;
                                        }

                                        break;
                                    }
                                    if (quit) break;

                                    while (true) {
                                        product_two = Utilities.getUserInput("Enter a product name (2/2): ", input);
                                        if (product_two == null) {
                                            quit = true; break;
                                        }

                                        product_id_two = Utilities.getProductID(product_two);
                                        if (product_id_two < 0) {
                                            System.out.println("Product does not exist (2/2).");
                                            continue;
                                        }

                                        if (product_id_two == product_id_one) {
                                            System.out.println("Product two may not match product one.");
                                            continue;
                                        }

                                        break;
                                    }
                                    if (quit) break;
                                    
                                    confirm = true;

                                } else if (ch == 'i') { // by id
                                    while (true) {
                                        while (true) {
                                            String tmp_one = Utilities.getUserInput("Enter a product ID (1/2): ", input);
                                            if (tmp_one == null) {
                                                quit = true; confirm = true; break;
                                            }

                                            try {
                                                product_id_one = Integer.parseInt(tmp_one.trim());
                                            } catch (NumberFormatException e) {
                                                System.out.println("Product ID MUST be an integer!");
                                                continue;
                                            }

                                            if (!Utilities.validateProductID(product_id_one)) {
                                                continue;
                                            }

                                            break;
                                        }
                                        if (quit) break;

                                        while (true) {
                                            String tmp_two = Utilities.getUserInput("Enter a product ID (2/2): ", input);
                                            if (tmp_two == null) {
                                                quit = true; confirm = true; break;
                                            }

                                            try {
                                                product_id_two = Integer.parseInt(tmp_two.trim());
                                            } catch (NumberFormatException e) {
                                                System.out.println("Product ID MUST be an integer!");
                                                continue;
                                            }

                                            if (!Utilities.validateProductID(product_id_two)) {
                                                continue;
                                            }

                                            if (product_id_two == product_id_one) {
                                                System.out.println("Product two may not match product one.");
                                                continue;
                                            }

                                            break;
                                        }
                                        if (quit) break;

                                        confirm = true;
                                        break;
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

                        // there are no matching records
                        final int src_id = product_id_one, dst_id = product_id_two;
                        if (productRelations.stream().filter(pr -> pr.src_id()==src_id && pr.dst_id()==dst_id).collect(Collectors.toList()).size() == 0) {
                            System.out.printf("Could not remove product relation '%d:%d' because it is not recorded in the database.\n", src_id, dst_id);
                            break;
                        }

                        if (!Utilities.removeProductRelation(product_id_one, product_id_two)) {
                            System.out.println("Failed to remove product-product relationship.");
                            break;
                        }

                        // repopulate productRelations
                        productRelations.clear();
                        Utilities.populateProductRelationList(productRelations);

                        break;
                    }
                    case 'L': { // View prod-prod r'shps [w/ opt. to search single product/id]
                        String product_one = null, product_two = null;
                        int product_id_one = -1, product_id_two = -1;
                        boolean confirm = false, quit = false, view_all = false;
                        
                        while (!confirm) {
                            System.out.print("View [a]ll, or view by [p]roduct_names or product_[i]ds [q:cancel]? ");
                            try {
                                char ch = input.nextLine().trim().toLowerCase().charAt(0);
                                if (ch == 'a') { // view all
                                    confirm = true;
                                    view_all = true;
                                } else if (ch == 'p') { // by product names
                                    while (true) {
                                        product_one = Utilities.getUserInput("Enter a product name (1/2): ", input);

                                        if (product_one == null) {
                                            quit = true; break;
                                        }

                                        product_id_one = Utilities.getProductID(product_one);
                                        if (product_id_one < 0) {
                                            System.out.println("Product does not exist.");
                                            continue;
                                        }

                                        break;
                                    }
                                    if (quit) break;

                                    while (true) {
                                        product_two = Utilities.getUserInput("Enter a product name (2/2) [x:stop at one product]: ", input);

                                        if (product_two == null) {
                                            quit = true; break;
                                        }

                                        if (product_two.length() > 0 && product_two.toLowerCase().charAt(0) == 'x') {
                                            product_id_two = -1; break;
                                        }

                                        product_id_two = Utilities.getProductID(product_two);
                                        if (product_id_two < 0) {
                                            System.out.println("Product does not exist.");
                                            continue;
                                        }

                                        if (product_id_two == product_id_one) {
                                            System.out.println("Product two may not match product one.");
                                            continue;
                                        }

                                        break;
                                    }
                                    if (quit) break;

                                    confirm = true;
                                } else if (ch == 'i') { // by product ids
                                    while (true) {
                                        String tmp_one = Utilities.getUserInput("Enter a product ID (1/2): ", input);
                                        if (tmp_one == null) {
                                            quit = true; confirm = true; break;
                                        }

                                        try {
                                            product_id_one = Integer.parseInt(tmp_one.trim());
                                        } catch (NumberFormatException e) {
                                            System.out.println("Product ID MUST be an integer!");
                                            continue;
                                        }

                                        if (!Utilities.validateProductID(product_id_one)) {
                                            continue;
                                        }

                                        break;
                                    }
                                    if (quit) break;

                                    while (true) {
                                        String tmp_two = Utilities.getUserInput("Enter a product ID (2/2) [x:stop at one product]: ", input);
                                        if (tmp_two == null) {
                                            quit = true; confirm = true; break;
                                        }

                                        if (tmp_two.length() > 0 && tmp_two.toLowerCase().charAt(0) == 'x') {
                                            product_id_two = -1; break;
                                        }

                                        try {
                                            product_id_two = Integer.parseInt(tmp_two.trim());
                                        } catch (NumberFormatException e) {
                                            System.out.println("Product ID MUST be an integer!");
                                            continue;
                                        }

                                        if (!Utilities.validateProductID(product_id_two)) {
                                            continue;
                                        }

                                        if (product_id_two == product_id_one) {
                                            System.out.println("Product two may not match product one.");
                                            continue;
                                        }

                                        break;
                                    }
                                    if (quit) break;

                                    confirm = true;
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

                        System.out.println("\nProduct-Product Relationships:");
                        int entries = 0;

                        if (view_all) { // view all product relationships
                            for (ProductRelation pr : productRelations) {
                                ++entries;
                                System.out.println(String.format(
                                    """
                                    {
                                      src_product_id: %d,
                                      src_product_name: %s,
                                      dst_product_id: %d,
                                      dst_product_name: %s,
                                      weight: %.2f
                                    }""",
                                    pr.src_id(),
                                    Utilities.getProduct(pr.src_id()),
                                    pr.dst_id(),
                                    Utilities.getProduct(pr.dst_id()),
                                    pr.weight()
                                ));
                            }
                        } else {
                            final int prod_id_src = product_id_one, prod_id_dst = product_id_two;
                            if (product_id_one != -1 && product_id_two != -1) { // search for specific relations
                                List<ProductRelation> matching = productRelations.stream().filter(pr -> pr.src_id()==prod_id_src && pr.dst_id()==prod_id_dst).collect(Collectors.toList());
                                if (matching.isEmpty()) {
                                    System.out.println("No results were returned for your query.");
                                }
                                
                                for (ProductRelation pr : matching) {
                                    ++entries;
                                    System.out.println(String.format(
                                        """
                                        {
                                          src_product_id: %d,
                                          src_product_name: %s,
                                          dst_product_id: %d,
                                          dst_product_name: %s,
                                          weight: %.2f
                                        }""",
                                        pr.src_id(),
                                        Utilities.getProduct(pr.src_id()),
                                        pr.dst_id(),
                                        Utilities.getProduct(pr.dst_id()),
                                        pr.weight()
                                    ));
                                }
                            } else if (product_id_two == -1) { // search for all relations related to first product
                                List<ProductRelation> matching = productRelations.stream().filter(pr -> pr.src_id() == prod_id_src).collect(Collectors.toList());
                                if (matching.isEmpty()) {
                                    System.out.println("No results were returned for your query.");
                                }

                                for (ProductRelation pr : matching) {
                                    ++entries;
                                    System.out.println(String.format(
                                        """
                                        {
                                          src_product_id: %d,
                                          src_product_name: %s,
                                          dst_product_id: %d,
                                          dst_product_name: %s,
                                          weight: %.2f
                                        }""",
                                        pr.src_id(),
                                        Utilities.getProduct(pr.src_id()),
                                        pr.dst_id(),
                                        Utilities.getProduct(pr.dst_id()),
                                        pr.weight()
                                    ));
                                }
                            } else {
                                System.out.println("Query error.");
                            }
                        }
                        System.out.println("END [" + entries + " entries]\n");

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
                        System.out.println("Bad character: " + choice + "\n");
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