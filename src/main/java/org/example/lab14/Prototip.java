package org.example.lab14;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;

public class Prototip {

    @FXML
    void goToAdmin(ActionEvent event) {
        // Переходим на панель администратора
        Main.setRoot("Admin.fxml");
    }

    @FXML
    void goToOfic(ActionEvent event) {
        // Переходим на панель официанта
        Main.setRoot("Ofic.fxml");
    }
}