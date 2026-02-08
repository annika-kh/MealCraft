import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.scene.text.Font;
import java.util.Optional;
import java.io.File;
import java.io.FileWriter;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Main UI controller.
 */
public class App extends Application {
    // Core data
    /** fridge data model */
    private Fridge fridge;

    /** selected food item */
    private FoodItem selectedFoodItem;

    /** selected recipe */
    private Recipe selectedRecipe;

    // Navigation buttons
    /** inventory tab */
    private Button tabInventory;
    /** shopping tab */
    private Button tabShopping;
    /** recipes tab */
    private Button tabRecipes;

    // Page containers
    /** main page host */
    private StackPane pageHost;
    /** inventory page */
    private Pane inventoryPage;
    /** shopping page */
    private Pane shoppingPage;
    /** recipes page */
    private Pane recipesPage;

    // Inventory UI components
    /** inventory grid */
    private GridPane inventoryGrid;
    /** item details box */
    private VBox itemDetailsBox;
    /** info box */
    private VBox infoBox;
    /** inventory sort dropdown */
    private ComboBox<String> inventorySortBox;

    // Recipe UI components
    /** recipe grid */
    private GridPane recipeGrid;
    /** recipe book box */
    private VBox recipeBookBox;
    /** recipe sort mode */
    private ComboBox<String> recipeSortBox;

    // Shopping UI components
    /** shopping list box */
    private VBox shoppingListBox;

    // UI style constants
    /** background color */
    private static final String BG = "#9c9c9c";
    /** panel background color */
    private static final String PANEL_BG = "#d6d6d6";
    /** border color */
    private static final String BORDER = "#2b2b2b";
    /** button background color */
    private static final String BTN_BG = "#6e6e6e";
    /** button border color */
    private static final String BTN_BORDER = "#404040";
    /** selected tab color */
    private static final String TAB_SELECTED = "#3f3f3f";
    /** alert color */
    private static final String RED = "#d95b57";

    public static void main(String[] args) {
        launch(args);
    }

    /** 
     * Initializes and displays the main application window.
     * 
     * @param stage primary application stage
     */
    public void start(Stage stage) {
        // Loads custom font
        Font.loadFont(getClass().getResourceAsStream("/fonts/PixelifySans-Regular.ttf"), 12);
        
        // Builds fridge data
        fridge = buildFridge();
        
        // Main layout
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #bdbdbd; -fx-font-family: 'Pixelify Sans';");

        // Top bar with title and navigation tabs
        HBox top = buildTopBar();
        root.setTop(top);

        // Container that holds all the pages
        pageHost = new StackPane();
        pageHost.setPadding(new Insets(10));
        root.setCenter(pageHost);

        // Buils individual pages
        inventoryPage = buildInventoryPage();
        recipesPage = buildRecipesPage();
        shoppingPage = buildShoppingPage();

        // Adds pages to host
        pageHost.getChildren().addAll(inventoryPage, recipesPage, shoppingPage);

        // Shows inventory page by default
        showPage("Inventory");

        // Creates and displays scene
        Scene scene = new Scene(root, 1100, 650);
        stage.setTitle("MealCraft");
        stage.setScene(scene);
        stage.show();

        // Refreshes UI to sync with initial data
        refreshAll();
    }

    // Top bar and tab handling
    /**
     * Builds the top navigation bar containing the title and tab buttons.
     * 
     * @return bar configured HBox for the top bar
     */
    private HBox buildTopBar() {
        HBox bar = new HBox(18);
        bar.setPadding(new Insets(12, 14, 8, 14));
        bar.setAlignment(Pos.CENTER_LEFT);

        // Application title
        Label title = new Label("MealCraft");
        title.setStyle("-fx-font-size: 40px; -fx-font-weight: bold;");

        // Creates tab buttons
        tabInventory = makeTabButton("Inventory");
        tabShopping = makeTabButton("Shopping List");
        tabRecipes = makeTabButton("Recipes");

        // Pushes tabs to the right
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Container for tab buttons
        HBox tabs = new HBox(10, tabInventory, tabShopping, tabRecipes);
        tabs.setAlignment(Pos.CENTER);

        bar.getChildren().addAll(title, spacer, tabs);
        return bar;
    }

    /** 
     * Creates a styled tab button with click behavior.
     * 
     * @param text label for the tab
     * @return b configured Button
     */
    private Button makeTabButton(String text) {
        Button b = new Button(text);
        // Applies default styling
        styleTab(b, false);
        // Switches pages on click
        b.setOnAction(e -> showPage(text));
        return b;
    }

    /**
     * Displays the selected page and updates tab styles.
     * 
     * @param which name of the page to show
     */
    private void showPage(String which) {
        // Toggles page visibility
        inventoryPage.setVisible(which.equals("Inventory"));
        recipesPage.setVisible(which.equals("Recipes"));
        shoppingPage.setVisible(which.equals("Shopping List"));

        // Updates tab highlight states
        styleTab(tabInventory, which.equals("Inventory"));
        styleTab(tabRecipes, which.equals("Recipes"));
        styleTab(tabShopping, which.equals("Shopping List"));

        // Refreshes UI after page change
        refreshAll();
    }

