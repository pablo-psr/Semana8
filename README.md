
# SpeedFast – Gestión de Entregas

Aplicación de escritorio en **Java (Swing)** con base de datos **MySQL** (conexión por **JDBC**) para administrar repartidores, pedidos y las entregas que asignan un pedido a un repartidor. Todo se maneja desde una sola ventana con tres secciones.

## Funcionalidades

- **Repartidores:** crear, listar, actualizar y eliminar.
- **Pedidos:** crear, listar, actualizar y eliminar. Cada pedido tiene dirección, tipo (`COMIDA`, `ENCOMIENDA`, `EXPRESS`) y estado (`PENDIENTE`, `EN_REPARTO`, `ENTREGADO`).
- **Entregas:** asignar un pedido a un repartidor (se guarda la fecha y hora actuales) y eliminar entregas.
- **Reglas de negocio:**
  - Un repartidor no puede tener dos pedidos en curso a la vez. Para asignarle uno nuevo, el pedido anterior debe estar `ENTREGADO`.
  - Al asignar un pedido `PENDIENTE`, su estado pasa automáticamente a `EN_REPARTO`.
  - Validación de largo: nombre entre 2 y 100 caracteres, dirección entre 5 y 100.
- Confirmación al eliminar registros y al cerrar la aplicación.
- Manejo de errores de base de datos con mensajes en pantalla.

## Estructura del proyecto

```
cl.duoc
├── data
│   ├── ConexionDB.java       # Conexión JDBC a MySQL
│   ├── RepartidorDAO.java    # CRUD de repartidores
│   ├── PedidoDAO.java        # CRUD de pedidos (incluye lectura filtrada)
│   └── EntregaDAO.java       # CRUD de entregas (incluye lectura filtrada y repartidorOcupado)
├── model
│   ├── Repartidor.java
│   ├── Pedido.java
│   └── Entrega.java
└── ui
    └── MainFrame.java        # Interfaz gráfica (punto de entrada: main)
```

Los DAO usan `PreparedStatement`, `ResultSet` y `try-with-resources` para cerrar los recursos automáticamente.

## Modelo de datos

```
repartidores (id, nombre)
pedidos      (id, direccion, tipo, estado)
entregas     (id, id_pedido → pedidos.id, id_repartidor → repartidores.id, fecha, hora)
```

Las claves foráneas de `entregas` usan `ON DELETE CASCADE`: si se elimina un pedido o un repartidor, sus entregas asociadas también se eliminan.

## Requisitos

- JDK 8 o superior
- MySQL Server 5.7 / 8.x
- Driver **MySQL Connector/J** agregado a las librerías del proyecto
- *(Opcional)* **FlatLaf** para el estilo visual moderno. Si no está disponible, la aplicación usa el estilo del sistema.

## Instalación y ejecución

### 1. Crear la base de datos

Ejecuta el siguiente script en MySQL Workbench (o desde la consola de MySQL). Crea la base `Speedfast_db`, las tablas y algunos datos de prueba:

```sql
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
```

> **Ojo:** el script hace `DROP TABLE` de las tres tablas, así que borra los datos existentes cada vez que se ejecuta.

### 2. Configurar la conexión

Abre `cl/duoc/data/ConexionDB.java` y ajusta los datos según tu instalación local de MySQL:

```java
private static final String URL = "jdbc:mysql://localhost:3306/Speedfast_db";
private static final String USER = "root";
private static final String PASSWORD = "tu_contraseña";
```

### 3. Agregar las librerías

Agrega al proyecto (NetBeans, IntelliJ, Eclipse, etc.) el `.jar` de MySQL Connector/J y, si lo deseas, el de FlatLaf.

### 4. Ejecutar

Ejecuta la clase `cl.duoc.ui.MainFrame`, que contiene el método `main`.

## Cómo usar la aplicación

1. **Repartidores:** escribe un nombre y pulsa *Crear*. Para editar o eliminar, selecciona una fila de la tabla, modifica el nombre y pulsa *Actualizar* o *Eliminar*.
2. **Pedidos:** completa dirección, tipo y estado, y pulsa *Crear*. Al seleccionar una fila, el formulario se llena para poder actualizarla o eliminarla.
3. **Asignación de entregas:** elige un pedido y un repartidor en los selectores y pulsa *Asignar Pedido*. Si el repartidor ya tiene un pedido en curso, la aplicación lo avisa.
4. **Liberar a un repartidor:** cambia el estado del pedido a `ENTREGADO` desde la sección Pedidos; así el repartidor queda disponible para una nueva asignación.

## Tecnologías

Java · Swing · JDBC · MySQL · Patrón DAO
