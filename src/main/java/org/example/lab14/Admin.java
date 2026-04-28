package org.example.lab14;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;

public class Admin {

    @FXML
    void logout(ActionEvent event) {
        Main.setRoot("Prototip.fxml");
    }
}