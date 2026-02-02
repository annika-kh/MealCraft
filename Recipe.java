import java.util.List;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.time.LocalDate;


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
    
    public double getMissingIngredients(Fridge fridge){
        double cnt = 0;
        for(IngredientLine line : ingredients){
            FoodItem item = fridge.getFoodItem(line.getNormalizedName());
            if(item == null){
                cnt = cnt + line.getAmount();
            }
            else if(item.getQuantity() < line.getAmount()){
                // note that this does not account for potential unit differences
                cnt = cnt + line.getAmount()-item.getQuantity();
            }
        }
        return cnt;
    }
    
    public long getEarliestExpirationDays(Fridge fridge) {
        long earliest = Long.MAX_VALUE;

        for (IngredientLine ing : ingredients) {
            FoodItem item = fridge.getFoodItem(ing.getNormalizedName());

            if (item != null) {
                long days = item.daysUntilExpiration(LocalDate.now());

                if (days < earliest) {
                    earliest = days;
                }
            }
        }

        return earliest;
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
    
    public static Recipe fromTxt(String recipeName,String txtResourcePath,String imgFilePath) {
        List<String> steps = new ArrayList<>();
        List<IngredientLine> ingredients = new ArrayList<>();

        boolean readingSteps = false;
        boolean readingIngredients = false;

        try (InputStream is = Recipe.class.getResourceAsStream(txtResourcePath);
             BufferedReader br = new BufferedReader(new InputStreamReader(is))) {

            if (is == null) {
                throw new RuntimeException("Recipe file not found: " + txtResourcePath);
            }

            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;

                if (line.equalsIgnoreCase("Steps:")) {
                    readingSteps = true;
                    readingIngredients = false;
                    continue;
                }

                if (line.equalsIgnoreCase("Ingredients:")) {
                    readingSteps = false;
                    readingIngredients = true;
                    continue;
                }
    
                if (readingSteps) {
                    // Remove leading number like "1. "
                    line = line.replaceFirst("^\\d+\\.\\s*", "");
                    steps.add(line);
                }

                if (readingIngredients) {
                    // Example: Onion <1x>
                    int lt = line.indexOf('<');
                    int gt = line.indexOf('>');

                    if (lt == -1 || gt == -1) {
                        continue;
                    }

                    String name = line.substring(0, lt).trim();
                    String amtStr = line.substring(lt + 1, gt).replace("x", "").trim();

                    double amount = Double.parseDouble(amtStr);

                    // You already normalize elsewhere
                    ingredients.add(new IngredientLine(name, amount, "unit"));
                }
            }

        } catch (Exception e) {
            throw new RuntimeException("Failed to load recipe txt", e);
        }

        return new Recipe(recipeName, steps, ingredients, imgFilePath);
    }
    
    public static Recipe fromTxtFile(String recipeName,File file,String imgFilePath) throws Exception {

        List<String> steps = new ArrayList<>();
        List<IngredientLine> ingredients = new ArrayList<>();

        boolean readingSteps = false;
        boolean readingIngredients = false;

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {

            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;

                if (line.equalsIgnoreCase("Steps:")) {
                    readingSteps = true;
                    readingIngredients = false;
                    continue;
                }

                if (line.equalsIgnoreCase("Ingredients:")) {
                    readingSteps = false;
                    readingIngredients = true;
                    continue;
                }

                if (readingSteps) {
                    line = line.replaceFirst("^\\d+\\.\\s*", "");
                    steps.add(line);
                }

                if (readingIngredients) {
                    int lt = line.indexOf('<');
                    int gt = line.indexOf('>');

                    if (lt == -1 || gt == -1) continue;

                    String name = line.substring(0, lt).trim();
                    String amtStr = line.substring(lt + 1, gt).replace("x", "").trim();

                    double amount = Double.parseDouble(amtStr);

                    ingredients.add(
                        new IngredientLine(name, amount, "unit")
                    );
                }
            }
        }

        return new Recipe(recipeName, steps, ingredients, imgFilePath);
    }

}
