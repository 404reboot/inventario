package modelo;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Clase utilitaria encargada de proveer conexiones a la base de datos
 * MySQL utilizada por el sistema de inventario.
 * <p>
 * Centraliza los parámetros de conexión (URL, usuario y contraseña) y
 * el registro del driver JDBC {@code com.mysql.cj.jdbc.Driver}, de modo
 * que los distintos DAO ({@link ProductDAO}, {@link CategoryDAO},
 * {@link UserDAO}, {@link InventoryMovementDAO}, etc.) puedan obtener
 * una {@link Connection} lista para usar sin duplicar esta lógica.
 */
public class conexionDB {

    /** URL JDBC de la base de datos MySQL local del sistema (esquema {@code inventario_db}). */
    private static final String URL = "jdbc:mysql://localhost:3306/inventario_db";
    /** Usuario utilizado para autenticarse contra la base de datos. */
    private static final String USER = "root";
    /** Contraseña utilizada para autenticarse contra la base de datos. */
    private static final String PASSWORD = "";

    /**
     * Abre y devuelve una nueva conexión a la base de datos MySQL configurada
     * en {@link #URL}, {@link #USER} y {@link #PASSWORD}.
     * <p>
     * Internamente carga el driver JDBC de MySQL y utiliza
     * {@link DriverManager#getConnection(String, String, String)}. Si ocurre
     * un error (driver no encontrado o fallo de conexión), se captura la
     * excepción, se imprime un mensaje en consola y se retorna {@code null}.
     *
     * @return una {@link Connection} activa hacia la base de datos, o
     *         {@code null} si la conexión no pudo establecerse
     */
    public static Connection conectar(){
        Connection conexion = null;
        try{
            Class.forName("com.mysql.cj.jdbc.Driver");
            conexion = DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (ClassNotFoundException | SQLException e) {
            System.out.println("Error de conexión: " + e.getMessage());
        }

        return conexion;
    }

}
