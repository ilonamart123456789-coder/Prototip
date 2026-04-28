package org.example.lab14;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;

public class Ofic {

    // Связываем переменную с ComboBox
    @FXML
    private ComboBox<String> tableComboBox;

    // Метод initialize() вызывается автоматически при загрузке окна
    @FXML
    public void initialize() {
        // Добавляем номера столов в выпадающий список
        tableComboBox.getItems().addAll("1", "2", "3", "4", "5");

        // Устанавливаем значение по умолчанию
        tableComboBox.setValue("1");

        // когда официант выберет другой стол, сработает этот код
        tableComboBox.setOnAction(this::onTableChanged);
    }

    // Метод, который срабатывает при выборе нового стола
    private void onTableChanged(ActionEvent event) {
        String selectedTable = tableComboBox.getValue();
    }

    @FXML
    void logout(ActionEvent event) {
        // Возвращаемся в меню авторизации
        Main.setRoot("Prototip.fxml");
    }
}