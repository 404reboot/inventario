/**
 * Capa de modelo y acceso a datos del sistema de inventario.
 * <p>
 * Incluye las entidades (JavaBeans) que representan los objetos del
 * dominio ({@link modelo.Product}, {@link modelo.Category},
 * {@link modelo.User}, {@link modelo.InventoryMovement}), los Data
 * Access Objects (DAO) responsables de las operaciones CRUD contra la
 * base de datos MySQL ({@link modelo.ProductDAO}, {@link modelo.CategoryDAO},
 * {@link modelo.UserDAO}, {@link modelo.InventoryMovementDAO}), la
 * utilidad de conexión ({@link modelo.conexionDB}) y el servicio de
 * generación de reportes en PDF/Excel ({@link modelo.ReportService}).
 */
package modelo;