    /** 
     * Applies styling to a tab button based on selection state.
     * 
     * @param b tab button
     * @param selected whether this tab is currently active
     */
    private void styleTab(Button b, boolean selected) {
        b.setStyle("-fx-background-color: " + (selected ? TAB_SELECTED : BTN_BG) + ";" + "-fx-text-fill: white;" + "-fx-font-size: 14px;" + "-fx-padding: 8 18 8 18;" + "-fx-border-color: " + BTN_BORDER + ";" + "-fx-border-width: 2px;");
    }

    // Inventory page
    /**
     * Builds the Inventory page layout.
     * 
     * @return row inventory page root pane
     * 
     * Contributed by: Annika Hambali
     */
    private Pane buildInventoryPage() {
        // Main horizontal layout
        HBox row = new HBox(22);
        row.setPadding(new Insets(6));

        // Inventory grid (left panel)
        VBox leftPanel = panelBox("FOOD STOCK");
        
        // Sorting dropdown aligned to the right
        HBox topLine = new HBox();
        topLine.setAlignment(Pos.CENTER_RIGHT);

        inventorySortBox = new ComboBox<>();
        inventorySortBox.getItems().addAll("A-Z", "Expiration Date");
        // Default sort mode
        inventorySortBox.setValue("A-Z");
        inventorySortBox.setPrefWidth(160);
        
        // Refreshes grid when sort option changes
        inventorySortBox.setOnAction(e -> refreshInventoryGrid());
        topLine.getChildren().add(inventorySortBox);

        // Grid that holds food item tiles
        inventoryGrid = new GridPane();
        inventoryGrid.setHgap(8);
        inventoryGrid.setVgap(8);
        inventoryGrid.setPadding(new Insets(10, 0, 10, 0));

        Button addItem = new Button("Add Item");
        styleButton(addItem);
        
        // Opens dialog to add a new food item
        addItem.setOnAction(e -> addItemDialog((Stage) addItem.getScene().getWindow()));

        leftPanel.getChildren().addAll(topLine, inventoryGrid, alignBottom(addItem));

        // Item details (middle panel)
        VBox midPanel = panelBox("ITEM DETAILS");
        itemDetailsBox = new VBox(10);
        itemDetailsBox.setPadding(new Insets(8));
        midPanel.getChildren().add(itemDetailsBox);

        // Info panel (right panel)
        VBox rightPanel = panelBox("INFO");
        infoBox = new VBox(14);
        infoBox.setPadding(new Insets(8));
        rightPanel.getChildren().add(infoBox);

        row.getChildren().addAll(leftPanel, midPanel, rightPanel);

        // Set consistent widths across panels
        leftPanel.setPrefWidth(350);
        midPanel.setPrefWidth(350);
        rightPanel.setPrefWidth(350);

        return row;
    }

    /**
     * Rebuilds the inventory grid UI based on the current sort option.
     * 
     * Contributed by: Annika Hambali
     */
    private void refreshInventoryGrid() {
        inventoryGrid.getChildren().clear();

        String sort = inventorySortBox.getValue();
        List<FoodItem> items;

        // If sorting by expiration
        if (sort != null && sort.equals("Expiration Date")) {
            items = fridge.getAllFoodItemsSortedExpiration();
        } 
        // Default sorting A-Z
        else {
            items = fridge.getAllFoodItemsSortedAZ();
        }
        
        // Fixed grid layout
        int cols = 5;
        int maxTiles = 25; // adjust to match your layout
        int tiles = Math.max(maxTiles, items.size());

        for (int i = 0; i < tiles; i++) {
            StackPane tile = makeTile();

            // If there is an item for this slot
            if (i < items.size()) {
                FoodItem it = items.get(i);

                // Item image
                ImageView iv = new ImageView(loadImageSafe(it.getImgFilePath()));
                iv.setFitWidth(46);
                iv.setFitHeight(46);
                iv.setPreserveRatio(true);

                // Quantity label
                Label qty = new Label(formatQty(it.getQuantity()));
                qty.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

                // Overlay badge for quantity
                StackPane qtyBadge = new StackPane(qty);
                qtyBadge.setStyle("-fx-background-color: rgba(255,255,255,0.85); -fx-padding: 2 6 2 6;");
                StackPane.setAlignment(qtyBadge, Pos.TOP_LEFT);

                tile.getChildren().addAll(iv, qtyBadge);

                // Selects the item and refreshes the details
                tile.setOnMouseClicked(e -> {
                    selectedFoodItem = it;
                    refreshItemDetails();
                });
            } 
            // If no item
            else {
                // Renders empty placeholder tile
                tile.setStyle(tile.getStyle() + "-fx-background-color: #bcbcbc;");
            }

            // Places tile in grid
            inventoryGrid.add(tile, i % cols, i / cols);
        }
    }

