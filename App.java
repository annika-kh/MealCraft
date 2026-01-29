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

import java.io.File;
import java.io.FileWriter;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * MealCraft UI (JavaFX) - interactive version matching mockups:
 * Inventory / Shopping List / Recipes.
 */
public class App extends Application {

    /** Main fridge data model */
    private Fridge fridge;

    /** Current selected FoodItem (Inventory page) */
    private FoodItem selectedFoodItem;

    /** Current selected Recipe (Recipes page) */
    private Recipe selectedRecipe;

    // ---- UI "tabs" ----
    private Button tabInventory;
    private Button tabShopping;
    private Button tabRecipes;

    // ---- Page containers ----
    private StackPane pageHost;
    private Pane inventoryPage;
    private Pane shoppingPage;
    private Pane recipesPage;

    // ---- Inventory UI pieces ----
    private GridPane inventoryGrid;
    private VBox itemDetailsBox;
    private VBox infoBox;

    // ---- Recipes UI pieces ----
    private GridPane recipeGrid;
    private VBox recipeBookBox;

    // ---- Shopping UI pieces ----
    private VBox shoppingListBox;

    // ---- Simple style constants
    private static final String BG = "#BDBDBD";
    private static final String PANEL_BG = "#d6d6d6";
    private static final String BORDER = "#2b2b2b";
    private static final String BTN_BG = "#6e6e6e";
    private static final String BTN_BORDER = "#404040";
    private static final String TAB_SELECTED = "#3f3f3f";
    private static final String RED = "#d95b57";

    public static void main(String[] args) {
        launch(args);
    }

    public void start(Stage stage) {
        // ----------------
        // Build sample data
        // ----------------
        fridge = buildDemoFridge(); // replace with your real loading later if you want

        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: " + BG + "; -fx-font-family: Pixelify Sans;");

        // Top bar: title + tabs
        HBox top = buildTopBar();
        root.setTop(top);

        // Pages
        pageHost = new StackPane();
        pageHost.setPadding(new Insets(10));
        root.setCenter(pageHost);

        inventoryPage = buildInventoryPage();
        recipesPage = buildRecipesPage();
        shoppingPage = buildShoppingPage();

        pageHost.getChildren().addAll(inventoryPage, recipesPage, shoppingPage);

        showPage("Inventory");

        Scene scene = new Scene(root, 1100, 650);
        stage.setTitle("MealCraft");
        stage.setScene(scene);
        stage.show();

        // initial selection defaults
        refreshAll();
    }

    // =========================================================
    // TOP BAR + TAB HANDLING
    // =========================================================

    private HBox buildTopBar() {
        HBox bar = new HBox(18);
        bar.setPadding(new Insets(12, 14, 8, 14));
        bar.setAlignment(Pos.CENTER_LEFT);

        Label title = new Label("MealCraft");
        title.setStyle("-fx-font-size: 40px; -fx-font-weight: bold;");

        tabInventory = makeTabButton("Inventory");
        tabShopping = makeTabButton("Shopping List");
        tabRecipes = makeTabButton("Recipes");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox tabs = new HBox(10, tabInventory, tabShopping, tabRecipes);
        tabs.setAlignment(Pos.CENTER);

        bar.getChildren().addAll(title, spacer, tabs);
        return bar;
    }

    private Button makeTabButton(String text) {
        Button b = new Button(text);
        styleTab(b, false);
        b.setOnAction(e -> showPage(text));
        return b;
    }

    private void showPage(String which) {
        inventoryPage.setVisible(which.equals("Inventory"));
        recipesPage.setVisible(which.equals("Recipes"));
        shoppingPage.setVisible(which.equals("Shopping List"));

        styleTab(tabInventory, which.equals("Inventory"));
        styleTab(tabRecipes, which.equals("Recipes"));
        styleTab(tabShopping, which.equals("Shopping List"));

        refreshAll();
    }

    private void styleTab(Button b, boolean selected) {
        b.setStyle(
                "-fx-background-color: " + (selected ? TAB_SELECTED : BTN_BG) + ";" +
                "-fx-text-fill: white;" +
                "-fx-font-size: 14px;" +
                "-fx-padding: 8 18 8 18;" +
                "-fx-border-color: " + BTN_BORDER + ";" +
                "-fx-border-width: 2px;"
        );
    }

