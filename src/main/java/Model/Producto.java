package Model;

import java.math.BigDecimal;

/**
 * Producto
 */
public class Producto {

    private int id;
    private String codigo;
    private String nombre;
    private String description;
    private int categoria_id;
    private BigDecimal precio_compra;
    private BigDecimal precio_venta;
    private int stock;
    private int stock_minimo;
    private String proveedor;
    private String ubicacion;

    public Producto() {
    }

    public Producto(
            int id,
            String codigo,
            String nombre,
            String description,
            int categoria_id,
            BigDecimal precio_compra,
            BigDecimal precio_venta,
            int stock,
            int stock_minimo,
            String proveedor,
            String ubicacion) {
        this.id = id;
        this.codigo = codigo;
        this.nombre = nombre;
        this.description = description;
        this.categoria_id = categoria_id;
        this.precio_compra = precio_compra;
        this.precio_venta = precio_venta;
        this.stock = stock;
        this.stock_minimo = stock_minimo;
        this.proveedor = proveedor;
        this.ubicacion = ubicacion;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getCategoria_id() {
        return categoria_id;
    }

    public void setCategoria_id(int categoria_id) {
        this.categoria_id = categoria_id;
    }

    public BigDecimal getPrecio_compra() {
        return precio_compra;
    }

    public void setPrecio_compra(BigDecimal precio_compra) {
        this.precio_compra = precio_compra;
    }

    public BigDecimal getPrecio_venta() {
        return precio_venta;
    }

    public void setPrecio_venta(BigDecimal precio_venta) {
        this.precio_venta = precio_venta;
    }

    public int getStock() {
        return stock;
    }

    public void setStock(int stock) {
        this.stock = stock;
    }

    public int getStock_minimo() {
        return stock_minimo;
    }

    public void setStock_minimo(int stock_minimo) {
        this.stock_minimo = stock_minimo;
    }

    public String getProveedor() {
        return proveedor;
    }

    public void setProveedor(String proveedor) {
        this.proveedor = proveedor;
    }

    public String getUbicacion() {
        return ubicacion;
    }

    public void setUbicacion(String ubicacion) {
        this.ubicacion = ubicacion;
    }

}
