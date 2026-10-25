import java.util.ArrayList;

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

public class User {
   //variables
   private int user_id;
   private String name;
   private ArrayList<Product> used_products = new ArrayList<Product>();
   private ArrayList<Product> recommended_products = new ArrayList<Product>();

   //constructor
   public User(int user_id, String name) {
      this.user_id = user_id;
      this.name = name;
   }

   // mutators
   // ADD TO EITHER ARRAY
   public void addUsedProduct(Product product) {
      try {
         this.used_products.add(product.clone());
      } catch (CloneNotSupportedException e) {
         System.out.println("internal error cloning product");
      }

   }
   public void addRecommendedProduct(Product product) {
      try {
         this.recommended_products.add(product.clone());
      } catch (CloneNotSupportedException e) {
         System.out.println("internal error cloning product");
      }

   }

   // REMOVE FROM USED PRODUCTS ARRAY
   public void removeUsedProduct(Product product) { // this implementation removes ALL instances where product's name is same as used product's name
      // Iterate through every element in the used products
      for (int i = 0; i < used_products.size(); i++) {
         // If the used product's name is equal to the one we want to remove
         if (used_products.get(i).name().equals(product.name())) {
            // delete it
            used_products.remove(i);
            --i; // ensure the pointer returns to the new index
         }
      }
   }

   // currently the only way to remove recommended products is to restart the program and re-read the database!
   // so don't implement this method unless that changes
   //public void removeRecommended Product(Product product) {//add code here}

   // CLEAR EITHER ARRAY
   public void clearUsedProducts() {
      this.used_products.clear();
   }
   public void clearRecommendedProducts() {
      this.recommended_products.clear();
   }

   // accessors
   public int getUserID() {
      return this.user_id;
   }
   public String getName() {
      return this.name;
   }
   // defensive copies
   public ArrayList<Product> getUsedProducts() {
      return new ArrayList<Product>(this.used_products);
   }
   public ArrayList<Product> getRecommendedProducts() {
      return new ArrayList<Product>(this.recommended_products);
   }

   @Override 
   public String toString() { // .JSON-like format because it's readable and cool 8)
      return String.format(
         """
         {
            User: %s,
            ID: %d,
            used_products: %d,
            recommended_products: %d
         }""",
         name,
         user_id,
         used_products.size(),
         recommended_products.size()
      );
   }
}