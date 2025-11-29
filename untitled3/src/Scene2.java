import javafx.beans.binding.Bindings;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.*;
import java.util.*;

class Scene2 {
    final static double screenSizeHeight = Interface.screenSizeHeight;
    final static double screenSizeWidth = Interface.screenSizeWidth;
    Stage mainStage;
    File orderStorageFile = new File(System.getProperty("user.dir") + "/Data/ProductData/orderStorage.txt");
    Map<String, int[]> currentOrders = new HashMap<>();

    Scene2(Stage mainStage) {
        this.mainStage = mainStage;
        try { new PrintWriter(orderStorageFile).close(); } catch (IOException ignored) {}
    }

    void figure2() throws IOException {
        var topNodeSize = Bindings.createDoubleBinding(
                () -> mainStage.getHeight() * 0.07,
                mainStage.heightProperty(),
                mainStage.widthProperty()
        );

        mainStage.maximizedProperty().addListener((_, _, thisBoolean) -> {
            if (!thisBoolean) {
                mainStage.setHeight(screenSizeHeight * 0.70);
                mainStage.setWidth(screenSizeWidth * 0.65);
            }
        });

        BorderPane mainPane = new BorderPane();

        HBox title = new HBox();
        title.prefHeightProperty().bind(topNodeSize);
        title.setStyle("-fx-background-color: red");

        HBox functions = new HBox(10);
        functions.prefHeightProperty().bind(topNodeSize);
        functions.setAlignment(Pos.CENTER_RIGHT);
        functions.setStyle("-fx-background-color: blue");

        Text totalText = new Text("Total: 0");
        totalText.fontProperty().bind(Bindings.createObjectBinding(
                () -> Font.font(topNodeSize.get() * 0.4), topNodeSize
        ));

        Button reset = new Button("Reset");
        Button checkOut = new Button("Check Out");
        reset.prefHeightProperty().bind(functions.heightProperty().multiply(0.8));
        reset.prefWidthProperty().bind(functions.widthProperty().multiply(0.1));
        checkOut.prefHeightProperty().bind(functions.heightProperty().multiply(0.8));
        checkOut.prefWidthProperty().bind(functions.widthProperty().multiply(0.1));

        functions.getChildren().addAll(totalText, reset, checkOut);

        List<List<String>> products = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(System.getProperty("user.dir") + "/Data/ProductData/products.txt"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                List<String> row = new ArrayList<>();
                for (String part : parts) row.add(part.trim());
                products.add(row);
            }
        }
        FlowPane flow = new FlowPane();
        flow.setPadding(new Insets(10));
        flow.setHgap(10);
        flow.setVgap(10);
        flow.setAlignment(Pos.TOP_LEFT);

        for (List<String> product : products) {
            String ref = product.get(0);
            String name = product.get(1);
            int maxQuantity = Integer.parseInt(product.get(2));
            int price = Integer.parseInt(product.get(3));
            boolean inStock = Boolean.parseBoolean(product.get(4));

            VBox container = new VBox(5);
            container.setAlignment(Pos.CENTER);
            Button mainBtn = new Button(name + " - " + price);
            mainBtn.prefWidthProperty().bind(mainStage.widthProperty().multiply(0.15));
            mainBtn.prefHeightProperty().bind(mainStage.heightProperty().multiply(0.10));

            HBox qtySelector = new HBox(5);
            qtySelector.setVisible(false);
            Button minus = new Button("-");
            TextField qtyField = new TextField("1");
            qtyField.setPrefWidth(40);
            Button plus = new Button("+");
            qtySelector.getChildren().addAll(minus, qtyField, plus);

            Label nameLabel = new Label("Name: " + name);
            Label priceLabel = new Label("Price: " + price);
            container.getChildren().addAll(mainBtn, qtySelector, nameLabel, priceLabel);

            if (!inStock) {
                mainBtn.setDisable(true);
                mainBtn.setStyle("-fx-background-color: lightgray; -fx-text-fill: darkgray;");
            }

            mainBtn.setOnAction(_ -> {
                boolean visible = qtySelector.isVisible();
                if (!visible) {
                    qtySelector.setVisible(true);
                    int prevQty = currentOrders.getOrDefault(ref, new int[]{1, price})[0];
                    qtyField.setText(String.valueOf(prevQty));
                    currentOrders.put(ref, new int[]{prevQty, price});
                } else {
                    qtySelector.setVisible(false);
                    qtyField.setText("1");
                    currentOrders.remove(ref);
                }
                updateFileAndTotal(totalText);
            });

            plus.setOnAction(_ -> {
                int val = Integer.parseInt(qtyField.getText());
                if (val < maxQuantity) val++;
                qtyField.setText(String.valueOf(val));
                currentOrders.put(ref, new int[]{val, price});
                updateFileAndTotal(totalText);
            });

            minus.setOnAction(_ -> {
                int val = Integer.parseInt(qtyField.getText());
                if (val > 1) val--;
                qtyField.setText(String.valueOf(val));
                currentOrders.put(ref, new int[]{val, price});
                updateFileAndTotal(totalText);
            });

            qtyField.textProperty().addListener((_, _, newText) -> {
                try {
                    int val = Integer.parseInt(newText);
                    if (val > maxQuantity) val = maxQuantity;
                    else if (val < 1) val = 1;
                    qtyField.setText(String.valueOf(val));
                    currentOrders.put(ref, new int[]{val, price});
                    updateFileAndTotal(totalText);
                } catch (NumberFormatException ignored) {}
            });

            flow.getChildren().add(container);
        }

        ScrollPane scrollPane = new ScrollPane(flow);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);

        reset.setOnAction(_ -> {
            flow.getChildren().forEach(node -> {
                if (node instanceof VBox container) {
                    for (javafx.scene.Node child : container.getChildren()) {
                        if (child instanceof HBox qty) qty.setVisible(false);
                        if (child instanceof TextField tf) tf.setText("1");
                    }
                }
            });
            currentOrders.clear();
            try { new PrintWriter(orderStorageFile).close(); } catch (IOException ignored) {}
            totalText.setText("Total: 0");
        });

        mainPane.setTop(title);
        mainPane.setCenter(scrollPane);
        mainPane.setBottom(functions);

        Scene scene = new Scene(mainPane, screenSizeWidth * 0.65, screenSizeHeight * 0.7);
        mainStage.setScene(scene);
        mainStage.show();
    }

    private void updateFileAndTotal(Text totalText) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(orderStorageFile, false))) {
            int total = 0;
            for (var entry : currentOrders.entrySet()) {
                String ref = entry.getKey();
                int quantity = entry.getValue()[0];
                int price = entry.getValue()[1];
                total += quantity * price;
                writer.println(ref + "," + quantity + "," + (quantity * price));
            }
            totalText.setText("Total: " + total);
        } catch (IOException ignored) {}
    }
}
