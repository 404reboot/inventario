package Model;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class InventoryMovementDAO {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // Registrar entrada de inventario
    public boolean registrarEntrada(int productoId, int cantidad, BigDecimal precioUnitario,
            String motivo, int usuarioId, String referencia) {
        return registrarMovimiento(productoId, "ENTRADA", cantidad, precioUnitario,
                motivo, usuarioId, referencia);
    }

    // Registrar salida de inventario
    public boolean registrarSalida(int productoId, int cantidad, BigDecimal precioUnitario,
            String motivo, int usuarioId, String referencia) {
        return registrarMovimiento(productoId, "SALIDA", cantidad, precioUnitario,
                motivo, usuarioId, referencia);
    }

    // Registrar movimiento genérico
    private boolean registrarMovimiento(int productoId, String tipoMovimiento, int cantidad,
            BigDecimal precioUnitario, String motivo,
            int usuarioId, String referencia) {
        Connection conn = null;
        PreparedStatement pstmtMovimiento = null;
        PreparedStatement pstmtProducto = null;

        try {
            conn = DBConnection.conectar();
            conn.setAutoCommit(false); // Iniciar transacción

            // 1. Insertar el movimiento
            String sqlMovimiento = "INSERT INTO movimientos_inventario " +
                    "(producto_id, tipo_movimiento, cantidad, precio_unitario, " +
                    "subtotal, motivo, usuario_id, referencia, fecha_movimiento) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

            pstmtMovimiento = conn.prepareStatement(sqlMovimiento);
            pstmtMovimiento.setInt(1, productoId);
            pstmtMovimiento.setString(2, tipoMovimiento);
            pstmtMovimiento.setInt(3, cantidad);
            pstmtMovimiento.setBigDecimal(4, precioUnitario);
            BigDecimal subtotal = precioUnitario.multiply(new BigDecimal(cantidad));
            pstmtMovimiento.setBigDecimal(5, subtotal);
            pstmtMovimiento.setString(6, motivo);
            pstmtMovimiento.setInt(7, usuarioId);
            pstmtMovimiento.setString(8, referencia);
            pstmtMovimiento.setString(9, LocalDateTime.now().format(DATE_FORMATTER));

            int rowsMovimiento = pstmtMovimiento.executeUpdate();

            if (rowsMovimiento == 0) {
                conn.rollback();
                return false;
            }

            // 2. Actualizar el stock del producto
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
                pstmtProducto.setInt(3, cantidad); // Validar stock suficiente
            }

            int rowsProducto = pstmtProducto.executeUpdate();

            if (rowsProducto == 0) {
                conn.rollback();
                if (tipoMovimiento.equals("SALIDA")) {
                    throw new SQLException("Stock insuficiente para realizar la salida.");
                }
                return false;
            }

            conn.commit();
            return true;

        } catch (SQLException e) {
            try {
                if (conn != null)
                    conn.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            System.err.println("Error al registrar movimiento: " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            try {
                if (pstmtMovimiento != null)
                    pstmtMovimiento.close();
                if (pstmtProducto != null)
                    pstmtProducto.close();
                if (conn != null) {
                    conn.setAutoCommit(true);
                    conn.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    // Obtener todos los movimientos
    public List<InventoryMovement> getAllMovements() {
        List<InventoryMovement> movements = new ArrayList<>();
        String query = "SELECT m.*, p.nombre as producto_nombre, p.codigo as producto_codigo, " +
                "u.nombre_completo as usuario_nombre " +
                "FROM movimientos_inventario m " +
                "LEFT JOIN productos p ON m.producto_id = p.id " +
                "LEFT JOIN usuarios u ON m.usuario_id = u.id " +
                "ORDER BY m.fecha_movimiento DESC";

        try (Connection conn = DBConnection.conectar();
                PreparedStatement pstmt = conn.prepareStatement(query);
                ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                InventoryMovement movement = extractMovementFromResultSet(rs);
                movements.add(movement);
            }

        } catch (SQLException e) {
            System.err.println("Error al obtener movimientos: " + e.getMessage());
            e.printStackTrace();
        }
        return movements;
    }

    // Obtener movimientos por producto
    public List<InventoryMovement> getMovementsByProduct(int productoId) {
        List<InventoryMovement> movements = new ArrayList<>();
        String query = "SELECT m.*, p.nombre as producto_nombre, p.codigo as producto_codigo, " +
                "u.nombre_completo as usuario_nombre " +
                "FROM movimientos_inventario m " +
                "LEFT JOIN productos p ON m.producto_id = p.id " +
                "LEFT JOIN usuarios u ON m.usuario_id = u.id " +
                "WHERE m.producto_id = ? " +
                "ORDER BY m.fecha_movimiento DESC";

        try (Connection conn = DBConnection.conectar();
                PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, productoId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                InventoryMovement movement = extractMovementFromResultSet(rs);
                movements.add(movement);
            }

        } catch (SQLException e) {
            System.err.println("Error al obtener movimientos por producto: " + e.getMessage());
            e.printStackTrace();
        }
        return movements;
    }

    // Obtener movimientos por tipo
    public List<InventoryMovement> getMovementsByType(String tipo) {
        List<InventoryMovement> movements = new ArrayList<>();
        String query = "SELECT m.*, p.nombre as producto_nombre, p.codigo as producto_codigo, " +
                "u.nombre_completo as usuario_nombre " +
                "FROM movimientos_inventario m " +
                "LEFT JOIN productos p ON m.producto_id = p.id " +
                "LEFT JOIN usuarios u ON m.usuario_id = u.id " +
                "WHERE m.tipo_movimiento = ? " +
                "ORDER BY m.fecha_movimiento DESC";

        try (Connection conn = DBConnection.conectar();
                PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, tipo);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                InventoryMovement movement = extractMovementFromResultSet(rs);
                movements.add(movement);
            }

        } catch (SQLException e) {
            System.err.println("Error al obtener movimientos por tipo: " + e.getMessage());
            e.printStackTrace();
        }
        return movements;
    }

    // Método auxiliar para extraer movimiento de ResultSet
    private InventoryMovement extractMovementFromResultSet(ResultSet rs) throws SQLException {
        InventoryMovement movement = new InventoryMovement();
        movement.setId(rs.getInt("id"));
        movement.setProductoId(rs.getInt("producto_id"));
        movement.setProductoNombre(rs.getString("producto_nombre"));
        movement.setProductoCodigo(rs.getString("producto_codigo"));
        movement.setTipoMovimiento(rs.getString("tipo_movimiento"));
        movement.setCantidad(rs.getInt("cantidad"));
        movement.setPrecioUnitario(rs.getBigDecimal("precio_unitario"));
        movement.setSubtotal(rs.getBigDecimal("subtotal"));
        movement.setMotivo(rs.getString("motivo"));
        movement.setUsuarioId(rs.getInt("usuario_id"));
        movement.setUsuarioNombre(rs.getString("usuario_nombre"));
        movement.setReferencia(rs.getString("referencia"));
        Timestamp timestamp = rs.getTimestamp("fecha_movimiento");
        if (timestamp != null) {
            movement.setFechaMovimiento(timestamp.toLocalDateTime());
        }

        return movement;
    }

    // Resumen de movimientos por período
    public MovementSummary getMovementSummary(LocalDateTime startDate, LocalDateTime endDate) {
        MovementSummary summary = new MovementSummary();
        String query = "SELECT " +
                "SUM(CASE WHEN tipo_movimiento = 'ENTRADA' THEN cantidad ELSE 0 END) as total_entradas, " +
                "SUM(CASE WHEN tipo_movimiento = 'SALIDA' THEN cantidad ELSE 0 END) as total_salidas, " +
                "SUM(CASE WHEN tipo_movimiento = 'ENTRADA' THEN subtotal ELSE 0 END) as valor_entradas, " +
                "SUM(CASE WHEN tipo_movimiento = 'SALIDA' THEN subtotal ELSE 0 END) as valor_salidas, " +
                "COUNT(*) as total_movimientos " +
                "FROM movimientos_inventario " +
                "WHERE fecha_movimiento BETWEEN ? AND ?";

        try (Connection conn = DBConnection.conectar();
                PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, startDate.format(DATE_FORMATTER));
            pstmt.setString(2, endDate.format(DATE_FORMATTER));

            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                summary.setTotalEntradas(rs.getInt("total_entradas"));
                summary.setTotalSalidas(rs.getInt("total_salidas"));
                summary.setValorEntradas(rs.getBigDecimal("valor_entradas"));
                summary.setValorSalidas(rs.getBigDecimal("valor_salidas"));
                summary.setTotalMovimientos(rs.getInt("total_movimientos"));
            }

        } catch (SQLException e) {
            System.err.println("Error al obtener resumen de movimientos: " + e.getMessage());
            e.printStackTrace();
        }

        return summary;
    }

    // Clase interna para resumen de movimientos
    public static class MovementSummary {
        private int totalEntradas;
        private int totalSalidas;
        private BigDecimal valorEntradas = BigDecimal.ZERO;
        private BigDecimal valorSalidas = BigDecimal.ZERO;
        private int totalMovimientos;

        // Getters y Setters
        public int getTotalEntradas() {
            return totalEntradas;
        }

        public void setTotalEntradas(int totalEntradas) {
            this.totalEntradas = totalEntradas;
        }

        public int getTotalSalidas() {
            return totalSalidas;
        }

        public void setTotalSalidas(int totalSalidas) {
            this.totalSalidas = totalSalidas;
        }

        public BigDecimal getValorEntradas() {
            return valorEntradas;
        }

        public void setValorEntradas(BigDecimal valorEntradas) {
            this.valorEntradas = valorEntradas;
        }

        public BigDecimal getValorSalidas() {
            return valorSalidas;
        }

        public void setValorSalidas(BigDecimal valorSalidas) {
            this.valorSalidas = valorSalidas;
        }

        public int getTotalMovimientos() {
            return totalMovimientos;
        }

        public void setTotalMovimientos(int totalMovimientos) {
            this.totalMovimientos = totalMovimientos;
        }
    }
}
