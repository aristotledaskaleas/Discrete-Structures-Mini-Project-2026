import java.util.ArrayList;

public class Main {
    final static String[] CATEGORIES = { // Only allow these categories to be searched for
        "Food",
        "Video",
        "Audio",
        "Peripherals",
        "Office Supplies"
    };

     public static void main(String args[]) {
        //Utilities.testDatabaseMethods(); // Debugging

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

    }
}