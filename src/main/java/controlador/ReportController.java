package controlador; // Cambiado de 'Controller' a 'controlador'

import modelo.ReportService; // Cambiado de 'Service.ReportService' a 'modelo.ReportService'
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.time.LocalDate;

/**
 * Controlador JavaFX del módulo de Reportes ({@code ReportView.fxml}).
 * <p>
 * Actúa como intermediario entre la interfaz de usuario y
 * {@link ReportService}, permitiendo al usuario elegir la ubicación de
 * guardado (mediante un {@link FileChooser}) y disparar la generación de
 * los distintos reportes disponibles (stock actual, movimientos por
 * rango de fechas, bajo stock, y productos más movidos), cada uno en
 * formato PDF o Excel.
 */
public class ReportController {

    /** Botón para generar el reporte de stock actual en PDF. */
    @FXML
    private Button btnStockPdf;
    /** Botón para generar el reporte de stock actual en Excel. */
    @FXML
    private Button btnStockExcel;
    /** Botón para generar el reporte de movimientos en PDF. */
    @FXML
    private Button btnMovementsPdf;
    /** Botón para generar el reporte de movimientos en Excel. */
    @FXML
    private Button btnMovementsExcel;
    /** Botón para generar el reporte de bajo stock en PDF. */
    @FXML
    private Button btnLowStockPdf;
    /** Botón para generar el reporte de bajo stock en Excel. */
    @FXML
    private Button btnLowStockExcel;
    /** Botón para generar el reporte de productos más movidos en PDF. */
    @FXML
    private Button btnMostMovedPdf;
    /** Botón para generar el reporte de productos más movidos en Excel. */
    @FXML
    private Button btnMostMovedExcel;

    /** Selector de la fecha de inicio para el reporte de movimientos. */
    @FXML
    private DatePicker dpStartDate;
    /** Selector de la fecha de fin para el reporte de movimientos. */
    @FXML
    private DatePicker dpEndDate;
    /** Selector numérico del umbral de stock para el reporte de bajo stock. */
    @FXML
    private Spinner<Integer> spinStockThreshold;
    /** Selector numérico del límite (top N) para el reporte de productos más movidos. */
    @FXML
    private Spinner<Integer> spinMostMovedLimit;

    /** Servicio utilizado para generar los distintos reportes en PDF y Excel. */
    private final ReportService reportService = new ReportService();

