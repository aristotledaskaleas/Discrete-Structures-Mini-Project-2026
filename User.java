import java.util.ArrayList;

public class User {
   private int user_id;
   private String name;
   private ArrayList<Product> used_products = new ArrayList<Product>();
   private ArrayList<Product> recommended_products = new ArrayList<Product>();

   public User(int user_id, String name) {
      this.user_id = user_id;
      this.name = name;
   }

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

   public void clearUsedProducts() {
      this.used_products.clear();
   }

   public void clearRecommendedProducts() {
      this.recommended_products.clear();
   }

   public int getUserID() {
      return this.user_id;
   }

   public String getName() {
      return this.name;
   }

   public ArrayList<Product> getUsedProducts() {
      return new ArrayList<Product>(this.used_products);
   }

   public ArrayList<Product> getRecommendedProducts() {
      return new ArrayList<Product>(this.recommended_products);
   }
}