module org.example.unogame {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;


    opens org.example.unogame to javafx.fxml;
    opens org.example.unogame.controller to javafx.fxml;
    exports org.example.unogame;
}