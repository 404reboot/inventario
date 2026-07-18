package modelo;

/**
 * Modelo (entidad) que representa a un usuario del sistema.
 * <p>
 * JavaBean simple utilizado por {@link UserDAO} para la autenticación
 * (login) y gestión de usuarios, incluyendo sus credenciales y el rol
 * asignado dentro de la aplicación.
 */
public class User {

    // Atributos privados
    /** Identificador único del usuario (clave primaria en la base de datos). */
    private int id;
    /** Nombre de usuario utilizado para iniciar sesión. */
    private String username;
    /** Contraseña del usuario. */
    private String password;
    /** Nombre completo del usuario. */
    private String nombre_completo;
    /** Rol del usuario dentro del sistema (por ejemplo, administrador u operador). */
    private String rol;



    /** Constructor vacío requerido para instanciar el bean. */
    public User() {}

    /**
     * Construye un usuario con todos sus atributos.
     *
     * @param id identificador único del usuario
     * @param username nombre de usuario para iniciar sesión
     * @param password contraseña del usuario
     * @param nombre_completo nombre completo del usuario
     * @param rol rol asignado al usuario dentro del sistema
     */
    public User(int id, String username, String password, String nombre_completo, String rol) {

        // Inicializacion de atributos
        this.id = id;
        this.username = username;
        this.password = password;
        this.nombre_completo = nombre_completo;
        this.rol = rol;
    }

    // Getter y Setter de id
    /** @return el identificador único del usuario */
    public int getId() {
        return id;
    }

    /** @param id nuevo identificador único del usuario */
    public void setId(int id) {
        this.id = id;
    }

    // Getter y Setter de username
    /** @return el nombre de usuario */
    public String getUsername() {
        return username;
    }

    /** @param username nuevo nombre de usuario */
    public void setUsername(String username) {
        this.username = username;
    }

    // Getter y Setter de password
    /** @return la contraseña del usuario */
    public String getPassword() {
        return password;
    }

    /** @param password nueva contraseña del usuario */
    public void setPassword(String password) {
        this.password = password;
    }

    // Getter y Setter de nombre completo
    /** @return el nombre completo del usuario */
    public String getNombre_completo() {
        return nombre_completo;
    }

    /** @param nombre_completo nuevo nombre completo del usuario */
    public void setNombre_completo(String nombre_completo) {
        this.nombre_completo = nombre_completo;
    }

    // Getter y Setter de rol
    /** @return el rol del usuario dentro del sistema */
    public String getRol() {
        return rol;
    }

    /** @param rol nuevo rol del usuario dentro del sistema */
    public void setRol(String rol) {
        this.rol = rol;
    }
}
