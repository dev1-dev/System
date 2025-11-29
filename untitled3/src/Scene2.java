import javafx.beans.binding.Bindings;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

class Scene2 {
    static int HOW_MANY_ROWS = 1000000; //baguhin sa sunod gamit yung pag read sa .txt kung gaano karami yung row
    static int HOW_MANY_COLUMNS = 5; //same shit per bahala na kung gaano karami usto mo fck you
    final static double screenSizeHeight = Interface.screenSizeHeight;
    final static double screenSizeWidth = Interface.screenSizeWidth;
    Stage mainStage;
    Scene2 (Stage mainStage){
        this.mainStage = mainStage;
    }
    void figure2 () throws IOException {
        var topNodeSize = Bindings.createDoubleBinding(
                () -> mainStage.getHeight() * .07,
                mainStage.heightProperty(),
                mainStage.widthProperty()
        );
        mainStage.maximizedProperty().addListener((_, _, thisBoolean) -> {
            if (!thisBoolean) {
                mainStage.setHeight(screenSizeHeight * .70);
                mainStage.setWidth(screenSizeWidth * .65);
            }
        });
        StackPane fig2 = new StackPane();
        fig2.setOnMouseClicked(_-> fig2.requestFocus());
        fig2.borderProperty().bind(Bindings.createObjectBinding(
                () -> new Border(new BorderStroke(
                        Color.BLACK,
                        BorderStrokeStyle.SOLID,
                        CornerRadii.EMPTY,
                        new BorderWidths(topNodeSize.doubleValue() * .03)
                )), topNodeSize
        ));
        BorderPane fig2borderPane = new BorderPane();
        HBox title = new HBox();
        title.prefHeightProperty().bind(topNodeSize);
        title.borderProperty().bind(Bindings.createObjectBinding(
                () -> new Border(new BorderStroke(
                        Color.BLACK,
                        BorderStrokeStyle.SOLID,
                        CornerRadii.EMPTY,
                        new BorderWidths(0, 0, topNodeSize.doubleValue() * .03, 0)
                )), topNodeSize
        ));
        title.setStyle("-fx-background-color: red");

        HBox functions = new HBox(10);
        functions.prefHeightProperty().bind(topNodeSize);
        functions.borderProperty().bind(Bindings.createObjectBinding(
                () -> new Border(new BorderStroke(
                        Color.BLACK,
                        BorderStrokeStyle.SOLID,
                        CornerRadii.EMPTY,
                        new BorderWidths(topNodeSize.doubleValue() * .03, 0, 0, 0)
                )), topNodeSize
        ));
        functions.setStyle("-fx-background-color: blue");
        Button reset = new Button("Reset");
        reset.prefHeightProperty().bind(functions.heightProperty().multiply(.8));
        reset.prefWidthProperty().bind(functions.widthProperty().multiply(.1));
        Button checkOut = new Button("Check Out");
        checkOut.prefHeightProperty().bind(functions.heightProperty().multiply(.8));
        checkOut.prefWidthProperty().bind(functions.widthProperty().multiply(.1));
        Text total = new Text("Total");
        total.fontProperty().bind(Bindings.createObjectBinding(
                () -> Font.font(topNodeSize.doubleValue() * .4),
                topNodeSize
        ));
        functions.setAlignment(Pos.CENTER_RIGHT);
        functions.getChildren().addAll(total, reset, checkOut);
        List<List<String>> data = new ArrayList<>();
        for (int r = 0; r < HOW_MANY_ROWS; r++) {
            List<String> row = new ArrayList<>();
            for (int c = 0; c < HOW_MANY_COLUMNS; c++) {
                row.add("[" + r + "," + c + "]");
            }
            data.add(row);
        }

        // eto mga 80% ko lang gets yung 20% copy paste nalang kaya wag na ako tanuning kung pano to gumagana
        //btw same shit na gagamiting para dun sa list ng items etc.
        ListView<List<String>> listView = new ListView<>();
        listView.setSelectionModel(null); // disable selection
        listView.setFocusTraversable(false);
        listView.setStyle("""
                -fx-background-color: transparent;
                -fx-control-inner-background: transparent;
                -fx-padding: 0;
                -fx-border-insets: 0;
                -fx-background-insets: 0;
                """);

        listView.getItems().addAll(data);
        // backend neto ay ganto gagawa ka ng item UUID kahit d UUID basta ano sya unique now magawa ka ng Map ilalagay mo yung cords nung
        //button dun sa map ganto ba Map<Pair<Integer,Integer>, Runnable> buttonActions = new HashMap<>(); tas lagay sa for loop
        //yung col tas yung row tas kung anong gagawin nung button basta ganon
        listView.setCellFactory(_ -> new ListCell<>() {
            @Override
            protected void updateItem(List<String> rowItems, boolean empty) {
                super.updateItem(rowItems, empty);
                if (empty || rowItems == null) {
                    setGraphic(null);
                } else {
                    HBox row = new HBox(10);
                    row.setStyle("-fx-padding: 5;");
                    int rowIndex = getIndex();
//                    System.out.println(rowItems.size());
//                    System.out.println(getIndex());
                    for (int colIndex = 0; colIndex < rowItems.size(); colIndex++) {
                        String s = rowItems.get(colIndex);
                        int finalCol = colIndex;
                        int finalRow = rowIndex;
                        String finalText = s;


                        VBox container = new VBox(5); // spacing
                        container.setStyle("-fx-alignment: center;");

                        Button mainBtn = new Button(s);
                        mainBtn.prefWidthProperty().bind(listView.widthProperty().multiply(.19));
                        mainBtn.prefHeightProperty().bind(listView.heightProperty().multiply(.15));

                        HBox qtySelector = new HBox(5);
                        qtySelector.setVisible(false);

                        Button minus = new Button("-");
                        TextField qtyField = new TextField("1");
                        qtyField.setPrefWidth(40);
                        Button plus = new Button("+");
                        qtySelector.getChildren().addAll(minus, qtyField, plus);
                        String color = "-fx-background-color: black";
                        Label nameLabel = new Label("Name: " + s);
                        nameLabel.setStyle(color);
                        Label priceLabel = new Label("Basta yung presyo dto from the .txt");
                        priceLabel.setStyle(color);

                        container.getChildren().addAll(mainBtn, qtySelector, nameLabel, priceLabel);
                        mainBtn.setOnAction(_ -> {
                            qtySelector.setVisible(!qtySelector.isVisible());
                            System.out.println("Clicked ROW " + finalRow +
                                    " COL " + finalCol +
                                    " VALUE " + finalText);

                        });
                        plus.setOnAction(_ -> {
                            int val = Integer.parseInt(qtyField.getText());
                            qtyField.setText(String.valueOf(val + 1));
                        });
                        minus.setOnAction(ev -> {
                            int val = Integer.parseInt(qtyField.getText());
                            if (val > 1) qtyField.setText(String.valueOf(val - 1));
                        });

                        row.getChildren().add(container);
                    }

                    setGraphic(row);
                }
            }
        });



        fig2borderPane.setCenter(listView);
        fig2borderPane.setTop(title);
        fig2borderPane.setBottom(functions);
        fig2.getChildren().add(fig2borderPane);
        Scene scene = new Scene(fig2);
        mainStage.setScene(scene);
        mainStage.show();
        mainStage.hide();
        new Scene3(mainStage).fig3();
    }
}
