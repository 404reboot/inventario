package controlador;

import modelo.User;
import modelo.UserDAO;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import java.io.IOException;

/**
 * Controlador JavaFX de la vista de inicio de sesión ({@code hello-view.fxml}).
 * <p>
 * Valida las credenciales ingresadas contra la base de datos mediante
 * {@link UserDAO} y, si son correctas, reemplaza la escena actual por la
 * ventana principal del sistema ({@code MainView.fxml}), propagando el
 * usuario autenticado al {@link MainController}.
 */
public class LoginController {

    /** Campo de texto donde el usuario ingresa su nombre de usuario. */
    @FXML
    private TextField usernameField;

    /** Campo de contraseña donde el usuario ingresa su clave. */
    @FXML
    private PasswordField passwordField;

    /** DAO utilizado para validar las credenciales del usuario contra la base de datos. */
    private UserDAO userDAO = new UserDAO();

    /**
     * Manejador del evento de inicio de sesión (botón "Ingresar").
     * <p>
     * Valida que los campos no estén vacíos, autentica al usuario mediante
     * {@link UserDAO#validarUser(String, String)} y, si las credenciales
     * son válidas, muestra un mensaje de bienvenida y abre la ventana
     * principal; en caso contrario, muestra un mensaje de error.
     */
    @FXML
    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();

        if (username.isEmpty() || password.isEmpty()) {
            showAlert("Error", "Por favor, complete todos los campos.");
            return;
        }

        User user = userDAO.validarUser(username, password);

        if (user != null) {
            showAlert("Éxito", "Bienvenido " + user.getNombre_completo());
            openMainWindow(user);
        } else {
            showAlert("Error", "Usuario o contraseña incorrectos.");
        }
    }

    /**
     * Carga la vista principal del sistema ({@code MainView.fxml}), le
     * asigna el usuario autenticado mediante
     * {@link MainController#setCurrentUser(User)} y reemplaza la escena
     * actual de la ventana ({@link Stage}) por la nueva vista, maximizándola.
     *
     * @param user usuario autenticado que se propagará al controlador principal
     */
    private void openMainWindow(User user) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/vista/MainView.fxml"));
            Parent root = loader.load();

            MainController mainController = loader.getController();
            mainController.setCurrentUser(user);

            Stage stage = (Stage) usernameField.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Sistema de Inventario - " + user.getNombre_completo());
            stage.setMaximized(true);
        } catch (IOException e) {
            System.err.println("Error al abrir la ventana principal: " + e.getMessage());
            e.printStackTrace();
            showAlert("Error", "No se pudo cargar la vista principal del sistema.");
        }
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
