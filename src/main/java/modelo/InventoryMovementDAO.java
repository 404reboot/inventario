package modelo;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object (DAO) responsable de registrar y consultar los
 * movimientos de inventario (entradas y salidas) en la tabla
 * {@code movimientos}, manteniendo sincronizado el stock de la tabla
 * {@code productos}.
 * <p>
 * Las consultas de lectura realizan {@code LEFT JOIN} con {@code productos}
 * y {@code usuarios} para completar el nombre/código del producto y el
 * nombre del usuario asociados a cada movimiento. El registro de
 * movimientos ({@link #registrarMovimiento}) se ejecuta de forma
 * transaccional para garantizar que la inserción del movimiento y la
 * actualización del stock ocurran de manera atómica.
 */
public class InventoryMovementDAO {

    /**
     * Obtiene todos los movimientos de inventario registrados, ordenados
     * del más reciente al más antiguo, incluyendo el nombre/código del
     * producto y el nombre del usuario asociados.
     *
     * @return lista de todos los {@link InventoryMovement}; puede estar
     *         vacía si no hay registros o si ocurre un error de conexión/consulta
     */
    public List<InventoryMovement> getAllMovements() {
        List<InventoryMovement> movements = new ArrayList<>();
        String query = "SELECT m.*, p.nombre AS producto_nombre, p.codigo AS producto_codigo, " +
                       "u.nombre_completo AS usuario_nombre " +
                       "FROM movimientos m " +
                       "LEFT JOIN productos p ON m.producto_id = p.id " +
                       "LEFT JOIN usuarios u ON m.usuario_id = u.id " +
                       "ORDER BY m.fecha_movimiento DESC";

        try (Connection conn = conexionDB.conectar();
             PreparedStatement pstmt = conn.prepareStatement(query);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                InventoryMovement movement = new InventoryMovement();
                movement.setId(rs.getInt("id"));
                movement.setProductoId(rs.getInt("producto_id"));
                movement.setProductoNombre(rs.getString("producto_nombre"));
                movement.setProductoCodigo(rs.getString("producto_codigo"));
                movement.setTipoMovimiento(rs.getString("tipo_movimiento"));
                movement.setCantidad(rs.getInt("cantidad"));
                movement.setPrecioUnitario(rs.getBigDecimal("precio_unitario"));
                // La tabla 'movimientos' no tiene columna subtotal: se calcula en el modelo
                movement.setMotivo(rs.getString("motivo"));
                movement.setUsuarioId(rs.getInt("usuario_id"));
                movement.setUsuarioNombre(rs.getString("usuario_nombre"));
                
                if (rs.getTimestamp("fecha_movimiento") != null) {
                    movement.setFechaMovimiento(rs.getTimestamp("fecha_movimiento").toLocalDateTime());
                }
                
                movement.setReferencia(rs.getString("referencia"));
                movements.add(movement);
            }

        } catch (SQLException e) {
            System.err.println("Error al obtener movimientos: " + e.getMessage());
        }
        return movements;
    }

    /**
     * Obtiene el historial de movimientos de inventario asociados a un
     * producto específico, ordenados del más reciente al más antiguo.
     *
     * @param productId identificador del producto cuyos movimientos se desean consultar
     * @return lista de {@link InventoryMovement} del producto indicado;
     *         puede estar vacía si no tiene movimientos o si ocurre un error
     */
    public List<InventoryMovement> getMovementsByProduct(int productId) {
        List<InventoryMovement> movements = new ArrayList<>();
        String query = "SELECT m.*, p.nombre AS producto_nombre, p.codigo AS producto_codigo, " +
                       "u.nombre_completo AS usuario_nombre " +
                       "FROM movimientos m " +
                       "LEFT JOIN productos p ON m.producto_id = p.id " +
                       "LEFT JOIN usuarios u ON m.usuario_id = u.id " +
                       "WHERE m.producto_id = ? " +
                       "ORDER BY m.fecha_movimiento DESC";

        try (Connection conn = conexionDB.conectar();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, productId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    InventoryMovement movement = new InventoryMovement();
                    movement.setId(rs.getInt("id"));
                    movement.setProductoId(rs.getInt("producto_id"));
                    movement.setProductoNombre(rs.getString("producto_nombre"));
                    movement.setProductoCodigo(rs.getString("producto_codigo"));
                    movement.setTipoMovimiento(rs.getString("tipo_movimiento"));
                    movement.setCantidad(rs.getInt("cantidad"));
                    movement.setPrecioUnitario(rs.getBigDecimal("precio_unitario"));
                    // La tabla 'movimientos' no tiene columna subtotal: se calcula en el modelo
                    movement.setMotivo(rs.getString("motivo"));
                    movement.setUsuarioId(rs.getInt("usuario_id"));
                    movement.setUsuarioNombre(rs.getString("usuario_nombre"));
                    
                    if (rs.getTimestamp("fecha_movimiento") != null) {
                        movement.setFechaMovimiento(rs.getTimestamp("fecha_movimiento").toLocalDateTime());
                    }
                    
                    movement.setReferencia(rs.getString("referencia"));
                    movements.add(movement);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener movimientos por producto: " + e.getMessage());
        }
        return movements;
    }

    /**
     * Registra un movimiento de tipo {@code "ENTRADA"} para un producto,
     * incrementando su stock. Delegación conveniente de
     * {@link #registrarMovimiento(int, String, int, BigDecimal, String, int, String)}.
     *
     * @param productoId identificador del producto que recibe la entrada
     * @param cantidad cantidad de unidades a ingresar (debe ser positiva)
     * @param precioUnitario precio unitario de la entrada
     * @param motivo motivo de la entrada
     * @param usuarioId identificador del usuario que registra el movimiento
     * @param referencia referencia adicional del movimiento (documento, factura, etc.)
     * @return {@code true} si el movimiento se registró y el stock se
     *         actualizó correctamente, {@code false} en caso de error
     */
    public boolean registrarEntrada(int productoId, int cantidad, BigDecimal precioUnitario, 
                                    String motivo, int usuarioId, String referencia) {
        return registrarMovimiento(productoId, "ENTRADA", cantidad, precioUnitario, motivo, usuarioId, referencia);
    }

    /**
     * Registra un movimiento de tipo {@code "SALIDA"} para un producto,
     * descontando su stock (solo si hay stock suficiente). Delegación
     * conveniente de
     * {@link #registrarMovimiento(int, String, int, BigDecimal, String, int, String)}.
     *
     * @param productoId identificador del producto que sufre la salida
     * @param cantidad cantidad de unidades a retirar (debe ser positiva)
     * @param precioUnitario precio unitario de la salida
     * @param motivo motivo de la salida
     * @param usuarioId identificador del usuario que registra el movimiento
     * @param referencia referencia adicional del movimiento (documento, factura, etc.)
     * @return {@code true} si el movimiento se registró y el stock se
     *         actualizó correctamente, {@code false} en caso de error o si
     *         no había stock suficiente
     */
    public boolean registrarSalida(int productoId, int cantidad, BigDecimal precioUnitario, 
                                   String motivo, int usuarioId, String referencia) {
        return registrarMovimiento(productoId, "SALIDA", cantidad, precioUnitario, motivo, usuarioId, referencia);
    }

    /**
     * Registra de forma transaccional un movimiento de inventario
     * ({@code "ENTRADA"} o {@code "SALIDA"}) e infiere en la misma
     * transacción la actualización del stock del producto involucrado.
     * <p>
     * Para movimientos de tipo {@code "ENTRADA"} suma la cantidad al stock;
     * para {@code "SALIDA"} la resta, exigiendo que el producto tenga stock
     * suficiente (condición {@code stock >= cantidad} en la sentencia SQL).
     * Si cualquiera de las dos operaciones falla, se revierte la
     * transacción completa mediante {@code rollback()}.
     *
     * @param productoId identificador del producto involucrado
     * @param tipoMovimiento tipo de movimiento, {@code "ENTRADA"} o {@code "SALIDA"}
     * @param cantidad cantidad de unidades del movimiento
     * @param precioUnitario precio unitario aplicado
     * @param motivo motivo del movimiento
     * @param usuarioId identificador del usuario que registra el movimiento
     * @param referencia referencia adicional del movimiento
     * @return {@code true} si el movimiento y la actualización de stock se
     *         completaron correctamente, {@code false} en caso de error
     */
    private boolean registrarMovimiento(int productoId, String tipoMovimiento, int cantidad, 
                                        BigDecimal precioUnitario, String motivo, 
                                        int usuarioId, String referencia) {
        Connection conn = null;
        PreparedStatement pstmtMovimiento = null;
        PreparedStatement pstmtProducto = null;

        try {
            conn = conexionDB.conectar();
            conn.setAutoCommit(false); 

            String sqlMovimiento = "INSERT INTO movimientos (producto_id, tipo_movimiento, cantidad, " +
                                   "precio_unitario, motivo, usuario_id, referencia, fecha_movimiento) " +
                                   "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
            
            pstmtMovimiento = conn.prepareStatement(sqlMovimiento);
            pstmtMovimiento.setInt(1, productoId);
            pstmtMovimiento.setString(2, tipoMovimiento);
            pstmtMovimiento.setInt(3, cantidad);
            pstmtMovimiento.setBigDecimal(4, precioUnitario);
            pstmtMovimiento.setString(5, motivo);
            pstmtMovimiento.setInt(6, usuarioId);
            pstmtMovimiento.setString(7, referencia);
            // Corregido a Timestamp para evitar fallos de compatibilidad en la base de datos
            pstmtMovimiento.setTimestamp(8, Timestamp.valueOf(LocalDateTime.now()));

            int rowsMovimiento = pstmtMovimiento.executeUpdate();
            if (rowsMovimiento == 0) {
                conn.rollback();
                return false;
            }

            String sqlProducto;
            if (tipoMovimiento.equals("ENTRADA")) {
                sqlProducto = "UPDATE productos SET stock = stock + ? WHERE id = ?";
            } else {
                sqlProducto = "UPDATE productos SET stock = stock - ? WHERE id = ? AND stock >= ?";
            }

            pstmtProducto = conn.prepareStatement(sqlProducto);
            pstmtProducto.setInt(1, cantidad);
            pstmtProducto.setInt(2, productoId);

            if (tipoMovimiento.equals("SALIDA")) {
                pstmtProducto.setInt(3, cantidad); 
            }

            int rowsProducto = pstmtProducto.executeUpdate();
            if (rowsProducto == 0) {
                conn.rollback();
                return false;
            }

            conn.commit(); 
            return true;

        } catch (SQLException e) {
            try {
                if (conn != null) conn.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            System.err.println("Error al registrar movimiento: " + e.getMessage());
            return false;
        } finally {
            try {
                if (pstmtMovimiento != null) pstmtMovimiento.close();
                if (pstmtProducto != null) pstmtProducto.close();
                if (conn != null) {
                    conn.setAutoCommit(true);
                    conn.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}