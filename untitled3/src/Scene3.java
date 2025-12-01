import javafx.event.Event;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.*;
//this class is 70% AI && 30% HUMAN
public class Scene3 {
    Stage mainStage;
    File productFile = new File(System.getProperty("user.dir") + "/Data/ProductData/products.txt");
    File adminFile = new File(System.getProperty("user.dir") + "/Data/UserDatas/adminData.txt");
    File employeeFile = new File(System.getProperty("user.dir") + "/Data/UserDatas/userData.txt");

    List<Map<String, String>> products = new ArrayList<>();
    List<Map<String, String>> admins = new ArrayList<>();
    List<Map<String, String>> employees = new ArrayList<>();

    public Scene3(Stage mainStage) {
        this.mainStage = mainStage;
    }

    public void fig3() {
        loadProducts();
        loadUsers();

        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #f5f5f5;");

        TabPane tabPane = new TabPane();

        // Products Tab
        Tab productTab = new Tab("Products");
        productTab.setClosable(false);
        VBox productBox = createProductBox();
        productTab.setContent(productBox);

        // Users Tab
        Tab userTab = new Tab("Users");
        userTab.setClosable(false);
        VBox userBox = createUserManagementTab();
        userTab.setContent(userBox);

        tabPane.getTabs().addAll(productTab, userTab);

        // Back button
        Button backBtn = new Button("Back");
        styleButton(backBtn, "#666666");
        backBtn.setOnAction(_ -> {
            try {
                mainStage.hide();
                Interface.figure1();
            } catch (Exception ignore) {
            }
        });

        HBox topBox = new HBox(backBtn);
        topBox.setPadding(new Insets(10));
        root.setTop(topBox);
        root.setCenter(tabPane);

        Scene scene = new Scene(root, 900, 600);
        mainStage.setScene(scene);
        mainStage.show();
    }

    private VBox createProductBox() {
        VBox root = new VBox(15);
        root.setPadding(new Insets(15));

        HBox topBox = new HBox(10);
        topBox.setAlignment(Pos.CENTER_LEFT);

        Button addBtn = new Button("Add");
        Button editBtn = new Button("Edit");
        Button deleteBtn = new Button("Delete Selected");
        Button resetBtn = new Button("Reset Selection");
        TextField searchField = new TextField();
        searchField.setPromptText("Search product...");

        styleButtons(addBtn, editBtn, deleteBtn);
        styleButton(resetBtn, "#FF5722");

        topBox.getChildren().addAll(addBtn, editBtn, deleteBtn, resetBtn, searchField);

        VBox productListBox = new VBox(15);
        productListBox.setPadding(new Insets(15));
        ScrollPane scroll = new ScrollPane(productListBox);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background: transparent; -fx-border-color: transparent;");

        populateProductList(productListBox, "");

        searchField.textProperty().addListener((_, _, newV) -> populateProductList(productListBox, newV));
        addBtn.setOnAction(_ -> openAddProduct(productListBox));
        editBtn.setOnAction(_ -> openEditProduct(productListBox));
        deleteBtn.setOnAction(_ -> deleteSelectedProductsWithConfirmation(productListBox));
        resetBtn.setOnAction(_ -> {
            for (Node n : productListBox.getChildren()) {
                if (n instanceof HBox row) {
                    CheckBox cb = (CheckBox) row.getChildren().getFirst();
                    cb.setSelected(false);
                }
            }
        });

        root.getChildren().addAll(topBox, scroll);
        return root;
    }

