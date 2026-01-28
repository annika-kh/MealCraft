public class IngredientLine {
    // Instance Variables
    private String name;
    private double amount;
    private String unit;

    // Constructor
    public IngredientLine(String itemName, double itemAmount, String itemUnit) {
        this.name = itemName.toLowerCase().trim();
        this.amount = itemAmount;
        this.unit = itemUnit;
    }
    
    // Getter Methods
    public String getName() {
        return name;
    }
    public double getAmount() {
        return amount;
    }
    public String getUnit() {
        return unit;
    }
}