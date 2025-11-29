import javafx.beans.binding.Bindings;
import javafx.beans.binding.DoubleBinding;
import javafx.geometry.Rectangle2D;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Screen;
import javafx.stage.Stage;

public class DesignHandler {
    String filePath = System.getProperty("user.dir");
    Rectangle2D screen = Screen.getPrimary().getBounds();
    String imagePath;
    static Stage mainStage = new Stage();
    static DoubleBinding movementListener = Bindings.createDoubleBinding(
            () -> mainStage.getHeight() * .07,
            mainStage.heightProperty(),
            mainStage.widthProperty());
    DesignHandler () {
        mainStage.setMaximized(true);
        mainStage.setMinWidth(screen.getWidth() * .65);
        mainStage.setMinHeight(screen.getWidth() * .70);
        mainStage.show();
    }
    String imagePath (String string) {
        return this.imagePath = "file:"+filePath+string;
    }
    double getScreenHeight () {
        return this.screen.getHeight();
    }
    double getScreeWidth () {
        return this.screen.getWidth();
    }
    static void BorderHandler (Object object) {
        if (object instanceof StackPane stackPane) {
            stackPane.borderProperty().bind(Bindings.createObjectBinding(
                    () -> new Border(new BorderStroke(
                            Color.BLACK,
                            BorderStrokeStyle.SOLID,
                            CornerRadii.EMPTY,
                            new BorderWidths(movementListener.doubleValue() * .03)
                    )), movementListener
            ));
        }
    }
}
