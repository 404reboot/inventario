module inventario {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires mysql.connector.j;
    requires com.github.librepdf.openpdf;
    requires javafx.base;
    requires javafx.graphics;
    requires java.desktop;

    exports App;
    exports Controller;
    exports Model;

    opens App to javafx.fxml;
    opens Controller to javafx.fxml;
    opens View to javafx.fxml;
}