    // =========================================================
    // INVENTORY PAGE (grid + item details + info)
    // =========================================================

    private Pane buildInventoryPage() {
        HBox row = new HBox(22);
        row.setPadding(new Insets(6));

        // Left panel: grid
        VBox leftPanel = panelBox("FOOD STOCK");
        HBox topLine = new HBox();
        topLine.setAlignment(Pos.CENTER_RIGHT);

        Button sort = new Button("sort by: a-z");
        styleButton(sort);
        sort.setOnAction(e -> {
            // already sorted by default; just refresh
            refreshInventoryGrid();
        });

        topLine.getChildren().add(sort);

        inventoryGrid = new GridPane();
        inventoryGrid.setHgap(8);
        inventoryGrid.setVgap(8);
        inventoryGrid.setPadding(new Insets(10, 0, 10, 0));

        Button addItem = new Button("add item");
        styleButton(addItem);
        addItem.setOnAction(e -> addItemDialog());

        leftPanel.getChildren().addAll(topLine, inventoryGrid, alignBottom(addItem));

        // Middle panel: item details
        VBox midPanel = panelBox("ITEM DETAILS");
        itemDetailsBox = new VBox(10);
        itemDetailsBox.setPadding(new Insets(8));
        midPanel.getChildren().add(itemDetailsBox);

        // Right panel: info
        VBox rightPanel = panelBox("INFO");
        infoBox = new VBox(14);
        infoBox.setPadding(new Insets(8));
        rightPanel.getChildren().add(infoBox);

        row.getChildren().addAll(leftPanel, midPanel, rightPanel);

        // make panels similar widths
        leftPanel.setPrefWidth(350);
        midPanel.setPrefWidth(350);
        rightPanel.setPrefWidth(350);

        return row;
    }

    private void refreshInventoryGrid() {
        inventoryGrid.getChildren().clear();

        // Pull items sorted A-Z (your Fridge method)
        List<FoodItem> items = fridge.getAllFoodItemsSortedAZ();

        // Grid size like mockup (5 columns x 6 rows)
        int cols = 5;
        int maxTiles = 25; // adjust to match your layout
        int tiles = Math.max(maxTiles, items.size());

        for (int i = 0; i < tiles; i++) {
            StackPane tile = makeTile();

            if (i < items.size()) {
                FoodItem it = items.get(i);

                ImageView iv = new ImageView(loadImageSafe(it.getImgFilePath()));
                iv.setFitWidth(46);
                iv.setFitHeight(46);
                iv.setPreserveRatio(true);

                Label qty = new Label(formatQty(it.getQuantity()));
                qty.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

                StackPane qtyBadge = new StackPane(qty);
                qtyBadge.setStyle("-fx-background-color: rgba(255,255,255,0.85); -fx-padding: 2 6 2 6;");
                StackPane.setAlignment(qtyBadge, Pos.TOP_LEFT);

                tile.getChildren().addAll(iv, qtyBadge);

                tile.setOnMouseClicked(e -> {
                    selectedFoodItem = it;
                    refreshItemDetails();
                });
            } else {
                tile.setStyle(tile.getStyle() + "-fx-background-color: #bcbcbc;");
            }

            inventoryGrid.add(tile, i % cols, i / cols);
        }
    }

    private void refreshItemDetails() {
        itemDetailsBox.getChildren().clear();

        if (selectedFoodItem == null) {
            Label msg = new Label("(Click an item to see details)");
            itemDetailsBox.getChildren().add(msg);
            return;
        }

        ImageView big = new ImageView(loadImageSafe(selectedFoodItem.getImgFilePath()));
        big.setFitWidth(90);
        big.setFitHeight(90);
        big.setPreserveRatio(true);

        Label name = new Label(selectedFoodItem.getName());
        name.setStyle("-fx-font-size: 22px; -fx-font-weight: bold;");

        Label cat = new Label("CATEGORY: " + selectedFoodItem.getCategory());
        Label qty = new Label("QUANTITY: " + formatQty(selectedFoodItem.getQuantity()));
        Label exp = new Label("EXPIRES: " + selectedFoodItem.getExpirationDate());

        Button edit = new Button("edit item");
        styleButton(edit);
        edit.setOnAction(e -> editItemDialog(selectedFoodItem));

        itemDetailsBox.getChildren().addAll(big, name, cat, qty, exp, edit);
    }

