import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Scene3 {
    Stage mainStage;
    static int HOW_MANY_ROWS = 1000000;
    static int HOW_MANY_COLUMNS = 5;
    final static double screenSizeHeight = Interface.screenSizeHeight;
    final static double screenSizeWidth = Interface.screenSizeWidth;

    Scene3(Stage mainStage) {
        this.mainStage = mainStage;
    }

    void fig3() throws IOException {
        BorderPane figure3 = new BorderPane();

        // 1. Load CSV
        List<List<String>> data = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(
                new FileReader(System.getProperty("user.dir") + "/Data/ProductData/products.txt"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] split = line.split(",");
                List<String> row = new ArrayList<>();
                for (String s : split) row.add(s);
                data.add(row);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // 2. ListView
        ListView<List<String>> listView = new ListView<>();
        listView.getItems().addAll(data);

        // 3. Custom cell
        listView.setCellFactory(_ -> new ListCell<>() {
            @Override
            protected void updateItem(List<String> rowItems, boolean empty) {
                super.updateItem(rowItems, empty);
                if (empty || rowItems == null || rowItems.size() < 5) { // ensure 5 elements
                    setGraphic(null);
                } else {
                    HBox row = new HBox(10);

                    // [Button][refNum]
                    Button selectButton = new Button("Select");
                    Label refNum = new Label(rowItems.get(0));
                    HBox buttonRef = new HBox(5, selectButton, refNum);

                    // [product]
                    Label product = new Label(rowItems.get(1));

                    // [quantity]
                    Label quantity = new Label(rowItems.get(2));

                    // ([price][availability])
                    Label price = new Label(rowItems.get(3));
                    boolean available = Boolean.parseBoolean(rowItems.get(4));
                    Label availability = new Label("●");
                    availability.setStyle("-fx-text-fill: " + (available ? "green" : "red"));
                    HBox priceAvail = new HBox(5, price, availability);

                    row.getChildren().addAll(buttonRef, product, quantity, priceAvail);
                    setGraphic(row);
                }
            }
        });

        figure3.setCenter(listView);
        Scene scene = new Scene(figure3, 800, 600);
        mainStage.setScene(scene);
        mainStage.show();
    }
}
