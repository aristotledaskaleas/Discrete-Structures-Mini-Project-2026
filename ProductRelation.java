public class ProductRelation {
    private int src_product_id, dst_product_id;
    private double weight;

    ProductRelation(int src_id, int dst_id, double weight) {
        this.src_product_id = src_id;
        this.dst_product_id = dst_id;
        this.weight = weight;
    }

    public double weight() {return this.weight;}
    public int src_id() {return this.src_product_id;}
    public int dst_id() {return this.dst_product_id;}
}