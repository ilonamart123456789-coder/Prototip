package org.example.lab14.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert; //класс для создания всплывающих окон с ошибками/сообщениями
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import org.example.lab14.Main;
import org.example.lab14.db.DatabaseHandler;
import org.example.lab14.models.User;

public class Prototip {

    // Привязываем главную панель из FXML
    @FXML private StackPane rootPane;
    @FXML private TextField loginField;
    @FXML private PasswordField passwordField;

    @FXML void initialize() {
        //блок возможных ошибок
        try {
            //поиск картинки
            BackgroundSize bgSize = new BackgroundSize(100, 100, true, true, false, true);
            BackgroundImage bgImage = new BackgroundImage(new Image(getClass().getResourceAsStream("/org/example/lab14/fon.jpg")), BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.CENTER, bgSize);
            rootPane.setBackground(new Background(bgImage));
        } catch (Exception e) { System.out.println("Не удалось загрузить фон: " + e.getMessage()); }
    }

    @FXML void goToAdmin(ActionEvent event) { handleLogin("admin", "/org/example/lab14/Admin.fxml"); }
    @FXML void goToOfic(ActionEvent event) { handleLogin("sotrudnik", "/org/example/lab14/Ofic.fxml"); }

    private void handleLogin(String expectedRole, String targetFxml) {
        String login = loginField.getText().trim(), pass = passwordField.getText().trim();

        if (login.isEmpty() || pass.isEmpty()) { showAlert("Ошибка", "Заполните логин и пароль"); return; }

        // Отправляем логин и пароль в базу данных
        User user = DatabaseHandler.authenticate(login, pass);

        // Если логин и пароль верные
        if (user != null) {
            if (user.getRole() != null && user.getRole().equalsIgnoreCase(expectedRole)) {
                Main.currentUser = user; Main.setRoot(targetFxml);
            } else {

                // Если логин верный, но роль не совпадает
                showAlert("Доступ запрещен", "У вас роль '" + (user.getRole() == null ? "не назначена" : user.getRole()) + "', а требуется '" + expectedRole + "'.");
            }
        } else showAlert("Ошибка авторизации", "Неверный логин или пароль");
    }

    private void showAlert(String title, String content) { // Вспомогательный метод для удобного создания всплывающих окон
        Alert alert = new Alert(Alert.AlertType.ERROR); alert.setTitle(title); alert.setContentText(content); alert.showAndWait(); // Создаем объект окна с типом "Ошибка" (красный крестик) // Устанавливаем заголовок окна // Устанавливаем основной текст сообщения // Показываем окно и ждем, пока пользователь его закроет
    }
}