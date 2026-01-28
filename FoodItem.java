import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class FoodItem{
    // Instance Variables
    private String name;
    private String normalizedName;
    private double quantity;
    private String unit;
    private Category category;
    private LocalDate expirationDate;
    private String imgFilePath;

    // Constructor
    public FoodItem(String name, double quantity, String unit, Category category, LocalDate expirationDate, String imgFilePath){
        this.name = name;
        normalizedName = name.toLowerCase().trim();
        this.quantity = quantity;
        this.unit = unit;
        this.category = category;
        this.expirationDate = expirationDate;
        this.imgFilePath = imgFilePath;
    }

    // Getter Methods
    public String getName(){
        return name;
    }
    public String getNormalizedName(){
        return normalizedName;
    }
    public double getQuantity(){
        return quantity;
    }
    public String getUnit(){
        return unit;
    }
    public Category getCategory(){
        return category;
    }
    public LocalDate getExpirationDate(){
        return expirationDate;
    }
    public String getImgFilePath(){
        return imgFilePath;
    }

    // Setter Methods
    public void setName(String newName){
        name = newName;
        normalizedName = name.toLowerCase().trim();
    }
    public void setQuantity(double newQuantity){
        quantity = newQuantity;
    }
    public void setUnit(String newUnit){
        unit = newUnit;
    }
    public void setCategory(Category newCategory){
        category = newCategory;
    }
    public void setExpirationDate(LocalDate newExpirationDate){
        expirationDate = newExpirationDate;
    }
    public void setImgFilePath(String newImgFilePath){
        imgFilePath = newImgFilePath;
    }
    
    // Adds to the current quantity (ignores negative/zero)
    public void addQuantity(double amt) {
        if (amt <= 0) return;
        quantity += amt;
    }
    
    // Subtracts quantity if possible.
    // Returns true if subtraction happened, false if invalid or not enough.
    public boolean subtractQuantity(double amt) {
        if (amt <= 0) return false;
        if (amt > quantity) return false;
    
        quantity -= amt;

        return true;
    }
    
    // Number of days until expiration (0 = expires today, negative = already expired)
    public long daysUntilExpiration(LocalDate today) {
        if (today == null) today = LocalDate.now();
        return ChronoUnit.DAYS.between(today, expirationDate);
    }
}