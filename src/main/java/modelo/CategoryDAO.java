package modelo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object (DAO) responsable de las operaciones CRUD sobre la
 * tabla {@code categorias} de la base de datos.
 * <p>
 * Cada método abre su propia conexión mediante {@link conexionDB#conectar()}
 * dentro de un bloque try-with-resources, garantizando el cierre automático
 * de la conexión, el {@link PreparedStatement} y el {@link ResultSet}
 * asociados. Los errores SQL se registran mediante {@code printStackTrace()}
 * y, según el método, se retorna una lista vacía o {@code false}.
 */
public class CategoryDAO {

    // 1. Método para listar todas las categorías (Obligatorio para la tabla)
    /**
     * Obtiene todas las categorías registradas en la base de datos.
     *
     * @return una lista con todas las {@link Category}; puede estar vacía
     *         si no hay registros o si ocurre un error de conexión/consulta
     */
    public List<Category> getAllCategories() {
        List<Category> list = new ArrayList<>();
        String query = "SELECT * FROM categorias";
        try (Connection conn = conexionDB.conectar();
             PreparedStatement pstmt = conn.prepareStatement(query);
             ResultSet rs = pstmt.executeQuery()) {
            
            while (rs.next()) {
                Category cat = new Category();
                cat.setId(rs.getInt("id"));
                cat.setNombre(rs.getString("nombre"));
                cat.setDescripcion(rs.getString("descripcion"));
                list.add(cat);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // 2. Método para crear (Insertar) una categoría
    /**
     * Inserta una nueva categoría en la base de datos.
     *
     * @param category categoría a crear; se utilizan su nombre y descripción
     * @return {@code true} si la inserción afectó al menos una fila,
     *         {@code false} en caso de error o si no se insertó ningún registro
     */
    public boolean createCategory(Category category) {
        String query = "INSERT INTO categorias (nombre, descripcion) VALUES (?, ?)";
        try (Connection conn = conexionDB.conectar();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setString(1, category.getNombre());
            pstmt.setString(2, category.getDescripcion());
            
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // 3. Método para actualizar una categoría existente
    /**
     * Actualiza el nombre y la descripción de una categoría existente,
     * identificada por su {@link Category#getId()}.
     *
     * @param category categoría con los datos actualizados y el id de la categoría a modificar
     * @return {@code true} si la actualización afectó al menos una fila,
     *         {@code false} en caso de error o si no existía la categoría
     */
    public boolean updateCategory(Category category) {
        String query = "UPDATE categorias SET nombre = ?, descripcion = ? WHERE id = ?";
        try (Connection conn = conexionDB.conectar();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setString(1, category.getNombre());
            pstmt.setString(2, category.getDescripcion());
            pstmt.setInt(3, category.getId());
            
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // 4. Método para eliminar una categoría por ID
    /**
     * Elimina la categoría cuyo identificador coincide con el proporcionado.
     *
     * @param id identificador de la categoría a eliminar
     * @return {@code true} si la eliminación afectó al menos una fila,
     *         {@code false} en caso de error o si no existía la categoría
     */
    public boolean deleteCategory(int id) {
        String query = "DELETE FROM categorias WHERE id = ?";
        try (Connection conn = conexionDB.conectar();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // 5. Método para buscar categorías por término (Barra de búsqueda)
    /**
     * Busca categorías cuyo nombre o descripción contengan el término
     * proporcionado (coincidencia parcial, mediante {@code LIKE '%term%'}).
     * Utilizado por la barra de búsqueda de la interfaz.
     *
     * @param searchTerm término de búsqueda a aplicar sobre nombre y descripción
     * @return lista de {@link Category} que coinciden con el término buscado;
     *         puede estar vacía si no hay coincidencias o si ocurre un error
     */
    public List<Category> searchCategories(String searchTerm) {
        List<Category> list = new ArrayList<>();
        String query = "SELECT * FROM categorias WHERE nombre LIKE ? OR descripcion LIKE ?";
        try (Connection conn = conexionDB.conectar();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            String filter = "%" + searchTerm + "%";
            pstmt.setString(1, filter);
            pstmt.setString(2, filter);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Category cat = new Category();
                    cat.setId(rs.getInt("id"));
                    cat.setNombre(rs.getString("nombre"));
                    cat.setDescripcion(rs.getString("descripcion"));
                    list.add(cat);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
}