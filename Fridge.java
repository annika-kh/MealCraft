import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;

/**
 * Stores food inventory and recipes.
 */
public class Fridge {
    /** low stock threshold */
    private static final double LOW_STOCK_THRESHOLD = 1;
    /** food inventory */
    private HashMap<String, FoodItem> inventoryByName;
    /** expiration index */
    private TreeMap<LocalDate, List<FoodItem>> expirationIndex;
    /** recipes */
    private List<Recipe> recipes;
    /** shopping list */
    private HashMap<String, IngredientLine> shoppingList;
    
    /** 
     * Creates an empty fridge.
     */
    public Fridge(){
        inventoryByName = new HashMap<>();
        expirationIndex = new TreeMap<>();
        recipes = new ArrayList<>();
        shoppingList = new HashMap<>();
    }

    /**
     * Adds a food item to the fridge inventory.
     * 
     * @param food the FoodItem to add
     * 
     * Contributed by: Annika Hambali
     */
    public void addFood(FoodItem food){
        String key = food.getNormalizedName();

        // If item already exists
        if (inventoryByName.containsKey(key)) {
            FoodItem existing = inventoryByName.get(key);
            // Merges quantities
            existing.addQuantity(food.getQuantity());
        }
        // If item is new
        else {
            // Stores it
            inventoryByName.put(key, food);
        }
        
        rebuildExpirationIndex();
    }
    
    /**
     * Removes a specified amount of a food item from the fridge inventory.
     * 
     * @param name name of the food item
     * @param amt amount to remove
     * @return true is removal was successful, false otherwise
     * 
     * Contributed by: Annika Hambali
     */
    public boolean removeFood(String name, double amt) {
        String key = name.toLowerCase().trim();
        FoodItem item = inventoryByName.get(key);
        
        // If item is missing or amount is too large
        if (item == null || amt > item.getQuantity()) {
            return false;
        }
        
        // If subtraction fails validation
        if (!item.subtractQuantity(amt)) {
            return false;
        }
        
        // If quantity reaches zero
        if (item.getQuantity() == 0) {
            // Removes the entry
            inventoryByName.remove(key);
        }
        
        rebuildExpirationIndex();
        return true;
    }

    /** @return food item by name */
    public FoodItem getFoodItem(String name) {
        return inventoryByName.get(name.toLowerCase().trim());
    }
    
    /**
     * Returns all food items sorted alphabetically (A-Z).
     * 
     * @return a list of FoodItems sorted by name
     * 
     * Contributed by: Annika Hambali
     */
    public List<FoodItem> getAllFoodItemsSortedAZ() {
        List<FoodItem> list = new ArrayList<>(inventoryByName.values());
        list.sort(Comparator.comparing(FoodItem::getNormalizedName));
        return list;
    }
    
    /**
     * Returns items sorted by expiration date (soonest first).
     * 
     * @return a list of FoodItems sorted by expiration date
     * 
     * Contributed by: Annika Hambali
     */
    public List<FoodItem> getAllFoodItemsSortedExpiration() {
        List<FoodItem> list = getAllFoodItemsSortedAZ();
        list.sort(Comparator.comparing(FoodItem::getExpirationDate));
        return list;
    }
    
    /**
     * Returns all food items that are at or below the low stock threshold.
     * 
     * @return a list of low-stock FoodItems
     * 
     * Contributed by: Annika Hambali
     */
    public List<FoodItem> getLowStockItems() {
        List<FoodItem> low = new ArrayList<>();
        for (FoodItem item : inventoryByName.values()) {
            // If quantity is low
            if (item.getQuantity() <= LOW_STOCK_THRESHOLD) {
                low.add(item);
            }
        }
        return low;
    }
    
    /**
     * Rebuilds the expiration index to match the current inventory.
     * 
     * Contributed by: Annika Hambali
     */
    public void rebuildExpirationIndex() {
        expirationIndex.clear();
        for (FoodItem item : inventoryByName.values()) {
            expirationIndex.computeIfAbsent(item.getExpirationDate(), d -> new ArrayList<>()).add(item);
        }
    }
    
