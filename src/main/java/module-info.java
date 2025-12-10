module com.example.vimpirelikegame {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.example.vimpirelikegame to javafx.fxml;
    exports com.example.vimpirelikegame;
}