    /**
     * Refreshes the item details panel based on the currently selected food item.
     * 
     * Contributed by: Annika Hambali
     */
    private void refreshItemDetails() {
        itemDetailsBox.getChildren().clear();

        // If nothing is selected
        if (selectedFoodItem == null) {
            Label msg = new Label("(Click an item to see details)");
            itemDetailsBox.getChildren().add(msg);
            return;
        }

        // Large preview image
        ImageView big = new ImageView(loadImageSafe(selectedFoodItem.getImgFilePath()));
        big.setFitWidth(90);
        big.setFitHeight(90);
        big.setPreserveRatio(true);

        Label name = new Label(selectedFoodItem.getName());
        name.setStyle("-fx-font-size: 22px; -fx-font-weight: bold;");

        // Displays item metadata
        Label cat = new Label("CATEGORY: " + selectedFoodItem.getCategory());
        Label qty = new Label("QUANTITY: " + formatQty(selectedFoodItem.getQuantity()));
        Label exp = new Label("EXPIRES: " + selectedFoodItem.getExpirationDate());

        Button edit = new Button("edit item");
        styleButton(edit);
        
        // Opens edit dialog for selected item
        edit.setOnAction(e -> editItemDialog((Stage) edit.getScene().getWindow(), selectedFoodItem));

        itemDetailsBox.getChildren().addAll(big, name, cat, qty, exp, edit);
    }

    /**
     * Refreshes the info panel.
     * 
     * Contributed by: Annika Hambali
     */
    private void refreshInfoPanel() {
        // Clears previous info entries
        infoBox.getChildren().clear();

        // Use soon section
        Label useSoon = new Label("USE SOON:");
        useSoon.setStyle("-fx-text-fill: " + RED + "; -fx-font-size: 16px; -fx-font-weight: bold;");
        VBox useSoonList = new VBox(4);

        List<FoodItem> soon = fridge.getItemsExpiringWithin(3);
        
        // If no items expiring soon
        if (soon.isEmpty()) {
            useSoonList.getChildren().add(new Label("• (none)"));
        } 
        else {
            // Lists each item and days remaining
            for (FoodItem f : soon) {
                long days = f.daysUntilExpiration(LocalDate.now());
                useSoonList.getChildren().add(new Label("• " + f.getName().toUpperCase() + " (expires " + days + " day)"));
            }
        }

        // Low stock section
        Label low = new Label("LOW STOCK:");
        low.setStyle("-fx-text-fill: " + RED + "; -fx-font-size: 16px; -fx-font-weight: bold;");
        VBox lowList = new VBox(4);

        List<FoodItem> lowStock = fridge.getLowStockItems();
        // If no items are low stock
        if (lowStock.isEmpty()) {
            lowList.getChildren().add(new Label("• (none)"));
        } 
        else {
            // Lists each low stock item and quantity
            for (FoodItem f : lowStock) {
                lowList.getChildren().add(new Label("• " + f.getName().toUpperCase() + " (x" + formatQty(f.getQuantity()) + ")"));
            }
        }

        infoBox.getChildren().addAll(useSoon, useSoonList, new Label(""), low, lowList);
    }

    // Recipes Page
    /**
     * Builds the page layout for the Recipe page with a grid of recipes and a description
     * 
     * Contributed by: Jessie Luo
     */
        /**
     * Builds the page layout for the Recipe page with a grid of recipes and a description
     * 
     * Contributed by: Jessie Luo
     */
    private Pane buildRecipesPage() {
        HBox row = new HBox(22);
        row.setPadding(new Insets(6));

        VBox leftPanel = panelBox("RECIPE");
        HBox topLine = new HBox();
        topLine.setAlignment(Pos.CENTER_RIGHT);

        //recipe sort dropdown menu
        recipeSortBox = new ComboBox<>();
        recipeSortBox.getItems().addAll("A-Z","Ingredient availability","Uses expiring ingredients");
        recipeSortBox.setValue("A-Z");
        recipeSortBox.setPrefWidth(220);
        recipeSortBox.setOnAction(e -> refreshRecipeGrid());
        topLine.getChildren().add(recipeSortBox);
        recipeGrid = new GridPane();
        recipeGrid.setHgap(8);
        recipeGrid.setVgap(8);
        recipeGrid.setPadding(new Insets(10, 0, 10, 0));

        //add recipe button
        Button addRecipe = new Button("add recipe");
        styleButton(addRecipe);
        addRecipe.setOnAction(e -> addRecipeDialog());
        leftPanel.getChildren().addAll(topLine, recipeGrid, alignBottom(addRecipe));
        leftPanel.setPrefWidth(350);

        //recipe description gui
        recipeBookBox = new VBox(10);
        recipeBookBox.setPadding(new Insets(12));
        recipeBookBox.setStyle(
                "-fx-background-color: #f4f0e6;" +
                "-fx-border-color: #6b4a2b;" +
                "-fx-border-width: 4px;" +
                "-fx-background-radius: 8;" +
                "-fx-border-radius: 8;"
        );
        recipeBookBox.setPrefWidth(700);
        recipeBookBox.setMinHeight(500);

        row.getChildren().addAll(leftPanel, recipeBookBox);
        return row;
    }