    /**
     * Returns food items expiring within a certain number of days (inclusive).
     * 
     * @param days the number of days from today
     * @return a list of expiring FoodItems expiring within the window
     * 
     * Contributed by: Annika Hambali
     */
    public List<FoodItem> getItemsExpiringWithin(int days) {
        List<FoodItem> soon = new ArrayList<>();
        LocalDate today = LocalDate.now();
        
        // If days is negative
        if (days < 0) {
            days = 0;
        }
        
        for (FoodItem item : inventoryByName.values()) {
            long diff = item.daysUntilExpiration(today);
            // If item expires within the window
            if (diff >= 0 && diff <= days) {
                soon.add(item);
            }
        }
        
        soon.sort(Comparator.comparing(FoodItem::getExpirationDate));
        return soon;
    }
    
    /**
     * Adds a recipe to the fridge's stored recipe list.
     * 
     * @param recipe recipe to add
     */
    public void addRecipe(Recipe recipe) {
        if (recipe != null) {
            recipes.add(recipe);
        }
    }
    
    /**
     * Returns cookable recipes.
     */
    public List<Recipe> getCookableRecipes() {
        List<Recipe> list = new ArrayList<>();
        for (Recipe recipe : recipes) {
            if (recipe.canCook(this)) {
                list.add(recipe);
            }
        }
        return list;
    }
    
    /**
     * Returns the stored recipe list.
     * 
     * @return recipes
     */
    public List<Recipe> getRecipes() {
        return recipes;
    }
    
    /**
     * Builds shopping list.
     */
    public void createShoppingList() {
        shoppingList.clear();
        
        for (FoodItem item : getLowStockItems()) {
            double shortage = LOW_STOCK_THRESHOLD - item.getQuantity();
            
            if (shortage > 0) {
                shoppingList.put(item.getNormalizedName(), new IngredientLine(item.getName(), shortage, item.getUnit()));
            }
        }
    }
    
    /**
     * Removes amount from shopping list.
     */
    public void removeShoppingListItem(String name, double amt) {
        name = name.toLowerCase().trim();
        IngredientLine line = shoppingList.get(name);
        if (line != null) {
            double remaining = line.getAmount() - amt;
            
            if (remaining <= 0) {
                shoppingList.remove(name);
            }
            else {
                shoppingList.put(name, new IngredientLine(line.getNormalizedName(), remaining, line.getUnit()));
            }
        }
    }
    
    /**
     * Returns the shopping list items.
     * 
     * @return shopping list values
     */
    public java.util.Collection<IngredientLine> getShoppingListItems() {
        return shoppingList.values();
    }

    /**
     * Adds a specific amount of an ingredient to the shopping list.
     * If it already exists, it increases the amount.
     */
    public void addShoppingListItem(String name, double amt, String unit) {
        if (name == null || name.isBlank()) return;
    
        String key = name.toLowerCase().trim().replaceAll("\\s+", "");
        if (unit == null || unit.isBlank()) unit = "";
    
        IngredientLine existing = shoppingList.get(key);
        if (existing == null) {
            shoppingList.put(key, new IngredientLine(name, amt, unit));
        } else {
            shoppingList.put(
                key,
                new IngredientLine(existing.getNormalizedName(), existing.getAmount() + amt, existing.getUnit())
            );
        }
    }

    /**
     * Returns the recipe with the least number of missing ingredients
     * 
     * @return Recipe
     * 
     * Contributed by: Jessie
     */
    public Recipe getRecipeWithIngredients(){
        double max = 0;
        Recipe bestRecipe = null;
        for(Recipe recipe : recipes){
            double temp = recipe.getMissingIngredients(this);
            if(max < temp){
                max = temp;
                bestRecipe = recipe;
            }
        }
        return bestRecipe;
    }
}
