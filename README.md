# Sistema de Gestión de Inventario 📦

Un sistema de escritorio moderno desarrollado en **JavaFX** y enfocado en el control integral de inventarios empresariales. Permite la administración de productos, categorización de mercancía, registro transaccional de movimientos (entradas y salidas) y generación de reportes analíticos avanzados en formatos PDF y Excel.

## 🚀 Características Principales

- **Gestión de Catálogo**: Módulo completo (CRUD) para productos y categorías.
- **Control de Movimientos**: Registro de abastecimiento (Entradas) y despacho (Salidas). Incluye bloqueo inteligente a nivel de base de datos para prevenir situaciones de inventario o stock negativo.
- **Reportes y Analítica**: Exportación en formato PDF y Excel (mediante *iText* y *Apache POI*) para:
  - Nivel de stock actual.
  - Historial de movimientos filtrado por fechas.
  - Alertas de productos con stock mínimo.
  - Análisis de productos de mayor rotación (Top N).
- **Seguridad**: Autenticación de usuarios para control de acceso al sistema y rastreo de quién ejecuta cada transacción de inventario.
- **Interfaz Moderna**: Experiencia de usuario fluida desarrollada completamente bajo JavaFX y estilizada nativamente.

## 🛠️ Stack Tecnológico

- **Lenguaje**: Java 21
- **UI Framework**: JavaFX 21
- **Gestión de Dependencias**: Maven
- **Base de Datos**: MySQL / MariaDB (Driver Connector/J)
- **Librerías Adicionales**:
  - `itextpdf`: Generación de reportes PDF.
  - `org.apache.poi`: Exportación a hojas de cálculo de Microsoft Excel.

## ⚙️ Instalación y Configuración

Sigue estos pasos para clonar, configurar y ejecutar el proyecto en tu entorno de desarrollo local.

### 1. Requisitos Previos
- **Java Development Kit (JDK) 21** o superior.
- **Apache Maven 3.8+** instalado y configurado en el `PATH`.
- Un servidor local de base de datos **MySQL** (por ejemplo, mediante XAMPP).

### 2. Configurar la Base de Datos
1. Inicia tu servidor local MySQL (en el puerto 3306).
2. Crea una base de datos llamada `inventario_db`.
3. Importa el archivo `schema.sql` (incluido en este repositorio) mediante phpMyAdmin o vía consola:
   ```bash
   mysql -u root < schema.sql
   ```
   *(Este script inicializará las tablas necesarias, la estructura relacional e insertará datos y un usuario de prueba)*.
4. Las credenciales de conexión por defecto se asumen como usuario `root` sin contraseña. Si utilizas otras credenciales, actualízalas en la clase `conexionDB.java`.

### 3. Compilar y Ejecutar

Abre la terminal en la raíz del proyecto y descarga las dependencias ejecutando:
```bash
mvn clean compile
```

Una vez que Maven termine de empaquetar el proyecto, arranca el sistema con:
```bash
mvn javafx:run
```

### 4. Credenciales de Acceso (Test)
El script SQL provee automáticamente una cuenta para evaluar el sistema:
- **Usuario**: `admin`
- **Contraseña**: `admin123`

## 👨‍💻 Autor
Desarrollado por **neovacode**.
