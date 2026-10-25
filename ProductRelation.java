//*/
// Aristotle Daskaleas - 2026 : "User Recommendation Database With Bipartite Graphing" 
// This class stores a single instance of a PRODUCT-PRODUCT relationship from the database.
//*/

//*/
// ProductRelation(int,int,double) is the only valid constructor because
//   if all of these fields are not known at the time of construction, the
//   database was incorrectly accessed or the record is invalid.
//
// Omitting mutators allow these records to become immutable.
//*/

public class ProductRelation {
    // private instance attributes
    private int src_product_id, dst_product_id;
    private double weight;

    // constructor
    ProductRelation(int src_id, int dst_id, double weight) {
        this.src_product_id = src_id;
        this.dst_product_id = dst_id;
        this.weight = weight;
    }

    // accessors
    public double weight() {return this.weight;}
    public int src_id() {return this.src_product_id;}
    public int dst_id() {return this.dst_product_id;}

    // no mutators [DO NOT ADD] (please :3)
}