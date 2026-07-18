package modelo;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Modelo (entidad) que representa un movimiento de inventario (entrada,
 * salida o ajuste de stock) asociado a un producto.
 * <p>
 * Además de los datos propios del movimiento, mantiene campos derivados
 * como el nombre y código del producto o el nombre del usuario que lo
 * registró, útiles cuando se recuperan movimientos mediante JOIN con las
 * tablas de productos y usuarios. El {@link #subtotal} se recalcula
 * automáticamente cada vez que cambian la cantidad o el precio unitario.
 */
public class InventoryMovement {

    /** Identificador único del movimiento (clave primaria en la base de datos). */
    private int id;
    /** Identificador del producto involucrado en el movimiento. */
    private int productoId;
    /** Nombre del producto (dato derivado, obtenido vía JOIN). */
    private String productoNombre;
    /** Código del producto (dato derivado, obtenido vía JOIN). */
    private String productoCodigo;
    /** Tipo de movimiento: {@code "ENTRADA"}, {@code "SALIDA"} o {@code "AJUSTE"}. */
    private String tipoMovimiento; // "ENTRADA", "SALIDA", "AJUSTE"
    /** Cantidad de unidades involucradas en el movimiento. */
    private int cantidad;
    /** Precio unitario aplicado en el movimiento. */
    private BigDecimal precioUnitario;
    /** Subtotal del movimiento, calculado como {@code precioUnitario * cantidad}. */
    private BigDecimal subtotal;
    /** Motivo o justificación del movimiento. */
    private String motivo;
    /** Identificador del usuario que registró el movimiento. */
    private int usuarioId;
    /** Nombre del usuario que registró el movimiento (dato derivado, obtenido vía JOIN). */
    private String usuarioNombre;
    /** Fecha y hora en que se realizó el movimiento. */
    private LocalDateTime fechaMovimiento;
    /** Referencia adicional del movimiento (por ejemplo, número de documento o factura). */
    private String referencia;

    // Constructor vacío
    /** Constructor vacío requerido para instanciar el bean. */
    public InventoryMovement() {}

    // Constructor con parámetros
    /**
     * Construye un movimiento de inventario con sus atributos principales.
     * Calcula automáticamente el {@link #subtotal} a partir de la cantidad
     * y el precio unitario proporcionados.
     *
     * @param id identificador único del movimiento
     * @param productoId identificador del producto involucrado
     * @param tipoMovimiento tipo de movimiento ({@code "ENTRADA"}, {@code "SALIDA"} o {@code "AJUSTE"})
     * @param cantidad cantidad de unidades del movimiento
     * @param precioUnitario precio unitario aplicado
     * @param motivo motivo del movimiento
     * @param usuarioId identificador del usuario que registra el movimiento
     * @param fechaMovimiento fecha y hora del movimiento
     */
    public InventoryMovement(int id, int productoId, String tipoMovimiento, int cantidad,
                             BigDecimal precioUnitario, String motivo, int usuarioId,
                             LocalDateTime fechaMovimiento) {
        this.id = id;
        this.productoId = productoId;
        this.tipoMovimiento = tipoMovimiento;
        this.cantidad = cantidad;
        this.precioUnitario = precioUnitario;
        this.motivo = motivo;
        this.usuarioId = usuarioId;
        this.fechaMovimiento = fechaMovimiento;
        calcularSubtotal();
    }

    // Getters y Setters
    /** @return el identificador único del movimiento */
    public int getId() { return id; }
    /** @param id nuevo identificador único del movimiento */
    public void setId(int id) { this.id = id; }

    /** @return el identificador del producto involucrado */
    public int getProductoId() { return productoId; }
    /** @param productoId nuevo identificador de producto */
    public void setProductoId(int productoId) { this.productoId = productoId; }

    /** @return el nombre del producto (dato derivado) */
    public String getProductoNombre() { return productoNombre; }
    /** @param productoNombre nuevo nombre de producto a asociar */
    public void setProductoNombre(String productoNombre) { this.productoNombre = productoNombre; }

    /** @return el código del producto (dato derivado) */
    public String getProductoCodigo() { return productoCodigo; }
    /** @param productoCodigo nuevo código de producto a asociar */
    public void setProductoCodigo(String productoCodigo) { this.productoCodigo = productoCodigo; }

    /** @return el tipo de movimiento ({@code "ENTRADA"}, {@code "SALIDA"} o {@code "AJUSTE"}) */
    public String getTipoMovimiento() { return tipoMovimiento; }
    /**
     * Actualiza el tipo de movimiento y recalcula el subtotal.
     * @param tipoMovimiento nuevo tipo de movimiento
     */
    public void setTipoMovimiento(String tipoMovimiento) {
        this.tipoMovimiento = tipoMovimiento;
        calcularSubtotal();
    }

    /** @return la cantidad de unidades del movimiento */
    public int getCantidad() { return cantidad; }
    /**
     * Actualiza la cantidad del movimiento y recalcula el subtotal.
     * @param cantidad nueva cantidad de unidades
     */
    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
        calcularSubtotal();
    }

    /** @return el precio unitario aplicado en el movimiento */
    public BigDecimal getPrecioUnitario() { return precioUnitario; }
    /**
     * Actualiza el precio unitario del movimiento y recalcula el subtotal.
     * @param precioUnitario nuevo precio unitario
     */
    public void setPrecioUnitario(BigDecimal precioUnitario) {
        this.precioUnitario = precioUnitario;
        calcularSubtotal();
    }

    /** @return el subtotal calculado del movimiento */
    public BigDecimal getSubtotal() { return subtotal; }
    /** @param subtotal nuevo valor de subtotal (asignación manual) */
    public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }

    /** @return el motivo del movimiento */
    public String getMotivo() { return motivo; }
    /** @param motivo nuevo motivo del movimiento */
    public void setMotivo(String motivo) { this.motivo = motivo; }

    /** @return el identificador del usuario que registró el movimiento */
    public int getUsuarioId() { return usuarioId; }
    /** @param usuarioId nuevo identificador de usuario */
    public void setUsuarioId(int usuarioId) { this.usuarioId = usuarioId; }

    /** @return el nombre del usuario que registró el movimiento (dato derivado) */
    public String getUsuarioNombre() { return usuarioNombre; }
    /** @param usuarioNombre nuevo nombre de usuario a asociar */
    public void setUsuarioNombre(String usuarioNombre) { this.usuarioNombre = usuarioNombre; }

    /** @return la fecha y hora del movimiento */
    public LocalDateTime getFechaMovimiento() { return fechaMovimiento; }
    /** @param fechaMovimiento nueva fecha y hora del movimiento */
    public void setFechaMovimiento(LocalDateTime fechaMovimiento) { this.fechaMovimiento = fechaMovimiento; }

    /** @return la referencia adicional del movimiento */
    public String getReferencia() { return referencia; }
    /** @param referencia nueva referencia del movimiento */
    public void setReferencia(String referencia) { this.referencia = referencia; }

    /**
     * Calcula y actualiza el {@link #subtotal} del movimiento como el
     * producto de {@link #precioUnitario} por {@link #cantidad}.
     * No realiza ningún cálculo si {@link #precioUnitario} es {@code null}
     * o la cantidad no es positiva.
     */
    private void calcularSubtotal() {
        if (precioUnitario != null && cantidad > 0) {
            this.subtotal = precioUnitario.multiply(new BigDecimal(cantidad));
        }
    }
}
