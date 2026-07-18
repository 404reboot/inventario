package controlador;

import modelo.Category;
import modelo.CategoryDAO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import java.util.Optional;

/**
 * Controlador JavaFX del módulo de gestión de Categorías ({@code categoryView.fxml}).
 * <p>
 * Muestra en una tabla ({@link #categoryTable}) el listado de categorías
 * obtenidas mediante {@link CategoryDAO}, permitiendo buscar, crear,
 * editar y eliminar categorías a través de un cuadro de diálogo modal
 * ({@link #showCategoryDialog(Category)}).
 */
public class CategoryController {

    // --- Declaración de elementos de la interfaz de usuario (FXML) ---
    /** Tabla que muestra el listado de categorías. */
    @FXML
    private TableView<Category> categoryTable;
    /** Columna que muestra el id de la categoría. */
    @FXML
    private TableColumn<Category, Integer> colId;
    /** Columna que muestra el nombre de la categoría. */
    @FXML
    private TableColumn<Category, String> colNombre;
    /** Columna que muestra la descripción de la categoría. */
    @FXML
    private TableColumn<Category, String> colDescripcion;

    /** Campo de texto de la barra de búsqueda en tiempo real. */
    @FXML
    private TextField searchField;
    /** Botón para crear una nueva categoría. */
    @FXML
    private Button btnNuevo;
    /** Botón para editar la categoría seleccionada. */
    @FXML
    private Button btnEditar;
    /** Botón para eliminar la categoría seleccionada. */
    @FXML
    private Button btnEliminar;
    /** Botón para recargar el listado de categorías. */
    @FXML
    private Button btnActualizar;
    
    /** Etiqueta que muestra el total de categorías listadas. */
    @FXML
    private Label totalLabel; 

    // --- Objetos para acceso a datos (DAO) y lista para la tabla ---
    /** DAO utilizado para las operaciones CRUD sobre categorías. */
    private CategoryDAO categoryDAO = new CategoryDAO();
    /** Lista observable que respalda los datos mostrados en {@link #categoryTable}. */
    private ObservableList<Category> categoryList = FXCollections.observableArrayList();

    /**
     * Método de inicialización llamado automáticamente al cargar la vista FXML.
     */
    @FXML
    public void initialize() {
        setupTableColumns();      // Prepara las columnas de la tabla
        loadCategories();         // Carga los datos iniciales
        setupSearchListener();    // Activa la barra de búsqueda en tiempo real
        setupSelectionListener(); // Activa la detección de clics en la tabla
    }

    /**
     * Configura el enlace entre las columnas de la tabla y los atributos del modelo Category.
     */
    private void setupTableColumns() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colDescripcion.setCellValueFactory(new PropertyValueFactory<>("descripcion"));
    
