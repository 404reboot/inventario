package modelo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object (DAO) responsable de las operaciones CRUD y de
 * búsqueda sobre la tabla {@code productos} de la base de datos.
 * <p>
 * Las consultas de lectura realizan un {@code LEFT JOIN} con la tabla
 * {@code categorias} para completar el nombre de categoría de cada
 * producto ({@link Product#getCategoriaNombre()}). Cada método abre su
 * propia conexión mediante {@link conexionDB#conectar()} y la libera
 * automáticamente gracias a try-with-resources.
 */
public class ProductDAO {
    // Crear producto
    /**
     * Inserta un nuevo producto en la base de datos. Si la inserción es
     * exitosa, asigna al {@code product} recibido el id generado por la
     * base de datos mediante {@link Statement#RETURN_GENERATED_KEYS}.
     *
     * @param product producto a crear, con todos sus campos completados
     * @return {@code true} si el producto fue insertado correctamente,
     *         {@code false} en caso de error
     */
    public boolean createProduct(Product product) {
        String query = "INSERT INTO productos (codigo, nombre, descripcion, categoria_id, " +
                "precio_compra, precio_venta, stock, stock_minimo, proveedor, ubicacion) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = conexionDB.conectar();
             PreparedStatement pstmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, product.getCodigo());
            pstmt.setString(2, product.getNombre());
            pstmt.setString(3, product.getDescripcion());
            pstmt.setInt(4, product.getCategoriaId());
            pstmt.setBigDecimal(5, product.getPrecioCompra());
            pstmt.setBigDecimal(6, product.getPrecioVenta());
            pstmt.setInt(7, product.getStock());
            pstmt.setInt(8, product.getStockMinimo());
            pstmt.setString(9, product.getProveedor());
            pstmt.setString(10, product.getUbicacion());

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                ResultSet rs = pstmt.getGeneratedKeys();
                if (rs.next()) {
                    product.setId(rs.getInt(1));
                }
                return true;
            }

        } catch (SQLException e) {
            System.err.println("Error al crear producto: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    // Leer todos los productos (con nombre de categoría)
    /**
     * Obtiene todos los productos registrados, incluyendo el nombre de su
     * categoría, ordenados alfabéticamente por nombre.
     *
     * @return lista de todos los {@link Product}; puede estar vacía si no
     *         hay registros o si ocurre un error de conexión/consulta
     */
    public List<Product> getAllProducts() {
        List<Product> products = new ArrayList<>();
        String query = "SELECT p.*, c.nombre as categoria_nombre " +
                "FROM productos p " +
                "LEFT JOIN categorias c ON p.categoria_id = c.id " +
                "ORDER BY p.nombre";

        try (Connection conn = conexionDB.conectar();
             PreparedStatement pstmt = conn.prepareStatement(query);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                Product product = extractProductFromResultSet(rs);
                products.add(product);
            }

        } catch (SQLException e) {
            System.err.println("Error al obtener productos: " + e.getMessage());
            e.printStackTrace();
        }
        return products;
    }

    // CORREGIDO: Método auxiliar mapea también el "categoria_nombre" si viene en la consulta
    /**
     * Método auxiliar que mapea la fila actual de un {@link ResultSet} a
     * una instancia de {@link Product}. Si la consulta incluyó un JOIN con
     * la tabla de categorías, también completa {@link Product#getCategoriaNombre()};
     * en caso contrario, ese campo se ignora de forma segura.
     *
     * @param rs resultado posicionado en la fila a mapear
     * @return el {@link Product} construido a partir de la fila actual
     * @throws SQLException si ocurre un error al leer las columnas del {@link ResultSet}
     */
    private Product extractProductFromResultSet(ResultSet rs) throws SQLException {
        Product product = new Product();

        product.setId(rs.getInt("id"));
        product.setCodigo(rs.getString("codigo"));
        product.setNombre(rs.getString("nombre"));
        product.setDescripcion(rs.getString("descripcion"));
        product.setCategoriaId(rs.getInt("categoria_id"));
        product.setPrecioCompra(rs.getBigDecimal("precio_compra"));
        product.setPrecioVenta(rs.getBigDecimal("precio_venta"));
        product.setStock(rs.getInt("stock"));
        product.setStockMinimo(rs.getInt("stock_minimo"));
        product.setProveedor(rs.getString("proveedor"));
        product.setUbicacion(rs.getString("ubicacion"));

        // Intentamos extraer el nombre de la categoría si existe en las columnas del ResultSet
        try {
            product.setCategoriaNombre(rs.getString("categoria_nombre"));
        } catch (SQLException e) {
            // Si la consulta no incluía un JOIN con categorías, no pasa nada, se ignora de forma segura
        }

        return product;
    }

    // Buscar producto por ID
    /**
     * Busca un producto por su identificador, incluyendo el nombre de su categoría.
     *
     * @param id identificador del producto a buscar
     * @return el {@link Product} encontrado, o {@code null} si no existe o
     *         si ocurre un error de conexión/consulta
     */
    public Product getProductById(int id) {
        String query = "SELECT p.*, c.nombre as categoria_nombre " +
                "FROM productos p " +
                "LEFT JOIN categorias c ON p.categoria_id = c.id " +
                "WHERE p.id = ?";

        try (Connection conn = conexionDB.conectar();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return extractProductFromResultSet(rs);
                }
            }

        } catch (SQLException e) {
            System.err.println("Error al obtener producto por ID: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    // Buscar producto por código
    /**
     * Busca un producto por su código (SKU), incluyendo el nombre de su categoría.
     *
     * @param codigo código del producto a buscar
     * @return el {@link Product} encontrado, o {@code null} si no existe o
     *         si ocurre un error de conexión/consulta
     */
    public Product getProductByCode(String codigo) {
        String query = "SELECT p.*, c.nombre as categoria_nombre " +
                "FROM productos p " +
                "LEFT JOIN categorias c ON p.categoria_id = c.id " +
                "WHERE p.codigo = ?";

        try (Connection conn = conexionDB.conectar();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, codigo);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return extractProductFromResultSet(rs);
                }
            }

        } catch (SQLException e) {
            System.err.println("Error al obtener producto por código: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    // Actualizar producto
    /**
     * Actualiza todos los campos de un producto existente, identificado
     * por su {@link Product#getId()}.
     *
     * @param product producto con los datos actualizados y el id del producto a modificar
     * @return {@code true} si la actualización afectó al menos una fila,
     *         {@code false} en caso de error o si no existía el producto
     */
    public boolean updateProduct(Product product) {
        String query = "UPDATE productos SET codigo = ?, nombre = ?, descripcion = ?, " +
                "categoria_id = ?, precio_compra = ?, precio_venta = ?, " +
                "stock = ?, stock_minimo = ?, proveedor = ?, ubicacion = ? " +
                "WHERE id = ?";

        try (Connection conn = conexionDB.conectar();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, product.getCodigo());
            pstmt.setString(2, product.getNombre());
            pstmt.setString(3, product.getDescripcion());
            pstmt.setInt(4, product.getCategoriaId());
            pstmt.setBigDecimal(5, product.getPrecioCompra());
            pstmt.setBigDecimal(6, product.getPrecioVenta());
            pstmt.setInt(7, product.getStock());
            pstmt.setInt(8, product.getStockMinimo());
            pstmt.setString(9, product.getProveedor());
            pstmt.setString(10, product.getUbicacion());
            pstmt.setInt(11, product.getId());

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
            System.err.println("Error al actualizar producto: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    // Eliminar producto
    /**
     * Elimina el producto cuyo identificador coincide con el proporcionado.
     *
     * @param id identificador del producto a eliminar
     * @return {@code true} si la eliminación afectó al menos una fila,
     *         {@code false} en caso de error o si no existía el producto
     */
    public boolean deleteProduct(int id) {
        String query = "DELETE FROM productos WHERE id = ?";

        try (Connection conn = conexionDB.conectar();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, id);
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
            System.err.println("Error al eliminar producto: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    // Buscar productos por nombre o código
    /**
     * Busca productos cuyo nombre o código contengan el término
     * proporcionado (coincidencia parcial), ordenados alfabéticamente
     * por nombre e incluyendo el nombre de su categoría.
     *
     * @param searchTerm término de búsqueda a aplicar sobre nombre y código
     * @return lista de {@link Product} que coinciden con el término
     *         buscado; puede estar vacía si no hay coincidencias o si
     *         ocurre un error
     */
    public List<Product> searchProducts(String searchTerm) {
        List<Product> products = new ArrayList<>();
        String query = "SELECT p.*, c.nombre as categoria_nombre " +
                "FROM productos p " +
                "LEFT JOIN categorias c ON p.categoria_id = c.id " +
                "WHERE p.nombre LIKE ? OR p.codigo LIKE ? " +
                "ORDER BY p.nombre";

        try (Connection conn = conexionDB.conectar();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            String searchPattern = "%" + searchTerm + "%";
            pstmt.setString(1, searchPattern);
            pstmt.setString(2, searchPattern);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Product product = extractProductFromResultSet(rs);
                    products.add(product);
                }
            }

        } catch (SQLException e) {
            System.err.println("Error al buscar productos: " + e.getMessage());
            e.printStackTrace();
        }
        return products;
    }

    // Productos con stock bajo
    /**
     * Obtiene los productos cuyo stock actual es menor o igual a su stock
     * mínimo configurado, ordenados de menor a mayor stock. Útil para
     * generar alertas de reabastecimiento.
     *
     * @return lista de {@link Product} en nivel de stock bajo; puede estar
     *         vacía si ningún producto cumple la condición o si ocurre un error
     */
    public List<Product> getLowStockProducts() {
        List<Product> products = new ArrayList<>();
        String query = "SELECT p.*, c.nombre as categoria_nombre " +
                "FROM productos p " +
                "LEFT JOIN categorias c ON p.categoria_id = c.id " +
                "WHERE p.stock <= p.stock_minimo " +
                "ORDER BY p.stock ASC";

        try (Connection conn = conexionDB.conectar();
             PreparedStatement pstmt = conn.prepareStatement(query);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                Product product = extractProductFromResultSet(rs);
                products.add(product);
            }

        } catch (SQLException e) {
            System.err.println("Error al obtener productos con stock bajo: " + e.getMessage());
            e.printStackTrace();
        }
        return products;
    }
}