import javafx.beans.binding.Bindings;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class Interface {
    static Rectangle2D screen = Screen.getPrimary().getBounds();
    final static double screenSizeWidth = screen.getWidth();
    final static double screenSizeHeight = screen.getHeight();
    static BufferedReader reader;
    static Stage mainStage;
    static final String userDirectory = System.getProperty("user.dir");
    private static final Image SHOW_IMG = new Image("file:"+userDirectory + "/interfaceImage/images/show.png");
    private static final Image HIDE_IMG = new Image("file:"+userDirectory + "/interfaceImage/images/hide.png");


    static void figure1() throws IOException {
        reader = new BufferedReader(new FileReader(userDirectory+"/Data/UserDatas/userData.txt"));
        mainStage = new Stage();
        mainStage.setMaximized(true);
        mainStage.setMinWidth(screenSizeWidth * .65);
        mainStage.setMinHeight(screenSizeHeight * .70);
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

        StackPane fig1nodes = new StackPane();
        fig1nodes.setOnMouseClicked(_ -> fig1nodes.requestFocus());
        fig1nodes.borderProperty().bind(Bindings.createObjectBinding(
                () -> new Border(new BorderStroke(
                        Color.BLACK,
                        BorderStrokeStyle.SOLID,
                        CornerRadii.EMPTY,
                        new BorderWidths(topNodeSize.doubleValue() * .03)
                )), topNodeSize
        ));
        BorderPane textBorder = new BorderPane();
        HBox topNode = new HBox();
        topNode.prefHeightProperty().bind(topNodeSize);
        topNode.paddingProperty().bind(Bindings.createObjectBinding(
                () -> new Insets(
                        0,
                        0,
                        0,
                        topNodeSize.doubleValue() * .1
                ), topNodeSize
        ));
        topNode.borderProperty().bind(Bindings.createObjectBinding(
                () -> new Border(new BorderStroke(
                        Color.BLACK,
                        BorderStrokeStyle.SOLID,
                        CornerRadii.EMPTY,
                        new BorderWidths(0, 0, topNodeSize.doubleValue() * .03, 0)
                )), topNodeSize
        ));
        topNode.setAlignment(Pos.CENTER_LEFT);
        Text pos = new Text("Point of System Sale");
        pos.fontProperty().bind(Bindings.createObjectBinding(
                () -> Font.font("Arial", FontWeight.BOLD, topNodeSize.doubleValue() * .4),
                topNodeSize
        ));
        topNode.getChildren().add(pos);

        VBox middleNode = new VBox();
        middleNode.maxWidthProperty().bind(Bindings.createObjectBinding(
                () -> mainStage.getWidth() * .4,
                topNodeSize
        ));
        BorderPane.setMargin(middleNode, new Insets(50, 0, 100, 0));
        middleNode.setStyle("-fx-background-color: lightgray; -fx-border-color: black");

        String textFieldStyle = ("""
                -fx-font-size: 20px;
                -fx-font-family: Calibri;
                -fx-border-radius: 20;
                -fx-background-radius: 20;
                -fx-focus-color: transparent;
                -fx-faint-focus-color: transparent;
                -fx-padding: 0 0 0 15;
                """);

        Text login = new Text("Log In");
        text(login, mainStage, true, .9);
        inset(login, .07, 0, .1);

        Text userName = new Text("Username");
        text(userName, mainStage, false, .3);
        inset(userName, .08, .0, .09);

        TextField userNameTextField = new TextField();
        textBoxBinder(userNameTextField);
        inset(userNameTextField, 0, 0, .07);
        userNameTextField.setStyle(textFieldStyle);

        Text passWord = new Text("Password");
        text(passWord, mainStage, false, .3);
        inset(passWord, .10, 0, .09);

        StackPane passWordShow = new StackPane();
        passWordShow.prefWidthProperty().bind(userNameTextField.widthProperty());
        passWordShow.setMaxWidth(Region.USE_PREF_SIZE);
        inset(passWordShow, 0, 0, .07);

        PasswordField passWordField = new PasswordField();
        textBoxBinder(passWordField);
        passWordField.setStyle(textFieldStyle);
        userNameTextField.setOnAction(_-> {
            passWordField.requestFocus();
            passWordField.positionCaret(passWordField.getLength());
        });

        TextField passWordTextField = new TextField();
        passWordTextField.setStyle(textFieldStyle);
        passWordField.textProperty().bindBidirectional(passWordTextField.textProperty());
        passWordTextField.setManaged(false);
        passWordTextField.setVisible(false);
        textBoxBinder(passWordTextField);
        passWordTextField.heightProperty().addListener(
                (_, _, ov) -> Font.font("Calibri", FontWeight.NORMAL, ov.doubleValue() * .05));
        Button showPass = new Button();
        showPass.setMinSize(0, 0);

        ImageView show = new ImageView(HIDE_IMG);
        show.setPreserveRatio(true);
        show.setSmooth(true);

        show.fitWidthProperty().bind(showPass.widthProperty());
        show.fitHeightProperty().bind(showPass.heightProperty());

        showPass.setStyle("-fx-background-color: transparent; -fx-padding: 0; -fx-border-radius: 50; -fx-background-radius: 50");
        buttonSize(showPass, userNameTextField);
        userNameTextField.widthProperty().addListener(
                (_, _, number) -> StackPane.setMargin(showPass, new Insets(0, number.doubleValue() * .02, 0, 0)));
        StackPane.setAlignment(showPass, Pos.CENTER_RIGHT);
        showPass.setOnAction((_) -> {
            boolean isVisible = passWordField.isVisible();
            passWordField.setManaged(!isVisible);
            passWordField.setVisible(!isVisible);
            passWordTextField.setManaged(isVisible);
            passWordTextField.setVisible(isVisible);
            show.setImage(isVisible ? SHOW_IMG : HIDE_IMG);
        });

        showPass.setGraphic(show);

//        userNameTextField.textProperty().addListener((_, _, _) -> {
//            try {
//                checkAutoLogin(userNameTextField, passWordField);
//            } catch (IOException e) {
//                throw new RuntimeException(e);
//            }
//        });
//        passWordField.textProperty().addListener((_, _, _) -> {
//            try {
//                checkAutoLogin(userNameTextField, passWordField);
//            } catch (IOException e) {
//                throw new RuntimeException(e);
//            }
//        });

        passWordShow.getChildren().addAll(passWordField, passWordTextField, showPass);
        middleNode.getChildren().addAll(login, userName, userNameTextField, passWord, passWordShow);
        textBorder.setTop(topNode);
        textBorder.setCenter(middleNode);
        fig1nodes.getChildren().add(textBorder);

        Scene fig1 = new Scene(fig1nodes);
        mainStage.setScene(fig1);
        mainStage.show();
        mainStage.hide();
        new Scene2(mainStage).figure2();
    }
    static void textBoxBinder (TextField node) {
        node.prefHeightProperty().bind(mainStage.heightProperty().multiply(.055));
        node.maxWidthProperty().bind(mainStage.widthProperty().multiply(.32));
    }
    static void textBoxBinder (PasswordField node) {
        node.prefHeightProperty().bind(mainStage.heightProperty().multiply(.055));
        node.maxWidthProperty().bind(mainStage.widthProperty().multiply(.32));
    }
    private static void checkAutoLogin (TextField textField, PasswordField passwordField) throws IOException{
        int col = 0;
        int row = 0;
        String read;
        while((read = reader.readLine()) != null) {
            String[] regex = read.split(",");
            if (row == 0) {
                col = regex.length;
            }
            row++;
        }

//        System.out.println(row + " "+ col+ " "+counter);
        String[][] datas = new String[row][col];
        BufferedReader reader1 = new BufferedReader(new FileReader(userDirectory+"/Data/UserDatas/userData.txt"));
        String read1;
        int colCounter = 0;
        while ((read1 = reader1.readLine()) != null) {
            String[] regex = read1.split(",");
            for (int i = 0; i < datas.length; i++) {
                datas[colCounter][i] = regex[i];
            }
            colCounter++;
        }
        if (textField.getText() != null && passwordField.getText() != null) {
            String userName;
            String passWord;
            for (int i = 0; i < datas.length; i++) {
                    userName = datas[i][0];
                    passWord = datas[i][1];
                    if (textField.getText().equals(userName) && passwordField.getText().equals(passWord)) {
                        mainStage.hide();
                        new Scene2(mainStage);
                    }
                }
        }
    }
    static void buttonSize (Button button, Node node) {
        if (node instanceof TextField textField) {
            button.prefHeightProperty().bind(textField.heightProperty().multiply(0.6));
            button.prefWidthProperty().bind(textField.widthProperty().multiply(0.1));
        }
    }
    static void inset (Node node, double topInset, double rightInset, double leftInset) {
        mainStage.heightProperty().addListener(
                (_, _, newVal) -> VBox.setMargin(node, new Insets(
                newVal.doubleValue() * topInset,
                newVal.doubleValue() * rightInset,
                newVal.doubleValue() * (double) 0,
                newVal.doubleValue() * leftInset)));
    }
    static void text (Text text, Stage mainStage, boolean bold, double fontSize) {
        var topNodeSize = Bindings.createDoubleBinding(
                () -> mainStage.getHeight() * .07,
                mainStage.heightProperty(),
                mainStage.widthProperty()
        );
        text.fontProperty().bind(Bindings.createObjectBinding(
                () -> Font.font(
                        "Calibri",
                        bold ? FontWeight.BOLD : FontWeight.NORMAL,
                        (fontSize == -1) ? topNodeSize.doubleValue() :
                        topNodeSize.doubleValue() * fontSize),
                topNodeSize
        ));
    }
}
