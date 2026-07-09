module inventario {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires mysql.connector.j;
    requires javafx.base;
    requires javafx.graphics;

    exports App;
    exports Controller;
    exports Model;

    opens App to javafx.fxml;
    opens Controller to javafx.fxml;
    opens View to javafx.fxml;
}
