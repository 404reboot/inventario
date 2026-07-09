package Model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * CategoriaDAO
 */
public class CategoriaDAO {

    public boolean createCategoria(Categoria categoria) {
        String query = "INSERT INTO categorias (nombre, description) VALUES (?, ?)";
        try (Connection conn = DBConnection.conectar();
                PreparedStatement pstmt = conn.prepareStatement(query, PreparedStatement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, categoria.getNombre());
            pstmt.setString(2, categoria.getDescription());
            int rowsAffected = pstmt.executeUpdate();
            if(rowsAffected > 0) {
                ResultSet rs = pstmt.getGeneratedKeys();
                if (rs.next()) {
                    categoria.setId(rs.getInt(1));
                }
                return true;
            } else {
                return false;
            }
        } catch (SQLException e) {
            System.err.println("Error al crear categoria: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // Actualizar categoría
    public boolean updateCategoria(Categoria category) {
        String query = "UPDATE categorias SET nombre = ?, description = ? WHERE id = ?";

        try (Connection conn = DBConnection.conectar();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, category.getNombre());
            pstmt.setString(2, category.getDescription());
            pstmt.setInt(3, category.getId());

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
            System.err.println("Error al actualizar categoría: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    // Eliminar categoría
    public boolean deleteCategoria(int id) {
        String query = "DELETE FROM categorias WHERE id = ?";

        try (Connection conn = DBConnection.conectar();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, id);
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
            System.err.println("Error al eliminar categoría: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    // Verificar si la categoría tiene productos asociados
    public boolean hasProductos(int categoryId) {
        String query = "SELECT COUNT(*) FROM productos WHERE categoria_id = ?";

        try (Connection conn = DBConnection.conectar();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, categoryId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1) > 0;
            }

        } catch (SQLException e) {
            System.err.println("Error al verificar productos de categoría: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    // Verificar si existe una categoría con el mismo nombre
    public boolean categoriaExists(String nombre) {
        String query = "SELECT COUNT(*) FROM categorias WHERE nombre = ?";

        try (Connection conn = DBConnection.conectar();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, nombre);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1) > 0;
            }

        } catch (SQLException e) {
            System.err.println("Error al verificar existencia de categoría: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    public List<Categoria> getAllCategorias() {
        List<Categoria> categorias = new ArrayList<>();
        String query = "SELECT * FROM categorias ORDER BY nombre";

        try (Connection conn = DBConnection.conectar();
                PreparedStatement pstmt = conn.prepareStatement(query);
                ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                Categoria categoria = new Categoria();
                categoria.setId(rs.getInt("id"));
                categoria.setNombre(rs.getString("nombre"));
                categoria.setDescription(rs.getString("description"));
                categorias.add(categoria);
            }
        } catch (Exception e) {
            System.err.println("Error al obtener categorias: " + e.getMessage());
            e.printStackTrace();
        }
        return categorias;
    }

    public Categoria getCategoriaById(int id) {
        String query = "SELECT * FROM categorias WHERE id = ?";
        try (Connection conn = DBConnection.conectar();
                PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                Categoria categoria = new Categoria();
                categoria.setId(rs.getInt("id"));
                categoria.setNombre(rs.getString("nombre"));
                categoria.setDescription(rs.getString("description"));
                return categoria;
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener categoria: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }
}
