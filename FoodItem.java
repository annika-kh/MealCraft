import java.time.LocalDate;

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
}