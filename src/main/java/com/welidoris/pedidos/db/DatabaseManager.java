package com.welidoris.pedidos.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DatabaseManager {

    // --- Configuración de la base de datos H2 ---
    private static final String JDBC_URL = "jdbc:h2:~/pedidos_db";
    private static final String JDBC_USER = "sa";
    private static final String JDBC_PASSWORD = "";
    private static final Logger LOGGER = Logger.getLogger(DatabaseManager.class.getName());

    // Obtiene una conexión a la base de datos
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(JDBC_URL, JDBC_USER, JDBC_PASSWORD);
    }

    // Inicializa la base de datos creando las tablas
    public static void initializeDatabase() {
        createTables();
        // Nota: El método inicializarDatos() no está definido en el código original,
        // por lo que no lo incluiremos en esta versión.
    }
    
    // Crea todas las tablas necesarias si no existen, siguiendo el diseño propuesto
    public static void createTables() {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            // Tabla 1: menu_items - Ahora incluye 'imagen' y un tipo de ENUM para ser más flexible
            String createMenuItemsTable = "CREATE TABLE IF NOT EXISTS menu_items (" +
                                          "id INT AUTO_INCREMENT PRIMARY KEY," +
                                          "nombre VARCHAR(255) NOT NULL UNIQUE," +
                                          "imagen VARCHAR(255)," +
                                          "tipo ENUM('PRODUCTO', 'PROMOCION') NOT NULL," +
                                          "tiene_tamanos BOOLEAN NOT NULL" +
                                          ");";
            stmt.execute(createMenuItemsTable);

            // Tabla 2: precios - Mantiene la misma estructura
            String createPreciosTable = "CREATE TABLE IF NOT EXISTS precios (" +
                                        "id INT AUTO_INCREMENT PRIMARY KEY," +
                                        "menu_item_id INT," +
                                        "tamano VARCHAR(50) NOT NULL," +
                                        "precio DECIMAL(10, 2) NOT NULL," +
                                        "FOREIGN KEY (menu_item_id) REFERENCES menu_items(id) ON DELETE CASCADE," +
                                        "UNIQUE (menu_item_id, tamano)" +
                                        ");";
            stmt.execute(createPreciosTable);

            // Tabla 3: pedidos - La tabla principal para los pedidos
            String createPedidosTable = "CREATE TABLE IF NOT EXISTS pedidos (" +
                                        "id INT AUTO_INCREMENT PRIMARY KEY," +
                                        "fecha TIMESTAMP NOT NULL," +
                                        "nombre_cliente VARCHAR(255)," +
                                        "pagado BOOLEAN NOT NULL DEFAULT FALSE," +
                                        "metodo_pago VARCHAR(50)," +
                                        "total DECIMAL(10, 2) NOT NULL DEFAULT 0.00" +
                                        ");";
            stmt.execute(createPedidosTable);
            
            // Tabla 4: pedidos_items - La tabla de unión para los ítems de un pedido
            String createPedidosItemsTable = "CREATE TABLE IF NOT EXISTS pedidos_items (" +
                                             "id INT AUTO_INCREMENT PRIMARY KEY," +
                                             "pedido_id INT," +
                                             "menu_item_id INT," +
                                             "tamano VARCHAR(50)," +
                                             "precio_unitario DECIMAL(10, 2) NOT NULL," +
                                             "cantidad INT NOT NULL," +
                                             "FOREIGN KEY (pedido_id) REFERENCES pedidos(id) ON DELETE CASCADE," +
                                             "FOREIGN KEY (menu_item_id) REFERENCES menu_items(id) ON DELETE CASCADE" +
                                             ");";
            stmt.execute(createPedidosItemsTable);

            LOGGER.log(Level.INFO, "Tablas creadas exitosamente.");
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al crear las tablas: " + e.getMessage(), e);
        }
    }

    // Método para guardar un nuevo pedido general y obtener su ID
    public static int saveNewPedido(String nombreCliente, boolean pagado, String metodoPago) {
        String sql = "INSERT INTO pedidos (fecha, nombre_cliente, pagado, metodo_pago) VALUES (NOW(), ?, ?, ?);";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            pstmt.setString(1, nombreCliente);
            pstmt.setBoolean(2, pagado);
            pstmt.setString(3, metodoPago);
            
            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        return rs.getInt(1);
                    }
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al guardar el nuevo pedido", e);
        }
        return -1;
    }

    // Nuevo método para guardar un PedidoItem en la base de datos
    // Este método es la versión correcta de lo que intentabas hacer
    public static void savePedidoItem(int pedidoId, int menuItemId, int cantidad, String tamano) {
        String sql = "INSERT INTO pedidos_items(pedido_id, menu_item_id, tamano, precio_unitario, cantidad) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            double precioUnitario = getPrecioUnitario(menuItemId, tamano);
            
            pstmt.setInt(1, pedidoId);
            pstmt.setInt(2, menuItemId);
            pstmt.setString(3, tamano);
            pstmt.setDouble(4, precioUnitario);
            pstmt.setInt(5, cantidad);
            
            pstmt.executeUpdate();
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al guardar el ítem del pedido", e);
        }
    }

    // Nuevo método auxiliar para obtener el precio unitario de un ítem del menú
    // Esto asegura que el precio unitario guardado sea correcto y esté en la base de datos
    private static double getPrecioUnitario(int menuItemId, String tamano) throws SQLException {
        String sql = "SELECT precio FROM precios WHERE menu_item_id = ? AND tamano = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, menuItemId);
            pstmt.setString(2, tamano);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble("precio");
                }
            }
        }
        throw new SQLException("Precio no encontrado para el ítem " + menuItemId + " y tamaño " + tamano);
    }
    
    // Método para actualizar el estado de completado de un pedido
    // Nota: la columna 'completado' no existía en el diseño, se ha eliminado para ser coherente.
    // Si la necesitas, deberías agregarla en la tabla 'pedidos'.
    // Los métodos 'updateMetodoPago' y 'updateEstadoPago' son correctos en su lógica,
    // pero necesitan la tabla 'pedidos' creada para funcionar.
    
    public static void updateMetodoPago(int pedidoId, String metodoPago) {
        String sql = "UPDATE pedidos SET metodo_pago = ? WHERE id = ?;";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, metodoPago);
            pstmt.setInt(2, pedidoId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al actualizar el método de pago del pedido", e);
        }
    }
    
    public static void updateEstadoPago(int pedidoId, boolean pagado) {
        String sql = "UPDATE pedidos SET pagado = ? WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setBoolean(1, pagado);
            pstmt.setInt(2, pedidoId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al actualizar el estado de pago del pedido", e);
        }
    }

    // Nuevo método para actualizar el total del pedido
    public static void updatePedidoTotal(int pedidoId, double total) {
        String sql = "UPDATE pedidos SET total = ? WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setDouble(1, total);
            pstmt.setInt(2, pedidoId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al actualizar el total del pedido", e);
        }
    }
}