    private void populateProductList(VBox box, String filter) {
        box.getChildren().clear();

        for (Map<String, String> p : products) {
            if (!p.get("name").toLowerCase().contains(filter.toLowerCase())) continue;

            HBox row = new HBox(15);
            row.setAlignment(Pos.CENTER_LEFT);
            row.setPadding(new Insets(10));
            row.setStyle("""
                    -fx-background-color: white;
                    -fx-background-radius: 10;
                    -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 4,0,0,1);
                """);

            CheckBox cb = new CheckBox();
            cb.setStyle("-fx-cursor: hand; -fx-scale-x: 1.2; -fx-scale-y: 1.2;");

            VBox info = new VBox(5);
            Label name = new Label(p.get("name"));
            name.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #333333;");
            Label details = new Label("Qty: " + p.get("qty") + "  |  Price: ₱" + p.get("price"));
            details.setStyle("-fx-font-size: 14px; -fx-text-fill: #666666;");
            info.getChildren().addAll(name, details);

            row.getChildren().addAll(cb, info);

            row.setOnMouseClicked(_ -> cb.setSelected(!cb.isSelected()));
            cb.setOnMouseClicked(Event::consume);

            box.getChildren().add(row);
        }

        box.setOnMouseClicked(Event::consume);
    }

    private void openAddProduct(VBox productListBox) {
        Stage addStage = new Stage();
        VBox root = new VBox(10);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: #ffffff; -fx-background-radius: 10;");

        TextField nameField = new TextField();
        nameField.setPromptText("Product Name");
        TextField qtyField = new TextField();
        qtyField.setPromptText("Quantity");
        TextField priceField = new TextField();
        priceField.setPromptText("Price");

        Button uploadImageBtn = new Button("Upload Image");
        styleButton(uploadImageBtn, "#FF9800");

        uploadImageBtn.setOnAction(_ -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg")
            );
            File selectedFile = fileChooser.showOpenDialog(addStage);
            if (selectedFile != null) {
                try {
                    String extension = selectedFile.getName().substring(selectedFile.getName().lastIndexOf("."));
                    File dest = new File(System.getProperty("user.dir") + "/Data/ProductImages/" + nameField.getText() + extension);
                    Files.copy(selectedFile.toPath(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
                } catch (Exception ex) {
                    showAlert("Error", "Failed to save image.", Alert.AlertType.ERROR);
                }
            }
        });

        Button saveBtn = new Button("Save");
        styleButton(saveBtn, "#4CAF50");
        saveBtn.setOnAction(_ -> {
            try {
                String name = nameField.getText();
                int qty = Integer.parseInt(qtyField.getText());
                int price = Integer.parseInt(priceField.getText());

                String serial;
                Random rand = new Random();
                while (true) {
                    serial = String.format("%08d", rand.nextInt(100_000_000));
                    final String s = serial;
                    boolean exists = products.stream().anyMatch(p -> p.get("serial").equals(s));
                    if (!exists) break;
                }

                Map<String, String> newProduct = new HashMap<>();
                newProduct.put("serial", serial);
                newProduct.put("name", name);
                newProduct.put("qty", String.valueOf(qty));
                newProduct.put("price", String.valueOf(price));
                newProduct.put("available", qty > 0 ? "true" : "false");

                products.add(newProduct);
                saveProducts();
                populateProductList(productListBox, "");
                addStage.close();
            } catch (Exception ignore) {
            }
        });

        root.getChildren().addAll(
                new Label("Name:"), nameField,
                new Label("Quantity:"), qtyField,
                new Label("Price:"), priceField,
                uploadImageBtn, saveBtn
        );

        addStage.setScene(new Scene(root, 350, 350));
        addStage.show();
    }

