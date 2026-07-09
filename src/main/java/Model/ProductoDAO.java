package Model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * ProductoDAO
 */
public class ProductoDAO {

    public Producto obtenerProducto(String nombre) {
        String query = "SELECT * FROM productos WHERE id = ?";
        try (Connection conn = DBConnection.conectar();
                PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, nombre);

            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                Producto producto = new Producto();
                producto.setId(rs.getInt("id"));
                producto.setCodigo(rs.getString("codigo"));
                producto.setNombre(rs.getString("nombre"));
                producto.setDescription(rs.getString("description"));
                producto.setCategoria_id(rs.getInt("categoria_id"));
                producto.setPrecio_compra(rs.getBigDecimal("precio_compra"));
                producto.setPrecio_venta(rs.getBigDecimal("precio_venta"));
                producto.setStock(rs.getInt("stock"));
                producto.setStock_minimo(rs.getInt("stock_minimo"));
                producto.setProveedor(rs.getString("proveedor"));
                producto.setUbicacion(rs.getString("ubicacion"));
                return producto;
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener el producto: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    public boolean createProducto(Producto producto) {
        String query = "INSERT INTO productos (codigo, nombre, description, categoria_id, " +
                "precio_compra, precio_venta, stock, stock_minimo, proveedor, ubicacion) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.conectar();
                PreparedStatement pstmt = conn.prepareStatement(query, PreparedStatement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, producto.getCodigo());
            pstmt.setString(2, producto.getNombre());
            pstmt.setString(3, producto.getDescription());
            pstmt.setInt(4, producto.getCategoria_id());
            pstmt.setBigDecimal(5, producto.getPrecio_compra());
            pstmt.setBigDecimal(6, producto.getPrecio_venta());
            pstmt.setInt(7, producto.getStock());
            pstmt.setInt(8, producto.getStock_minimo());
            pstmt.setString(9, producto.getProveedor());
            pstmt.setString(10, producto.getUbicacion());

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                ResultSet rs = pstmt.getGeneratedKeys();
                if (rs.next()) {
                    producto.setId(rs.getInt(1));
                }
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Error al crear el producto: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    public List<Producto> getAllProductos() {
        List<Producto> products = new ArrayList<>();
        String query = "SELECT p.*, c.nombre as categoria_nombre "
                + "FROM productos p "
                + "LEFT JOIN categorias c "
                + "ON p.categoria_id = c.id "
                + "ORDER BY p.nombre ";
        try (Connection conn = DBConnection.conectar();
                PreparedStatement pstmt = conn.prepareStatement(query);
                ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                Producto producto = extractProductoFromResultSet(rs);
                products.add(producto);

            }
        } catch (SQLException e) {
            System.err.println("Error al obtener los productos: " + e.getMessage());
            e.printStackTrace();
        }
        return products;
    }

    public Producto getProductoById(int id) {
        String query = "SELECT p.*, c.nombre as categoria_nombre "
                + "FROM productos p "
                + "LEFT JOIN categorias c "
                + "ON p.categoria_id = c.id "
                + "WHERE p.id = ? ";
        try (Connection conn = DBConnection.conectar();
                PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return extractProductoFromResultSet(rs);
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener el producto por ID: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    public Producto getProductoByCodigo(String codigo) {
        String query = "SELECT p.*, c.nombre as categoria_nombre "
                + "FROM productos p "
                + "LEFT JOIN categorias c "
                + "ON p.categoria_id = c.id "
                + "WHERE p.codigo = ? ";
        try (Connection conn = DBConnection.conectar();
                PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, codigo);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return extractProductoFromResultSet(rs);
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener el producto por código: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    public boolean updateProducto(Producto producto) {
        String query = "UPDATE productos SET codigo = ?, nombre = ?, description = ?, categoria_id = ?,"
                + "precio_compra = ?, precio_venta = ?, stock = ?, stock_minimo = ?, proveedor = ?, ubicacion = ?"
                + "WHERE id = ?";
        try (Connection conn = DBConnection.conectar();
                PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, producto.getCodigo());
            pstmt.setString(2, producto.getNombre());
            pstmt.setString(3, producto.getDescription());
            pstmt.setInt(4, producto.getCategoria_id());
            pstmt.setBigDecimal(5, producto.getPrecio_compra());
            pstmt.setBigDecimal(6, producto.getPrecio_venta());
            pstmt.setInt(7, producto.getStock());
            pstmt.setInt(8, producto.getStock_minimo());
            pstmt.setString(9, producto.getProveedor());
            pstmt.setString(10, producto.getUbicacion());
            pstmt.setInt(11, producto.getId());

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("Error al actualizar el producto: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    public boolean deleteProducto(int id) {
        String query = "DELETE FROM productos WHERE id = ?";
        try (Connection conn = DBConnection.conectar();
                PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, id);
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("Error al eliminar el producto: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    public List<Producto> searchProductos(String searchTerm) {
        List<Producto> products = new ArrayList<>();
        String query = "SELECT p.*, c.nombre as categoria_nombre "
                + "FROM productos p "
                + "LEFT JOIN categorias c "
                + "ON p.categoria_id = c.id "
                + "WHERE p.nombre LIKE ? OR p.codigo LIKE ? "
                + "ORDER BY p.nombre";
        try (Connection conn = DBConnection.conectar();
                PreparedStatement pstmt = conn.prepareStatement(query)) {
            String searchPattern = "%" + searchTerm + "%";
            pstmt.setString(1, searchPattern);
            pstmt.setString(2, searchPattern);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Producto producto = extractProductoFromResultSet(rs);
                products.add(producto);
            }
        } catch (SQLException e) {
            System.err.println("Error al buscar productos: " + e.getMessage());
            e.printStackTrace();
        }
        return products;
    }

    public List<Producto> getLowStockProductos(int threshold) {
        List<Producto> products = new ArrayList<>();
        String query = "SELECT p.*, c.nombre as categoria_nombre "
                + "FROM productos p "
                + "LEFT JOIN categorias c "
                + "ON p.categoria_id = c.id "
                + "WHERE p.stock <= p.stock_minimo "
                + "ORDER BY p.stock ASC";
        try (Connection conn = DBConnection.conectar();
                PreparedStatement pstmt = conn.prepareStatement(query)) {
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Producto producto = extractProductoFromResultSet(rs);
                products.add(producto);
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener productos con bajo stock: " + e.getMessage());
            e.printStackTrace();
        }
        return products;
    }

    private Producto extractProductoFromResultSet(ResultSet rs) throws SQLException {
        Producto producto = new Producto();
        producto.setId(rs.getInt("id"));
        producto.setCodigo(rs.getString("codigo"));
        producto.setNombre(rs.getString("nombre"));
        producto.setDescription(rs.getString("description"));
        producto.setCategoria_id(rs.getInt("categoria_id"));
        producto.setPrecio_compra(rs.getBigDecimal("precio_compra"));
        producto.setPrecio_venta(rs.getBigDecimal("precio_venta"));
        producto.setStock(rs.getInt("stock"));
        producto.setStock_minimo(rs.getInt("stock_minimo"));
        producto.setProveedor(rs.getString("proveedor"));
        producto.setUbicacion(rs.getString("ubicacion"));
        return producto;
    }
}
