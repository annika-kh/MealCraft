import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.io.InputStream;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class MainTest extends Application {

    // ----------------------------
    // DATA (plug your real model here)
    // ----------------------------
    private Fridge fridge = new Fridge();

    // “selected item” state for inventory screen
    private FoodItem selectedItem = null;

    // ----------------------------
    // UI ROOT + SWITCHING
    // ----------------------------
    private BorderPane root;
    private StackPane content; // switches between inventory/shopping/recipes pages

    // pages
    private Parent inventoryPage;
    private Parent shoppingPage;
    private Parent recipesPage;

    // tab buttons
    private Button tabInventory;
    private Button tabShopping;
    private Button tabRecipes;

    // inventory screen sub-areas we update
    private GridPane stockGrid;
    private VBox detailsBox;
    private VBox infoBox;

    // ----------------------------------
    // START
    // ----------------------------------
    @Override
    public void start(Stage stage) {
        // Starter items so you can see the UI immediately
        seedDemoData();

        root = new BorderPane();
        root.getStyleClass().add("app-root");

        // TOP BAR (title + tabs)
        Parent topBar = buildTopBar();
        root.setTop(topBar);

        // CENTER content area (screen switcher)
        content = new StackPane();
        content.setPadding(new Insets(16, 20, 20, 20));
        root.setCenter(content);

        // Build pages
        inventoryPage = buildInventoryPage();
        shoppingPage  = buildShoppingListPage();
        recipesPage   = buildRecipesPage();

        // Default page
        showPage("inventory");

        Scene scene = new Scene(root, 1180, 640);

        // Optional CSS (recommended). If you don’t have style.css yet, comment this out.
        // Put style.css in the SAME BlueJ project folder.
        scene.getStylesheets().add("file:style.css");

        stage.setTitle("MealCraft");
        stage.setScene(scene);
        stage.show();
    }

    // ----------------------------------
    // TOP BAR: title + tabs
    // ----------------------------------
    private Parent buildTopBar() {
        BorderPane bar = new BorderPane();
        bar.setPadding(new Insets(18, 22, 10, 22));
        bar.getStyleClass().add("top-bar");

        Label title = new Label("MealCraft");
        title.getStyleClass().add("title");

        HBox tabs = new HBox(14);
        tabs.setAlignment(Pos.CENTER);

        tabInventory = makeTabButton("inventory", true);
        tabShopping  = makeTabButton("shopping list", false);
        tabRecipes   = makeTabButton("recipes", false);

        tabs.getChildren().addAll(tabInventory, tabShopping, tabRecipes);

        bar.setLeft(title);
        bar.setCenter(tabs);
        return bar;
    }

    private Button makeTabButton(String text, boolean selected) {
        Button b = new Button(text);
        b.getStyleClass().add("tab");
        if (selected) b.getStyleClass().add("tab-selected");

        b.setOnAction(e -> showPage(text));
        return b;
    }

    private void setSelectedTab(String which) {
        tabInventory.getStyleClass().remove("tab-selected");
        tabShopping.getStyleClass().remove("tab-selected");
        tabRecipes.getStyleClass().remove("tab-selected");

        if (which.equals("inventory")) tabInventory.getStyleClass().add("tab-selected");
        if (which.equals("shopping list")) tabShopping.getStyleClass().add("tab-selected");
        if (which.equals("recipes")) tabRecipes.getStyleClass().add("tab-selected");
    }

    private void showPage(String which) {
        setSelectedTab(which);

        content.getChildren().clear();
        if (which.equals("inventory")) content.getChildren().add(inventoryPage);
        if (which.equals("shopping list")) content.getChildren().add(shoppingPage);
        if (which.equals("recipes")) content.getChildren().add(recipesPage);
    }

    // ----------------------------------
    // INVENTORY PAGE (matches screenshot 1)
    // ----------------------------------
    private Parent buildInventoryPage() {
        // 3 big panels like your screenshot: Food Stock | Item Details | Info
        HBox row = new HBox(22);
        row.setAlignment(Pos.TOP_CENTER);

        Pane left  = buildFoodStockPanel();
        Pane mid   = buildItemDetailsPanel();
        Pane right = buildInfoPanel();

        row.getChildren().addAll(left, mid, right);
        return row;
    }

    private Pane buildFoodStockPanel() {
        VBox panel = new VBox(10);
        panel.getStyleClass().add("panel");
        panel.setPrefWidth(360);

        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);

        Label label = new Label("FOOD STOCK");
        label.getStyleClass().add("panel-title");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button sort = new Button("sort by: a-z");
        sort.getStyleClass().add("small-btn");
        sort.setOnAction(e -> {
            // TODO: sort your display
            refreshInventoryGrid();
        });

        header.getChildren().addAll(label, spacer, sort);

        stockGrid = new GridPane();
        stockGrid.setHgap(8);
        stockGrid.setVgap(8);
        stockGrid.getStyleClass().add("stock-grid");

        // wrap grid in a scroll area (optional)
        ScrollPane scroll = new ScrollPane(stockGrid);
        scroll.setFitToWidth(true);
        scroll.setFitToHeight(true);
        scroll.getStyleClass().add("grid-scroll");
        scroll.setPrefHeight(470);

        Button add = new Button("add item");
        add.getStyleClass().add("primary-btn");
        add.setOnAction(e -> {
            // TODO: open add dialog
            // For now, add a sample item:
            fridge.addFood(new FoodItem("New Item", 1, "x", Category.OTHER, LocalDate.now().plusDays(7), "icons/placeholder.png"));
            refreshInventoryGrid();
            refreshInfoPanel();
        });

        panel.getChildren().addAll(header, scroll, add);

        refreshInventoryGrid();
        return panel;
    }

    private void refreshInventoryGrid() {
        stockGrid.getChildren().clear();

        // Your mockup is a tile grid of icons with small quantity numbers.
        // We'll do 5 columns like the screenshot.
        int cols = 5;
        int i = 0;

        for (FoodItem item : fridge.getAllFoodItems()) {
            int r = i / cols;
            int c = i % cols;

            StackPane tile = makeItemTile(item);
            stockGrid.add(tile, c, r);

            i++;
        }

        // Fill empty tiles to keep the “grid” look (optional)
        int totalTiles = Math.max(i, 25);
        for (int j = i; j < totalTiles; j++) {
            int r = j / cols;
            int c = j % cols;

            StackPane empty = new StackPane();
            empty.getStyleClass().add("tile-empty");
            empty.setPrefSize(58, 58);
            stockGrid.add(empty, c, r);
        }
    }

    private StackPane makeItemTile(FoodItem item) {
        StackPane tile = new StackPane();
        tile.getStyleClass().add("tile");
        tile.setPrefSize(58, 58);

        ImageView icon = new ImageView(loadImage(item.getImgFilePath()));
        icon.setFitWidth(46);
        icon.setFitHeight(46);
        icon.setPreserveRatio(true);

        Label qty = new Label(formatQty(item.getQuantity()));
        qty.getStyleClass().add("tile-qty");
        StackPane.setAlignment(qty, Pos.BOTTOM_LEFT);
        StackPane.setMargin(qty, new Insets(0, 0, 2, 4));

        tile.getChildren().addAll(icon, qty);

        tile.setOnMouseClicked(e -> {
            if (e.getButton() == MouseButton.PRIMARY) {
                selectedItem = item;
                refreshDetailsPanel();
            }
        });

        return tile;
    }

    private String formatQty(double q) {
        // display as integer if whole number
        if (Math.abs(q - Math.round(q)) < 0.00001) return "" + (int)Math.round(q);
        return "" + q;
    }

    private Pane buildItemDetailsPanel() {
        VBox panel = new VBox(14);
        panel.getStyleClass().add("panel");
        panel.setPrefWidth(360);

        Label title = new Label("ITEM DETAILS");
        title.getStyleClass().add("panel-title");

        detailsBox = new VBox(10);
        detailsBox.setPadding(new Insets(10, 10, 10, 10));

        panel.getChildren().addAll(title, detailsBox);

        // default selection
        selectedItem = fridge.getAllFoodItems().stream().findFirst().orElse(null);
        refreshDetailsPanel();

        return panel;
    }

    private void refreshDetailsPanel() {
        detailsBox.getChildren().clear();

        if (selectedItem == null) {
            detailsBox.getChildren().add(new Label("(Select an item)"));
            return;
        }

        // Big icon
        ImageView big = new ImageView(loadImage(selectedItem.getImgFilePath()));
        big.setFitWidth(120);
        big.setFitHeight(120);
        big.setPreserveRatio(true);

        HBox top = new HBox(18, big, makeDetailsText(selectedItem));
        top.setAlignment(Pos.TOP_LEFT);

        Button edit = new Button("edit item");
        edit.getStyleClass().add("primary-btn");
        edit.setOnAction(e -> {
            // TODO: edit dialog
        });

        detailsBox.getChildren().addAll(top, edit);
    }

    private VBox makeDetailsText(FoodItem item) {
        VBox box = new VBox(8);

        Label name = new Label(item.getName().toLowerCase());
        name.getStyleClass().add("details-name");

        Label cat = new Label("CATEGORY: " + item.getCategory());
        cat.getStyleClass().add("details-line");

        Label qty = new Label("QUANTITY: " + formatQty(item.getQuantity()) + " " + item.getUnit());
        qty.getStyleClass().add("details-line");

        Label exp = new Label("EXPIRES: " + item.getExpirationDate());
        exp.getStyleClass().add("details-line");

        box.getChildren().addAll(name, cat, qty, exp);
        return box;
    }

    private Pane buildInfoPanel() {
        VBox panel = new VBox(12);
        panel.getStyleClass().add("panel");
        panel.setPrefWidth(360);

        Label title = new Label("INFO");
        title.getStyleClass().add("panel-title");

        infoBox = new VBox(16);
        infoBox.setPadding(new Insets(10));

        panel.getChildren().addAll(title, infoBox);
        refreshInfoPanel();
        return panel;
    }

    private void refreshInfoPanel() {
        infoBox.getChildren().clear();

        // USE SOON (expiring within 3 days)
        VBox useSoon = new VBox(6);
        Label useSoonTitle = new Label("USE SOON:");
        useSoonTitle.getStyleClass().add("info-header-red");
        useSoon.getChildren().add(useSoonTitle);

        List<FoodItem> soon = fridge.getItemsExpiringWithin(3);
        if (soon.isEmpty()) {
            Label none = new Label("• (none)");
            none.getStyleClass().add("info-line");
            useSoon.getChildren().add(none);
        } else {
            for (FoodItem f : soon) {
                long days = Math.max(0, java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(), f.getExpirationDate()));
                Label line = new Label("• " + f.getName().toUpperCase() + " (expires " + days + " day)");
                line.getStyleClass().add("info-line");
                useSoon.getChildren().add(line);
            }
        }

        // LOW STOCK (example: <2)
        VBox low = new VBox(6);
        Label lowTitle = new Label("LOW STOCK:");
        lowTitle.getStyleClass().add("info-header-red");
        low.getChildren().add(lowTitle);

        boolean anyLow = false;
        for (FoodItem f : fridge.getAllFoodItems()) {
            if (f.getQuantity() <= 2) {
                anyLow = true;
                Label line = new Label("• " + f.getName().toUpperCase() + " (x" + formatQty(f.getQuantity()) + ")");
                line.getStyleClass().add("info-line");
                low.getChildren().add(line);
            }
        }
        if (!anyLow) {
            Label none = new Label("• (none)");
            none.getStyleClass().add("info-line");
            low.getChildren().add(none);
        }

        infoBox.getChildren().addAll(useSoon, new Region(), low);
        VBox.setVgrow(infoBox.getChildren().get(1), Priority.ALWAYS);
    }

    // ----------------------------------
    // SHOPPING LIST PAGE (matches screenshot 2)
    // ----------------------------------
    private Parent buildShoppingListPage() {
        StackPane page = new StackPane();
        page.getStyleClass().add("shopping-page");

        // parchment background image
        ImageView parchment = new ImageView(loadImage("ui/parchment.png"));
        parchment.setPreserveRatio(true);
        parchment.setFitHeight(520);

        VBox text = new VBox(18);
        text.setMaxWidth(520);
        text.setPadding(new Insets(40, 50, 40, 50));
        text.setAlignment(Pos.TOP_LEFT);

        Label heading = new Label("ShoppingList");
        heading.getStyleClass().add("paper-title");

        VBox lowStock = new VBox(6);
        Label lowTitle = new Label("LOW STOCK:");
        lowTitle.getStyleClass().add("paper-red");
        lowStock.getChildren().add(lowTitle);
        lowStock.getChildren().add(makePaperLine("• TOMATO (x2)"));

        VBox missing = new VBox(6);
        Label missTitle = new Label("MISSING INGREDIENTS:");
        missTitle.getStyleClass().add("paper-red");
        missing.getChildren().add(missTitle);
        missing.getChildren().add(makePaperLine("• MILK (x1, Annika Stew)"));
        missing.getChildren().add(makePaperLine("• LETTUCE (x2, Caesar Salad)"));

        Button export = new Button("Export TXT");
        export.getStyleClass().add("primary-btn");
        export.setOnAction(e -> {
            // TODO: export to file
        });

        text.getChildren().addAll(heading, lowStock, missing, new Region(), export);
        VBox.setVgrow(text.getChildren().get(3), Priority.ALWAYS);

        StackPane card = new StackPane(parchment, text);
        card.setAlignment(Pos.CENTER);

        page.getChildren().add(card);
        return page;
    }

    private Label makePaperLine(String s) {
        Label l = new Label(s);
        l.getStyleClass().add("paper-line");
        return l;
    }

    // ----------------------------------
    // RECIPES PAGE (matches screenshot 3)
    // ----------------------------------
    private Parent buildRecipesPage() {
        BorderPane page = new BorderPane();
        page.getStyleClass().add("recipes-page");

        // open book background
        StackPane book = new StackPane();
        book.setAlignment(Pos.CENTER);

        ImageView openBook = new ImageView(loadImage("ui/openbook.png"));
        openBook.setPreserveRatio(true);
        openBook.setFitHeight(450);

        HBox spread = new HBox(30);
        spread.setAlignment(Pos.CENTER);
        spread.setPadding(new Insets(35, 60, 35, 60));
        spread.setMaxWidth(860);

        // left page: recipe name + icon + ingredients
        VBox left = new VBox(12);
        left.setAlignment(Pos.TOP_LEFT);
        Label recipeName = new Label("Annika Stew");
        recipeName.getStyleClass().add("paper-title");

        ImageView bowl = new ImageView(loadImage("icons/placeholder.png"));
        bowl.setFitWidth(120);
        bowl.setFitHeight(120);
        bowl.setPreserveRatio(true);

        VBox ing = new VBox(6);
        ing.getChildren().addAll(
                makePaperLine("Ingredients:"),
                makePaperLine("• POTATO (2)"),
                makePaperLine("• MILK (1 cup)"),
                makePaperLine("• CHICKEN (1)")
        );

        Button select = new Button("Select Recipe");
        select.getStyleClass().add("primary-btn");

        Label status = new Label("• Expiring Soon\n• No Stock");
        status.getStyleClass().add("mini-status-red");

        left.getChildren().addAll(recipeName, bowl, ing, select, status);

        // right page: steps
        VBox right = new VBox(10);
        right.setAlignment(Pos.TOP_LEFT);
        Label stepsTitle = new Label("Steps:");
        stepsTitle.getStyleClass().add("details-line");

        Label steps = new Label(
                "1. Heat oil in a large pot,\n" +
                "   and brown the chicken\n" +
                "2. In the same pot, add\n" +
                "   potatoes, and cook\n" +
                "   until soft\n" +
                "3. Add water and milk\n" +
                "4. Bring to a boil\n" +
                "5. Chop up annika FRESH\n" +
                "   AND LIVE\n" +
                "6. Watch her boil in the\n" +
                "   pot and try to lift the\n" +
                "   lid to come out"
        );
        steps.getStyleClass().add("paper-line");

        right.getChildren().addAll(stepsTitle, steps);

        spread.getChildren().addAll(left, right);

        StackPane overlay = new StackPane(spread);
        overlay.setPickOnBounds(false);

        book.getChildren().addAll(openBook, overlay);

        // bottom “Finish Recipe” button like screenshot
        Button finish = new Button("Finish Recipe");
        finish.getStyleClass().add("finish-btn");
        BorderPane.setAlignment(finish, Pos.CENTER);
        BorderPane.setMargin(finish, new Insets(14, 0, 0, 0));

        page.setCenter(book);
        page.setBottom(finish);

        return page;
    }

    // ----------------------------------
    // IMAGE LOADING (BlueJ-friendly)
    // ----------------------------------
    private Image loadImage(String path) {
        // Put images in your BlueJ project folder under:
        // ui/..., icons/...
        // Example: ui/openbook.png, ui/parchment.png, icons/milk.png
        try {
            // first try as a resource
            InputStream is = getClass().getResourceAsStream("/" + path);
            if (is != null) return new Image(is);

            // fallback to file path
            return new Image("file:" + path);
        } catch (Exception e) {
            // placeholder empty image if missing
            return new Image("file:placeholder.png");
        }
    }

    // ----------------------------------
    // DEMO DATA
    // ----------------------------------
    private void seedDemoData() {
        fridge.addFood(new FoodItem("potato", 8, "x", Category.FRUITS_VEGETABLES, LocalDate.now().plusDays(1), "fooditem-images/potato.png"));
        fridge.addFood(new FoodItem("milk", 1, "carton", Category.DAIRY_EGGS, LocalDate.now().plusDays(2), "fooditem-images/milk.png"));
        fridge.addFood(new FoodItem("chicken", 1, "x", Category.PROTEINS, LocalDate.now().plusDays(3), "fooditem-images/chicken.png"));
        fridge.addFood(new FoodItem("watermelon", 1, "x", Category.FRUITS_VEGETABLES, LocalDate.now().plusDays(7), "fooditem-images/melon.png"));
    }

    public static void main(String[] args) {
        launch(args);
    }
}