    /**
     * Rebuilds the grid of recipes whenever the sort mode changes, a recipe is added, or inventory updates
     * 
     * Contributed by: Jessie Luo
     */
     private void refreshRecipeGrid() {
        recipeGrid.getChildren().clear();
        List<Recipe> recipes = new ArrayList<>(fridge.getRecipes());
        
        //checks if sort mode changes
        String sort = recipeSortBox.getValue();
        if (sort == null || sort.equals("A-Z")) {
            sortRecipesAZ(recipes);
        }
        else if (sort.equals("Ingredient availability")) {
            sortByIngredientAvailability(recipes);
        }
        else if (sort.equals("Uses expiring ingredients")) {
            sortByExpiringIngredients(recipes);
        }

        int cols = 5;
        int maxTiles = 20;
        int tiles = Math.max(maxTiles, recipes.size());
        
        //adds the recipe image into the tiles
        for (int i = 0; i < tiles; i++) {
            StackPane tile = makeTile();
            if (i < recipes.size()) {
                Recipe r = recipes.get(i);
                ImageView iv = new ImageView(loadImageSafe(r.getImgFilePath()));
                iv.setFitWidth(46);
                iv.setFitHeight(46);
                iv.setPreserveRatio(true);
                tile.getChildren().add(iv);
                tile.setOnMouseClicked(e -> {
                    selectedRecipe = r;
                    refreshRecipeBook();
                });
            } else {
                tile.setStyle(tile.getStyle() + "-fx-background-color: #bcbcbc;");
            }
            recipeGrid.add(tile, i % cols, i / cols);
        }
    }
    
    /**
     * Uploading the recipe description based on what recipe the user clicks on & the inventory after cooking
     * 
     * Contributed by: Jessie Luo
     */
    private void refreshRecipeBook() {
        recipeBookBox.getChildren().clear();
        if (selectedRecipe == null) {
            recipeBookBox.getChildren().add(new Label("(Click a recipe to view it)"));
            return;
        }
        //recipe name
        Label title = new Label(selectedRecipe.getName());
        title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold;");
        //image
        ImageView img = new ImageView(loadImageSafe(selectedRecipe.getImgFilePath()));
        img.setFitWidth(140);
        img.setFitHeight(140);
        img.setPreserveRatio(true);
        //ingredients
        VBox ingBox = new VBox(4);
        Label ingTitle = new Label("Ingredients:");
        ingTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
        ingBox.getChildren().add(ingTitle);

        for (IngredientLine ing : selectedRecipe.getIngredients()) {
            //get the ingredient from the fridge
            FoodItem have = fridge.getFoodItem(ing.getNormalizedName());
            Label line = new Label(
                "• " + ing.getNormalizedName().toUpperCase() +
                " (" + formatQty(ing.getAmount()) + " " + ing.getUnit() + ")"
            );
            //check if the ingredient is missing or not enough for the recipe
            if (have == null) {
                //makes the name red if there's not enough for the recipe
                line.setStyle("-fx-text-fill: " + RED + "; -fx-font-weight: bold;");
            } else if (have.getQuantity() < ing.getAmount()) {
                line.setStyle("-fx-text-fill: " + RED + "; -fx-font-weight: bold;");
            }
            ingBox.getChildren().add(line);
        }

        //steps
        VBox stepBox = new VBox(4);
        Label stepTitle = new Label("Steps:");
        stepTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
        stepBox.getChildren().add(stepTitle);

        int idx = 1;
        for (String s : selectedRecipe.getSteps()) {
            Label stepLabel = new Label(idx + ". " + s);
            stepLabel.setWrapText(true);
            stepLabel.setMaxWidth(330);   
            stepBox.getChildren().add(stepLabel);
            idx++;
        }

        //buttons
        HBox actions = new HBox(12);
        Button addToList = new Button("Add To Shopping List");
        styleButton(addToList);

        Button cook = new Button("Cook");
        cook.setStyle(
                "-fx-background-color: " + RED + ";" +
                "-fx-text-fill: white;" +
                "-fx-border-color: #7a2a28;" +
                "-fx-border-width: 2px;" +
                "-fx-padding: 8 14 8 14;"
        );

        addToList.setOnAction(e -> {
            addMissingIngredientsToShoppingList(selectedRecipe);
            showPage("shopping list");
            refreshShoppingList();
        });

        cook.setOnAction(e -> {
            boolean ok = selectedRecipe.cook(fridge);
            if (!ok) {
                alert("Not enough ingredients to cook this recipe.");
            } else {
                refreshAll();
                alert("Cooked! Inventory updated.");
            }
        });
        
        actions.getChildren().addAll(addToList, cook);

        //making a minecraft book-like layout
        HBox topRow = new HBox(20);
        VBox left = new VBox(10, title, img, ingBox, actions);
        VBox right = new VBox(10, stepBox);
        left.setPrefWidth(320);
        topRow.getChildren().addAll(left, right);
        recipeBookBox.getChildren().add(topRow);
    }
    
    /**
     * Sorts a list of recipes based on alphabetical order
     * 
     * @param recipes is the list of recipes 
     * 
     * Contributed by: Jessie Luo
     */
    private void sortRecipesAZ(List<Recipe> recipes) {
        for (int i = 0; i < recipes.size(); i++) {
            for (int j = i + 1; j < recipes.size(); j++) {
                String name1 = recipes.get(i).getName();
                String name2 = recipes.get(j).getName();
                if (name1.compareToIgnoreCase(name2) > 0) {
                    Recipe temp = recipes.get(i);
                    recipes.set(i, recipes.get(j));
                    recipes.set(j, temp);
                }
            }
        }
    }
    