    private void refreshInfoPanel() {
        infoBox.getChildren().clear();

        // USE SOON (within 3 days)
        Label useSoon = new Label("USE SOON:");
        useSoon.setStyle("-fx-text-fill: " + RED + "; -fx-font-size: 16px; -fx-font-weight: bold;");
        VBox useSoonList = new VBox(4);

        List<FoodItem> soon = fridge.getItemsExpiringWithin(3);
        if (soon.isEmpty()) {
            useSoonList.getChildren().add(new Label("• (none)"));
        } else {
            for (FoodItem f : soon) {
                long days = f.daysUntilExpiration(LocalDate.now());
                useSoonList.getChildren().add(new Label("• " + f.getName().toUpperCase() + " (expires " + days + " day)"));
            }
        }

        // LOW STOCK
        Label low = new Label("LOW STOCK:");
        low.setStyle("-fx-text-fill: " + RED + "; -fx-font-size: 16px; -fx-font-weight: bold;");
        VBox lowList = new VBox(4);

        List<FoodItem> lowStock = fridge.getLowStockItems();
        if (lowStock.isEmpty()) {
            lowList.getChildren().add(new Label("• (none)"));
        } else {
            for (FoodItem f : lowStock) {
                lowList.getChildren().add(new Label("• " + f.getName().toUpperCase() + " (x" + formatQty(f.getQuantity()) + ")"));
            }
        }

        infoBox.getChildren().addAll(useSoon, useSoonList, new Label(""), low, lowList);
    }

    // =========================================================
    // RECIPES PAGE (left grid + right "book")
    // =========================================================

    private Pane buildRecipesPage() {
        HBox row = new HBox(22);
        row.setPadding(new Insets(6));

        // Left: recipe tiles
        VBox leftPanel = panelBox("RECIPE");
        HBox topLine = new HBox();
        topLine.setAlignment(Pos.CENTER_RIGHT);

        Button best = new Button("sort by: best recipe");
        styleButton(best);
        best.setOnAction(e -> {
            // choose a best recipe if you have that method; otherwise pick first cookable
            Recipe r = null;
            List<Recipe> cookable = fridge.getCookableRecipes();
            if (!cookable.isEmpty()) r = cookable.get(0);
            if (r == null && !fridge.getRecipes().isEmpty()) r = fridge.getRecipes().get(0);
            selectedRecipe = r;
            refreshRecipeBook();
        });

        topLine.getChildren().add(best);

        recipeGrid = new GridPane();
        recipeGrid.setHgap(8);
        recipeGrid.setVgap(8);
        recipeGrid.setPadding(new Insets(10, 0, 10, 0));

        Button addRecipe = new Button("add recipe");
        styleButton(addRecipe);
        addRecipe.setOnAction(e -> addRecipeDialog());

        leftPanel.getChildren().addAll(topLine, recipeGrid, alignBottom(addRecipe));
        leftPanel.setPrefWidth(350);

        // Right: recipe "book"
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

    private void refreshRecipeGrid() {
        recipeGrid.getChildren().clear();
        List<Recipe> recipes = new ArrayList<>(fridge.getRecipes());
        recipes.sort(Comparator.comparing(Recipe::getName, String.CASE_INSENSITIVE_ORDER));

        int cols = 5;
        int maxTiles = 20;
        int tiles = Math.max(maxTiles, recipes.size());

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

    private void refreshRecipeBook() {
        recipeBookBox.getChildren().clear();

        if (selectedRecipe == null) {
            recipeBookBox.getChildren().add(new Label("(Click a recipe to view it)"));
            return;
        }

        // Title
        Label title = new Label(selectedRecipe.getName());
        title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold;");

        // Image
        ImageView img = new ImageView(loadImageSafe(selectedRecipe.getImgFilePath()));
        img.setFitWidth(140);
        img.setFitHeight(140);
        img.setPreserveRatio(true);

        // Ingredients
        VBox ingBox = new VBox(4);
        Label ingTitle = new Label("Ingredients:");
        ingTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
        ingBox.getChildren().add(ingTitle);

        for (IngredientLine ing : selectedRecipe.getIngredients()) {
            FoodItem have = fridge.getFoodItem(ing.getNormalizedName());

            boolean ok = have != null && have.getQuantity() >= ing.getAmount();
            Label line = new Label("• " + ing.getNormalizedName().toUpperCase() + " (" + formatQty(ing.getAmount()) + " " + ing.getUnit() + ")");
            if (!ok) line.setStyle("-fx-text-fill: " + RED + "; -fx-font-weight: bold;");
            ingBox.getChildren().add(line);
        }

        // Steps
        VBox stepBox = new VBox(4);
        Label stepTitle = new Label("Steps:");
        stepTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
        stepBox.getChildren().add(stepTitle);

        int idx = 1;
        for (String s : selectedRecipe.getSteps()) {
            stepBox.getChildren().add(new Label(idx + ". " + s));
            idx++;
        }

        // Buttons
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
            // easiest: regenerate shopping list (based on current inventory + recipes)
            fridge.createShoppingList();
            showPage("Shopping List");
        });

        cook.setOnAction(e -> {
            boolean ok = selectedRecipe.cook(fridge);
            if (!ok) {
                alert("Not enough ingredients to cook this recipe.");
            } else {
                fridge.createShoppingList();
                refreshAll();
                alert("Cooked! Inventory updated.");
            }
        });

        actions.getChildren().addAll(addToList, cook);

        // Layout similar to a “book”
        HBox topRow = new HBox(20);
        VBox left = new VBox(10, title, img, ingBox, actions);
        VBox right = new VBox(10, stepBox);
        left.setPrefWidth(320);

        topRow.getChildren().addAll(left, right);
        recipeBookBox.getChildren().add(topRow);
    }

