module org.example.humanity {
    requires javafx.controls;
    requires javafx.fxml;


    opens org.example.humanity to javafx.fxml;
    exports org.example.humanity;
}