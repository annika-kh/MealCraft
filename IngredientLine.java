/**
 * Represents a single ingredient in a recipe.
 * 
 * Contributed by: Annika Hambali
 */
public class IngredientLine {
    /** normalized name */
    private String normalizedName;
    /** required amount */
    private double amount;
    /** unit */
    private String unit;
    
    /**
     * Creates an ingredient line.
     */
    public IngredientLine(String itemName, double itemAmount, String itemUnit) {
        normalizedName = itemName.toLowerCase().trim();
        amount = itemAmount;
        unit = itemUnit;
    }
    
    /** @return normalized name */
    public String getNormalizedName() {
        return normalizedName;
    }
    
    /** @return amount */
    public double getAmount() {
        return amount;
    }
    
    /** @return unit */
    public String getUnit() {
        return unit;
    }
}