    // =========================================================
    // SHOPPING LIST PAGE (paper + removable items + export)
    // =========================================================

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

        // ensure list exists
        fridge.createShoppingList();

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

    // =========================================================
    // DIALOGS (simple BlueJ-friendly)
    // =========================================================

    private void addItemDialog() {
        TextInputDialog d = new TextInputDialog("tomato");
        d.setTitle("Add Food");
        d.setHeaderText("Enter item name:");
        d.setContentText("Name:");
        d.showAndWait().ifPresent(name -> {
            // quick default add: qty=1, unit=x, OTHER, expires in 7 days, image empty
            fridge.addFood(new FoodItem(name, 1, "x", Category.OTHER, LocalDate.now().plusDays(7), ""));
            refreshAll();
        });
    }

    private void editItemDialog(FoodItem item) {
        if (item == null) return;

        TextInputDialog d = new TextInputDialog(formatQty(item.getQuantity()));
        d.setTitle("Edit Item");
        d.setHeaderText("Edit quantity for: " + item.getName());
        d.setContentText("New quantity:");
        d.showAndWait().ifPresent(qStr -> {
            try {
                double q = Double.parseDouble(qStr);
                item.setQuantity(q);
                refreshAll();
            } catch (Exception ex) {
                alert("Invalid number.");
            }
        });
    }

    private void addRecipeDialog() {
        TextInputDialog d = new TextInputDialog("New Recipe");
        d.setTitle("Add Recipe");
        d.setHeaderText("Enter recipe name:");
        d.setContentText("Name:");
        d.showAndWait().ifPresent(name -> {
            // quick empty recipe; you can expand later
            fridge.addRecipe(new Recipe(name, new ArrayList<>(), new ArrayList<>(), ""));
            refreshAll();
        });
    }

    private void addShoppingItemDialog() {
        TextInputDialog d = new TextInputDialog("milk");
        d.setTitle("Add Shopping Item");
        d.setHeaderText("Enter item name to add (amount + unit are basic here):");
        d.setContentText("Name:");
        d.showAndWait().ifPresent(name -> {
            // easiest: add 1 unit in shopping list by regenerating then manually adjusting
            fridge.createShoppingList();
            // You can add a dedicated "addShoppingListItem" method later if you want.
            refreshShoppingList();
        });
    }

