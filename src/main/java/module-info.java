module org.example.lab14 {
    requires javafx.controls;
    requires javafx.fxml;


    opens org.example.lab14 to javafx.fxml;
    exports org.example.lab14;
}