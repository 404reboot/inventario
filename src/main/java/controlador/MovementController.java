package controlador;

import modelo.InventoryMovement;
import modelo.InventoryMovementDAO;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Controlador JavaFX del módulo de gestión de Movimientos de Inventario
 * ({@code MovementView.fxml}).
 * <p>
 * Muestra el historial de movimientos en {@link #movementTable}, calcula
 * estadísticas agregadas (totales de entradas/salidas y su valor
 * monetario) y permite filtrar por tipo de movimiento y rango de fechas.
 * También permite registrar nuevas entradas o salidas de inventario a
 * través de un formulario modal, delegando en {@link InventoryMovementDAO}.
 */
public class MovementController {

    // --- Componentes FXML de Resumen ---
    /** Etiqueta que muestra el total de movimientos listados. */
    @FXML private Label totalMovimientosLabel;
    /** Etiqueta que muestra el conteo de movimientos de tipo entrada. */
    @FXML private Label totalEntradasLabel;
    /** Etiqueta que muestra el conteo de movimientos de tipo salida. */
    @FXML private Label totalSalidasLabel;
    /** Etiqueta que muestra el valor monetario total de las entradas. */
    @FXML private Label valorEntradasLabel;
    /** Etiqueta que muestra el valor monetario total de las salidas. */
    @FXML private Label valorSalidasLabel;

    // --- Componentes FXML de Filtros ---
    /** Selector del tipo de movimiento a filtrar ({@code TODOS}, {@code ENTRADA} o {@code SALIDA}). */
    @FXML private ComboBox<String> filterTypeCombo;
    /** Selector de la fecha de inicio del rango de filtrado. */
    @FXML private DatePicker startDatePicker;
    /** Selector de la fecha de fin del rango de filtrado. */
    @FXML private DatePicker endDatePicker;

    // --- Tabla FXML ---
    /** Tabla que muestra el listado de movimientos de inventario. */
    @FXML private TableView<InventoryMovement> movementTable;
    /** Columna que muestra el id del movimiento. */
    @FXML private TableColumn<InventoryMovement, Integer> colId;
    /** Columna que muestra el tipo de movimiento (entrada/salida). */
    @FXML private TableColumn<InventoryMovement, String> colTipo;
    /** Columna que muestra el nombre del producto involucrado. */
    @FXML private TableColumn<InventoryMovement, String> colProducto;
    /** Columna que muestra la cantidad de unidades del movimiento. */
    @FXML private TableColumn<InventoryMovement, Integer> colCantidad;
    /** Columna que muestra el precio unitario aplicado. */
    @FXML private TableColumn<InventoryMovement, BigDecimal> colPrecio;
    /** Columna que muestra el subtotal calculado del movimiento. */
    @FXML private TableColumn<InventoryMovement, BigDecimal> colSubtotal;
    /** Columna que muestra el motivo del movimiento. */
    @FXML private TableColumn<InventoryMovement, String> colMotivo;
    /** Columna que muestra el nombre del usuario que registró el movimiento. */
    @FXML private TableColumn<InventoryMovement, String> colUsuario;
    /** Columna que muestra la fecha y hora del movimiento, formateada. */
    @FXML private TableColumn<InventoryMovement, String> colFecha;

    // --- Atributos de Datos ---
    /** DAO utilizado para consultar y registrar movimientos de inventario. */
    private final InventoryMovementDAO movementDAO = new InventoryMovementDAO();
    /** Lista maestra (sin filtrar) de movimientos cargados desde la base de datos. */
    private final ObservableList<InventoryMovement> masterData = FXCollections.observableArrayList();
    /** Formateador utilizado para mostrar la fecha/hora de cada movimiento en la tabla. */
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /** Identificador del usuario actualmente logueado, usado al registrar nuevos movimientos. */
    private int currentUserId = 1; 

    /**
     * Método de inicialización llamado automáticamente al cargar la vista
     * FXML. Configura las columnas de la tabla, el combo de filtro por
     * tipo y carga el listado inicial de movimientos.
     */
    @FXML
    public void initialize() {
        setupTableColumns();
        setupFilterCombo();
        loadMovements();
    }

    /**
     * Configura el enlace entre las columnas de la tabla y los atributos
     * del modelo {@link InventoryMovement}, incluyendo el formateo
     * personalizado de la columna de fecha.
     */
    private void setupTableColumns() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colTipo.setCellValueFactory(new PropertyValueFactory<>("tipoMovimiento"));
        colProducto.setCellValueFactory(new PropertyValueFactory<>("productoNombre"));
        colCantidad.setCellValueFactory(new PropertyValueFactory<>("cantidad"));
        colPrecio.setCellValueFactory(new PropertyValueFactory<>("precioUnitario"));
        colSubtotal.setCellValueFactory(new PropertyValueFactory<>("subtotal"));
        colMotivo.setCellValueFactory(new PropertyValueFactory<>("motivo"));
        colUsuario.setCellValueFactory(new PropertyValueFactory<>("usuarioNombre"));
        
        colFecha.setCellValueFactory(cellData -> {
            if (cellData.getValue().getFechaMovimiento() != null) {
                return new javafx.beans.property.SimpleStringProperty(cellData.getValue().getFechaMovimiento().format(formatter));
            }
            return new javafx.beans.property.SimpleStringProperty("");
        });
    }

    /**
     * Inicializa el {@link #filterTypeCombo} con las opciones de filtro
     * disponibles ({@code TODOS}, {@code ENTRADA}, {@code SALIDA}) y
     * selecciona {@code TODOS} por defecto.
     */
    private void setupFilterCombo() {
        filterTypeCombo.setItems(FXCollections.observableArrayList("TODOS", "ENTRADA", "SALIDA"));
        filterTypeCombo.getSelectionModel().selectFirst();
    }

    /**
     * Manejador del botón "Actualizar": recarga el listado completo de movimientos.
     */
    @FXML
    private void handleActualizar() {
        loadMovements();
    }

    /**
     * Carga (o recarga) todos los movimientos de inventario desde la base
     * de datos hacia {@link #masterData} y la tabla, y recalcula las
     * estadísticas mostradas en el panel de resumen.
     */
    private void loadMovements() {
        masterData.clear();
        List<InventoryMovement> dbList = movementDAO.getAllMovements();
        masterData.addAll(dbList);
        movementTable.setItems(masterData);
        calcularEstadisticas(dbList);
    }

    /**
     * Calcula, a partir de la lista de movimientos proporcionada, el
     * total de movimientos, el conteo de entradas y salidas, y el valor
     * monetario acumulado de cada tipo, actualizando las etiquetas del
     * panel de resumen.
     *
     * @param lista lista de movimientos sobre la cual calcular las estadísticas
     */
    private void calcularEstadisticas(List<InventoryMovement> lista) {
        int totalMovimientos = lista.size();
        
        long entradas = lista.stream().filter(m -> m.getTipoMovimiento() != null && "ENTRADA".equalsIgnoreCase(m.getTipoMovimiento().trim())).count();
        long salidas = lista.stream().filter(m -> m.getTipoMovimiento() != null && "SALIDA".equalsIgnoreCase(m.getTipoMovimiento().trim())).count();

        BigDecimal sumaEntradas = lista.stream()
                .filter(m -> m.getTipoMovimiento() != null && "ENTRADA".equalsIgnoreCase(m.getTipoMovimiento().trim()))
                .map(InventoryMovement::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal sumaSalidas = lista.stream()
                .filter(m -> m.getTipoMovimiento() != null && "SALIDA".equalsIgnoreCase(m.getTipoMovimiento().trim()))
                .map(InventoryMovement::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        totalMovimientosLabel.setText(String.valueOf(totalMovimientos));
        totalEntradasLabel.setText(String.valueOf(entradas));
        totalSalidasLabel.setText(String.valueOf(salidas));
        valorEntradasLabel.setText(String.format("$%.2f", sumaEntradas));
        valorSalidasLabel.setText(String.format("$%.2f", sumaSalidas));
    }

    /**
     * Manejador del botón "Filtrar": aplica sobre {@link #masterData} el
     * filtro seleccionado (tipo de movimiento y/o rango de fechas) y
     * actualiza la tabla y las estadísticas con el resultado filtrado.
     */
    @FXML
    private void handleFilter() {
        String selectedType = filterTypeCombo.getSelectionModel().getSelectedItem();
        LocalDate startDate = startDatePicker.getValue();
        LocalDate endDate = endDatePicker.getValue();

        List<InventoryMovement> filteredList = masterData.stream().filter(m -> {
            if (!"TODOS".equals(selectedType) && !selectedType.equalsIgnoreCase(m.getTipoMovimiento().trim())) {
                return false;
            }
            if (startDate != null && m.getFechaMovimiento() != null) {
                if (m.getFechaMovimiento().toLocalDate().isBefore(startDate)) return false;
            }
            if (endDate != null && m.getFechaMovimiento() != null) {
                if (m.getFechaMovimiento().toLocalDate().isAfter(endDate)) return false;
            }
            return true;
        }).collect(Collectors.toList());

        movementTable.setItems(FXCollections.observableArrayList(filteredList));
        calcularEstadisticas(filteredList);
    }

    /**
     * Manejador del botón "Limpiar filtros": restablece el combo de tipo
     * a {@code TODOS}, limpia las fechas seleccionadas y vuelve a mostrar
     * el listado completo ({@link #masterData}) con sus estadísticas.
     */
    @FXML
    private void handleClearFilter() {
        filterTypeCombo.getSelectionModel().selectFirst();
        startDatePicker.setValue(null);
        endDatePicker.setValue(null);
        movementTable.setItems(masterData);
        calcularEstadisticas(masterData);
    }

    /**
     * Manejador del botón "Nueva Entrada": abre el formulario modal de
     * registro de movimiento configurado para el tipo {@code "ENTRADA"}.
     */
    @FXML
    private void handleNuevaEntrada() {
        mostrarFormularioMovimiento("REGISTRAR ENTRADA DE PRODUCTO", "ENTRADA");
    }

    /**
     * Manejador del botón "Nueva Salida": abre el formulario modal de
     * registro de movimiento configurado para el tipo {@code "SALIDA"}.
     */
    @FXML
    private void handleNuevaSalida() {
        mostrarFormularioMovimiento("REGISTRAR SALIDA DE PRODUCTO", "SALIDA");
    }

    /**
     * Construye y muestra un formulario modal para registrar un nuevo
     * movimiento de inventario (entrada o salida), solicitando el id del
     * producto, la cantidad, el precio unitario, el motivo y una
     * referencia opcional.
     * <p>
     * Al confirmar, valida el formato numérico de los campos y registra
     * el movimiento mediante {@link InventoryMovementDAO#registrarEntrada}
     * o {@link InventoryMovementDAO#registrarSalida} según el {@code tipo}
     * recibido, mostrando una alerta de éxito o error y recargando la
     * tabla de movimientos si la operación fue exitosa.
     *
     * @param titulo título mostrado en la ventana del diálogo
     * @param tipo tipo de movimiento a registrar, {@code "ENTRADA"} o {@code "SALIDA"}
     */
    private void mostrarFormularioMovimiento(String titulo, String tipo) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle(titulo);
        dialog.setHeaderText("Ingrese los datos del movimiento de inventario.");

        ButtonType guardarButtonType = new ButtonType("Guardar", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(guardarButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField txtProductoId = new TextField();
        txtProductoId.setPromptText("ID del Producto");
        TextField txtCantidad = new TextField();
        txtCantidad.setPromptText("Cantidad");
        TextField txtPrecio = new TextField();
        txtPrecio.setPromptText("Precio Unitario");
        TextField txtMotivo = new TextField();
        txtMotivo.setPromptText("Ej. Compra / Ajuste");
        TextField txtReferencia = new TextField();
        txtReferencia.setPromptText("N° Factura o Remisión");

        grid.add(new Label("ID Producto:"), 0, 0);
        grid.add(txtProductoId, 1, 0);
        grid.add(new Label("Cantidad:"), 0, 1);
        grid.add(txtCantidad, 1, 1);
        grid.add(new Label("Precio Unitario:"), 0, 2);
        grid.add(txtPrecio, 1, 2);
        grid.add(new Label("Motivo / Detalle:"), 0, 3);
        grid.add(txtMotivo, 1, 3);
        grid.add(new Label("Referencia:"), 0, 4);
        grid.add(txtReferencia, 1, 4);

        dialog.getDialogPane().setContent(grid);

        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == guardarButtonType) {
            try {
                int productoId = Integer.parseInt(txtProductoId.getText().trim());
                int cantidad = Integer.parseInt(txtCantidad.getText().trim());
                BigDecimal precio = new BigDecimal(txtPrecio.getText().trim());
                String motivo = txtMotivo.getText().trim();
                String ref = txtReferencia.getText().trim();

                boolean exito;
                if ("ENTRADA".equals(tipo)) {
                    exito = movementDAO.registrarEntrada(productoId, cantidad, precio, motivo, currentUserId, ref);
                } else {
                    exito = movementDAO.registrarSalida(productoId, cantidad, precio, motivo, currentUserId, ref);
                }

                if (exito) {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION, "¡Movimiento guardado con éxito!");
                    alert.showAndWait();
                    loadMovements(); 
                } else {
                    Alert alert = new Alert(Alert.AlertType.ERROR, "La base de datos rechazó el registro.\nVerifica si el ID de producto existe o si hay stock suficiente.");
                    alert.showAndWait();
                }

            } catch (NumberFormatException e) {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Error: Por favor introduce valores numéricos válidos.");
                alert.showAndWait();
            } catch (Exception e) {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Ocurrió un error inesperado al procesar el movimiento: " + e.getMessage());
                alert.showAndWait();
            }
        }
    }

    // --- Control del ID de usuario logueado ---
    /**
     * Establece el identificador del usuario actualmente logueado, el
     * cual se utilizará para registrar la autoría de los movimientos de
     * inventario que se creen desde este controlador.
     *
     * @param currentUserId identificador del usuario autenticado en el sistema
     */
    public void setCurrentUserId(int currentUserId) {
        this.currentUserId = currentUserId;
    }
}