    // =========================================================
    // EXPORT
    // =========================================================

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


    // =========================================================
    // UI HELPERS
    // =========================================================

    private VBox panelBox(String title) {
        VBox box = new VBox(10);
        box.setPadding(new Insets(14));
        box.setStyle(
                "-fx-background-color: " + PANEL_BG + ";" +
                "-fx-border-color: " + BORDER + ";" +
                "-fx-border-width: 3px;"
        );

        Label t = new Label(title);
        t.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        box.getChildren().add(t);
        return box;
    }

    private StackPane makeTile() {
        StackPane tile = new StackPane();
        tile.setPrefSize(55, 55);
        tile.setStyle(
                "-fx-background-color: #ececec;" +
                "-fx-border-color: " + BORDER + ";" +
                "-fx-border-width: 2px;"
        );
        return tile;
    }

    private void styleButton(Button b) {
        b.setStyle(
                "-fx-background-color: " + BTN_BG + ";" +
                "-fx-text-fill: white;" +
                "-fx-border-color: " + BTN_BORDER + ";" +
                "-fx-border-width: 2px;" +
                "-fx-padding: 6 12 6 12;"
        );
    }

    private Pane alignBottom(Button b) {
        VBox wrap = new VBox();
        wrap.setAlignment(Pos.BOTTOM_RIGHT);
        wrap.getChildren().add(b);
        return wrap;
    }

    private void refreshAll() {
        refreshInventoryGrid();
        refreshItemDetails();
        refreshInfoPanel();
        refreshRecipeGrid();
        refreshRecipeBook();
        refreshShoppingList();
    }

    private void alert(String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }

    private String formatQty(double q) {
        if (Math.abs(q - Math.round(q)) < 1e-9) return String.valueOf((int) Math.round(q));
        return String.valueOf(q);
    }

    private Image loadImageSafe(String path) {
        try {
            if (path == null || path.trim().isEmpty()) {
                return new Image("file:fooditem-images/placeholder.png");
            }

            File f = new File(path);
            if (!f.isAbsolute()) {
                f = new File(System.getProperty("user.dir"), path);
            }

            if (!f.exists()) {
                return new Image("file:fooditem-images/placeholder.png");
            }

            return new Image(f.toURI().toString());
        } catch (Exception e) {
            return new Image("file:fooditem-images/placeholder.png");
        }
    }

    private Stage stageFrom(Pane anyNodeOnScene) {
        if (anyNodeOnScene == null) return null;
        return (Stage) anyNodeOnScene.getScene().getWindow();
    }

    // =========================================================
    // DEMO DATA (replace with your own later)
    // =========================================================

    private Fridge buildDemoFridge() {
        Fridge f = new Fridge();

        f.addFood(new FoodItem("watermelon", 1, "x", Category.FRUITS_VEGETABLES, LocalDate.now().plusDays(7), "melon.png"));
        f.addFood(new FoodItem("potato", 1, "x", Category.FRUITS_VEGETABLES, LocalDate.now().plusDays(1), "potato.png"));
        f.addFood(new FoodItem("milk", 1, "cup", Category.DAIRY_EGGS, LocalDate.now().plusDays(2), "milk.png"));
        f.addFood(new FoodItem("chicken", 1, "x", Category.PROTEINS, LocalDate.now().plusDays(3), "chicken.png"));
        f.addFood(new FoodItem("tomato", 2, "x", Category.FRUITS_VEGETABLES, LocalDate.now().plusDays(8), "tomato.png"));

        // sample recipe
        List<IngredientLine> ing = new ArrayList<>();
        ing.add(new IngredientLine("potato", 2, "x"));
        ing.add(new IngredientLine("milk", 1, "cup"));
        ing.add(new IngredientLine("chicken", 1, "x"));

        List<String> steps = new ArrayList<>();
        steps.add("Heat oil in a large pot, and brown the chicken");
        steps.add("In the same pot, add potatoes, and cook until soft");
        steps.add("Add water and milk");
        steps.add("Bring to a boil");

        f.addRecipe(new Recipe("Annika Stew", steps, ing, "recipe-images/stew.png"));

        f.createShoppingList();
        return f;
    }
}