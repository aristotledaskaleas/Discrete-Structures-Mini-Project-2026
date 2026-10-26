import java.time.ZonedDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Scanner;

//*/
// "User Recommendation Database With Bipartite Graphing"
//   Research Mini Project - Discrete Structures 2026
//
// Bipartite graphing linking users to products and recommended products.
//   All user information and data is persistent in SQL database.
//
// Author(s): Aristotle Daskaleas
// Date: 2026 (October-November)
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
                                            if (Utilities.validateUserID(user_id)) {
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
                        break;
                    }
                    case 'E': { // Remove user-product r'shp
                        break;
                    }
                    case 'F': { // View user-product r'shps [w/ opt. to search single name/product/id]
                        break;
                    }
                    case 'G': { // Register product
                        break;
                    }
                    case 'H': { // Remove product
                        break;
                    }
                    case 'I': { // View products [ w/ opt. to search single product/id]
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