module org.example.lab14 {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;

    opens org.example.lab14 to javafx.fxml;
    exports org.example.lab14;
    exports org.example.lab14.controllers;
    opens org.example.lab14.controllers to javafx.fxml;
    exports org.example.lab14.db;
    opens org.example.lab14.db to javafx.fxml;
    exports org.example.lab14.models;
    opens org.example.lab14.models to javafx.fxml;
}