    /**
     * Método de inicialización llamado automáticamente al cargar la vista
     * FXML. Configura los valores por defecto de los {@link Spinner} de
     * umbral de stock y límite de ranking, y asocia cada botón de la
     * interfaz con su manejador de generación de reporte correspondiente.
     */
    @FXML
    public void initialize() {
        // Inicializar los spinners con valores por defecto
        spinStockThreshold.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 1000, 10));
        spinMostMovedLimit.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 100, 5));

        // Configurar eventos de los botones de Stock Actual
        btnStockPdf.setOnAction(e -> handleCurrentStockPdf());
        btnStockExcel.setOnAction(e -> handleCurrentStockExcel());

        // Configurar eventos de los botones de Movimientos
        btnMovementsPdf.setOnAction(e -> handleMovementsPdf());
        btnMovementsExcel.setOnAction(e -> handleMovementsExcel());

        // Configurar eventos de los botones de Alerta / Bajo Stock
        btnLowStockPdf.setOnAction(e -> handleLowStockPdf());
        btnLowStockExcel.setOnAction(e -> handleLowStockExcel());

        // Configurar eventos de los botones de Productos Más Movidos
        btnMostMovedPdf.setOnAction(e -> handleMostMovedPdf());
        btnMostMovedExcel.setOnAction(e -> handleMostMovedExcel());
    }

    // ==========================================
    // ACCIONES: REPORTE DE STOCK ACTUAL
    // ==========================================

    /**
     * Manejador del botón "Stock (PDF)": solicita al usuario la ubicación
     * de guardado y genera el reporte de stock actual en formato PDF
     * mediante {@link ReportService#generateCurrentStockPdf(String)}.
     */
    private void handleCurrentStockPdf() {
        File file = showSaveDialog("Guardar Reporte Stock (PDF)", "PDF Files (*.pdf)", "*.pdf");
        if (file != null) {
            try {
                reportService.generateCurrentStockPdf(file.getAbsolutePath());
                showAlert(Alert.AlertType.INFORMATION, "Éxito", "Reporte de stock actual generado correctamente en PDF.");
            } catch (Exception ex) {
                showAlert(Alert.AlertType.ERROR, "Error", "No se pudo generar el PDF: " + ex.getMessage());
            }
        }
    }

    /**
     * Manejador del botón "Stock (Excel)": solicita al usuario la
     * ubicación de guardado y genera el reporte de stock actual en
     * formato Excel mediante {@link ReportService#generateCurrentStockExcel(String)}.
     */
    private void handleCurrentStockExcel() {
        File file = showSaveDialog("Guardar Reporte Stock (Excel)", "Excel Files (*.xlsx)", "*.xlsx");
        if (file != null) {
            try {
                reportService.generateCurrentStockExcel(file.getAbsolutePath());
                showAlert(Alert.AlertType.INFORMATION, "Éxito", "Reporte de stock actual generado correctamente en Excel.");
            } catch (Exception ex) {
                showAlert(Alert.AlertType.ERROR, "Error", "No se pudo generar el Excel: " + ex.getMessage());
            }
        }
    }

    // ==========================================
    // ACCIONES: REPORTE DE MOVIMIENTOS
    // ==========================================

    /**
     * Manejador del botón "Movimientos (PDF)": valida que se haya
     * seleccionado un rango de fechas coherente, solicita la ubicación de
     * guardado y genera el reporte de movimientos en formato PDF mediante
     * {@link ReportService#generateMovementsReportPdf(String, String, String)}.
     */
    private void handleMovementsPdf() {
        LocalDate start = dpStartDate.getValue();
        LocalDate end = dpEndDate.getValue();

        if (start == null || end == null) {
            showAlert(Alert.AlertType.WARNING, "Campos Requeridos", "Por favor, seleccione un rango de fechas válido.");
            return;
        }

        if (start.isAfter(end)) {
            showAlert(Alert.AlertType.WARNING, "Rango Inválido", "La fecha de inicio no puede ser posterior a la fecha de fin.");
            return;
        }

        File file = showSaveDialog("Guardar Reporte de Movimientos (PDF)", "PDF Files (*.pdf)", "*.pdf");
        if (file != null) {
            try {
                reportService.generateMovementsReportPdf(file.getAbsolutePath(), start.toString(), end.toString());
                showAlert(Alert.AlertType.INFORMATION, "Éxito", "Reporte de movimientos generado en PDF.");
            } catch (Exception ex) {
                showAlert(Alert.AlertType.ERROR, "Error", "Error al crear el PDF de movimientos: " + ex.getMessage());
            }
        }
    }

    /**
     * Manejador del botón "Movimientos (Excel)": valida que se haya
     * seleccionado un rango de fechas coherente, solicita la ubicación de
     * guardado y genera el reporte de movimientos en formato Excel
     * mediante {@link ReportService#generateMovementsReportExcel(String, String, String)}.
     */
    private void handleMovementsExcel() {
        LocalDate start = dpStartDate.getValue();
        LocalDate end = dpEndDate.getValue();

        if (start == null || end == null) {
            showAlert(Alert.AlertType.WARNING, "Campos Requeridos", "Por favor, seleccione un rango de fechas válido.");
            return;
        }

        if (start.isAfter(end)) {
            showAlert(Alert.AlertType.WARNING, "Rango Inválido", "La fecha de inicio no puede ser posterior a la fecha de fin.");
            return;
        }

        File file = showSaveDialog("Guardar Reporte de Movimientos (Excel)", "Excel Files (*.xlsx)", "*.xlsx");
        if (file != null) {
            try {
                reportService.generateMovementsReportExcel(file.getAbsolutePath(), start.toString(), end.toString());
                showAlert(Alert.AlertType.INFORMATION, "Éxito", "Reporte de movimientos generado en Excel.");
            } catch (Exception ex) {
                showAlert(Alert.AlertType.ERROR, "Error", "Error al crear el Excel de movimientos: " + ex.getMessage());
            }
        }
    }

    // ==========================================
    // ACCIONES: REPORTE DE BAJO STOCK
    // ==========================================

    /**
     * Manejador del botón "Bajo Stock (PDF)": toma el umbral configurado
     * en {@link #spinStockThreshold}, solicita la ubicación de guardado y
     * genera el reporte de bajo stock en formato PDF mediante
     * {@link ReportService#generateLowStockReportPdf(String, int)}.
     */
    private void handleLowStockPdf() {
        int threshold = spinStockThreshold.getValue();
        File file = showSaveDialog("Guardar Reporte de Bajo Stock (PDF)", "PDF Files (*.pdf)", "*.pdf");
        if (file != null) {
            try {
                reportService.generateLowStockReportPdf(file.getAbsolutePath(), threshold);
                showAlert(Alert.AlertType.INFORMATION, "Éxito", "Alerta de bajo stock generada correctamente en PDF.");
            } catch (Exception ex) {
                showAlert(Alert.AlertType.ERROR, "Error", "Error al crear el PDF de bajo stock: " + ex.getMessage());
            }
        }
    }

    /**
     * Manejador del botón "Bajo Stock (Excel)": toma el umbral configurado
     * en {@link #spinStockThreshold}, solicita la ubicación de guardado y
     * genera el reporte de bajo stock en formato Excel mediante
     * {@link ReportService#generateLowStockReportExcel(String, int)}.
     */
    private void handleLowStockExcel() {
        int threshold = spinStockThreshold.getValue();
        File file = showSaveDialog("Guardar Reporte de Bajo Stock (Excel)", "Excel Files (*.xlsx)", "*.xlsx");
        if (file != null) {
            try {
                reportService.generateLowStockReportExcel(file.getAbsolutePath(), threshold);
                showAlert(Alert.AlertType.INFORMATION, "Éxito", "Alerta de bajo stock generada correctamente en Excel.");
            } catch (Exception ex) {
                showAlert(Alert.AlertType.ERROR, "Error", "Error al crear el Excel de bajo stock: " + ex.getMessage());
            }
        }
    }

    // ==========================================
    // ACCIONES: PRODUCTOS MÁS MOVIDOS
    // ==========================================

    /**
     * Manejador del botón "Más Movidos (PDF)": toma el límite configurado
     * en {@link #spinMostMovedLimit}, solicita la ubicación de guardado y
     * genera el reporte estadístico en formato PDF mediante
     * {@link ReportService#generateMostMovedProductsPdf(String, int)}.
     */
    private void handleMostMovedPdf() {
        int limit = spinMostMovedLimit.getValue();
        File file = showSaveDialog("Guardar Estadísticas de Movimiento (PDF)", "PDF Files (*.pdf)", "*.pdf");
        if (file != null) {
            try {
                reportService.generateMostMovedProductsPdf(file.getAbsolutePath(), limit);
                showAlert(Alert.AlertType.INFORMATION, "Éxito", "Reporte estadístico generado correctamente en PDF.");
            } catch (Exception ex) {
                showAlert(Alert.AlertType.ERROR, "Error", "Error al crear el PDF estadístico: " + ex.getMessage());
            }
        }
    }

    /**
     * Manejador del botón "Más Movidos (Excel)": toma el límite
     * configurado en {@link #spinMostMovedLimit}, solicita la ubicación de
     * guardado y genera el reporte estadístico en formato Excel mediante
     * {@link ReportService#generateMostMovedProductsExcel(String, int)}.
     */
    private void handleMostMovedExcel() {
        int limit = spinMostMovedLimit.getValue();
        File file = showSaveDialog("Guardar Estadísticas de Movimiento (Excel)", "Excel Files (*.xlsx)", "*.xlsx");
        if (file != null) {
            try {
                reportService.generateMostMovedProductsExcel(file.getAbsolutePath(), limit);
                showAlert(Alert.AlertType.INFORMATION, "Éxito", "Reporte estadístico generado correctamente en Excel.");
            } catch (Exception ex) {
                showAlert(Alert.AlertType.ERROR, "Error", "Error al crear el Excel estadístico: " + ex.getMessage());
            }
        }
    }

    // ==========================================
    // MÉTODOS AUXILIARES / SOPORTE
    // ==========================================

    /**
     * Muestra un diálogo nativo de guardado de archivo ({@link FileChooser})
     * configurado con el título y filtro de extensión indicados.
     *
     * @param title título de la ventana del diálogo
     * @param description descripción del filtro de extensión (por ejemplo, {@code "PDF Files (*.pdf)"})
     * @param extension patrón de extensión permitido (por ejemplo, {@code "*.pdf"})
     * @return el {@link File} elegido por el usuario, o {@code null} si canceló el diálogo
     */
    private File showSaveDialog(String title, String description, String extension) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(title);
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter(description, extension));
        
        // Obtener la ventana actual a partir de cualquier botón para montar el diálogo modal
        Stage stage = (Stage) btnStockPdf.getScene().getWindow();
        return fileChooser.showSaveDialog(stage);
    }

    /**
     * Muestra una ventana emergente ({@link Alert}) del tipo, título y
     * contenido indicados.
     *
     * @param type tipo de alerta a mostrar (información, advertencia, error, etc.)
     * @param title título de la ventana de alerta
     * @param content mensaje a mostrar en el cuerpo de la alerta
     */
    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}