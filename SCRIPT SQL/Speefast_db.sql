-- 1. Crear la base de datos si no existe
CREATE DATABASE IF NOT EXISTS Speedfast_db;
USE Speedfast_db;

-- 2. Eliminar tablas previas si existen (en orden por claves foráneas)
DROP TABLE IF EXISTS entregas;
DROP TABLE IF EXISTS pedidos;
DROP TABLE IF EXISTS repartidores;

-- 3. Tabla de Repartidores
CREATE TABLE repartidores (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL
);

-- 4. Tabla de Pedidos
CREATE TABLE pedidos (
    id INT AUTO_INCREMENT PRIMARY KEY,
    direccion VARCHAR(100) NOT NULL,
    tipo VARCHAR(50) NOT NULL,    -- COMIDA, ENCOMIENDA, EXPRESS
    estado VARCHAR(50) NOT NULL   -- PENDIENTE, EN_REPARTO, ENTREGADO
);

-- 5. Tabla de Entregas (Relaciona Pedidos y Repartidores)
CREATE TABLE entregas (
    id INT AUTO_INCREMENT PRIMARY KEY,
    id_pedido INT NOT NULL,
    id_repartidor INT NOT NULL,
    fecha DATE NOT NULL,
    hora TIME NOT NULL,
    FOREIGN KEY (id_pedido) REFERENCES pedidos(id) ON DELETE CASCADE,
    FOREIGN KEY (id_repartidor) REFERENCES repartidores(id) ON DELETE CASCADE
);
select * from repartidores;
-- --------------------------------------------------------
-- DATOS DE PRUEBA (Opcional)
-- --------------------------------------------------------

INSERT INTO repartidores (nombre) VALUES 
('Juan Pérez'),
('María González');

INSERT INTO pedidos (direccion, tipo, estado) VALUES 
('Av. Italia 1234, Providencia', 'COMIDA', 'PENDIENTE'),
('Calle Los Leones 567, Providencia', 'ENCOMIENDA', 'PENDIENTE');