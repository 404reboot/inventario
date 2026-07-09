package Controller;

import java.math.BigDecimal;
import java.util.Optional;

import Model.Categoria;
import Model.CategoriaDAO;
import Model.Producto;
import Model.ProductoDAO;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.util.StringConverter;

/**
 * ProductoController
 */
public class ProductoController {

    @FXML
    private TableView<Producto> productTable;

    @FXML
    private TableColumn<Producto, Integer> colId;

    @FXML
    private TableColumn<Producto, String> colCodigo;

    @FXML
    private TableColumn<Producto, String> colNombre;

    @FXML
    private TableColumn<Producto, String> colCategoria;

    @FXML
    private TableColumn<Producto, BigDecimal> colPrecioVenta;

    @FXML
    private TableColumn<Producto, Integer> colStock;

    @FXML
    private TableColumn<Producto, String> colEstado;

    @FXML
    private TextField searchField;

    @FXML
    private Button btnNuevo;

    @FXML
    private Button btnEditar;

    @FXML
    private Button btnEliminar;

    @FXML
    private Button btnActualizar;

    @FXML
    private Label totalLabel;

    private ProductoDAO productoDAO = new ProductoDAO();
    private CategoriaDAO categoriaDAO = new CategoriaDAO();
    private ObservableList<Producto> productoList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        setupTableColumns();
        loadProductos();
        setupSearchListener();
        setupSelectionListener();
        updateTotalLabel();
    }

    private void setupTableColumns() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colCodigo.setCellValueFactory(new PropertyValueFactory<>("codigo"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colCategoria.setCellValueFactory(new PropertyValueFactory<>("categoria_id"));
        colPrecioVenta.setCellValueFactory(new PropertyValueFactory<>("precio_venta"));
        colStock.setCellValueFactory(new PropertyValueFactory<>("stock"));

        colCategoria.setCellValueFactory(cellData -> {
            Producto producto = cellData.getValue();
            if (producto != null) {
                Categoria categoria = categoriaDAO.getCategoriaById(producto.getCategoria_id());
                if (categoria != null) {
                    return new SimpleStringProperty(categoria.getNombre());
                }
            }
            return new SimpleStringProperty("Sin categoria");
        });

        colEstado.setCellValueFactory(cellData -> {
            Producto producto = cellData.getValue();
            if (producto.getStock() <= producto.getStock_minimo()) {
                return new SimpleStringProperty("Stock bajo");
            } else {
                return new SimpleStringProperty("Normal");
            }
        });

        colEstado.setCellFactory(column -> new TableCell<Producto, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    if (item.equals("Stock bajo")) {
                        setStyle("-fx-background-color: red; -fx-font-weight: bold;");
                    } else {
                        setStyle("-fx-background-color: green;");
                    }
                }
            }
        });
    }

    private void loadProductos() {
        productoList.clear();
        productoList.addAll(productoDAO.getAllProductos());
        productTable.setItems(productoList);

        updateTotalLabel();
    }

    public void updateTotalLabel() {
        totalLabel.setText(String.valueOf(productoList.size()));
    }

    private void setupSearchListener() {
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.isEmpty()) {
                loadProductos();
            } else {
                searchProductos(newValue);
            }
        });
    }

    private void searchProductos(String query) {
        productoList.clear();
        productoList.addAll(productoDAO.searchProductos(query));
        productTable.setItems(productoList);

        updateTotalLabel();
    }

    private void setupSelectionListener() {
        productTable.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            boolean selected = newValue != null;
            btnEditar.setDisable(!selected);
            btnEliminar.setDisable(!selected);
        });
    }

    @FXML
    private void handleNuevo() {
        showProductoDialog(null);
    }

    @FXML
    private void handleEditar() {
        Producto selectedProducto = productTable.getSelectionModel().getSelectedItem();
        if (selectedProducto != null) {
            showProductoDialog(selectedProducto);
        }
    }

    @FXML
    private void handleEliminar() {
        Producto selectedProducto = productTable.getSelectionModel().getSelectedItem();
        if (selectedProducto != null) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirmar eliminación");
            alert.setHeaderText("¿Está seguro de que desea eliminar el producto?");
            alert.setContentText(
                    "Producto: " + selectedProducto.getNombre() + "\nCódigo: " + selectedProducto.getCodigo());

            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                if (productoDAO.deleteProducto(selectedProducto.getId())) {
                    showAlert("Éxito", "Producto eliminado correctamente.");
                    loadProductos();
                } else {
                    showAlert("Error", "No se pudo eliminar el producto.");
                }
            }
        }
    }

    @FXML
    private void handleActualizar() {
        loadProductos();
        showAlert("Información", "Lista de productos actualizada.");
    }

    private void showProductoDialog(Producto producto) {
        boolean isEdit = producto != null;
        String title = isEdit ? "Editar Producto" : "Nuevo Producto";

        Dialog<Producto> dialog = new Dialog<>();
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

        ComboBox<Categoria> categoriaCombo = new ComboBox<>();
        categoriaCombo.setItems(FXCollections.observableArrayList(categoriaDAO.getAllCategorias()));
        categoriaCombo.setConverter(new StringConverter<Categoria>() {
            @Override
            public String toString(Categoria categoria) {
                return categoria != null ? categoria.getNombre() : "";
            }

            @Override
            public Categoria fromString(String string) {
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

        if (isEdit) {
            codigoField.setText(producto.getCodigo());
            nombreField.setText(producto.getNombre());
            descripcionArea.setText(producto.getDescription());

            Categoria categoria = categoriaDAO.getCategoriaById(producto.getCategoria_id());
            categoriaCombo.setValue(categoria);

            precioCompraField.setText(producto.getPrecio_compra().toString());
            precioVentaField.setText(producto.getPrecio_venta().toString());
            stockField.setText(String.valueOf(producto.getStock()));
            stockMinimoField.setText(String.valueOf(producto.getStock_minimo()));
            proveedorField.setText(producto.getProveedor());
            ubicacionField.setText(producto.getUbicacion());
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

        // Validación y conversión de datos
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                try {
                    if (codigoField.getText().trim().isEmpty() || nombreField.getText().trim().isEmpty()) {
                        showAlert("Error", "Código y Nombre son obligatorios.");
                        return null;
                    }

                    Producto newProduct = new Producto();
                    newProduct.setCodigo(codigoField.getText().trim());
                    newProduct.setNombre(nombreField.getText().trim());
                    newProduct.setDescription(descripcionArea.getText().trim());

                    Categoria selectedCategory = categoriaCombo.getValue();
                    if (selectedCategory != null) {
                        newProduct.setCategoria_id(selectedCategory.getId());
                    }

                    newProduct.setPrecio_compra(new BigDecimal(precioCompraField.getText().trim()));
                    newProduct.setPrecio_venta(new BigDecimal(precioVentaField.getText().trim()));
                    newProduct.setStock(Integer.parseInt(stockField.getText().trim()));
                    newProduct.setStock_minimo(Integer.parseInt(stockMinimoField.getText().trim()));
                    newProduct.setProveedor(proveedorField.getText().trim());
                    newProduct.setUbicacion(ubicacionField.getText().trim());

                    if (isEdit) {
                        newProduct.setId(producto.getId());
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

        Optional<Producto> result = dialog.showAndWait();
        result.ifPresent(newProduct -> {
            boolean success;
            if (isEdit) {
                success = productoDAO.updateProducto(newProduct);
            } else {
                success = productoDAO.createProducto(newProduct);
            }

            if (success) {
                showAlert("Éxito", isEdit ? "Producto actualizado correctamente." : "Producto creado correctamente.");
                loadProductos();
            } else {
                showAlert("Error", "No se pudo " + (isEdit ? "actualizar" : "crear") + " el producto.");
            }
        });
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
