public class Product implements Cloneable {
    private int product_id;
    private String name;
    private String manufacturer;
    private String category;

    public Product(int product_id, String name, String manufacturer, String category) {
        this.product_id = product_id;
        this.name = name;
        this.manufacturer = manufacturer;
        this.category = category;
    }

    public int product_id() {return this.product_id; }
    public String name() { return this.name; }
    public String manufacturer() { return this.manufacturer; }
    public String category() { return this.category; }

    @Override
    public Product clone() throws CloneNotSupportedException {
        return new Product(this.product_id, this.name, this.manufacturer, this.category);
    }
}