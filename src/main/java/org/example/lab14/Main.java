package org.example.lab14;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class Main extends Application {

    private static Scene scene;

    @Override
    public void start(Stage stage) throws IOException {
        scene = new Scene(loadFXML("Prototip.fxml"));
        stage.setTitle("Taste world");
        stage.setResizable(false);
        stage.setScene(scene);
        stage.show();
    }

    // метод для переключения FXML файлов
    public static void setRoot(String fxml) {
        try {
            scene.setRoot(loadFXML(fxml));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static Parent loadFXML(String fxml) throws IOException {
        // Создаем загрузчик FXML, указывая путь к ресурсу
        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource(fxml));
        // Загружаем и возвращаем иерархию объектов интерфейса
        return fxmlLoader.load();
    }

    public static void main(String[] args) {
        launch();
    }
}