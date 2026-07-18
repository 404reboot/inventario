package modelo;

import java.math.BigDecimal;

/**
 * Modelo (entidad) que representa un producto del inventario.
 * <p>
 * Es un JavaBean simple usado para transportar la información de un
 * producto entre la capa de acceso a datos ({@link ProductDAO}), los
 * controladores y las vistas JavaFX. Incluye tanto los datos propios
 * del producto (código, nombre, precios, stock, etc.) como el campo
 * derivado {@link #categoriaNombre}, útil para mostrar el nombre de la
 * categoría cuando el producto se obtiene mediante un JOIN con la tabla
 * de categorías.
 */
public class Product {

        // Atributos privados
        /** Identificador único del producto (clave primaria en la base de datos). */
        private int id;
        /** Código o SKU único que identifica al producto. */
        private String codigo;
        /** Nombre descriptivo del producto. */
        private String nombre;
        /** Descripción detallada del producto. */
        private String descripcion;
        /** Identificador de la categoría a la que pertenece el producto. */
        private int categoriaId;
        /** Nombre de la categoría asociada; se completa al hacer JOIN con la tabla de categorías. */
        private String categoriaNombre; // <-- AGREGADO: Para almacenar el nombre de la categoría en los JOINs
        /** Precio de compra (costo) del producto. */
        private BigDecimal precioCompra;
        /** Precio de venta al público del producto. */
        private BigDecimal precioVenta;
        /** Cantidad actual de unidades disponibles en inventario. */
        private int stock;
        /** Cantidad mínima de stock antes de considerarse en nivel bajo. */
        private int stockMinimo;
        /** Nombre del proveedor del producto. */
        private String proveedor;
        /** Ubicación física del producto en el almacén. */
        private String ubicacion;

        /**
         * Constructor vacío requerido para instanciar el bean (por ejemplo,
         * antes de completar sus datos mediante los setters o al mapear
         * resultados de una consulta SQL).
         */
        public Product() {}

        /**
         * Construye un producto con todos sus atributos principales.
         *
         * @param id identificador único del producto
         * @param codigo código o SKU del producto
         * @param nombre nombre del producto
         * @param descripcion descripción del producto
         * @param categoriaId identificador de la categoría asociada
         * @param precioCompra precio de compra del producto
         * @param precioVenta precio de venta del producto
         * @param stock cantidad actual en inventario
         * @param stockMinimo cantidad mínima permitida en inventario
         * @param proveedor nombre del proveedor
         * @param ubicacion ubicación física del producto
         */
        public Product(int id, String codigo, String nombre, String descripcion, int categoriaId, BigDecimal precioCompra,
                       BigDecimal precioVenta, int stock, int stockMinimo, String proveedor, String ubicacion) {

            // Inicializacion de atributos
            this.id = id;
            this.codigo = codigo;
            this.nombre = nombre;
            this.descripcion = descripcion;
            this.categoriaId = categoriaId;
            this.precioCompra = precioCompra;
            this.precioVenta = precioVenta;
            this.stock = stock;
            this.stockMinimo = stockMinimo;
            this.proveedor = proveedor;
            this.ubicacion = ubicacion;
        }

        // Getter y Setter de id
        /** @return el identificador único del producto */
        public int getId() {
            return id;
        }

        /** @param id nuevo identificador único del producto */
        public void setId(int id) {
            this.id = id;
        }

        // Getter y Setter de codigo
        /** @return el código (SKU) del producto */
        public String getCodigo() {
            return codigo;
        }

        /** @param codigo nuevo código (SKU) del producto */
        public void setCodigo(String codigo) {
            this.codigo = codigo;
        }

        // Getter y Setter de nombre
        /** @return el nombre del producto */
        public String getNombre() {
            return nombre;
        }

        /** @param nombre nuevo nombre del producto */
        public void setNombre(String nombre) {
            this.nombre = nombre;
        }

        // Getter y Setter de descripcion
        /** @return la descripción del producto */
        public String getDescripcion() {
            return descripcion;
        }

        /** @param descripcion nueva descripción del producto */
        public void setDescripcion(String descripcion) {
            this.descripcion = descripcion;
        }

        // Getter y Setter de categoriaId
        /** @return el identificador de la categoría asociada al producto */
        public int getCategoriaId() {
            return categoriaId;
        }

        /** @param categoriaId nuevo identificador de categoría */
        public void setCategoriaId(int categoriaId) {
            this.categoriaId = categoriaId;
        }

        // AGREGADO: Getter y Setter de categoriaNombre
        /** @return el nombre de la categoría asociada (obtenido vía JOIN) */
        public String getCategoriaNombre() {
            return categoriaNombre;
        }

        /** @param categoriaNombre nuevo nombre de categoría a asociar */
        public void setCategoriaNombre(String categoriaNombre) {
            this.categoriaNombre = categoriaNombre;
        }

        // Getter y Setter de precioCompra
        /** @return el precio de compra del producto */
        public BigDecimal getPrecioCompra() {
            return precioCompra;
        }

        /** @param precioCompra nuevo precio de compra */
        public void setPrecioCompra(BigDecimal precioCompra) {
            this.precioCompra = precioCompra;
        }

        // Getter y Setter de precioVenta
        /** @return el precio de venta del producto */
        public BigDecimal getPrecioVenta() {
            return precioVenta;
        }

        /** @param precioVenta nuevo precio de venta */
        public void setPrecioVenta(BigDecimal precioVenta) {
            this.precioVenta = precioVenta;
        }

        // Getter y Setter de stock
        /** @return la cantidad actual en inventario */
        public int getStock() {
            return stock;
        }

        /** @param stock nueva cantidad en inventario */
        public void setStock(int stock) {
            this.stock = stock;
        }

        // Getter y Setter de stockMinimo
        /** @return la cantidad mínima permitida en inventario */
        public int getStockMinimo() {
            return stockMinimo;
        }

        /** @param stockMinimo nueva cantidad mínima en inventario */
        public void setStockMinimo(int stockMinimo) {
            this.stockMinimo = stockMinimo;
        }

        // Getter y Setter de proveedor
        /** @return el nombre del proveedor del producto */
        public String getProveedor() {
            return proveedor;
        }

        /** @param proveedor nuevo nombre de proveedor */
        public void setProveedor(String proveedor) {
            this.proveedor = proveedor;
        }

        // Getter y Setter de ubicacion
        /** @return la ubicación física del producto */
        public String getUbicacion() {
            return ubicacion;
        }

        /** @param ubicacion nueva ubicación física del producto */
        public void setUbicacion(String ubicacion) {
            this.ubicacion = ubicacion;
        }
}
