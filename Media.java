public class Media{

    protected char type;
    protected String title;
    protected String name;
    protected int rating;
    protected String genre;
    protected int length;
    protected int yearReleased;
    public static int[] mediaCounts = {0,0,0};

    //default constructor
    public Media(){
    this.type = ' ';
    this.title = " ";
    this.name = " ";
    this.rating = -1;
    this.genre = " ";
    this.length = -1;
    this.yearReleased = -1;

    }

    //overloaded constructor
 
 

    //getters (accessors)
 


    //setters (mutators)
 

    public String printMedia() {
        String out = String.format("%-37s%10d%10d     %-23s%7s\n",title, yearReleased, rating, genre, type);
        return out;
    }
}