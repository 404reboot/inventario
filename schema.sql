-- Script de inicialización de la Base de Datos para el Sistema de Gestión de Inventario
-- Ejecutar en phpMyAdmin o la consola de MariaDB en XAMPP

-- Crear base de datos
CREATE DATABASE IF NOT EXISTS inventario_db;
USE inventario_db;

-- 1. Tabla de Usuarios
CREATE TABLE IF NOT EXISTS usuarios (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    nombre_completo VARCHAR(100) NOT NULL,
    rol VARCHAR(50) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 2. Tabla de Categorías
CREATE TABLE IF NOT EXISTS categorias (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    descripcion TEXT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 3. Tabla de Productos
CREATE TABLE IF NOT EXISTS productos (
    id INT AUTO_INCREMENT PRIMARY KEY,
    codigo VARCHAR(50) UNIQUE NOT NULL,
    nombre VARCHAR(100) NOT NULL,
    descripcion TEXT,
    categoria_id INT,
    precio_compra DECIMAL(10, 2) NOT NULL,
    precio_venta DECIMAL(10, 2) NOT NULL,
    stock INT NOT NULL DEFAULT 0,
    stock_minimo INT NOT NULL DEFAULT 0,
    proveedor VARCHAR(100),
    ubicacion VARCHAR(100),
    FOREIGN KEY (categoria_id) REFERENCES categorias(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 4. Tabla de Movimientos (Entradas y Salidas)
CREATE TABLE IF NOT EXISTS movimientos (
    id INT AUTO_INCREMENT PRIMARY KEY,
    producto_id INT,
    tipo_movimiento VARCHAR(20) NOT NULL, -- 'ENTRADA' o 'SALIDA'
    cantidad INT NOT NULL,
    precio_unitario DECIMAL(10, 2) NOT NULL,
    motivo VARCHAR(255),
    usuario_id INT,
    referencia VARCHAR(100),
    fecha_movimiento TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (producto_id) REFERENCES productos(id) ON DELETE CASCADE,
    FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -----------------------------------------------------
-- Datos de prueba iniciales
-- -----------------------------------------------------

-- Insertar usuario administrador por defecto (Usuario: admin, Clave: admin123)
INSERT INTO usuarios (username, password, nombre_completo, rol) 
VALUES ('admin', 'admin123', 'Administrador del Sistema', 'Administrador')
ON DUPLICATE KEY UPDATE username=username;

-- Insertar categorías por defecto
INSERT INTO categorias (nombre, descripcion) VALUES
('Electrónica', 'Dispositivos electrónicos, componentes y accesorios de computación'),
('Papelería', 'Útiles de oficina, hojas, archivadores y materiales de escritura'),
('Herramientas', 'Herramientas manuales y eléctricas de ferretería');

-- Insertar productos de prueba (Stock inicial)
-- Nota: Asegurar que el categoria_id corresponda a los IDs insertados previamente
INSERT INTO productos (codigo, nombre, descripcion, categoria_id, precio_compra, precio_venta, stock, stock_minimo, proveedor, ubicacion) VALUES
('PROD001', 'Mouse Inalámbrico Logitech', 'Mouse óptico con receptor USB Nano M185', 1, 10.50, 19.99, 50, 5, 'Logitech S.A.', 'Pasillo A - Estantería 2'),
('PROD002', 'Teclado Mecánico Redragon', 'Teclado mecánico retroiluminado RGB Kumara K552', 1, 25.00, 45.00, 30, 8, 'Redragon Latam', 'Pasillo A - Estantería 1'),
('PROD003', 'Paquete de Hojas Resma A4', 'Resma de 500 hojas blancas de 80g Chamex', 2, 3.20, 5.50, 100, 15, 'Distribuidora Papelera', 'Pasillo B - Estantería 3'),
('PROD004', 'Taladro Percutor Bosch', 'Taladro percutor eléctrico GSB 13 RE Professional', 3, 45.00, 79.99, 10, 2, 'Bosch Ferreterías', 'Pasillo C - Estantería 1');
