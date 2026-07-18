package Controller;

import Model.InventoryMovement;
import Model.InventoryMovementDAO;
import Model.User;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class MovementController {

    @FXML
    private TableView<InventoryMovement> movementTable;
    @FXML
    private TableColumn<InventoryMovement, Integer> colId;
    @FXML
    private TableColumn<InventoryMovement, String> colTipo;
    @FXML
    private TableColumn<InventoryMovement, String> colProducto;
    @FXML
    private TableColumn<InventoryMovement, Integer> colCantidad;
    @FXML
    private TableColumn<InventoryMovement, java.math.BigDecimal> colPrecio;
    @FXML
    private TableColumn<InventoryMovement, java.math.BigDecimal> colSubtotal;
    @FXML
    private TableColumn<InventoryMovement, String> colMotivo;
    @FXML
    private TableColumn<InventoryMovement, String> colUsuario;
    @FXML
    private TableColumn<InventoryMovement, String> colFecha;

    @FXML
    private ComboBox<String> filterTypeCombo;
    @FXML
    private DatePicker startDatePicker;
    @FXML
    private DatePicker endDatePicker;
    @FXML
    private Button btnNuevaEntrada;
    @FXML
    private Button btnNuevaSalida;
    @FXML
    private Button btnActualizar;

    @FXML
    private Label totalMovimientosLabel;

    private InventoryMovementDAO movementDAO = new InventoryMovementDAO();
    private ObservableList<InventoryMovement> movementList = FXCollections.observableArrayList();
    private User currentUser;

    @FXML
    public void initialize() {
        System.out.println("MovementController inicializado correctamente");

        // Configuración básica de la tabla
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colTipo.setCellValueFactory(new PropertyValueFactory<>("tipoMovimiento"));
        colProducto.setCellValueFactory(new PropertyValueFactory<>("productoNombre"));
        colCantidad.setCellValueFactory(new PropertyValueFactory<>("cantidad"));
        colPrecio.setCellValueFactory(new PropertyValueFactory<>("precioUnitario"));
        colSubtotal.setCellValueFactory(new PropertyValueFactory<>("subtotal"));
        colMotivo.setCellValueFactory(new PropertyValueFactory<>("motivo"));
        colUsuario.setCellValueFactory(new PropertyValueFactory<>("usuarioNombre"));

        colFecha.setCellValueFactory(cellData -> {
            LocalDateTime fecha = cellData.getValue().getFechaMovimiento();
            if (fecha != null) {
                return new javafx.beans.property.SimpleObjectProperty<>(
                        fecha.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
                );
            }
            return new javafx.beans.property.SimpleObjectProperty<>("");
        });

        // Configurar combo de filtros
        filterTypeCombo.setItems(FXCollections.observableArrayList("Todos", "ENTRADA", "SALIDA"));
        filterTypeCombo.setValue("Todos");

        // Configurar fechas
        startDatePicker.setValue(LocalDate.now().minusDays(30));
        endDatePicker.setValue(LocalDate.now());

        // Cargar datos de prueba
        loadSampleData();
    }

    private void loadSampleData() {
        System.out.println("Cargando datos de prueba...");
        movementList.clear();

        // Crear datos de prueba
        InventoryMovement m1 = new InventoryMovement();
        m1.setId(1);
        m1.setProductoNombre("Laptop HP");
        m1.setTipoMovimiento("ENTRADA");
        m1.setCantidad(5);
        m1.setPrecioUnitario(new java.math.BigDecimal("800.00"));
        m1.setSubtotal(new java.math.BigDecimal("4000.00"));
        m1.setMotivo("Compra inicial");
        m1.setUsuarioNombre("Admin");
        m1.setFechaMovimiento(LocalDateTime.now().minusDays(5));
        movementList.add(m1);

        InventoryMovement m2 = new InventoryMovement();
        m2.setId(2);
        m2.setProductoNombre("Mouse USB");
        m2.setTipoMovimiento("SALIDA");
        m2.setCantidad(3);
        m2.setPrecioUnitario(new java.math.BigDecimal("25.00"));
        m2.setSubtotal(new java.math.BigDecimal("75.00"));
        m2.setMotivo("Venta a cliente");
        m2.setUsuarioNombre("Admin");
        m2.setFechaMovimiento(LocalDateTime.now().minusDays(2));
        movementList.add(m2);

        movementTable.setItems(movementList);
        totalMovimientosLabel.setText(String.valueOf(movementList.size()));
        System.out.println("Datos cargados: " + movementList.size() + " movimientos");
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
        System.out.println("Usuario establecido: " + (user != null ? user.getNombre_completo() : "null"));
    }

    @FXML
    private void handleFilter() {
        System.out.println("handleFilter ejecutado");
    }

    @FXML
    private void handleClearFilter() {
        System.out.println("handleClearFilter ejecutado");
    }

    @FXML
    private void handleNuevaEntrada() {
        System.out.println("handleNuevaEntrada ejecutado");
    }

    @FXML
    private void handleNuevaSalida() {
        System.out.println("handleNuevaSalida ejecutado");
    }

    @FXML
    private void handleActualizar() {
        System.out.println("handleActualizar ejecutado");
        loadSampleData();
    }
}
