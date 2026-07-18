package app;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;

/**
 * Clase principal y punto de entrada de la aplicación de escritorio
 * "Sistema de Inventario".
 * <p>
 * Extiende {@link javafx.application.Application} para inicializar el
 * entorno JavaFX, cargar la vista de inicio de sesión ({@code hello-view.fxml})
 * y mostrar la ventana principal ({@link Stage}) de la aplicación.
 *
 * @author Equipo Inventario
 */
public class Main extends Application {

    /**
     * Método de arranque de JavaFX. Es invocado automáticamente por el
     * framework luego de {@link #main(String[])} y {@code launch(args)}.
     * <p>
     * Carga el archivo FXML de la vista de login ({@code /vista/hello-view.fxml})
     * ubicado en {@code resources}, construye la {@link Scene} inicial de
     * 400x300 px, y la asigna al {@link Stage} principal con el título
     * "Sistema de Inventario".
     *
     * @param stage ventana principal (stage) proporcionada por JavaFX
     * @throws IOException si el archivo FXML no puede localizarse o cargarse
     */
    @Override
    public void start(Stage stage) throws IOException {
        // Al usar Maven, la carpeta resources ya cuenta como la raíz (/), 
        // por lo que buscará directamente dentro de resources/vista/hello-view.fxml
        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("/vista/hello-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 400, 300);
        stage.setTitle("Sistema de Inventario");
        stage.setScene(scene);
        stage.show();
    }

    /**
     * Punto de entrada estándar de la aplicación Java.
     * Delega en {@link Application#launch(String...)} para inicializar
     * el ciclo de vida de JavaFX.
     *
     * @param args argumentos de línea de comandos (no utilizados)
     */
    public static void main(String[] args) {
        launch(args);
    }
}