    /**
     * Sorts a list of recipes based on ingredient availabilty 
     * (moves recipes with less missing ingredients to the front)
     * 
     * @param recipes is the list of recipes 
     * 
     * Contributed by: Jessie Luo
     */
    private void sortByIngredientAvailability(List<Recipe> recipes) {
        for (int i = 0; i < recipes.size(); i++) {
            for (int j = i + 1; j < recipes.size(); j++) {
                double missing1 = recipes.get(i).getMissingIngredients(fridge);
                double missing2 = recipes.get(j).getMissingIngredients(fridge);
                if (missing1 > missing2) {
                    Recipe temp = recipes.get(i);
                    recipes.set(i, recipes.get(j));
                    recipes.set(j, temp);
                }
            }
        }
    }
    
    /**
     * Sorts a list of recipes based on expirationdate 
     * (moves recipes with ingredients that are expiring the soonest to the front)
     * 
     * @param recipes is the list of recipes 
     * 
     * Contributed by: Jessie Luo
     */
    private void sortByExpiringIngredients(List<Recipe> recipes) {
        for (int i = 0; i < recipes.size(); i++) {
            for (int j = i + 1; j < recipes.size(); j++) {

                long days1 = recipes.get(i).getEarliestExpirationDays(fridge);
                long days2 = recipes.get(j).getEarliestExpirationDays(fridge);

                if (days1 > days2) {
                    Recipe temp = recipes.get(i);
                    recipes.set(i, recipes.get(j));
                    recipes.set(j, temp);
                }
            }
        }
    }
    
    /**
     * Adds the missing ingredients in a recipe to the shopping list
     * 
     * @param recipe
     * 
     * Contributed by: Jessie Luo
     */
    private void addMissingIngredientsToShoppingList(Recipe recipe) {
        if (recipe == null){
            return;
        }
        
        for (IngredientLine ing : recipe.getIngredients()) {
            FoodItem have = fridge.getFoodItem(ing.getNormalizedName());
            double haveAmount = 0;
            if (have != null) {
                haveAmount = have.getQuantity();
            }
            double needAmount = ing.getAmount();
            if (haveAmount < needAmount) {
                double missing = needAmount - haveAmount;
                // add missing amount to shopping list
                fridge.addShoppingListItem(ing.getNormalizedName(), missing, ing.getUnit());
            }
        }
    }

