package Controller;

import java.util.Optional;

import Model.Categoria;
import Model.CategoriaDAO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;

/**
 * CategoriaController
 */
public class CategoriaController {

    @FXML
    private TableView<Categoria> categoriaTable;

    @FXML
    private TableColumn<Categoria, Integer> colId;

    @FXML
    private TableColumn<Categoria, String> colNombre;

    @FXML
    private TableColumn<Categoria, String> colDescripcion;

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

    private CategoriaDAO categoriaDAO = new CategoriaDAO();
    private ObservableList<Categoria> categoriaList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        setupTableColumns();
        loadCategorias();
        setupSearchListener();
        setupSelectionListener();
        updateTotalLabel();
    }

    private void setupTableColumns() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colDescripcion.setCellValueFactory(new PropertyValueFactory<>("description"));

        colId.setPrefWidth(50);
        colNombre.setPrefWidth(200);
        colDescripcion.setPrefWidth(350);
    }

    private void loadCategorias() {
        categoriaList.clear();
        categoriaList.addAll(categoriaDAO.getAllCategorias());
        categoriaTable.setItems(categoriaList);

        updateTotalLabel();
    }

    private void updateTotalLabel() {
        totalLabel.setText(String.valueOf(categoriaList.size()));
    }

    private void setupSearchListener() {
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.isEmpty()) {
                loadCategorias();
            } else {
                searchCategorias(newValue);
            }
        });
    }

    private void searchCategorias(String query) {
        categoriaList.clear();

        for (Categoria categoria : categoriaDAO.getAllCategorias()) {
            if (categoria.getNombre().toLowerCase().contains(query.toLowerCase())) {
                categoriaList.add(categoria);
            }
        }

        categoriaTable.setItems(categoriaList);
        updateTotalLabel();
    }

    private void setupSelectionListener() {
        categoriaTable.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            boolean selected = newValue != null;
            btnEditar.setDisable(!selected);
            btnEliminar.setDisable(!selected);
        });
    }

    @FXML
    private void handleNuevo() {
        showCategoriaDialog(null);
    }

    @FXML
    private void handleEditar() {
        Categoria selectedProducto = categoriaTable.getSelectionModel().getSelectedItem();
        if (selectedProducto != null) {
            showCategoriaDialog(selectedProducto);
        }
    }

    @FXML
    private void handleEliminar() {
        Categoria selectedCategoria = categoriaTable.getSelectionModel().getSelectedItem();
        if (selectedCategoria != null) {

            if (categoriaDAO.hasProductos(selectedCategoria.getId())) {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Advertencia");
                alert.setHeaderText("No se puede eliminar la categoria.");
                alert.setContentText("La categoria '" + selectedCategoria.getNombre()
                        + "' tiene productos asociados, no se puede eliminar.");
                alert.showAndWait();
                return;
            }

            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Confirmar eliminación");
            alert.setHeaderText("¿Está seguro de que desea eliminar la categoria?");
            alert.setContentText(
                    "Categoria: " + selectedCategoria.getNombre());

            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                if (categoriaDAO.deleteCategoria(selectedCategoria.getId())) {
                    showAlert("Éxito", "Producto eliminado correctamente.");
                    loadCategorias();
                } else {
                    showAlert("Error", "No se pudo eliminar el producto.");
                }
            }
        }
    }

    @FXML
    private void handleActualizar() {
        loadCategorias();
        showAlert("Información", "Lista de categorias actualizada.");
    }

    private void showCategoriaDialog(Categoria categoria) {
        boolean isEdit = categoria != null;
        String title = isEdit ? "Editar Producto" : "Nuevo Categoria";

        Dialog<Categoria> dialog = new Dialog<>();
        dialog.setTitle(title);
        dialog.setHeaderText(isEdit ? "Editar información del categoria" : "Ingresar nuevo categoria");

        ButtonType saveButtonType = new ButtonType("Guardar", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setStyle("-fx-padding: 20;");

        TextField nombreField = new TextField();
        nombreField.setPromptText("Nombre");
        TextArea descripcionArea = new TextArea();
        descripcionArea.setPromptText("Descripción");
        descripcionArea.setPrefRowCount(3);

        if (isEdit) {
            nombreField.setText(categoria.getNombre());
            descripcionArea.setText(categoria.getDescription());
        }

        grid.add(new Label("Nombre:"), 0, 0);
        grid.add(nombreField, 1, 0);
        grid.add(new Label("Descripción:"), 0, 1);
        grid.add(descripcionArea, 1, 1);

        dialog.getDialogPane().setContent(grid);

        // Validación y conversión de datos
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                String nombre = nombreField.getText().trim();
                String descripcion = descripcionArea.getText().trim();

                if (nombre.isEmpty()) {
                    showAlert("Error", "El nombre de la categoria es obligatorio.");
                    return null;
                }

                // Verificar si ya existe una categoria con el mismo nombre
                if (!isEdit && categoriaDAO.categoriaExists(nombre)) {
                    showAlert("Error", "Ya existe una categoria con el nombre '" + nombre + "'.");
                    return null;
                }

                if (isEdit && !categoria.getNombre().equals(nombre) && categoriaDAO.categoriaExists(nombre)) {
                    showAlert("Error", "Ya existe una categoria con el nombre '" + nombre + "'.");
                    return null;
                }

                Categoria newCategoria = new Categoria();
                newCategoria.setNombre(nombre);
                newCategoria.setDescription(descripcion);

                if (isEdit) {
                    newCategoria.setId(categoria.getId());
                }

                return newCategoria;
            }

            return null;

        });

        Optional<Categoria> result = dialog.showAndWait();
        result.ifPresent(newCategoria -> {
            boolean success;
            if (isEdit) {
                success = categoriaDAO.updateCategoria(newCategoria);
            } else {
                success = categoriaDAO.createCategoria(newCategoria);
            }

            if (success) {
                showAlert("Éxito", isEdit ? "Categoria actualizado correctamente." : "Categoria creado correctamente.");
                loadCategorias();
            } else {
                showAlert("Error", "No se pudo " + (isEdit ? "actualizar" : "crear") + " la categoria.");
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
