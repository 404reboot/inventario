package Model;

/**
 * User
 */
public class User {

    private int id;
    private String username;
    private String password;
    private String nombre_completo;
    private String rol;

    public User() {}

    public User(int id, String username, String password, String nombre_completo, String rol) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.nombre_completo = nombre_completo;
        this.rol = rol;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getNombre_completo() {
        return nombre_completo;
    }

    public void setNombre_completo(String nombre_completo) {
        this.nombre_completo = nombre_completo;
    }

    public String getRol() {
        return rol;
    }

    public void setRol(String rol) {
        this.rol = rol;
    } 
}
