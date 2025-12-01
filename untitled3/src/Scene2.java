import javafx.beans.binding.Bindings;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import java.io.*;
import java.util.*;
//this class is 50% AI && 50% HUMAN
class Scene2 {
    final static double screenSizeHeight = Interface.screenSizeHeight;
    final static double screenSizeWidth = Interface.screenSizeWidth;

    Stage mainStage;

    File orderStorageFile = new File(System.getProperty("user.dir") + "/Data/ProductData/orderStorage.txt");

    Map<String, int[]> currentOrders = new HashMap<>();
    List<List<String>> products = new ArrayList<>();

    Scene2(Stage mainStage) {
        this.mainStage = mainStage;
        try { new PrintWriter(orderStorageFile).close(); } catch (Exception ignored) {}
    }

    void figure2() throws IOException {
        validateProductsFile();

        BorderPane mainPane = new BorderPane();

        var topNodeSize = Bindings.createDoubleBinding(
                () -> mainStage.getHeight() * 0.07,
                mainStage.heightProperty(),
                mainStage.widthProperty()
        );

        HBox topBox = new HBox(10);
        topBox.prefHeightProperty().bind(topNodeSize);
        topBox.setAlignment(Pos.CENTER_LEFT);
        topBox.setPadding(new Insets(5));
        topBox.setStyle("-fx-background-color: #DDDDDD");

        TextField searchBox = new TextField();
        searchBox.setPromptText("Search product...");
        searchBox.setPrefWidth(200);

        ComboBox<String> categoryBox = new ComboBox<>();
        categoryBox.setPrefWidth(150);
        categoryBox.getItems().add("ALL");
        categoryBox.getSelectionModel().selectFirst();

        topBox.getChildren().addAll(new Label("Search:"), searchBox, new Label("Category:"), categoryBox);

        HBox bottomBox = new HBox(10);
        bottomBox.prefHeightProperty().bind(topNodeSize);
        bottomBox.setAlignment(Pos.CENTER_RIGHT);
        bottomBox.setPadding(new Insets(5));
        bottomBox.setStyle("-fx-background-color: #CCCCCC");

        Text totalText = new Text("Total: " + getCurrentTotal());
        totalText.fontProperty().bind(Bindings.createObjectBinding(
                () -> Font.font(topNodeSize.get() * 0.4),
                topNodeSize
        ));

        Button reset = new Button("Reset");
        Button checkOut = new Button("Check Out");

        reset.prefHeightProperty().bind(bottomBox.heightProperty().multiply(0.8));
        reset.prefWidthProperty().bind(bottomBox.widthProperty().multiply(0.1));
        checkOut.prefHeightProperty().bind(bottomBox.heightProperty().multiply(0.8));
        checkOut.prefWidthProperty().bind(bottomBox.widthProperty().multiply(0.1));

        checkOut.setOnAction(_ -> {
            mainStage.hide();
            openCheckoutScene();
        });

        bottomBox.getChildren().addAll(totalText, reset, checkOut);

        products.clear();
        Set<String> categories = new HashSet<>();
        try (BufferedReader reader = new BufferedReader(new FileReader("Data/ProductData/products.txt"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                List<String> p = Arrays.asList(line.split(","));
                if (p.size() < 6) continue;
                products.add(p);
                categories.add(p.get(4));
            }
        }

        for (String c : categories) {
            if (!categoryBox.getItems().contains(c)) categoryBox.getItems().add(c);
        }

        FlowPane flow = new FlowPane(10, 10);
        flow.setPadding(new Insets(10));

        List<VBox> productBoxes = new ArrayList<>();

        for (List<String> p : products) {
            String ref = p.get(0);
            String name = p.get(1);
            int maxQty = Integer.parseInt(p.get(2));
            int price = Integer.parseInt(p.get(3));
            boolean available = Boolean.parseBoolean(p.get(5));

            VBox box = new VBox(5);
            box.setAlignment(Pos.CENTER);

            Button btn = new Button();
            btn.prefWidthProperty().bind(mainStage.widthProperty().multiply(0.15));
            btn.prefHeightProperty().bind(mainStage.heightProperty().multiply(0.10));
            btn.setStyle("""
                        -fx-background-color: transparent;
                        -fx-border-color: black;
                        -fx-border-width: 2;
                        -fx-focus-color: transparent;
                        -fx-faint-focus-color: transparent;
                        """);


            // Load image and fit it inside the button
            javafx.scene.image.Image img = new javafx.scene.image.Image("file:Data/ImageData/" + name + ".png");
            ImageView iv = new ImageView(img);
            iv.setPreserveRatio(true);
            iv.fitWidthProperty().bind(btn.widthProperty().multiply(0.8));
            iv.fitHeightProperty().bind(btn.heightProperty().multiply(0.8));
            btn.setGraphic(iv);

            Label nameLabel = new Label(name + ": " + maxQty);
            Label priceLabel = new Label("₱ " + price);

            HBox qtyBox = new HBox(5);
            qtyBox.setAlignment(Pos.CENTER);
            qtyBox.setVisible(false);

            Button minus = new Button("-");
            TextField qtyField = new TextField("1");
            qtyField.setPrefWidth(45);
            Button plus = new Button("+");
            qtyBox.getChildren().addAll(minus, qtyField, plus);

            box.getChildren().addAll(btn, nameLabel, priceLabel, qtyBox);

            refreshButtonState(btn, qtyField, maxQty, available);

            if (currentOrders.containsKey(ref)) {
                qtyBox.setVisible(true);
                qtyField.setText(String.valueOf(currentOrders.get(ref)[0]));
            }

            btn.setOnAction(_ -> {
                boolean showing = qtyBox.isVisible();
                if (!showing) {
                    qtyBox.setVisible(true);
                    btn.setStyle("-fx-background-color: transparent; -fx-border-color: green; -fx-border-width: 2;");
                    int prev = currentOrders.getOrDefault(ref, new int[]{1, price})[0];
                    qtyField.setText(String.valueOf(prev));
                    currentOrders.put(ref, new int[]{prev, price});
                } else {
                    qtyBox.setVisible(false);
                    btn.setStyle("-fx-background-color: transparent; -fx-border-color: black; -fx-border-width: 2;");
                    qtyField.setText("1");
                    currentOrders.remove(ref);
                }
                updateFileAndTotal(totalText);
            });

            plus.setOnAction(_ -> {
                int v = Integer.parseInt(qtyField.getText());
                if (v < maxQty) v++;
                qtyField.setText(String.valueOf(v));
                currentOrders.put(ref, new int[]{v, price});
                updateFileAndTotal(totalText);
            });

            minus.setOnAction(_ -> {
                int v = Integer.parseInt(qtyField.getText());
                if (v > 1) v--;
                qtyField.setText(String.valueOf(v));
                currentOrders.put(ref, new int[]{v, price});
                updateFileAndTotal(totalText);
            });

            qtyField.textProperty().addListener((_, _, newV) -> {
                if (!newV.matches("\\d*")) qtyField.setText(newV.replaceAll("\\D", ""));
                else if (!newV.isEmpty()) {
                    int v = Integer.parseInt(newV);
                    if (v < 1) v = 1;
                    if (v > maxQty) v = maxQty;
                    qtyField.setText(String.valueOf(v));
                    currentOrders.put(ref, new int[]{v, price});
                    updateFileAndTotal(totalText);
                }
            });

            productBoxes.add(box);
            flow.getChildren().add(box);
        }


        searchBox.textProperty().addListener((_, _, _) -> filterProducts(productBoxes, searchBox, categoryBox));
        categoryBox.valueProperty().addListener((_, _, _) -> filterProducts(productBoxes, searchBox, categoryBox));

        ScrollPane scroll = new ScrollPane(flow);
        scroll.setFitToWidth(true);

        reset.setOnAction(_ -> {
            currentOrders.clear();
            try { new PrintWriter(orderStorageFile).close(); } catch (Exception ignored) {}
            totalText.setText("Total: 0");

            for (VBox box : productBoxes) {
                HBox qtyBox = (HBox) box.getChildren().get(3);
                qtyBox.setVisible(false);
                ((TextField) qtyBox.getChildren().get(1)).setText("1");

                Button btn = (Button) box.getChildren().getFirst();
                int idx = productBoxes.indexOf(box);
                int stock = Integer.parseInt(products.get(idx).get(2));
                boolean available = Boolean.parseBoolean(products.get(idx).get(5));
                refreshButtonState(btn, (TextField) qtyBox.getChildren().get(1), stock, available);
            }
        });

        mainPane.setTop(topBox);
        mainPane.setCenter(scroll);
        mainPane.setBottom(bottomBox);

        Scene sc = new Scene(mainPane, screenSizeWidth * 0.65, screenSizeHeight * 0.7);
        mainStage.setScene(sc);
        mainStage.show();
    }

    private void filterProducts(List<VBox> productBoxes, TextField searchBox, ComboBox<String> categoryBox) {
        String query = searchBox.getText().toLowerCase();
        String selectedCategory = categoryBox.getValue();
        List<Node> matched = new ArrayList<>();
        List<Node> unmatched = new ArrayList<>();

        for (int i = 0; i < productBoxes.size(); i++) {
            VBox box = productBoxes.get(i);
            String name = products.get(i).get(1).toLowerCase();
            String category = products.get(i).get(4);

            boolean matchesSearch = name.contains(query);
            boolean matchesCategory = selectedCategory.equals("ALL") || selectedCategory.equals(category);

            boolean visible = matchesSearch && matchesCategory;
            box.setVisible(visible);

            if (visible) matched.add(box);
            else unmatched.add(box);
        }
        FlowPane flow = (FlowPane) productBoxes.getFirst().getParent();
        flow.getChildren().setAll(matched);
        flow.getChildren().addAll(unmatched);
    }


    private int getCurrentTotal() {
        int total = 0;
        for (int[] v : currentOrders.values()) total += v[0] * v[1];
        return total;
    }

    private void updateFileAndTotal(Text totalText) {
        try (PrintWriter write = new PrintWriter(orderStorageFile)) {
            int total = 0;
            for (var e : currentOrders.entrySet()) {
                int qty = e.getValue()[0];
                int price = e.getValue()[1];
                total += qty * price;
                write.println(e.getKey() + "," + qty + "," + (qty * price));
            }
            totalText.setText("Total: " + total);
        } catch (Exception ignored) {}
    }

    private void refreshButtonState(Button btn, TextField qtyField, int stock, boolean available) {
        if (!available || stock <= 0) {
            btn.setDisable(true);
            btn.setOpacity(0.6);
            btn.setStyle("""
            -fx-background-color: #f0f0f0;  /* subtle grey */
            -fx-border-color: black;
            -fx-border-width: 2;
            -fx-focus-color: transparent;
            -fx-faint-focus-color: transparent;
        """);

            StackPane stack = new StackPane();
            Node graphic = btn.getGraphic();
            stack.getChildren().add(graphic);

            Label overlay = new Label("Out of Stock");
            overlay.setStyle("""
            -fx-background-color: rgba(255,255,255,0.7);
            -fx-text-fill: red;
            -fx-font-weight: bold;
            -fx-padding: 2 5 2 5;
        """);
            overlay.setMouseTransparent(true);
            stack.getChildren().add(overlay);
            btn.setGraphic(stack);

            qtyField.setText("1");
        } else {
            // Available state
            btn.setDisable(false);
            btn.setOpacity(1.0);

            String ref = (String) btn.getUserData();
            if (currentOrders.containsKey(ref)) {
                btn.setStyle("""
                            -fx-background-color: transparent;
                            -fx-border-color: green;
                            -fx-border-width: 2;
                            -fx-focus-color: transparent;
                            -fx-faint-focus-color: transparent;
                            """);
            } else {
                btn.setStyle("""
                            -fx-background-color: transparent;
                            -fx-border-color: black;
                            -fx-border-width: 2;
                            -fx-focus-color: transparent;
                            -fx-faint-focus-color: transparent;
                            """);
            }


            if (btn.getGraphic() instanceof StackPane sp && sp.getChildren().size() > 1) {
                Node originalGraphic = sp.getChildren().getFirst();
                btn.setGraphic(originalGraphic);
            }
        }
    }



    private void openCheckoutScene() {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #f5f5f5;");

        HBox top = new HBox();
        top.setAlignment(Pos.CENTER);
        top.setPadding(new Insets(20));
        Text title = new Text("Checkout");
        title.setFont(Font.font("Arial", 32));
        title.setStyle("-fx-font-weight: bold; -fx-fill: #333333;"); // dark text
        top.getChildren().add(title);
        root.setTop(top);

        VBox list = new VBox(15);
        list.setPadding(new Insets(20));
        ScrollPane scroll = new ScrollPane(list);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        root.setCenter(scroll);

        for (String ref : new HashSet<>(currentOrders.keySet())) {
            String name = getProductName(ref);
            int qty = currentOrders.get(ref)[0];
            int price = currentOrders.get(ref)[1];
            int maxStock = getMaxStock(ref);

            HBox row = new HBox(15);
            row.setAlignment(Pos.CENTER_LEFT);
            row.setPadding(new Insets(15));
            row.setStyle("""
            -fx-background-color: white;
            -fx-background-radius: 10;
            -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0, 0, 2);
        """);

            Label nameL = new Label(name);
            nameL.setPrefWidth(200);
            nameL.setFont(Font.font("Arial", 16));
            nameL.setStyle("-fx-text-fill: #333333;");

            Button minus = new Button("-");
            Button plus = new Button("+");
            TextField qtyField = new TextField(String.valueOf(qty));
            qtyField.setPrefWidth(50);
            Button remove = new Button("X");

            String flatStyle = """
            -fx-background-color: #e0e0e0;
            -fx-background-radius: 5;
            -fx-border-color: transparent;
            -fx-font-size: 14px;
        """;

            minus.setStyle(flatStyle);
            plus.setStyle(flatStyle);
            remove.setStyle(flatStyle + "-fx-background-color: #ff6b6b; -fx-text-fill: white;");

            qtyField.setStyle("""
            -fx-background-color: #f0f0f0;
            -fx-background-radius: 5;
            -fx-alignment: center;
            -fx-font-size: 14px;
        """);

            qtyField.textProperty().addListener((_, _, newV) -> {
                if (!newV.matches("\\d*"))
                    qtyField.setText(newV.replaceAll("\\D", ""));
                else if (!newV.isEmpty()) {
                    int v = Integer.parseInt(newV);
                    if (v < 1) v = 1;
                    if (v > maxStock) v = maxStock;
                    qtyField.setText(String.valueOf(v));
                    currentOrders.put(ref, new int[]{v, price});
                    updateCheckoutTotal(root);
                }
            });

            minus.setOnAction(_ -> {
                int v = Integer.parseInt(qtyField.getText());
                if (v > 1) v--;
                qtyField.setText(String.valueOf(v));
                currentOrders.put(ref, new int[]{v, price});
                updateCheckoutTotal(root);
            });

            plus.setOnAction(_ -> {
                int v = Integer.parseInt(qtyField.getText());
                if (v < maxStock) v++;
                qtyField.setText(String.valueOf(v));
                currentOrders.put(ref, new int[]{v, price});
                updateCheckoutTotal(root);
            });

            remove.setOnAction(_ -> {
                list.getChildren().remove(row);
                currentOrders.remove(ref);
                updateCheckoutTotal(root);
            });

            row.getChildren().addAll(nameL, minus, qtyField, plus, remove);
            list.getChildren().add(row);
        }

        HBox bottom = new HBox(20);
        bottom.setAlignment(Pos.CENTER_RIGHT);
        bottom.setPadding(new Insets(20));
        bottom.setStyle("-fx-background-color: transparent;");

        Text totalText = new Text("Total: ₱" + getCurrentTotal());
        totalText.setFont(Font.font("Arial", 24));
        totalText.setStyle("-fx-font-weight: bold; -fx-fill: #333333;");

        Button cancel = new Button("Cancel");
        Button confirm = new Button("Confirm");

        cancel.setStyle("""
        -fx-background-color: #e0e0e0;
        -fx-background-radius: 8;
        -fx-font-size: 16px;
        -fx-text-fill: #333333;
        -fx-padding: 8 20 8 20;
    """);

        confirm.setStyle("""
        -fx-background-color: #4CAF50;
        -fx-background-radius: 8;
        -fx-font-size: 16px;
        -fx-text-fill: white;
        -fx-padding: 8 20 8 20;
    """);

        cancel.setOnAction(_ -> {
            mainStage.hide();
            try { figure2(); } catch (Exception ignored) {}
        });

        confirm.setOnAction(_ -> {
            applyOrderToProductFile();
            currentOrders.clear();
            try { new PrintWriter(orderStorageFile).close(); } catch (Exception ignored) {}
            mainStage.hide();
            try { figure2(); } catch (Exception ignored) {}
        });

        bottom.getChildren().addAll(totalText, cancel, confirm);
        root.setBottom(bottom);

        Scene checkoutScene = new Scene(root, screenSizeWidth * 0.65, screenSizeHeight * 0.7);
        mainStage.setScene(checkoutScene);
        mainStage.show();
    }


    private String getProductName(String ref) {
        try (BufferedReader product = new BufferedReader(new FileReader("Data/ProductData/products.txt"))) {
            String line;
            while ((line = product.readLine()) != null) {
                String[] p = line.split(",");
                if (p[0].equals(ref)) return p[1];
            }
        } catch (Exception ignored) {}
        return "";
    }

    private int getMaxStock(String ref) {
        try (BufferedReader product = new BufferedReader(new FileReader("Data/ProductData/products.txt"))) {
            String line;
            while ((line = product.readLine()) != null) {
                String[] p = line.split(",");
                if (p[0].equals(ref)) return Integer.parseInt(p[2]);
            }
        } catch (Exception ignored) {}
        return 1;
    }

    private void validateProductsFile() {
        File file = new File("Data/ProductData/products.txt");
        List<String> newLines = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] p = line.split(",");
                if (p.length < 6) continue;
                String ref = p[0];
                String name = p[1];
                int stock = Integer.parseInt(p[2]);
                String price = p[3];
                String category = p[4];
                boolean available = Boolean.parseBoolean(p[5]);

                newLines.add(ref + "," + name + "," + stock + "," + price + "," + category + "," + available);
            }
        } catch (IOException ignore) {
            return;
        }

        if (newLines.isEmpty()) return;

        try (PrintWriter writer = new PrintWriter(file)) {
            for (String l : newLines) writer.println(l);
        } catch (IOException ignore) {}
    }


    private void applyOrderToProductFile() {
        try {
            File file = new File("Data/ProductData/products.txt");
            List<String> newLines = new ArrayList<>();

            try (BufferedReader r = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = r.readLine()) != null) {
                    String[] p = line.split(",");
                    if (p.length < 6) continue;
                    String ref = p[0];
                    int qty = Integer.parseInt(p[2]);
                    String category = p[4];
                    boolean available = Boolean.parseBoolean(p[5]);

                    if (currentOrders.containsKey(ref)) {
                        int bought = currentOrders.get(ref)[0];
                        qty -= bought;
                        if (qty <= 0) {
                            qty = 0;
                            available = false;
                        }
                    }
                    newLines.add(ref + "," + p[1] + "," + qty + "," + p[3] + "," + category + "," + available);
                }
            }

            if (!newLines.isEmpty()) {
                try (PrintWriter w = new PrintWriter(file)) {
                    newLines.forEach(w::println);
                }
            }

        } catch (Exception ignored) {}
    }


    private void updateCheckoutTotal(BorderPane root) {
        Text t = (Text)((HBox)root.getBottom()).getChildren().getFirst();
        t.setText("Total: " + getCurrentTotal());
    }
}