    private void openEditProduct(VBox productListBox) {
        List<HBox> selectedRows = new ArrayList<>();
        for (Node n : productListBox.getChildren()) {
            if (n instanceof HBox row) {
                CheckBox cb = (CheckBox) row.getChildren().getFirst();
                if (cb.isSelected()) selectedRows.add(row);
            }
        }

        if (selectedRows.isEmpty()) {
            showAlert("Edit Product", "No product selected!", Alert.AlertType.WARNING);
            return;
        } else if (selectedRows.size() > 1) {
            showAlert("Edit Product", "Select only one product to edit!", Alert.AlertType.WARNING);
            return;
        }

        HBox row = selectedRows.getFirst();
        VBox info = (VBox) row.getChildren().get(1);
        Label nameLabel = (Label) info.getChildren().getFirst();

        Map<String, String> product = products.stream()
                .filter(p -> p.get("name").equals(nameLabel.getText()))
                .findFirst().orElse(null);
        if (product != null) {
            Stage stage = new Stage();
            VBox root = new VBox(10);
            root.setPadding(new Insets(20));
            root.setStyle("-fx-background-color: #ffffff; -fx-background-radius: 10;");

            TextField nameField = new TextField(product.get("name"));
            TextField qtyField = new TextField(product.get("qty"));
            TextField priceField = new TextField(product.get("price"));

            Button replaceImageBtn = new Button("Replace Image");
            styleButton(replaceImageBtn, "#FF9800");
            replaceImageBtn.setOnAction(_ -> {
                FileChooser fileChooser = new FileChooser();
                fileChooser.getExtensionFilters().addAll(
                        new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg")
                );
                File selectedFile = fileChooser.showOpenDialog(stage);
                if (selectedFile != null) {
                    try {
                        String extension = selectedFile.getName().substring(selectedFile.getName().lastIndexOf("."));
                        File dest = new File(System.getProperty("user.dir") + "/Data/ProductImages/" + nameField.getText() + extension);
                        Files.copy(selectedFile.toPath(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
                        showAlert("Success", "Image replaced successfully!", Alert.AlertType.INFORMATION);
                    } catch (Exception ex) {
                        showAlert("Error", "Failed to replace image.", Alert.AlertType.ERROR);
                    }
                }
            });

            Button saveBtn = new Button("Save");
            styleButton(saveBtn, "#2196F3");
            saveBtn.setOnAction(_ -> {
                product.put("name", nameField.getText());
                product.put("qty", qtyField.getText());
                product.put("price", priceField.getText());
                product.put("available", Integer.parseInt(qtyField.getText()) > 0 ? "true" : "false");
                saveProducts();
                populateProductList(productListBox, "");
                stage.close();
            });

            root.getChildren().addAll(
                    new Label("Name:"), nameField,
                    new Label("Quantity:"), qtyField,
                    new Label("Price:"), priceField,
                    replaceImageBtn, saveBtn
            );

            stage.setScene(new Scene(root, 400, 350));
            stage.show();
        }
    }

    private void deleteSelectedProductsWithConfirmation(VBox productListBox) {
        List<HBox> selectedRows = new ArrayList<>();
        for (Node n : productListBox.getChildren()) {
            if (n instanceof HBox row) {
                CheckBox cb = (CheckBox) row.getChildren().getFirst();
                if (cb.isSelected()) selectedRows.add(row);
            }
        }

        if (selectedRows.isEmpty()) {
            showAlert("Delete Product", "No products selected!", Alert.AlertType.WARNING);
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Delete Confirmation");
        confirm.setHeaderText("Are you sure you want to delete selected products?");
        Optional<ButtonType> result = confirm.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            for (HBox row : selectedRows) {
                VBox info = (VBox) row.getChildren().get(1);
                Label nameLabel = (Label) info.getChildren().getFirst();
                products.removeIf(p -> p.get("name").equals(nameLabel.getText()));
            }
            saveProducts();
            populateProductList(productListBox, "");
        }
    }

    private void saveProducts() {
        try (PrintWriter pw = new PrintWriter(productFile)) {
            for (Map<String, String> p : products) {
                pw.println(p.get("serial") + "," + p.get("name") + "," + p.get("qty") + "," + p.get("price") + "," + p.get("available"));
            }
        } catch (Exception ignored) {}
    }


    private VBox createUserManagementTab() {
        VBox root = new VBox(15);
        root.setPadding(new Insets(15));

        HBox topBox = new HBox(10);
        topBox.setAlignment(Pos.CENTER_LEFT);

        Button addBtn = new Button("Add");
        Button editBtn = new Button("Edit");
        Button deleteBtn = new Button("Delete Selected");
        Button resetBtn = new Button("Reset Selection");

        styleButtons(addBtn, editBtn, deleteBtn);
        styleButton(resetBtn, "#FF5722");

        ComboBox<String> filterBox = new ComboBox<>();
        filterBox.getItems().addAll("All", "Admins", "Employees");
        filterBox.setValue("All");

        topBox.getChildren().addAll(addBtn, editBtn, deleteBtn, resetBtn, filterBox);

        VBox userListBox = new VBox(10);
        ScrollPane scroll = new ScrollPane(userListBox);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background: transparent; -fx-border-color: transparent;");

        populateUserList(userListBox, "All");

        addBtn.setOnAction(_ -> openAddUser(userListBox));
        editBtn.setOnAction(_ -> openEditUser(userListBox));
        deleteBtn.setOnAction(_ -> deleteSelectedUsersWithConfirmation(userListBox));
        resetBtn.setOnAction(_ -> {
            for (Node n : userListBox.getChildren()) {
                if (n instanceof HBox row) {
                    CheckBox cb = (CheckBox) row.getChildren().getFirst();
                    cb.setSelected(false);
                }
            }
        });

        filterBox.setOnAction(_ -> populateUserList(userListBox, filterBox.getValue()));

        root.getChildren().addAll(topBox, scroll);
        return root;
    }

    private void populateUserList(VBox box, String filter) {
        box.getChildren().clear();

        List<Map<String, String>> listToShow = new ArrayList<>();
        if (filter.equals("All")) {
            listToShow.addAll(admins);
            listToShow.addAll(employees);
        } else if (filter.equals("Admins")) {
            listToShow.addAll(admins);
        } else {
            listToShow.addAll(employees);
        }

        for (Map<String, String> u : listToShow) {
            HBox row = new HBox(10);
            row.setPadding(new Insets(8));
            row.setAlignment(Pos.CENTER_LEFT);
            row.setStyle("""
                    -fx-background-color: white;
                    -fx-background-radius: 10;
                    -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 4,0,0,1);
                """);

            CheckBox cb = new CheckBox();
            cb.setStyle("-fx-cursor: hand; -fx-scale-x: 1.2; -fx-scale-y: 1.2;");

            VBox info = new VBox(2);
            Label name = new Label(u.get("username"));
            name.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #333;");
            Label pass = new Label("Password: " + u.get("password"));
            pass.setStyle("-fx-font-size: 12px; -fx-text-fill: #666;");
            Label role = new Label(admins.contains(u) ? "Admin" : "Employee");
            role.setStyle("-fx-font-size: 12px; -fx-text-fill: #999;");
            info.getChildren().addAll(name, pass, role);

            row.getChildren().addAll(cb, info);

            row.setOnMouseClicked(_ -> cb.setSelected(!cb.isSelected()));
            cb.setOnMouseClicked(Event::consume);

            box.getChildren().add(row);
        }

        box.setOnMouseClicked(Event::consume);
    }

    private void openAddUser(VBox box) {
        Stage stage = new Stage();
        stage.setTitle("Add User");

        VBox root = new VBox(15);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: #ffffff; -fx-background-radius: 10;");

        TextField username = new TextField();
        username.setPromptText("Username");

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");

        TextField visiblePassword = new TextField();
        visiblePassword.setManaged(false);
        visiblePassword.setVisible(false);
        passwordField.textProperty().bindBidirectional(visiblePassword.textProperty());

        Button toggleBtn = new Button("Show");
        toggleBtn.setOnAction(_ -> {
            if (visiblePassword.isVisible()) {
                visiblePassword.setVisible(false);
                visiblePassword.setManaged(false);
                passwordField.setVisible(true);
                passwordField.setManaged(true);
                toggleBtn.setText("Show");
            } else {
                visiblePassword.setVisible(true);
                visiblePassword.setManaged(true);
                passwordField.setVisible(false);
                passwordField.setManaged(false);
                toggleBtn.setText("Hide");
            }
        });

        HBox passBox = new HBox(5, passwordField, visiblePassword, toggleBtn);

        CheckBox elevated = new CheckBox("Elevated (Admin)");

        Button saveBtn = new Button("Save");
        styleButton(saveBtn, "#2196F3");
        saveBtn.setOnAction(_ -> {
            if (username.getText().isEmpty() || passwordField.getText().isEmpty()) {
                showAlert("Error", "Username and password cannot be empty!", Alert.AlertType.ERROR);
                return;
            }

            Map<String, String> newUser = new HashMap<>();
            newUser.put("username", username.getText());
            newUser.put("password", passwordField.getText());

            if (elevated.isSelected()) admins.add(newUser);
            else employees.add(newUser);

            saveUsers(admins, adminFile);
            saveUsers(employees, employeeFile);
            populateUserList(box, "All");
            stage.close();
        });

        root.getChildren().addAll(
                new Label("Username:"), username,
                new Label("Password:"), passBox,
                elevated, saveBtn
        );

        stage.setScene(new Scene(root, 350, 300));
        stage.show();
    }

    private void openEditUser(VBox box) {
        List<HBox> selectedRows = new ArrayList<>();
        for (Node n : box.getChildren()) {
            if (n instanceof HBox row) {
                CheckBox cb = (CheckBox) row.getChildren().getFirst();
                if (cb.isSelected()) selectedRows.add(row);
            }
        }

        if (selectedRows.isEmpty()) {
            showAlert("Edit User", "No user selected!", Alert.AlertType.WARNING);
            return;
        } else if (selectedRows.size() > 1) {
            showAlert("Edit User", "Select only one user to edit!", Alert.AlertType.WARNING);
            return;
        }

        HBox row = selectedRows.getFirst();
        VBox info = (VBox) row.getChildren().get(1);
        Label nameLabel = (Label) info.getChildren().getFirst();

        Map<String, String> user;
        List<Map<String, String>> list = null;
        if ((user = admins.stream().filter(u -> u.get("username").equals(nameLabel.getText())).findFirst().orElse(null)) != null) {
            list = admins;
        } else if ((user = employees.stream().filter(u -> u.get("username").equals(nameLabel.getText())).findFirst().orElse(null)) != null) {
            list = employees;
        }

        if (user != null && list != null) {
            Stage stage = new Stage();
            stage.setTitle("Edit User");

            VBox root = new VBox(15);
            root.setPadding(new Insets(20));
            root.setStyle("-fx-background-color: #ffffff; -fx-background-radius: 10;");

            TextField username = new TextField(user.get("username"));

            PasswordField passwordField = new PasswordField();
            passwordField.setText(user.get("password"));

            TextField visiblePassword = new TextField();
            visiblePassword.setManaged(false);
            visiblePassword.setVisible(false);
            passwordField.textProperty().bindBidirectional(visiblePassword.textProperty());

            Button toggleBtn = new Button("Show");
            toggleBtn.setOnAction(_ -> {
                if (visiblePassword.isVisible()) {
                    visiblePassword.setVisible(false);
                    visiblePassword.setManaged(false);
                    passwordField.setVisible(true);
                    passwordField.setManaged(true);
                    toggleBtn.setText("Show");
                } else {
                    visiblePassword.setVisible(true);
                    visiblePassword.setManaged(true);
                    passwordField.setVisible(false);
                    passwordField.setManaged(false);
                    toggleBtn.setText("Hide");
                }
            });

            HBox passBox = new HBox(5, passwordField, visiblePassword, toggleBtn);

            CheckBox elevated = new CheckBox("Elevated (Admin)");
            elevated.setSelected(admins.contains(user));

            Button saveBtn = new Button("Save");
            styleButton(saveBtn, "#2196F3");
            final Map<String, String> finalUser = user;
            final List<Map<String, String>> finalList = list;

            saveBtn.setOnAction(_ -> {
                if (username.getText().isEmpty() || passwordField.getText().isEmpty()) {
                    showAlert("Error", "Username and password cannot be empty!", Alert.AlertType.ERROR);
                    return;
                }

                // Remove from current list
                finalList.remove(finalUser);

                // Update user info
                finalUser.put("username", username.getText());
                finalUser.put("password", passwordField.getText());

                // Add to correct list based on elevated
                if (elevated.isSelected()) {
                    admins.add(finalUser);
                } else {
                    employees.add(finalUser);
                }

                saveUsers(admins, adminFile);
                saveUsers(employees, employeeFile);
                populateUserList(box, "All");
                stage.close();
            });


            root.getChildren().addAll(
                    new Label("Username:"), username,
                    new Label("Password:"), passBox,
                    elevated, saveBtn
            );

            stage.setScene(new Scene(root, 400, 300));
            stage.show();
        }
    }

    private void deleteSelectedUsersWithConfirmation(VBox box) {
        List<HBox> selectedRows = new ArrayList<>();
        for (Node n : box.getChildren()) {
            if (n instanceof HBox row) {
                CheckBox cb = (CheckBox) row.getChildren().getFirst();
                if (cb.isSelected()) selectedRows.add(row);
            }
        }

        if (selectedRows.isEmpty()) {
            showAlert("Delete User", "No users selected!", Alert.AlertType.WARNING);
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Delete Confirmation");
        confirm.setHeaderText("Are you sure you want to delete selected users?");
        Optional<ButtonType> result = confirm.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            for (HBox row : selectedRows) {
                VBox info = (VBox) row.getChildren().get(1);
                Label nameLabel = (Label) info.getChildren().getFirst();
                admins.removeIf(u -> u.get("username").equals(nameLabel.getText()));
                employees.removeIf(u -> u.get("username").equals(nameLabel.getText()));
            }
            saveUsers(admins, adminFile);
            saveUsers(employees, employeeFile);
            populateUserList(box, "All");
        }
    }

    // ------------------- UTILITIES -------------------
    private void loadProducts() {
        try {
            products.clear();
            List<String> lines = Files.readAllLines(productFile.toPath());
            for (String line : lines) {
                String[] split = line.split(",");
                if (split.length >= 5) {
                    Map<String, String> p = new HashMap<>();
                    p.put("serial", split[0]);
                    p.put("name", split[1]);
                    p.put("qty", split[2]);
                    p.put("price", split[3]);
                    p.put("available", split[4]);
                    products.add(p);
                }
            }
        } catch (Exception ignore) {}
    }

    private void loadUsers() {
        admins.clear();
        employees.clear();
        loadUsersFromFile(adminFile, admins);
        loadUsersFromFile(employeeFile, employees);
    }

    private void loadUsersFromFile(File file, List<Map<String, String>> list) {
        try {
            List<String> lines = Files.readAllLines(file.toPath());
            for (String line : lines) {
                String[] split = line.split(",");
                if (split.length >= 2) {
                    Map<String, String> u = new HashMap<>();
                    u.put("username", split[0]);
                    u.put("password", split[1]);
                    list.add(u);
                }
            }
        } catch (Exception ignore) {}
    }

    private void saveUsers(List<Map<String, String>> list, File file) {
        try {
            PrintWriter pw = new PrintWriter(file);
            for (Map<String, String> u : list) {
                pw.println(u.get("username") + "," + u.get("password"));
            }
            pw.close();
        } catch (Exception ignored) {}
    }

    private void styleButtons(Button... buttons) {
        for (Button b : buttons) styleButton(b, "#4CAF50");
    }

    private void styleButton(Button btn, String color) {
        btn.setStyle("-fx-background-color: " + color + "; -fx-text-fill: white; -fx-font-weight: bold;");
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
