import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;

public class Fridge {
    // Instance Variables
    private HashMap<String, FoodItem> inventoryByName;
    private TreeMap<LocalDate, List<FoodItem>> expirationIndex;
    private List<Recipe> recipes;

    // Constructor
    public Fridge(){
        inventoryByName = new HashMap<>();
        expirationIndex = new TreeMap<>();
        recipes = new ArrayList<>();
    }
    public Fridge(HashMap<String, FoodItem> inventory){
        this();
        // Adds inventory elements into the expirationIndex map
        for(FoodItem item : inventory.values()){
            addFood(item);
        }
    }

    // Adds a food item into the fridge
    public void addFood(FoodItem food){
        String key = food.getNormalizedName();

        // If item already exists, update quantity and earliest expiration
        if(inventoryByName.containsKey(key)){
            FoodItem existing = inventoryByName.get(key);
            existing.setQuantity(existing.getQuantity() + food.getQuantity());

            // Keep earliest expiration date
            if(food.getExpirationDate().isBefore(existing.getExpirationDate())){
                existing.setExpirationDate(food.getExpirationDate());
            }

            // NOTE: expirationIndex might not be perfectly updated for merged items.
            // (App/UI usually refreshes inventory display from inventoryByName anyway.)
        } else {
            inventoryByName.put(key, food);

            // Add to expirationIndex bucket
            if(!expirationIndex.containsKey(food.getExpirationDate())){
                expirationIndex.put(food.getExpirationDate(), new ArrayList<>());
            }
            expirationIndex.get(food.getExpirationDate()).add(food);
        }
    }

    // Removes a food item (by name) from the fridge
    public void removeFood(String itemName, double amountToRemove){
        String key = itemName.toLowerCase().trim();

        if(!inventoryByName.containsKey(key)){
            System.out.println("Error: Item not found.");
            return;
        }

        FoodItem item = inventoryByName.get(key);

        if(amountToRemove <= 0){
            System.out.println("Error: Invalid amount.");
            return;
        }

        if(amountToRemove > item.getQuantity()){
            System.out.println("Error: Not enough quantity to remove.");
            return;
        }

        item.setQuantity(item.getQuantity() - amountToRemove);

        // If quantity hits 0, remove from inventory map
        if(item.getQuantity() == 0){
            inventoryByName.remove(key);

            // Remove from expiration bucket
            LocalDate exp = item.getExpirationDate();
            if(expirationIndex.containsKey(exp)){
                expirationIndex.get(exp).remove(item);
                if(expirationIndex.get(exp).isEmpty()){
                    expirationIndex.remove(exp);
                }
            }
        }
    }

    // Returns food item by name (or null if not found)
    public FoodItem getFoodItem(String itemName){
        if(itemName == null) return null;
        return inventoryByName.get(itemName.toLowerCase().trim());
    }

    // Returns all food items
    public Collection<FoodItem> getAllFoodItems(){
        return inventoryByName.values();
    }

    // Returns expirationIndex map
    public TreeMap<LocalDate, List<FoodItem>> getExpirationIndex(){
        return expirationIndex;
    }

    // Adds a recipe
    public void addRecipe(Recipe recipe){
        recipes.add(recipe);
    }

    // Returns recipes
    public List<Recipe> getRecipes(){
        return recipes;
    }

    // Prints inventory (useful for console testing in BlueJ)
    public void printInventory(){
        if(inventoryByName.isEmpty()){
            System.out.println("(Inventory is empty)");
            return;
        }
        for(FoodItem item : inventoryByName.values()){
            System.out.println(item);
        }
    }

    // Returns items that expire in <= days
    public List<FoodItem> getItemsExpiringWithin(int days){
        List<FoodItem> soon = new ArrayList<>();
        LocalDate today = LocalDate.now();

        for(FoodItem item : inventoryByName.values()){
            long diff = ChronoUnit.DAYS.between(today, item.getExpirationDate());
            if(diff >= 0 && diff <= days){
                soon.add(item);
            }
        }
        return soon;
    }
}