import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * Represents a food item stored in the fridge.
 */
public class FoodItem{
    /** display name */
    private String name;
    /** normalized name used as key */
    private String normalizedName;
    /** quantity of the item */
    private double quantity;
    /** unit label */
    private String unit;
    /** category */
    private Category category;
    /** expiration date */
    private LocalDate expirationDate;
    /** image file path */
    private String imgFilePath;

    /**
     * Creates a food item.
     */
    public FoodItem(String name, double quantity, String unit,Category category, LocalDate expirationDate, String imgFilePath) {
        this.name = name.trim();
        this.normalizedName = name.toLowerCase().trim();
        this.quantity = quantity;
        this.unit = unit;
        this.category = category;
        this.expirationDate = expirationDate;
        this.imgFilePath = imgFilePath;
    }

    /** @return name */
    public String getName() {
        return name;
    }
    
    /** @return normalized name */
    public String getNormalizedName() {
        return normalizedName;
    }
    
    /** @return quantity */
    public double getQuantity() {
        return quantity;
    }
    
    /** @return unit */
    public String getUnit() {
        return unit;
    }
    
    /** @return category */
    public Category getCategory() {
        return category;
    }
    
    /** @return expiration date */
    public LocalDate getExpirationDate() {
        return expirationDate;
    }
    
    /** @return image path */
    public String getImgFilePath() {
        return imgFilePath;
    }
    
    /**
     * Adds quantity.
     * 
     * @param amt amount to add
     */
    public void addQuantity(double amt) {
        if (amt > 0) {
            quantity += amt;
        }
    }
    
    /**
     * Subtracts quantity if possible.
     * 
     * @param amt amount to subtract
     * @return true if successful
     */
    public boolean subtractQuantity(double amt) {
        if (amt <= 0 || amt > quantity) {
            return false;
        }
    
        quantity -= amt;

        return true;
    }
    
    /**
     * Sets quantity by adjusting using addQuantity and subtractQuantity
     * 
     * @param amt new quantity
     */
    public void setQuantity(double amt) {
        if (amt >= 0) {
            double diff = amt - quantity;
            
            if (diff > 0) {
                addQuantity(diff);
            }
            else if (diff < 0) {
                subtractQuantity(-diff);
            }
        }
    }
    
    /**
     * Calculates days until expiration.
     * @param today reference date
     * @return days until expiration
     */
    public long daysUntilExpiration(LocalDate today) {
        return ChronoUnit.DAYS.between(today, expirationDate);
    }
    
    /** @return readable string */
    public String toString() {
        return name + " (" + quantity + " " + unit + ")";
    }
}