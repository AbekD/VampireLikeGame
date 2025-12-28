module com.example.vimpirelikegame {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;


    opens com.vampirelikegame to javafx.fxml;
    exports com.vampirelikegame;
}