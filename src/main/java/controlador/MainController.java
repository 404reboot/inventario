package controlador;

import modelo.User;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Controlador JavaFX de la ventana principal del sistema ({@code MainView.fxml}).
 * <p>
 * Actúa como contenedor/orquestador de los distintos módulos de la
 * aplicación (Productos, Categorías, Movimientos, Reportes), cargando
 * dinámicamente la vista correspondiente dentro de {@link #contentArea}
 * según la opción de menú seleccionada. También gestiona el cierre de
 * sesión y el cierre de la aplicación.
 */
public class MainController {

    // --- Elementos de la interfaz (Inyectados desde el archivo FXML) ---

    /** Etiqueta para el mensaje de bienvenida. */
    @FXML
    private Label welcomeLabel; // Etiqueta para el mensaje de bienvenida

    /** Etiqueta para mostrar el rol y nombre de usuario en la barra de estado. */
    @FXML
    private Label statusLabel; // Etiqueta para mostrar el rol y nombre de usuario en la barra de estado

    /** Área principal (contenedor) donde se cargan dinámicamente las distintas vistas (módulos). */
    @FXML
    private StackPane contentArea; // Área principal donde se cargarán las distintas vistas (módulos)

    // Variable para almacenar el usuario que ha iniciado sesión
    /** Usuario actualmente autenticado en el sistema. */
    private User currentUser;

    /**
     * Establece el usuario actual que acaba de iniciar sesión.
     * Actualiza las etiquetas de la interfaz con los datos del usuario.
     *
     * @param user El usuario autenticado.
     */
    public void setCurrentUser(User user) {
        this.currentUser = user;

        // Mostramos un mensaje de bienvenida personalizado
        welcomeLabel.setText("Bienvenido, " + user.getNombre_completo());

        // Mostramos el nombre de usuario y su rol en la barra de estado inferior
        statusLabel.setText("Usuario: " + user.getUsername() + " | Rol: " + user.getRol());
    }

    /**
     * Cierra la sesión del usuario actual y lo devuelve a la pantalla de Login.
     */
    @FXML
    private void handleLogout() {
        try {
            // Cargamos el archivo FXML de la vista de inicio de sesión
            Parent loginRoot = FXMLLoader.load(getClass().getResource("/vista/hello-view.fxml"));

            // Obtenemos la ventana (Stage) actual a partir de uno de los elementos de la vista
            Stage stage = (Stage) welcomeLabel.getScene().getWindow();

            // Reemplazamos la vista actual por la vista de Login
            stage.getScene().setRoot(loginRoot);

        } catch (IOException e) {
            // Si ocurre un error al cargar el archivo, mostramos una alerta
            showAlert("Error", "No se pudo cargar la pantalla de inicio de sesión: " + e.getMessage());
        }
    }

    /**
     * Cierra completamente la aplicación.
     */
    @FXML
    private void handleExit() {
        // Cierra todos los hilos y procesos de JavaFX de manera segura
        javafx.application.Platform.exit();
    }

    /**
     * Carga y muestra el módulo de gestión de Productos en el área principal.
     */
    @FXML
    private void handleProductos() {
        // Llama al método auxiliar loadView pasando la ruta del FXML de Productos
        loadView("/vista/ProductView.fxml");
    }

    /**
     * Carga y muestra el módulo de gestión de Categorías en el área principal.
     */
    @FXML
    private void handleCategorias() {
        loadView("/vista/categoryView.fxml");
    }

    /**
     * Carga y muestra el módulo de Movimientos de Inventario en el área principal.
     * Vincula dinámicamente el ID del usuario logueado al controlador de movimientos.
     */
    @FXML
    private void handleMovimientos() {
        try {
            // Usamos un FXMLLoader manual para poder interactuar con su controlador e inyectar el usuario activo
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/vista/MovementView.fxml"));
            Parent view = loader.load();

            // Obtenemos el controlador de la vista de movimientos
            MovementController movementController = loader.getController();
            if (movementController != null && currentUser != null) {
                // Le pasamos el ID del usuario que está usando el sistema en este instante
                movementController.setCurrentUserId(currentUser.getId());
            }

            // Limpiamos y añadimos la vista al panel contenedor principal
            contentArea.getChildren().clear();
            contentArea.getChildren().add(view);

        } catch (IOException e) {
            showAlert("Error", "No se pudo cargar la vista de movimientos: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Carga y muestra el módulo de Reportes en el área principal.
     */
    @FXML
    private void handleReportes() {
        loadView("/vista/ReportView.fxml");
    }

    /**
     * Método auxiliar para cargar una nueva vista dentro del contenedor principal (contentArea).
     *
     * @param fxmlPath La ruta del archivo FXML que se desea cargar.
     */
    private void loadView(String fxmlPath) {
        try {
            // Cargamos la nueva vista desde la ruta proporcionada
            Parent view = FXMLLoader.load(getClass().getResource(fxmlPath));

            // Limpiamos cualquier vista que estuviera cargada previamente en el área principal
            contentArea.getChildren().clear();

            // Agregamos la nueva vista al área principal
            contentArea.getChildren().add(view);

        } catch (IOException e) {
            // En caso de que el archivo FXML no exista o tenga errores, mostramos una alerta
            showAlert("Error", "No se pudo cargar la vista: " + e.getMessage());
        }
    }

    /**
     * Muestra una ventana emergente tipo alerta en pantalla.
     *
     * @param title El título de la ventana.
     * @param message El mensaje descriptivo para el usuario.
     */
    private void showAlert(String title, String message) {
        // Creamos una alerta de tipo información
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null); // Ocultamos el encabezado predeterminado
        alert.setContentText(message);
        alert.showAndWait(); // Pausamos la interacción hasta que el usuario cierre la alerta
    }
}