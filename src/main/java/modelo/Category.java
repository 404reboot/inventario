package modelo;

/**
 * Modelo (entidad) que representa una categoría de productos.
 * <p>
 * JavaBean simple utilizado por {@link CategoryDAO} para persistir y
 * recuperar categorías, y por los controladores/vistas JavaFX (por
 * ejemplo dentro de un {@code ComboBox}) para asociar productos a una
 * categoría.
 */
public class Category {

        // Atributos privados
        /** Identificador único de la categoría (clave primaria en la base de datos). */
        private int id;
        /** Nombre de la categoría. */
        private String nombre;
        /** Descripción de la categoría. */
        private String descripcion;

        //bod
        /** Constructor vacío requerido para instanciar el bean. */
        public Category() {}

        /**
         * Construye una categoría con todos sus atributos.
         *
         * @param id identificador único de la categoría
         * @param nombre nombre de la categoría
         * @param descripcion descripción de la categoría
         */
        public Category(int id, String nombre, String descripcion) {
            // Inicializacion de atributos
            this.id = id;
            this.nombre = nombre;
            this.descripcion = descripcion;
        }

        // Getter y Setter de id
        /** @return el identificador único de la categoría */
        public int getId() {
            return id;
        }

        /** @param id nuevo identificador único de la categoría */
        public void setId(int id) {
            this.id = id;
        }

        // Getter y Setter de nombre
        /** @return el nombre de la categoría */
        public String getNombre() {
            return nombre;
        }

        /** @param nombre nuevo nombre de la categoría */
        public void setNombre(String nombre) {
            this.nombre = nombre;
        }

        // Getter y Setter de descripcion
        /** @return la descripción de la categoría */
        public String getDescripcion() {
            return descripcion;
        }

        /** @param descripcion nueva descripción de la categoría */
        public void setDescripcion(String descripcion) {
            this.descripcion = descripcion;
        }

        // AGREGADO: Para que el ComboBox dibuje correctamente el texto en la interfaz
        /**
         * Representación en texto de la categoría, utilizada por controles
         * JavaFX (como {@code ComboBox}) para mostrar el nombre de la
         * categoría en lugar de la referencia del objeto.
         *
         * @return el {@link #nombre} de la categoría, o cadena vacía si es {@code null}
         */
        @Override
        public String toString() {
            return this.nombre != null ? this.nombre : "";
        }
    }
