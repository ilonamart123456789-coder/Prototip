package org.example.lab14;

import javafx.application.Application;// управление жизненным циклом
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.example.lab14.models.User;
import java.io.IOException;// датчика непредвиденных проблем

// преобразование в графику
public class Main extends Application {
    private static Scene scene;
    public static User currentUser;

    @Override
    public void start(Stage stage) throws IOException {
        scene = new Scene(loadFXML("/org/example/lab14/Prototip.fxml"), 1300, 800);

        stage.setTitle("Taste World - Система управления кафе");

        // минимальные размеры окна
        stage.setMinWidth(1300);
        stage.setMinHeight(800);

        stage.setScene(scene);
        stage.show();
    }
    // смена окон
    public static void setRoot(String fxml) {
        try {
            scene.setRoot(loadFXML(fxml));
        } catch (IOException e) {
            System.err.println("Ошибка при смене окна: " + fxml);
            e.printStackTrace();
        }
    }
    // загрузка fxml в память
    private static Parent loadFXML(String fxml) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource(fxml));
        return fxmlLoader.load();
    }
    public static void main(String[] args) {
        launch();
    }
}