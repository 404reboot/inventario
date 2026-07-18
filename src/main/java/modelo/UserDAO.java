package modelo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Data Access Object (DAO) responsable de la autenticación de usuarios
 * contra la tabla {@code usuarios} de la base de datos.
 */
public class UserDAO {

    /**
     * Valida las credenciales de un usuario contra la base de datos y,
     * si son correctas, retorna sus datos completos.
     *
     * @param username nombre de usuario ingresado
     * @param password contraseña ingresada
     * @return el {@link User} correspondiente si las credenciales son
     *         válidas, o {@code null} si no existe coincidencia o si
     *         ocurre un error de conexión/consulta
     */
    public User validarUser(String username, String password) {

        String query = "select * from usuarios where username = ? and password = ?";

        // El uso de try-with-resources asegura que la conexión se cierre sola
        try (Connection con = conexionDB.conectar();
             PreparedStatement pst = con.prepareStatement(query)) {

            // Asigna los parámetros a los signos de interrogación '?'
            pst.setString(1, username);
            pst.setString(2, password);

            //Consulta si existen los parametros ingresados en la base de datos
            ResultSet rs = pst.executeQuery();

            if (rs.next()) {

                //Creamos un objeto de la clase usuario
                User user = new User();

                //Obtenemos todos los datos del usuario de la base de datos
                user.setId(rs.getInt("id"));
                user.setUsername(rs.getString("username"));
                user.setPassword(rs.getString("password"));
                user.setNombre_completo(rs.getString("nombre_completo"));
                user.setRol(rs.getString("rol"));

                return user;
            }

        } catch (SQLException e) {
            System.err.println("Error al validar usuario: " + e.getMessage());
            e.printStackTrace();
        }

        return null;

    }

}