        // Ajustar ancho de columnas
        colId.setPrefWidth(50);
        colNombre.setPrefWidth(200);
        colDescripcion.setPrefWidth(350);
    } // <-- ¡AQUÍ ESTABA EL ERROR! FALTABA ESTA LLAVE DE CIERRE

    /**
     * Carga o recarga todas las categorías desde la base de datos hacia la tabla.
     */
    private void loadCategories() {
        categoryList.clear();
        categoryList.addAll(categoryDAO.getAllCategories());
        categoryTable.setItems(categoryList);
        
        // Actualiza el contador de la etiqueta inferior
        if (totalLabel != null) {
            totalLabel.setText(String.valueOf(categoryList.size()));
        }
    }

    /**
     * Registra un listener sobre {@link #searchField} que ejecuta la
     * búsqueda en tiempo real: si el texto está vacío recarga todas las
     * categorías, de lo contrario filtra mediante {@link #searchCategories(String)}.
     */
    private void setupSearchListener() {
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null || newValue.isEmpty()) {
                loadCategories();
            } else {
                searchCategories(newValue);
            }
        });
    }

    /**
     * Busca categorías por nombre o descripción y actualiza la tabla con
     * los resultados obtenidos de {@link CategoryDAO#searchCategories(String)}.
     *
     * @param searchTerm término de búsqueda ingresado por el usuario
     */
    private void searchCategories(String searchTerm) {
        categoryList.clear();
        categoryList.addAll(categoryDAO.searchCategories(searchTerm));
        categoryTable.setItems(categoryList);
        if (totalLabel != null) {
            totalLabel.setText(String.valueOf(categoryList.size()));
        }
    }

    /**
     * Registra un listener sobre la selección de {@link #categoryTable} que
     * habilita o deshabilita los botones {@link #btnEditar} y
     * {@link #btnEliminar} según si hay una categoría seleccionada.
     */
    private void setupSelectionListener() {
        categoryTable.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> {
                    boolean isSelected = newValue != null;
                    btnEditar.setDisable(!isSelected);
                    btnEliminar.setDisable(!isSelected);
                }
        );
    }

    /**
     * Manejador del botón "Nuevo": abre el diálogo de categoría en modo
     * creación (sin categoría preseleccionada).
     */
    @FXML
    private void handleNuevo() {
        showCategoryDialog(null);
    }

    /**
     * Manejador del botón "Editar": abre el diálogo de categoría en modo
     * edición, precargado con los datos de la categoría seleccionada en la tabla.
     */
    @FXML
    private void handleEditar() {
        Category selected = categoryTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            showCategoryDialog(selected);
        }
    }

    /**
     * Manejador del botón "Eliminar": solicita confirmación al usuario y,
     * de aceptarse, elimina la categoría seleccionada mediante
     * {@link CategoryDAO#deleteCategory(int)} y recarga la tabla. Si la
     * categoría tiene productos asociados, la eliminación puede fallar.
     */
    @FXML
    private void handleEliminar() {
        Category selected = categoryTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Confirmar Eliminación");
            alert.setHeaderText("¿Está seguro de eliminar la categoría?");
            alert.setContentText("Categoría: " + selected.getNombre());

            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && bondTypeToOk(result.get())) {
                if (categoryDAO.deleteCategory(selected.getId())) {
                    showAlert("Éxito", "Categoría eliminada correctamente.");
                    loadCategories();
                } else {
                    showAlert("Error", "No se pudo eliminar la categoría. Verifique si tiene productos asociados.");
                }
            }
        }
    }

    /**
     * Método auxiliar que indica si el {@link ButtonType} recibido
     * corresponde al botón de confirmación (OK) de una alerta.
     *
     * @param buttonType botón pulsado por el usuario en el diálogo de confirmación
     * @return {@code true} si el botón pulsado fue {@link ButtonType#OK}
     */
    private boolean bondTypeToOk(ButtonType buttonType) {
        return buttonType == ButtonType.OK;
    }

    /**
     * Manejador del botón "Actualizar": recarga el listado completo de
     * categorías y notifica al usuario.
     */
    @FXML
    private void handleActualizar() {
        loadCategories();
        showAlert("Información", "Lista de categorías actualizada.");
    }

    /**
     * Construye y muestra un cuadro de diálogo modal con un formulario
     * para crear o editar una categoría. Si {@code category} es {@code null}
     * el diálogo opera en modo creación; en caso contrario, precarga los
     * campos con los datos de la categoría recibida (modo edición).
     * <p>
     * Valida que el nombre no esté vacío antes de construir la
     * {@link Category} resultante. Al aceptar el diálogo, invoca
     * {@link CategoryDAO#createCategory(Category)} o
     * {@link CategoryDAO#updateCategory(Category)} según corresponda y
     * recarga la tabla de categorías.
     *
     * @param category categoría a editar, o {@code null} para crear una nueva
     */
    private void showCategoryDialog(Category category) {
        boolean isEdit = category != null;
        String title = isEdit ? "Editar Categoría" : "Nueva Categoría";

        Dialog<Category> dialog = new Dialog<>();
        dialog.setTitle(title);
        dialog.setHeaderText(isEdit ? "Editar información de la categoría" : "Ingresar nueva categoría");

        ButtonType saveButtonType = new ButtonType("Guardar", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setStyle("-fx-padding: 20;");

        // campos de formulario
        TextField nombreField = new TextField();
        nombreField.setPromptText("Nombre");

        TextArea descripcionArea = new TextArea();
        descripcionArea.setPromptText("Descripción");
        descripcionArea.setPrefRowCount(3);

        // Rellenar datos en caso de edición
        if (isEdit) {
            nombreField.setText(category.getNombre());
            descripcionArea.setText(category.getDescripcion());
        }

        grid.add(new Label("Nombre:"), 0, 0);
        grid.add(nombreField, 1, 0);
        grid.add(new Label("Descripción:"), 0, 1);
        grid.add(descripcionArea, 1, 1);

        dialog.getDialogPane().setContent(grid);

        // Mensaje de campos obligatorios
        Label requiredLabel = new Label("* Campos obligatorios");
        requiredLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: #7f8c8d;");
        grid.add(requiredLabel, 1, 2);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                try {
                    if (nombreField.getText().trim().isEmpty()) {
                        showAlert("Error", "El nombre de la categoría es obligatorio.");
                        return null;
                    }

                    Category newCategory = new Category();
                    newCategory.setNombre(nombreField.getText().trim());
                    newCategory.setDescripcion(descripcionArea.getText().trim());

                    if (isEdit) {
                        newCategory.setId(category.getId());
                    }

                    return newCategory;

                } catch (Exception e) {
                    showAlert("Error", "Error al procesar los datos: " + e.getMessage());
                    return null;
                }
            }
            return null;
        });

        Optional<Category> result = dialog.showAndWait();

        result.ifPresent(newCategory -> {
            boolean success = isEdit ? categoryDAO.updateCategory(newCategory) : categoryDAO.createCategory(newCategory);
            if (success) {
                showAlert("Éxito", isEdit ? "Categoría actualizada correctamente." : "Categoría creada correctamente.");
                loadCategories();
            } else {
                showAlert("Error", "No se pudo " + (isEdit ? "actualizar" : "crear") + " la categoría.");
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