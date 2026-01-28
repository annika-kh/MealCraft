import java.util.ArrayList;
import java.util.List;

public class Recipe {
    // Instance Variables
    private String name;
    private List<String> steps;
    private List<IngredientLine> ingredients;
    private String imgFilePath;

    // Constructor
    public Recipe (String name, List<String> steps, List<IngredientLine> ingredients, String imgFilePath) {
        this.name = name;
        this.steps = steps;
        this.ingredients = ingredients;
        this.imgFilePath = imgFilePath;
    }

    // Getter Methods
    public String getName() {
        return name;
    }
    public List<String> getSteps() {
        return steps;
    }
    public List<IngredientLine> getIngredients() {
        return ingredients;
    }
    public String getImgFilePath() {
        return imgFilePath;
    }

    // Setter Methods
    public void setName(String name) {
        this.name = name;
    }
    public void setSteps(List<String> steps) {
        this.steps = steps;
    }
    public void setIngredients(List<IngredientLine> ingredients) {
        this.ingredients = ingredients;
    }
    public void setImgFilePath(String imgFilePath) {
        this.imgFilePath = imgFilePath;
    }

    // Returns true if the fridge contains all required ingredients (by name)
    public boolean canCook(Fridge fridge) {
        for (IngredientLine line : ingredients) {
            if (fridge.getFoodItem(line.getName()) == null) {
                return false;
            }
        }
        return true;
    }

    // Returns missing ingredients (by name) if not cookable
    public List<String> getMissingIngredients(Fridge fridge) {
        List<String> missing = new ArrayList<>();
        for (IngredientLine line : ingredients) {
            FoodItem item = fridge.getFoodItem(line.getName());
            if (item == null) {
                missing.add(line.getName());
            }
        }
        return missing;
    }
}