package Controller;

import Model.*;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

public class ReportController {

    @FXML
    private TabPane reportTabPane;

    // Pestaña de Inventario
    @FXML
    private Button btnGenerateInventoryReport;
    @FXML
    private Label lblTotalProductos;
    @FXML
    private Label lblTotalStock;
    @FXML
    private Label lblLowStock;
    @FXML
    private Label lblTotalValue;

    // Pestaña de Stock Bajo
    @FXML
    private Button btnGenerateLowStockReport;
    @FXML
    private TableView<Producto> lowStockTable;

    // Pestaña de Movimientos
    @FXML
    private DatePicker startDatePicker;
    @FXML
    private DatePicker endDatePicker;
    @FXML
    private Button btnGenerateMovementsReport;
    @FXML
    private ComboBox<String> movementTypeFilter;

    // Pestaña de Categorías
    @FXML
    private Button btnGenerateCategoriesReport;

    private ProductoDAO productDAO = new ProductoDAO();
    private CategoriaDAO categoryDAO = new CategoriaDAO();
    private InventoryMovementDAO movementDAO = new InventoryMovementDAO();

    @FXML
    public void initialize() {
        loadDashboardData();
        loadLowStockTable();
        setupDatePickers();
        setupMovementFilter();
    }

    private void loadDashboardData() {
        List<Producto> products = productDAO.getAllProductos();

        int totalProductos = products.size();
        int totalStock = products.stream().mapToInt(Producto::getStock).sum();
        long lowStock = products.stream().filter(p -> p.getStock() <= p.getStock_minimo()).count();
        double totalValue = products.stream()
                .mapToDouble(p -> p.getPrecio_venta().doubleValue() * p.getStock())
                .sum();

        lblTotalProductos.setText(String.valueOf(totalProductos));
        lblTotalStock.setText(String.valueOf(totalStock));
        lblLowStock.setText(String.valueOf(lowStock));
        lblTotalValue.setText(String.format("$%.2f", totalValue));
    }

    private void loadLowStockTable() {
        // Configurar y cargar tabla de stock bajo (simplificado)
        // Puedes reutilizar la tabla de productos con filtro
    }

    private void setupDatePickers() {
        startDatePicker.setValue(LocalDate.now().minusDays(30));
        endDatePicker.setValue(LocalDate.now());
    }

    private void setupMovementFilter() {
        movementTypeFilter.setItems(javafx.collections.FXCollections.observableArrayList(
                "Todos", "ENTRADA", "SALIDA"));
        movementTypeFilter.setValue("Todos");
    }

    @FXML
    private void handleGenerateInventoryReport() {
        List<Producto> products = productDAO.getAllProductos();

        if (products.isEmpty()) {
            showAlert("Error", "No hay productos para generar el reporte.");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Guardar Reporte de Inventario");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
        fileChooser.setInitialFileName("Inventario_General_" + LocalDate.now().toString() + ".pdf");

        File file = fileChooser.showSaveDialog(new Stage());

        if (file != null) {
            boolean success = ReportService.generateInventoryReport(products, file.getAbsolutePath());

            if (success) {
                showAlert("Éxito", "Reporte generado correctamente en:\n" + file.getAbsolutePath());
            } else {
                showAlert("Error", "No se pudo generar el reporte.");
            }
        }
    }

    @FXML
    private void handleGenerateLowStockReport() {
        List<Producto> products = productDAO.getAllProductos();
        List<Producto> lowStockProductos = products.stream()
                .filter(p -> p.getStock() <= p.getStock_minimo())
                .toList();

        if (lowStockProductos.isEmpty()) {
            showAlert("Información", "No hay productos con stock bajo.");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Guardar Reporte de Stock Bajo");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
        fileChooser.setInitialFileName("Stock_Bajo_" + LocalDate.now().toString() + ".pdf");
        File file = fileChooser.showSaveDialog(new Stage());

        if (file != null) {
            boolean success = ReportService.generateLowStockReport(lowStockProductos, file.getAbsolutePath());

            if (success) {
                showAlert("Éxito", "Reporte generado correctamente en:\n" + file.getAbsolutePath());
            } else {
                showAlert("Error", "No se pudo generar el reporte.");
            }
        }
    }

    @FXML
    private void handleGenerateMovementsReport() {
        LocalDate startDate = startDatePicker.getValue();
        LocalDate endDate = endDatePicker.getValue();

        if (startDate == null || endDate == null) {
            showAlert("Error", "Seleccione un rango de fechas válido.");
            return;
        }

        if (startDate.isAfter(endDate)) {
            showAlert("Error", "La fecha de inicio no puede ser posterior a la fecha de fin.");
            return;
        }

        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(LocalTime.MAX);

        List<InventoryMovement> movements = movementDAO.getAllMovements();

        // Filtrar por fecha
        movements = movements.stream()
                .filter(m -> {
                    LocalDateTime fecha = m.getFechaMovimiento();
                    return fecha != null && !fecha.isBefore(start) && !fecha.isAfter(end);
                })
                .toList();

        // Filtrar por tipo
        String tipo = movementTypeFilter.getValue();
        if (!tipo.equals("Todos")) {
            movements = movements.stream()
                    .filter(m -> m.getTipoMovimiento().equals(tipo))
                    .toList();
        }

        if (movements.isEmpty()) {
            showAlert("Información", "No hay movimientos en el período seleccionado.");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Guardar Reporte de Movimientos");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
        fileChooser.setInitialFileName("Movimientos_" + LocalDate.now().toString() + ".pdf");

        File file = fileChooser.showSaveDialog(new Stage());

        if (file != null) {
            boolean success = ReportService.generateMovementsReport(
                    movements, start, end, file.getAbsolutePath());

            if (success) {
                showAlert("Éxito", "Reporte generado correctamente en:\n" + file.getAbsolutePath());
            } else {
                showAlert("Error", "No se pudo generar el reporte.");
            }
        }
    }

    @FXML
    private void handleGenerateCategoriesReport() {
        List<Categoria> categories = categoryDAO.getAllCategorias();
        List<Producto> products = productDAO.getAllProductos();

        if (categories.isEmpty()) {
            showAlert("Error", "No hay categorías para generar el reporte.");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Guardar Reporte de Categorías");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
        fileChooser.setInitialFileName("Categorias_" + LocalDate.now().toString() + ".pdf");

        File file = fileChooser.showSaveDialog(new Stage());

        if (file != null) {
            boolean success = ReportService.generateCategoriesReport(
                    categories, products, file.getAbsolutePath());

            if (success) {
                showAlert("Éxito", "Reporte generado correctamente en:\n" + file.getAbsolutePath());
            } else {
                showAlert("Error", "No se pudo generar el reporte.");
            }
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
