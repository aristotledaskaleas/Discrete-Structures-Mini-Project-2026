//*/
// Aristotle Daskaleas - 2026 : "User Recommendation Database With Bipartite Graphing" 
// This class stores a single instance of a product from the database.
//*/

//*/
// Product(int,String,String,String) is the only valid constructor because
//   if all of these fields are not known at the time of construction, the
//   database was incorrectly accessed or the record is invalid.
// NOTE: While enforced elsewhere, ONLY the field 'manufacturer' is optional.
// ALL OTHER FIELDS ARE REQUIRED (id, name, category)
//
// Omitting mutators allow these records to become immutable.
//*/

public class Product implements Cloneable {
    // private instance variables
    private int product_id;
    private String name;
    private String manufacturer;
    private String category;

    // constructor
    public Product(int product_id, String name, String manufacturer, String category) {
        this.product_id = product_id;
        this.name = name;
        this.manufacturer = manufacturer;
        this.category = category;
    }

    // accessors
    public int product_id() {return this.product_id; }
    public String name() { return this.name; }
    public String manufacturer() { return this.manufacturer; }
    public String category() { return this.category; }

    // Allow the Product to be copied (defensive copying)
    @Override
    public Product clone() throws CloneNotSupportedException {
        return new Product(this.product_id, this.name, this.manufacturer, this.category);
    }

    // do not add mutators (immutable records) [thx :3]
}