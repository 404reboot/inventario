module com.example.inventario {
    // Módulos requeridos base
    requires javafx.controls;
    requires javafx.fxml;
    requires transitive javafx.graphics;
    requires transitive java.sql;
    requires mysql.connector.j;
    requires java.desktop;

    // 🔥 NUEVO: Módulos necesarios para los Reportes (iText y Apache POI)
    requires itextpdf;
    requires org.apache.poi.poi;
    requires org.apache.poi.ooxml;

    // Exportar paquetes
    exports app;
    exports controlador;
    exports modelo;

    // Abrir paquetes para JavaFX (Ajustado para corregir accesos de componentes)
    opens app to javafx.fxml;
    opens controlador to javafx.fxml;
    opens modelo to javafx.fxml, javafx.base; // <-- Añadido javafx.base para el mapeo de tablas
}