import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.io.InputStream;
import java.time.LocalDate;

public class App extends Application {

    private Fridge fridge = new Fridge();

    private GridPane inventoryGrid = new GridPane();
    private VBox itemDetailsBox = new VBox(10);

    @Override
    public void start(Stage stage) {
        stage.setTitle("MealCraft");

        // Root layout
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(15));

        // Left inventory panel
        VBox leftPanel = new VBox(10);
        leftPanel.setPrefWidth(450);

        Label inventoryTitle = new Label("Inventory");
        inventoryTitle.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

        inventoryGrid.setHgap(10);
        inventoryGrid.setVgap(10);

        ScrollPane scrollPane = new ScrollPane(inventoryGrid);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(550);

        // Buttons
        Button addButton = new Button("Add Item");
        Button removeButton = new Button("Remove Item");

        HBox buttonBox = new HBox(10, addButton, removeButton);
        buttonBox.setAlignment(Pos.CENTER_LEFT);

        leftPanel.getChildren().addAll(inventoryTitle, scrollPane, buttonBox);

        // Right details panel
        VBox rightPanel = new VBox(10);
        rightPanel.setPrefWidth(300);

        Label detailsTitle = new Label("Item Details");
        detailsTitle.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

        itemDetailsBox.setPadding(new Insets(10));
        itemDetailsBox.setStyle("-fx-border-color: lightgray; -fx-border-radius: 5; -fx-background-radius: 5;");

        rightPanel.getChildren().addAll(detailsTitle, itemDetailsBox);

        // Add panels to root
        root.setLeft(leftPanel);
        root.setRight(rightPanel);

        // Starter Data
        loadStarterData();

        // Initial refresh
        refreshInventory();

        // Button Actions
        addButton.setOnAction(e -> showAddItemDialog());
        removeButton.setOnAction(e -> showRemoveItemDialog());

        // Scene setup
        Scene scene = new Scene(root, 800, 600);
        stage.setScene(scene);
        stage.show();
    }

    // Loads starter inventory & recipes
    private void loadStarterData() {
        fridge.addFood(new FoodItem("Milk", 1, "carton", Category.DAIRY_EGGS, LocalDate.now().plusDays(5), "milk.png"));
        fridge.addFood(new FoodItem("Eggs", 12, "count", Category.DAIRY_EGGS, LocalDate.now().plusDays(10), "eggs.png"));
        fridge.addFood(new FoodItem("Apple", 6, "count", Category.FRUITS_VEGETABLES, LocalDate.now().plusDays(7), "apple.png"));
    }

    // Refreshes inventory grid from fridge items
    private void refreshInventory() {
        inventoryGrid.getChildren().clear();

        int row = 0;
        for (FoodItem item : fridge.getAllFoodItems()) {
            Button itemButton = new Button(item.getName());
            itemButton.setPrefWidth(200);

            itemButton.setOnAction(e -> showItemDetails(item));
            inventoryGrid.add(itemButton, 0, row);
            row++;
        }
    }

    // Displays item details on the right side
    private void showItemDetails(FoodItem item) {
        itemDetailsBox.getChildren().clear();

        Label name = new Label("Name: " + item.getName());
        Label qty = new Label("Quantity: " + item.getQuantity() + " " + item.getUnit());
        Label cat = new Label("Category: " + item.getCategory());
        Label exp = new Label("Expires: " + item.getExpirationDate());

        itemDetailsBox.getChildren().addAll(name, qty, cat, exp);

        // Optional: Display image if available in resources
        try {
            InputStream stream = getClass().getResourceAsStream("/" + item.getImgFilePath());
            if (stream != null) {
                Image img = new Image(stream);
                ImageView view = new ImageView(img);
                view.setFitWidth(200);
                view.setPreserveRatio(true);
                itemDetailsBox.getChildren().add(view);
            }
        } catch (Exception ignored) {
            // If image fails to load, it's fine (BlueJ may not have resource setup)
        }
    }

    // Add Item Dialog
    private void showAddItemDialog() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Add Item");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        TextField nameField = new TextField();
        TextField qtyField = new TextField();
        TextField unitField = new TextField();
        ComboBox<Category> categoryBox = new ComboBox<>();
        categoryBox.getItems().addAll(Category.values());
        DatePicker datePicker = new DatePicker(LocalDate.now().plusDays(3));
        TextField imgField = new TextField();

        grid.add(new Label("Name:"), 0, 0);
        grid.add(nameField, 1, 0);

        grid.add(new Label("Quantity:"), 0, 1);
        grid.add(qtyField, 1, 1);

        grid.add(new Label("Unit:"), 0, 2);
        grid.add(unitField, 1, 2);

        grid.add(new Label("Category:"), 0, 3);
        grid.add(categoryBox, 1, 3);

        grid.add(new Label("Expiration:"), 0, 4);
        grid.add(datePicker, 1, 4);

        grid.add(new Label("Image File (optional):"), 0, 5);
        grid.add(imgField, 1, 5);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.showAndWait().ifPresent(result -> {
            if (result == ButtonType.OK) {
                try {
                    String name = nameField.getText();
                    double qty = Double.parseDouble(qtyField.getText());
                    String unit = unitField.getText();
                    Category cat = categoryBox.getValue();
                    LocalDate exp = datePicker.getValue();
                    String img = imgField.getText();

                    fridge.addFood(new FoodItem(name, qty, unit, cat, exp, img));
                    refreshInventory();
                } catch (Exception ex) {
                    // Basic error handling
                    Alert a = new Alert(Alert.AlertType.ERROR, "Invalid input. Please try again.");
                    a.showAndWait();
                }
            }
        });
    }

    // Remove Item Dialog
    private void showRemoveItemDialog() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Remove Item");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        TextField nameField = new TextField();
        TextField amtField = new TextField();

        grid.add(new Label("Name:"), 0, 0);
        grid.add(nameField, 1, 0);

        grid.add(new Label("Amount to remove:"), 0, 1);
        grid.add(amtField, 1, 1);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.showAndWait().ifPresent(result -> {
            if (result == ButtonType.OK) {
                try {
                    String name = nameField.getText();
                    double amt = Double.parseDouble(amtField.getText());

                    fridge.removeFood(name, amt);
                    refreshInventory();
                } catch (Exception ex) {
                    Alert a = new Alert(Alert.AlertType.ERROR, "Invalid input. Please try again.");
                    a.showAndWait();
                }
            }
        });
    }

    public static void main(String[] args) {
        launch(args);
    }
}