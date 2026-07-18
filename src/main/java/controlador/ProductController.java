package controlador;

import modelo.Category;
import modelo.CategoryDAO;
import modelo.Product;
import modelo.ProductDAO;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.util.StringConverter;
import java.math.BigDecimal;
import java.util.Optional;

/**
 * Controlador JavaFX del módulo de gestión de Productos ({@code ProductView.fxml}).
 * <p>
 * Muestra en una tabla ({@link #productTable}) el listado de productos
 * obtenidos mediante {@link ProductDAO}, permitiendo buscar, crear,
 * editar y eliminar productos a través de un cuadro de diálogo modal
 * ({@link #showProductDialog(Product)}). La columna de estado indica
 * visualmente si un producto está en nivel de stock bajo.
 */
public class ProductController {

    // --- Declaración de elementos de la interfaz de usuario (FXML) ---
    /** Tabla que muestra el listado de productos. */
    @FXML
    private TableView<Product> productTable;
    /** Columna que muestra el id del producto. */
    @FXML
    private TableColumn<Product, Integer> colId;
    /** Columna que muestra el código (SKU) del producto. */
    @FXML
    private TableColumn<Product, String> colCodigo;
    /** Columna que muestra el nombre del producto. */
    @FXML
    private TableColumn<Product, String> colNombre;
    /** Columna que muestra el nombre de la categoría del producto. */
    @FXML
    private TableColumn<Product, String> colCategoria;
    /** Columna que muestra el precio de venta del producto. */
    @FXML
    private TableColumn<Product, BigDecimal> colPrecioVenta;
    /** Columna que muestra el stock actual del producto. */
    @FXML
    private TableColumn<Product, Integer> colStock;
    /** Columna dinámica que indica si el producto está en estado normal o de stock bajo. */
    @FXML
    private TableColumn<Product, String> colEstado;

    /** Campo de texto de la barra de búsqueda en tiempo real. */
    @FXML
    private TextField searchField;
    /** Botón para crear un nuevo producto. */
    @FXML
    private Button btnNuevo;
    /** Botón para editar el producto seleccionado. */
    @FXML
    private Button btnEditar;
    /** Botón para eliminar el producto seleccionado. */
    @FXML
    private Button btnEliminar;
    /** Botón para recargar el listado de productos. */
    @FXML
    private Button btnActualizar;
    
    /** Etiqueta que muestra el total de productos listados. */
    @FXML
    private Label totalLabel; // CORREGIDO: Mapeado para que coincida con el FXML

    // --- Objetos para acceso a datos (DAO) y lista para la tabla ---
    /** DAO utilizado para las operaciones CRUD sobre productos. */
    private ProductDAO productDAO = new ProductDAO();
    /** DAO utilizado para obtener las categorías disponibles (ComboBox del diálogo). */
    private CategoryDAO categoryDAO = new CategoryDAO();
    /** Lista observable que respalda los datos mostrados en {@link #productTable}. */
    private ObservableList<Product> productList = FXCollections.observableArrayList();

    /**
     * Método de inicialización llamado automáticamente al cargar la vista FXML.
     */
    @FXML
    public void initialize() {
        setupTableColumns();      // Prepara las columnas de la tabla
        loadProducts();           // Carga los datos iniciales
        setupSearchListener();    // Activa la barra de búsqueda en tiempo real
        setupSelectionListener(); // Activa la detección de clics en la tabla
    }

