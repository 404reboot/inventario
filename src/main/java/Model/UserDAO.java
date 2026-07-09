package Model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * UserDAO
 */
public class UserDAO {
    public User validarUser(String username, String password) {
        String query = "SELECT * FROM usuarios WHERE username = ? AND password = ?";
        try (Connection conn = DBConnection.conectar();
                PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, username);
            pstmt.setString(2, password);

            ResultSet rs = pstmt.executeQuery();

            if(rs.next()) {
                User user = new User();
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