    /**
     * prompts user to choose a recipe text file to add
     * 
     * Contributed by: Jessie Luo
     */
    private void addRecipeDialog() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Choose Recipe Text File");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Text Files", "*.txt"));

        File file = chooser.showOpenDialog(null);
        if (file == null){
            return;
        }

        TextInputDialog nameDialog = new TextInputDialog();
        nameDialog.setTitle("Recipe Name");
        nameDialog.setHeaderText("Enter a name for this recipe");
        nameDialog.setContentText("Recipe name:");

        Optional<String> result = nameDialog.showAndWait();
        if (result.isEmpty()){
            return;
        }

        try {
            Recipe recipe = Recipe.fromTxtFile(result.get(),file,"fooditem-images/default_recipe.png");
            fridge.addRecipe(recipe);
            refreshRecipeGrid();
        } catch (Exception ex) {
            alert("Failed to load recipe file.\n" + ex.getMessage());
        }
    }

    // Shopping list page
    private Pane buildShoppingPage() {
        HBox row = new HBox();
        row.setPadding(new Insets(6));
        row.setAlignment(Pos.TOP_CENTER);

        VBox paper = new VBox(10);
        paper.setPadding(new Insets(18));
        paper.setPrefWidth(520);
        paper.setMinHeight(540);

        paper.setStyle(
                "-fx-background-color: #f7f3ea;" +
                "-fx-border-color: #6b4a2b;" +
                "-fx-border-width: 4px;" +
                "-fx-background-radius: 8;" +
                "-fx-border-radius: 8;"
        );

        Label title = new Label("Shopping List");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

        shoppingListBox = new VBox(8);

        HBox buttons = new HBox(12);
        Button add = new Button("Add Item");
        styleButton(add);
        add.setOnAction(e -> addShoppingItemDialog());

        Button export = new Button("Export TXT");
        styleButton(export);
        export.setOnAction(e -> exportShoppingList(stageFrom(row)));

        buttons.getChildren().addAll(add, export);

        paper.getChildren().addAll(title, shoppingListBox, new Label(""), buttons);
        row.getChildren().add(paper);
        return row;
    }

    private void refreshShoppingList() {
        shoppingListBox.getChildren().clear();
        
        List<IngredientLine> lines = new ArrayList<>(fridge.getShoppingListItems());
        lines.sort(Comparator.comparing(IngredientLine::getNormalizedName));

        if (lines.isEmpty()) {
            shoppingListBox.getChildren().add(new Label("• (none)"));
            return;
        }

        for (IngredientLine line : lines) {
            HBox row = new HBox(10);
            row.setAlignment(Pos.CENTER_LEFT);

            Label txt = new Label("• " + line.getNormalizedName().toUpperCase() + " (x" + formatQty(line.getAmount()) + " " + line.getUnit().toUpperCase() + ")");
            txt.setStyle("-fx-font-size: 14px;");

            Button x = new Button("x");
            x.setStyle(
                    "-fx-background-color: transparent;" +
                    "-fx-text-fill: " + RED + ";" +
                    "-fx-font-weight: bold;" +
                    "-fx-font-size: 14px;"
            );

            String key = line.getNormalizedName();
            
            // remove 1 unit each click (you can change to line.getAmount() to remove all)
            x.setOnAction(e -> {
                fridge.removeShoppingListItem(line.getNormalizedName(), 1);
                refreshShoppingList();
            });

            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            row.getChildren().addAll(txt, spacer, x);
            shoppingListBox.getChildren().add(row);
        }
    }

    // Dialogues
    /**
     * Opens a dialog that collects user input and adds a new FoodItem to the fridge.
     * 
     * @param stage the parent window used for the file chooser
     */
    private void addItemDialog(Stage stage) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Add Food Item");
    
        ButtonType addBtn = new ButtonType("Add", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addBtn, ButtonType.CANCEL);
    
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(10));
    
        // Input fields
        TextField nameField = new TextField();
        // Default quantity
        TextField qtyField = new TextField("1");
        // Default unit
        TextField unitField = new TextField("x");
    
        // Category dropdown
        ComboBox<Category> catBox = new ComboBox<>();
        catBox.getItems().addAll(Category.values());
        // Default category
        catBox.setValue(Category.OTHER);
    
        // Expiration date picker
        DatePicker expPicker = new DatePicker(LocalDate.now().plusDays(7));
    
        // Image path field
        TextField imgField = new TextField();
        imgField.setEditable(false);
    
        Button browse = new Button("Browse...");
        styleButton(browse);
    
        // File chooser for selecting an image
        browse.setOnAction(e -> {
            FileChooser chooser = new FileChooser();
            chooser.setTitle("Choose Image");
            chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg"));
            File file = chooser.showOpenDialog(stage);
            // If a file was selected
            if (file != null) {
                // Stores as file:/ URL
                imgField.setText(file.toURI().toString()); 
            }
        });
    
        // Adds form fields to the grid
        grid.add(new Label("Name:"), 0, 0);
        grid.add(nameField, 1, 0);
    
        grid.add(new Label("Quantity:"), 0, 1);
        grid.add(qtyField, 1, 1);
    
        grid.add(new Label("Unit:"), 0, 2);
        grid.add(unitField, 1, 2);
    
        grid.add(new Label("Category:"), 0, 3);
        grid.add(catBox, 1, 3);
    
        grid.add(new Label("Expiration:"), 0, 4);
        grid.add(expPicker, 1, 4);
    
        grid.add(new Label("Image:"), 0, 5);
        grid.add(new HBox(10, imgField, browse), 1, 5);
    
        dialog.getDialogPane().setContent(grid);
    
        // Handles add button submission
        dialog.showAndWait().ifPresent(result -> {
            if (result == addBtn) {
                try {
                    String name = nameField.getText().trim();
                    double qty = Double.parseDouble(qtyField.getText().trim());
                    String unit = unitField.getText().trim();
                    Category cat = catBox.getValue();
                    LocalDate exp = expPicker.getValue();
                    String img = imgField.getText().trim();
                    
                    // If they didn't pick an image, store empty string and loader will use placeholder
                    if (img.isBlank()) {
                        img = imagePathForItemName(name);  // <--name+png
                    }
    
                    if (exp == null) exp = LocalDate.now().plusDays(7);
                    if (cat == null) cat = Category.OTHER;
    
                    fridge.addFood(new FoodItem(name, qty, unit, cat, exp, img));
    
                    refreshAll();
    
                } catch (Exception ex) {
                    alert("Invalid input. Check quantity and fields.");
                }
            }
        });
    }

    private void editItemDialog(Stage stage, FoodItem item) {
        if (item == null) {
            alert("No item selected.");
        } else {
    
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setTitle("Edit Item");
    
            ButtonType saveBtn = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes().addAll(saveBtn, ButtonType.CANCEL);
    
            GridPane grid = new GridPane();
            grid.setHgap(10);
            grid.setVgap(10);
            grid.setPadding(new Insets(10));
    
            Label nameLabel = new Label(item.getName());
    
            TextField qtyField = new TextField(String.valueOf(item.getQuantity()));
    
            ComboBox<Category> catBox = new ComboBox<>();
            catBox.getItems().addAll(Category.values());
            catBox.setValue(item.getCategory());
    
            DatePicker expPicker = new DatePicker(item.getExpirationDate());
    
            TextField imgField = new TextField(item.getImgFilePath());
            imgField.setEditable(false);
    
            Button browse = new Button("Browse...");
            styleButton(browse);
    
            browse.setOnAction(e -> {
                FileChooser chooser = new FileChooser();
                chooser.setTitle("Choose Image");
                chooser.getExtensionFilters().add(
                        new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg")
                );
                File file = chooser.showOpenDialog(stage);
                if (file != null) {
                    imgField.setText(file.toURI().toString()); // file:/... url
                }
            });
    
            grid.add(new Label("Item:"), 0, 0);
            grid.add(nameLabel, 1, 0);
    
            grid.add(new Label("Quantity:"), 0, 1);
            grid.add(qtyField, 1, 1);
    
            grid.add(new Label("Category:"), 0, 2);
            grid.add(catBox, 1, 2);
    
            grid.add(new Label("Expiration:"), 0, 3);
            grid.add(expPicker, 1, 3);
    
            grid.add(new Label("Image:"), 0, 4);
            grid.add(new HBox(10, imgField, browse), 1, 4);
    
            dialog.getDialogPane().setContent(grid);
    
            dialog.showAndWait().ifPresent(result -> {
                if (result == saveBtn) {
                    try {
                        double newQty = Double.parseDouble(qtyField.getText().trim());
                        Category newCat = catBox.getValue();
                        LocalDate newExp = expPicker.getValue();
                        String newImg = imgField.getText().trim();
    
                        // Use your "setQuantity using add/subtract"
                        item.setQuantity(newQty);
    
                        if (newCat != null) item.setCategory(newCat);
                        if (newExp != null) item.setExpirationDate(newExp);
    
                        // Only if you have setter; if not, skip this line
                        // item.setImgFilePath(newImg);
    
                        fridge.rebuildExpirationIndex(); // expiration changed
                        refreshAll();
    
                    } catch (Exception ex) {
                        alert("Invalid input. Quantity must be a number.");
                    }
                }
            });
        }
    }

    private void addShoppingItemDialog() {
        TextInputDialog d = new TextInputDialog("ex. milk");
        d.setTitle("Add Shopping Item");
        d.setHeaderText("Enter item name to add:");
        d.setContentText("Name:");
        d.showAndWait().ifPresent(name -> {
            // easiest: add 1 unit in shopping list by regenerating then manually adjusting
            fridge.addShoppingListItem(name, 1, "");
            // You can add a dedicated "addShoppingListItem" method later if you want.
            refreshShoppingList();
        });
    }

    /**
     * Exports the shopping list to a text file.
     */
    private void exportShoppingList(Stage stage) {
        if (stage != null) {
    
            try {
                // Make sure shopping list is up to date
                fridge.createShoppingList();
    
                FileChooser chooser = new FileChooser();
                chooser.setTitle("Save Shopping List");
                chooser.getExtensionFilters()
                        .add(new FileChooser.ExtensionFilter("Text Files", "*.txt"));
                chooser.setInitialFileName("shopping-list.txt");
    
                File file = chooser.showSaveDialog(stage);
    
                if (file != null) {
    
                    FileWriter fw = new FileWriter(file);
    
                    for (IngredientLine line : fridge.getShoppingListItems()) {
                        fw.write(line.getNormalizedName() + " (x" +
                                 line.getAmount() + " " +
                                 line.getUnit() + ")\n");
                    }
    
                    fw.close();
                    alert("Exported!");
                }
    
            } catch (Exception e) {
                alert("Export failed: " + e.getMessage());
            }
        }
    }

    // UI helpers
    /** 
     * Creates a styled panel container with a title label.
     * 
     * @param title panel title text
     * @return box styled VBox panel
     */
    private VBox panelBox(String title) {
        VBox box = new VBox(10);
        box.setPadding(new Insets(14));
        box.setStyle("-fx-background-color: " + PANEL_BG + ";" + "-fx-border-color: " + BORDER + ";" + "-fx-border-width: 3px;");

        Label t = new Label(title);
        t.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        box.getChildren().add(t);
        return box;
    }

    /**
     * Creates a style empty inventory tile.
     * 
     * @return tile StackPane tile
     */
    private StackPane makeTile() {
        StackPane tile = new StackPane();
        tile.setPrefSize(55, 55);
        tile.setStyle("-fx-background-color: #ececec;" + "-fx-border-color: " + BORDER + ";" + "-fx-border-width: 2px;");
        return tile;
    }

    /** Applies the standard button style used throughout the UI.
     * 
     * @param b button to style
     */
    private void styleButton(Button b) {
        b.setStyle("-fx-background-color: " + BTN_BG + ";" + "-fx-text-fill: white;" + "-fx-border-color: " + BTN_BORDER + ";" + "-fx-border-width: 2px;" + "-fx-padding: 6 12 6 12;");
    }

    /**
     * Wraps a button in a container aligned to the bottom-right
     * 
     * @param b button to align
     * @return wrap Pane wrapper containing the button
     */
    private Pane alignBottom(Button b) {
        VBox wrap = new VBox();
        wrap.setAlignment(Pos.BOTTOM_RIGHT);
        wrap.getChildren().add(b);
        return wrap;
    }

    /**
     * Refreshes all UI sections to match the current application state.
     */
    private void refreshAll() {
        refreshInventoryGrid();
        refreshItemDetails();
        refreshInfoPanel();
        refreshRecipeGrid();
        refreshRecipeBook();
        refreshShoppingList();
    }

    /**
     * Shows a simple information alert dialog.
     * 
     * @param msg message to display
     */
    private void alert(String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }

    /**
     * Formats a quantity for display
     * 
     * @param q quantity value
     * @return formatted quantity string
     */
    private String formatQty(double q) {
        if (Math.abs(q - Math.round(q)) < 1e-9) {
            return String.valueOf((int) Math.round(q));
        }
        return String.valueOf(q);
    }

    /** 
     * Loads an image from a variety of possible locations.
     * Falls back to a placeholder if nothing can be loaded.
     */
    private Image loadImageSafe(String pathOrUrl) {
        // Direct URL or file URI
        if (pathOrUrl != null && !pathOrUrl.isBlank()) {
            try {
                if (pathOrUrl.startsWith("file:") || pathOrUrl.startsWith("http")) {
                    return new Image(pathOrUrl, true);
                }
            } catch (Exception ignore) {}
        }
    
        // Classpath resource
        if (pathOrUrl != null && !pathOrUrl.isBlank()) {
            try {
                String res = pathOrUrl.startsWith("/") ? pathOrUrl : "/" + pathOrUrl;
                var url = getClass().getResource(res);
                if (url != null) {
                    return new Image(url.toExternalForm(), true);
                }
            } catch (Exception ignore) {}
        }
    
        // Relative disk path
        if (pathOrUrl != null && !pathOrUrl.isBlank()) {
            try {
                File f = new File(pathOrUrl);
                if (f.exists()) {
                    return new Image(f.toURI().toString(), true);
                }
            } catch (Exception ignore) {}
        }
    
        // Fallback based on normalized name
        if (pathOrUrl != null && !pathOrUrl.isBlank()) {
            String normalized = pathOrUrl.toLowerCase().replaceAll("[^a-z0-9]+", "");
        
            Image byName = tryResourceThenDisk("/fooditem-images/" + normalized + ".png", "fooditem-images/" + normalized + ".png");
            if (byName != null) {
                return byName;
            }
        }
    
        // 5) Final fallback placeholder
        Image placeholder = tryResourceThenDisk("placeholder.png", "placeholder.png");
        if (placeholder != null) {
            return placeholder;
        }
    
        return new Image("data:,", true);
    }
    
    /**
     * Generates the default image path for a food item name.
     * 
     * @param itemName item display name
     * @return image file path
     */
    private String imagePathForItemName(String itemName) {
        if (itemName == null) {
            return "placeholder.png";
        }
        String normalized = itemName.trim().toLowerCase().replaceAll("\\s+", "");
        return "fooditem-images/" + normalized + ".png";
    }

    /**
     * Tries to load an image from a classpath resource first, then from disk.
     * 
     * @param resPath classpath resource path
     * @param diskPath disk path
     * @return Image if found, null otherwise
     */
    private Image tryResourceThenDisk(String resPath, String diskPath) {
        try {
            var url = getClass().getResource(resPath);
            if (url != null) {
                return new Image(url.toExternalForm(), true);
            }
        } catch (Exception ignore) {}
    
        try {
            File f = new File(diskPath);
            if (f.exists()) {
                return new Image(f.toURI().toString(), true);
            }
        } catch (Exception ignore) {}
    
        return null;
    }

    /**
     * Sets the Stage from any node currently on a Scene
     * 
     * @param anyNodeOnScene a node that is already attached to a Scene
     * @return Stage, or null if the node is not attached
     */
    private Stage stageFrom(Pane anyNodeOnScene) {
        if (anyNodeOnScene == null) {
            return null;
        }
        return (Stage) anyNodeOnScene.getScene().getWindow();
    }

    /**
     * Builds a sample fridge.
     * 
     * @return f fridge containing inventory, reecipes, and shopping list
     */
    private Fridge buildFridge() {
        Fridge f = new Fridge();

        // Inventory names
        f.addFood(new FoodItem("watermelon", 1, "x", Category.FRUITS_VEGETABLES, LocalDate.now().plusDays(7), "fooditem-images/watermelon.png"));
        f.addFood(new FoodItem("potato", 1, "x", Category.FRUITS_VEGETABLES, LocalDate.now().plusDays(1), "fooditem-images/potato.png"));
        f.addFood(new FoodItem("milk", 1, "cup", Category.DAIRY_EGGS, LocalDate.now().plusDays(2), "fooditem-images/milk.png"));
        f.addFood(new FoodItem("chicken", 1, "x", Category.PROTEINS, LocalDate.now().plusDays(3), "fooditem-images/chicken.png"));
        f.addFood(new FoodItem("tomato", 2, "x", Category.FRUITS_VEGETABLES, LocalDate.now().plusDays(8), "fooditem-images/tomato.png"));

        // Recipe ingredients
        List<IngredientLine> ing = new ArrayList<>();
        ing.add(new IngredientLine("potato", 2, "x"));
        ing.add(new IngredientLine("milk", 1, "cup"));
        ing.add(new IngredientLine("chicken", 1, "x"));

        // Recipe steps
        List<String> steps = new ArrayList<>();
        steps.add("Heat oil in a large pot, and brown the chicken");
        steps.add("In the same pot, add potatoes, and cook until soft");
        steps.add("Add water and milk");
        steps.add("Bring to a boil");

        // Recipe
        f.addRecipe(new Recipe("Annika Stew", steps, ing, "fooditem-images/annikastew.png"));

        // Initializes shopping list
        f.createShoppingList();
        
        return f;
    }
}