    /**
     * Configura el enlace entre las columnas de la tabla y los atributos del modelo Product.
     */
    private void setupTableColumns() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colCodigo.setCellValueFactory(new PropertyValueFactory<>("codigo"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colCategoria.setCellValueFactory(new PropertyValueFactory<>("categoriaNombre"));
        colPrecioVenta.setCellValueFactory(new PropertyValueFactory<>("precioVenta"));
        colStock.setCellValueFactory(new PropertyValueFactory<>("stock"));

        // Columna dinámica de estado
        colEstado.setCellValueFactory(cellData -> {
            Product product = cellData.getValue();
            if (product.getStock() <= product.getStockMinimo()) {
                return new SimpleStringProperty("Stock Bajo");
            } else {
                return new SimpleStringProperty("✓ Normal");
            }
        });

        // Estilo visual para la columna de estado
        colEstado.setCellFactory(column -> new TableCell<Product, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    if (item.contains("Stock Bajo")) {
                        setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
                    } else {
                        setStyle("-fx-text-fill: green;");
                    }
                }
            }
        });
    }

    /**
     * Carga o recarga todos los productos desde la base de datos hacia la tabla.
     */
    private void loadProducts() {
        productList.clear();
        productList.addAll(productDAO.getAllProducts());
        productTable.setItems(productList);
        
        // Actualiza el contador de la etiqueta inferior
        if (totalLabel != null) {
            totalLabel.setText(String.valueOf(productList.size()));
        }
    }

    /**
     * Registra un listener sobre {@link #searchField} que ejecuta la
     * búsqueda en tiempo real: si el texto está vacío recarga todos los
     * productos, de lo contrario filtra mediante {@link #searchProducts(String)}.
     */
    private void setupSearchListener() {
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null || newValue.isEmpty()) {
                loadProducts();
            } else {
                searchProducts(newValue);
            }
        });
    }

    /**
     * Busca productos por nombre o código y actualiza la tabla con los
     * resultados obtenidos de {@link ProductDAO#searchProducts(String)}.
     *
     * @param searchTerm término de búsqueda ingresado por el usuario
     */
    private void searchProducts(String searchTerm) {
        productList.clear();
        productList.addAll(productDAO.searchProducts(searchTerm));
        productTable.setItems(productList);
        if (totalLabel != null) {
            totalLabel.setText(String.valueOf(productList.size()));
        }
    }

    /**
     * Registra un listener sobre la selección de {@link #productTable} que
     * habilita o deshabilita los botones {@link #btnEditar} y
     * {@link #btnEliminar} según si hay un producto seleccionado.
     */
    private void setupSelectionListener() {
        productTable.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> {
                    boolean isSelected = newValue != null;
                    btnEditar.setDisable(!isSelected);
                    btnEliminar.setDisable(!isSelected);
                }
        );
    }

    /**
     * Manejador del botón "Nuevo": abre el diálogo de producto en modo
     * creación (sin producto preseleccionado).
     */
    @FXML
    private void handleNuevo() {
        showProductDialog(null);
    }

    /**
     * Manejador del botón "Editar": abre el diálogo de producto en modo
     * edición, precargado con los datos del producto seleccionado en la tabla.
     */
    @FXML
    private void handleEditar() {
        Product selected = productTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            showProductDialog(selected);
        }
    }

    /**
     * Manejador del botón "Eliminar": solicita confirmación al usuario y,
     * de aceptarse, elimina el producto seleccionado mediante
     * {@link ProductDAO#deleteProduct(int)} y recarga la tabla.
     */
    @FXML
    private void handleEliminar() {
        Product selected = productTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirmar Eliminación");
            alert.setHeaderText("¿Está seguro de eliminar el producto?");
            alert.setContentText("Producto: " + selected.getNombre());

            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                if (productDAO.deleteProduct(selected.getId())) {
                    showAlert("Éxito", "Producto eliminado correctamente.");
                    loadProducts();
                } else {
                    showAlert("Error", "No se pudo eliminar el producto.");
                }
            }
        }
    }

    /**
     * Manejador del botón "Actualizar": recarga el listado completo de
     * productos y notifica al usuario.
     */
    @FXML
    private void handleActualizar() {
        loadProducts();
        showAlert("Información", "Lista de productos actualizada.");
    }

    /**
     * Construye y muestra un cuadro de diálogo modal con un formulario
     * para crear o editar un producto. Si {@code product} es {@code null}
     * el diálogo opera en modo creación; en caso contrario, precarga los
     * campos con los datos del producto recibido (modo edición).
     * <p>
     * Valida que el código y el nombre no estén vacíos, que se haya
     * seleccionado una categoría y que los campos numéricos tengan un
     * formato válido antes de construir el {@link Product} resultante.
     * Al aceptar el diálogo, invoca {@link ProductDAO#createProduct(Product)}
     * o {@link ProductDAO#updateProduct(Product)} según corresponda y
     * recarga la tabla de productos.
     *
     * @param product producto a editar, o {@code null} para crear uno nuevo
     */
    private void showProductDialog(Product product) {
        boolean isEdit = product != null;
        String title = isEdit ? "Editar Producto" : "Nuevo Producto";

        Dialog<Product> dialog = new Dialog<>();
        dialog.setTitle(title);
        dialog.setHeaderText(isEdit ? "Editar información del producto" : "Ingresar nuevo producto");

        ButtonType saveButtonType = new ButtonType("Guardar", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setStyle("-fx-padding: 20;");

        TextField codigoField = new TextField();
        codigoField.setPromptText("Código");

        TextField nombreField = new TextField();
        nombreField.setPromptText("Nombre");

        TextArea descripcionArea = new TextArea();
        descripcionArea.setPromptText("Descripción");
        descripcionArea.setPrefRowCount(3);

        // CONFIGURACIÓN ACTIVA DEL COMBOBOX
        ComboBox<Category> categoriaCombo = new ComboBox<>();
        categoriaCombo.setItems(FXCollections.observableArrayList(categoryDAO.getAllCategories()));
        
        // Convertidor visual integrado para el ComboBox
        categoriaCombo.setConverter(new StringConverter<Category>() {
            @Override
            public String toString(Category category) {
                return category != null ? category.getNombre() : "";
            }
            @Override
            public Category fromString(String string) {
                return null; 
            }
        });

        TextField precioCompraField = new TextField();
        precioCompraField.setPromptText("Precio Compra");

        TextField precioVentaField = new TextField();
        precioVentaField.setPromptText("Precio Venta");

        TextField stockField = new TextField();
        stockField.setPromptText("Stock");

        TextField stockMinimoField = new TextField();
        stockMinimoField.setPromptText("Stock Mínimo");

        TextField proveedorField = new TextField();
        proveedorField.setPromptText("Proveedor");

        TextField ubicacionField = new TextField();
        ubicacionField.setPromptText("Ubicación");

        // Rellenar datos en caso de edición
        if (isEdit) {
            codigoField.setText(product.getCodigo());
            nombreField.setText(product.getNombre());
            descripcionArea.setText(product.getDescripcion());

            // Seleccionar la categoría correspondiente
            for (Category cat : categoriaCombo.getItems()) {
                if (cat.getId() == product.getCategoriaId()) {
                    categoriaCombo.setValue(cat);
                    break;
                }
            }

            precioCompraField.setText(product.getPrecioCompra().toString());
            precioVentaField.setText(product.getPrecioVenta().toString());
            stockField.setText(String.valueOf(product.getStock()));
            stockMinimoField.setText(String.valueOf(product.getStockMinimo()));
            proveedorField.setText(product.getProveedor());
            ubicacionField.setText(product.getUbicacion());
        }

        grid.add(new Label("Código:"), 0, 0);
        grid.add(codigoField, 1, 0);
        grid.add(new Label("Nombre:"), 0, 1);
        grid.add(nombreField, 1, 1);
        grid.add(new Label("Descripción:"), 0, 2);
        grid.add(descripcionArea, 1, 2);
        grid.add(new Label("Categoría:"), 0, 3);
        grid.add(categoriaCombo, 1, 3);
        grid.add(new Label("Precio Compra:"), 0, 4);
        grid.add(precioCompraField, 1, 4);
        grid.add(new Label("Precio Venta:"), 0, 5);
        grid.add(precioVentaField, 1, 5);
        grid.add(new Label("Stock:"), 0, 6);
        grid.add(stockField, 1, 6);
        grid.add(new Label("Stock Mínimo:"), 0, 7);
        grid.add(stockMinimoField, 1, 7);
        grid.add(new Label("Proveedor:"), 0, 8);
        grid.add(proveedorField, 1, 8);
        grid.add(new Label("Ubicación:"), 0, 9);
        grid.add(ubicacionField, 1, 9);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                try {
                    if (codigoField.getText().trim().isEmpty() || nombreField.getText().trim().isEmpty()) {
                        showAlert("Error", "Código y Nombre son obligatorios.");
                        return null;
                    }

                    // CORREGIDO: Validar que se haya seleccionado una categoría
                    Category selectedCategory = categoriaCombo.getValue();
                    if (selectedCategory == null) {
                        showAlert("Error", "Debe seleccionar una categoría.");
                        return null;
                    }

                    Product newProduct = new Product();
                    newProduct.setCodigo(codigoField.getText().trim());
                    newProduct.setNombre(nombreField.getText().trim());
                    newProduct.setDescripcion(descripcionArea.getText().trim());
                    newProduct.setCategoriaId(selectedCategory.getId());

                    newProduct.setPrecioCompra(new BigDecimal(precioCompraField.getText().trim()));
                    newProduct.setPrecioVenta(new BigDecimal(precioVentaField.getText().trim()));
                    newProduct.setStock(Integer.parseInt(stockField.getText().trim()));
                    newProduct.setStockMinimo(Integer.parseInt(stockMinimoField.getText().trim()));
                    newProduct.setProveedor(proveedorField.getText().trim());
                    newProduct.setUbicacion(ubicacionField.getText().trim());

                    if (isEdit) {
                        newProduct.setId(product.getId());
                    }

                    return newProduct;

                } catch (NumberFormatException e) {
                    showAlert("Error", "Por favor, verifique los campos numéricos.");
                    return null;
                } catch (Exception e) {
                    showAlert("Error", "Error al procesar los datos: " + e.getMessage());
                    return null;
                }
            }
            return null;
        });

        Optional<Product> result = dialog.showAndWait();

        result.ifPresent(newProduct -> {
            boolean success = isEdit ? productDAO.updateProduct(newProduct) : productDAO.createProduct(newProduct);
            if (success) {
                showAlert("Éxito", isEdit ? "Producto actualizado correctamente." : "Producto creado correctamente.");
                loadProducts();
            } else {
                showAlert("Error", "No se pudo " + (isEdit ? "actualizar" : "crear") + " el producto.");
            }
        });
    }

    /**
     * Muestra una ventana emergente ({@link Alert}) de tipo informativo con
     * el título y mensaje indicados.
     *
     * @param title título de la ventana de alerta
     * @param message mensaje a mostrar en el cuerpo de la alerta
     */
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}