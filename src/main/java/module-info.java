module org.example.unogame {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;


    opens org.example.unogame to javafx.fxml;
    opens org.example.unogame.controller to javafx.fxml;
    opens org.example.unogame.view.Alert to javafx.fxml;
    exports org.example.unogame;
    exports org.example.unogame.view.Alert;
    exports org.example.unogame.model.machine.observers;
    exports org.example.unogame.model.fileHanldlers;
}