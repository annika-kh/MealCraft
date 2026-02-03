import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * Represents a food item stored in the fridge.
 */
public class FoodItem {
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
     * Adds to the current quantity.
     * Prevents invalid input by allowing only positive values.
     * 
     * @param amt amount to add
     * 
     * Contributed by: Annika Hambali
     */
    public void addQuantity(double amt) {
        // Only allows positive values
        if (amt > 0) {
            // Applies increase
            quantity += amt;
        }
    }
    
    /**
     * Subtracts from the current quantity.
     * Prevents invalid input by blocking negative values and amounts greater than available.
     * 
     * @param amt amount to subtract
     * @return true if successful
     * 
     * Contributed by: Annika Hambali
     */
    public boolean subtractQuantity(double amt) {
        // Blocks negative values or amounts greater than available
        if (amt <= 0 || amt > quantity) {
            return false;
        }
    
        // Applies decrease
        quantity -= amt;

        return true;
    }
    
    /**
     * Sets a new quantity by calculating the difference and using addQuantity and subtractQuantity.
     * 
     * @param amt new quantity
     * 
     * Contributed by: Annika Hambali
     */
    public void setQuantity(double amt) {
        if (amt >= 0) {
            // Computes change amount
            double diff = amt - quantity;
            
            if (diff > 0) {
                // Applies increase
                addQuantity(diff);
            }
            else if (diff < 0) {
                // Applies decrease
                subtractQuantity(-diff);
            }
        }
    }
    
    /**
     * Calculates days until expiration.
     * 
     * @param today reference date
     * @return days until expiration
     * 
     * Contributed by: Angela Zhong
     */
    public long daysUntilExpiration(LocalDate today) {
        return ChronoUnit.DAYS.between(today, expirationDate);
    }
    
    /**
     * Sets the expiration date.
     * 
     * @param date new expiration date
     * 
     * Contributed by: Angela Zhong
     */
    public void setExpirationDate(LocalDate date) {
        if (date != null) {
            expirationDate = date;
        }
    }
    
    /**
     * Sets the category.
     * 
     * @param category new category
     * 
     * Contributed by: Angela Zhong
     */
    public void setCategory(Category newCategory) {
        if (newCategory != null) {
            category = newCategory;
        }
    }
    
    /** @return readable string */
    public String toString() {
        return name + " (" + quantity + " " + unit + ")";
    }
}
