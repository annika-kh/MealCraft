import java.util.List;

public class Recipe {
    /** recipe name */
    private String name;
    /** cooking steps */
    private List<String> steps;
    /** ingredient list */
    private List<IngredientLine> ingredients;
    /** image path */
    private String imgFilePath;

    /**
     * Creates a recipe. 
     */
    public Recipe (String name, List<String> steps, List<IngredientLine> ingredients, String imgFilePath) {
        this.name = name;
        this.steps = steps;
        this.ingredients = ingredients;
        this.imgFilePath = imgFilePath;
    }

    /** @return recipe name */
    public String getName() {
        return name;
    }
    
    /** @return steps lists */
    public List<String> getSteps() {
        return steps;
    }
    
    /** @return ingredients */
    public List<IngredientLine> getIngredients() {
        return ingredients;
    }
    
    /** @return recipe image path */
    public String getImgFilePath() {
        return imgFilePath;
    }

    /**
     * Checks if a recipe can be cooked.
     * 
     * @param fridge
     * @return true if possible
     */
    public boolean canCook(Fridge fridge) {
        for (IngredientLine line : ingredients) {
            FoodItem item = fridge.getFoodItem(line.getNormalizedName());
            if (item == null || item.getQuantity() < line.getAmount()) {
                return false;
            }
        }
        return true;
    }
    
    /**
     * Consumes ingredients from fridge.
     */
    public boolean cook(Fridge fridge) {
        if (!canCook(fridge)) {
            return false;
        }
        
        for (IngredientLine line : ingredients) {
            fridge.removeFood(line.getNormalizedName(), line.getAmount());
        }
        
        return true;
    }
}