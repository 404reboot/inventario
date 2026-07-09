package Model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * InventoryMovement
 */
public class InventoryMovement {

    private int id;
    private int productoId;
    private String productoNombre;
    private String productoCodigo;
    private String tipoMovimiento;
    private int cantidad;
    private BigDecimal precioUnitario;
    private BigDecimal subtotal;
    private String motivo;
    private int usuarioId;
    private String usuarioNombre;
    private LocalDateTime fechaMovimiento;
    private String referencia;

    public InventoryMovement() {}

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

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getProductoId() { return productoId; }
    public void setProductoId(int productoId) { this.productoId = productoId; }

    public String getProductoNombre() { return productoNombre; }
    public void setProductoNombre(String productoNombre) { this.productoNombre = productoNombre; }

    public String getProductoCodigo() { return productoCodigo; }
    public void setProductoCodigo(String productoCodigo) { this.productoCodigo = productoCodigo; }

    public String getTipoMovimiento() { return tipoMovimiento; }
    public void setTipoMovimiento(String tipoMovimiento) {
        this.tipoMovimiento = tipoMovimiento;
        calcularSubtotal();
    }

    public int getCantidad() { return cantidad; }
    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
        calcularSubtotal();
    }

    public BigDecimal getPrecioUnitario() { return precioUnitario; }
    public void setPrecioUnitario(BigDecimal precioUnitario) {
        this.precioUnitario = precioUnitario;
        calcularSubtotal();
    }

    public BigDecimal getSubtotal() { return subtotal; }
    public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }

    public String getMotivo() { return motivo; }
    public void setMotivo(String motivo) { this.motivo = motivo; }

    public int getUsuarioId() { return usuarioId; }
    public void setUsuarioId(int usuarioId) { this.usuarioId = usuarioId; }

    public String getUsuarioNombre() { return usuarioNombre; }
    public void setUsuarioNombre(String usuarioNombre) { this.usuarioNombre = usuarioNombre; }

    public LocalDateTime getFechaMovimiento() { return fechaMovimiento; }
    public void setFechaMovimiento(LocalDateTime fechaMovimiento) { this.fechaMovimiento = fechaMovimiento; }

    public String getReferencia() { return referencia; }
    public void setReferencia(String referencia) { this.referencia = referencia; }

    private void calcularSubtotal(){
        if (precioUnitario != null && cantidad > 0){
            this.subtotal = precioUnitario.multiply(new BigDecimal(cantidad));
        }
    }
}

