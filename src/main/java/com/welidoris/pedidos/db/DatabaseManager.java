package com.welidoris.pedidos.db;

import com.welidoris.pedidos.models.MenuItem;
import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class DatabaseManager {

    private static final String USER_HOME = System.getProperty("user.home");
    private static final String APP_DIR = USER_HOME + File.separator + "WeliDoris";
    private static final String DB_PATH = APP_DIR + File.separator + "pedidos_db";
    
    private static final String JDBC_URL = "jdbc:h2:file:" + DB_PATH;
    private static final String JDBC_USER = "sa";
    private static final String JDBC_PASSWORD = "";
    private static final Logger LOGGER = Logger.getLogger(DatabaseManager.class.getName());

    static {
        File dbDir = new File(APP_DIR);
        if (!dbDir.exists()) {
            dbDir.mkdirs();
            LOGGER.info(() -> "Se ha creado la carpeta para la base de datos: " + dbDir.getAbsolutePath());
        }
    }
    
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(JDBC_URL, JDBC_USER, JDBC_PASSWORD);
    }

    public static void initializeDatabase() {
        System.out.println("Iniciando la inicialización de la base de datos...");
        createTables();
        insertInitialData();
        System.out.println("Inicialización de la base de datos completa.");
    }
    
    public static void createTables() {
        System.out.println("Intentando crear tablas...");
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
                
            String createMenuItemsTable = "CREATE TABLE IF NOT EXISTS menu_items (" +
                                        "id INT AUTO_INCREMENT PRIMARY KEY," +
                                        "nombre VARCHAR(255) NOT NULL UNIQUE," +
                                        "imagen VARCHAR(255)," +
                                        "tipo ENUM('PRODUCTO', 'PROMOCION') NOT NULL," +
                                        "tiene_tamanos BOOLEAN NOT NULL" +
                                        ");";
            stmt.execute(createMenuItemsTable);

            String createPreciosTable = "CREATE TABLE IF NOT EXISTS precios (" +
                                        "id INT AUTO_INCREMENT PRIMARY KEY," +
                                        "menu_item_id INT," +
                                        "tamano VARCHAR(50) NOT NULL," +
                                        "precio DECIMAL(10, 2) NOT NULL," +
                                        "FOREIGN KEY (menu_item_id) REFERENCES menu_items(id) ON DELETE CASCADE," +
                                        "UNIQUE (menu_item_id, tamano)" +
                                        ");";
            stmt.execute(createPreciosTable);

            String createPedidosTable = "CREATE TABLE IF NOT EXISTS pedidos (" +
                                        "id INT AUTO_INCREMENT PRIMARY KEY," +
                                        "fecha TIMESTAMP NOT NULL," +
                                        "nombre_cliente VARCHAR(255)," +
                                        "pagado BOOLEAN NOT NULL DEFAULT FALSE," +
                                        "completado BOOLEAN NOT NULL DEFAULT FALSE," +
                                        "metodo_pago VARCHAR(50)," +
                                        "total DECIMAL(10, 2) NOT NULL DEFAULT 0.00," +
                                        "estado VARCHAR(50) NOT NULL DEFAULT 'GUARDADO'" +
                                        ");";
            stmt.execute(createPedidosTable);
            
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
            System.err.println("Error al crear las tablas: " + e.getMessage());
            LOGGER.log(Level.SEVERE, "Error al crear las tablas: " + e.getMessage(), e);
        }
    }
    
    private static void insertInitialData() {
        System.out.println("Intentando insertar datos iniciales...");
        try {
            if (getMenuItems().isEmpty()) {
                
                int salchipapasId = saveMenuItem(new MenuItem("Salchipapas", "salchipapas.jpeg", "PRODUCTO", true));
                savePrecio(salchipapasId, "Chica", 1500.0);
                savePrecio(salchipapasId, "Mediana", 2500.0);
                savePrecio(salchipapasId, "Grande", 3500.0);
                
                int completoId = saveMenuItem(new MenuItem("Completo", "completo.jpeg", "PRODUCTO", true));
                savePrecio(completoId, "Chico", 1600.0);
                savePrecio(completoId, "Grande", 2300.0);
                
                int completoItalianoId = saveMenuItem(new MenuItem("Completo Italiano", "completoItaliano.jpeg", "PRODUCTO", true));
                savePrecio(completoItalianoId, "Chico", 1800.0);
                savePrecio(completoItalianoId, "Grande", 2500.0);
                
                int completoDinamicoId = saveMenuItem(new MenuItem("Completo Dinamico", "completoDinamico.jpg", "PRODUCTO", true));
                savePrecio(completoDinamicoId, "Chico", 2000.0);
                savePrecio(completoDinamicoId, "Grande", 2700.0);
                
                int assQuesoId = saveMenuItem(new MenuItem("Ass Queso", "assQueso.jpg", "PRODUCTO", true));
                savePrecio(assQuesoId, "Chico", 2500.0);
                savePrecio(assQuesoId, "Grande", 3200.0);
                
                int assItalianoId = saveMenuItem(new MenuItem("Ass Italiano", "assitaliano.jpg", "PRODUCTO", true));
                savePrecio(assItalianoId, "Chico", 2800.0);
                savePrecio(assItalianoId, "Grande", 3500.0);
                
                int assDinamicoId = saveMenuItem(new MenuItem("Ass Dinamico", "assdinamico.png", "PRODUCTO", true));
                savePrecio(assDinamicoId, "Chico", 3000.0);
                savePrecio(assDinamicoId, "Grande", 3700.0);
                
                int papasFritasId = saveMenuItem(new MenuItem("Papas Fritas", "papasfritas.png", "PRODUCTO", true));
                savePrecio(papasFritasId, "Chica", 1000.0);
                savePrecio(papasFritasId, "Mediana", 2000.0);
                savePrecio(papasFritasId, "Grande", 3000.0);
                
                int churrascoId = saveMenuItem(new MenuItem("Churrasco", "churrasco.jpg", "PRODUCTO", false));
                savePrecio(churrascoId, "unico", 3500.0);
                
                int churrascoItalianoId = saveMenuItem(new MenuItem("Churrasco Italiano", "churrascoitaliano.jpeg", "PRODUCTO", false));
                savePrecio(churrascoItalianoId, "unico", 3500.0);
                
                int chacareroId = saveMenuItem(new MenuItem("Chacarero", "chacarero.jpg", "PRODUCTO", false));
                savePrecio(chacareroId, "unico", 4000.0);
                
                int promoChurrascoId = saveMenuItem(new MenuItem("Promoción Churrasco (2x)", "promocionchurrasco.jpeg", "PROMOCION", false));
                savePrecio(promoChurrascoId, "unico", 6000.0);
                
                int promoChurrascoItalianoId = saveMenuItem(new MenuItem("Promoción Churrasco Italiano 2x", "promocionchurrascoitaliano.jpeg", "PROMOCION", false));
                savePrecio(promoChurrascoItalianoId, "unico", 6000.0);
                
                int promoChacareroId = saveMenuItem(new MenuItem("Promoción Chacarero 2x", "promocionchacarero.jpg", "PROMOCION", false));
                savePrecio(promoChacareroId, "unico", 7000.0);
                
                int promoChurrascoMasPapasId = saveMenuItem(new MenuItem("Promoción Churrasco + Papas", "promocionchurrascopapas.png", "PROMOCION", false));
                savePrecio(promoChurrascoMasPapasId, "unico", 5000.0);

                System.out.println("Datos iniciales insertados exitosamente.");
            } else {
                System.out.println("Ya existen datos en el menú, no se insertarán datos iniciales.");
            }
        } catch (SQLException e) {
            System.err.println("Error al insertar datos iniciales: " + e.getMessage());
            LOGGER.log(Level.SEVERE, "Error al insertar datos iniciales", e);
        }
    }
    
    private static int saveMenuItem(MenuItem item) throws SQLException {
        String sql = "INSERT INTO menu_items (nombre, imagen, tipo, tiene_tamanos) VALUES (?, ?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            pstmt.setString(1, item.getNombre());
            pstmt.setString(2, item.getImagen());
            pstmt.setString(3, item.getTipo());
            pstmt.setBoolean(4, item.tieneTamanos());
            
            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        return rs.getInt(1);
                    }
                }
            }
        }
        return -1;
    }
    
    private static void savePrecio(int menuItemId, String tamano, double precio) throws SQLException {
        String sql = "INSERT INTO precios (menu_item_id, tamano, precio) VALUES (?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, menuItemId);
            pstmt.setString(2, tamano);
            pstmt.setDouble(3, precio);
            pstmt.executeUpdate();
        }
    }
    
    public static Map<String, Double> getPreciosForMenuItem(int menuItemId) throws SQLException {
        Map<String, Double> precios = new HashMap<>();
        String sql = "SELECT tamano, precio FROM precios WHERE menu_item_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, menuItemId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    precios.put(rs.getString("tamano"), rs.getDouble("precio"));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al obtener precios para el item " + menuItemId, e);
            throw e;
        }
        return precios;
    }
    
    public static List<MenuItem> getMenuItems() throws SQLException {
        List<MenuItem> items = new ArrayList<>();
        String sql = "SELECT * FROM menu_items";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                int id = rs.getInt("id");
                String nombre = rs.getString("nombre");
                String imagen = rs.getString("imagen");
                String tipo = rs.getString("tipo");
                boolean tieneTamanos = rs.getBoolean("tiene_tamanos");

                MenuItem item = new MenuItem(id, nombre, imagen, tipo, tieneTamanos);
                
                Map<String, Double> precios = getPreciosForMenuItem(id);
                item.setPrecios(precios);

                items.add(item);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al obtener los items del menu.", e);
            throw e;
        }
        return items;
    }
        
    public static String getNombreProducto(int menuItemId) throws SQLException {
        String nombre = "Producto Desconocido";
        String sql = "SELECT nombre FROM menu_items WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, menuItemId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    nombre = rs.getString("nombre");
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al obtener el nombre del producto con id: " + menuItemId, e);
            throw e;
        }
        return nombre;
    }
    
    public static int saveNewPedido(String nombreCliente, boolean pagado, String metodoPago) {
        String sql = "INSERT INTO pedidos (fecha, nombre_cliente, pagado, metodo_pago, total, estado) VALUES (?, ?, ?, ?, ?, ?);";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setTimestamp(1, new Timestamp(System.currentTimeMillis()));
            pstmt.setString(2, nombreCliente);
            pstmt.setBoolean(3, pagado);
            pstmt.setString(4, metodoPago);
            pstmt.setDouble(5, 0.0);
            pstmt.setString(6, "GUARDADO");
            
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
    
    public static void updateEstadoCompletado(int pedidoId, boolean completado) throws SQLException {
        String sql = "UPDATE pedidos SET completado = ? WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setBoolean(1, completado);
            pstmt.setInt(2, pedidoId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al actualizar el estado de completado del pedido: " + pedidoId, e);
            throw e;
        }
    }
    
    public static void eliminarPedido(int pedidoId) {
        String sql = "DELETE FROM pedidos WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, pedidoId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al eliminar el pedido", e);
        }
    }
    
    public static List<Map<String, Object>> getPedidosGuardados() throws SQLException {
        List<Map<String, Object>> pedidos = new ArrayList<>();
        String sqlPedidos = "SELECT id, nombre_cliente, pagado, completado, metodo_pago, total FROM pedidos WHERE completado = FALSE";

        try (Connection conn = getConnection();
             Statement stmtPedidos = conn.createStatement();
             ResultSet rsPedidos = stmtPedidos.executeQuery(sqlPedidos)) {

            while (rsPedidos.next()) {
                Map<String, Object> pedido = new HashMap<>();
                int pedidoId = rsPedidos.getInt("id");
                pedido.put("id", pedidoId);
                pedido.put("nombre_cliente", rsPedidos.getString("nombre_cliente"));
                pedido.put("pagado", rsPedidos.getBoolean("pagado"));
                pedido.put("completado", rsPedidos.getBoolean("completado"));
                pedido.put("metodo_pago", rsPedidos.getString("metodo_pago"));
                
                // CORREGIDO: Se cambia de getInt a getDouble para evitar el error de casteo
                pedido.put("total", rsPedidos.getDouble("total")); 

                String sqlItems = "SELECT id, menu_item_id, cantidad, tamano FROM pedidos_items WHERE pedido_id = ?;";
                List<Map<String, Object>> items = new ArrayList<>();
                try (PreparedStatement pstmtItems = conn.prepareStatement(sqlItems)) {
                    pstmtItems.setInt(1, pedidoId);
                    try (ResultSet rsItems = pstmtItems.executeQuery()) {
                        while (rsItems.next()) {
                            Map<String, Object> item = new HashMap<>();
                            int pedidoItemId = rsItems.getInt("id"); 
                            int menuItemId = rsItems.getInt("menu_item_id");
                            String nombreProducto = getNombreProducto(menuItemId);
                            item.put("id_pedido_item", pedidoItemId); 
                            item.put("nombre_producto", nombreProducto);
                            item.put("cantidad", rsItems.getInt("cantidad"));
                            item.put("tamano", rsItems.getString("tamano"));
                            items.add(item);
                        }
                    }
                }
                pedido.put("items", items);
                pedidos.add(pedido);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al cargar los pedidos guardados.", e);
            throw e;
        }
        return pedidos;
    }
    
    public static List<Map<String, Object>> getPedidosCompletados(String filtroMetodoPago) throws SQLException {
        List<Map<String, Object>> pedidos = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT id, fecha, nombre_cliente, metodo_pago, total FROM pedidos WHERE completado = TRUE");

        if (!"Todos".equals(filtroMetodoPago)) {
            sql.append(" AND metodo_pago = ?");
        }
        sql.append(" ORDER BY fecha DESC;");

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {

            if (!"Todos".equals(filtroMetodoPago)) {
                pstmt.setString(1, filtroMetodoPago);
            }

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> pedido = new HashMap<>();
                    pedido.put("id", rs.getInt("id"));
                    pedido.put("fecha", rs.getTimestamp("fecha"));
                    pedido.put("nombre_cliente", rs.getString("nombre_cliente"));
                    pedido.put("metodo_pago", rs.getString("metodo_pago"));
                    pedido.put("total", rs.getDouble("total"));
                    pedidos.add(pedido);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al obtener los pedidos completados con filtro.", e);
            throw e;
        }
        return pedidos;
    }
    
    public static void updateMetodoPago(int pedidoId, String metodoPago) throws SQLException {
        String sql = "UPDATE pedidos SET metodo_pago = ? WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, metodoPago);
            pstmt.setInt(2, pedidoId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al actualizar el metodo de pago del pedido: " + pedidoId, e);
            throw e;
        }
    }
    
    public static void updateEstadoPago(int pedidoId, boolean pagado) throws SQLException {
        String sql = "UPDATE pedidos SET pagado = ? WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setBoolean(1, pagado);
            pstmt.setInt(2, pedidoId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al actualizar el estado de pago del pedido: " + pedidoId, e);
            throw e;
        }
    }

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
    
    public static void ajustarCantidadPedidoItem(int idPedidoItem, int cambio) throws SQLException {
        String sql = "UPDATE pedidos_items SET cantidad = cantidad + ? WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, cambio);
            pstmt.setInt(2, idPedidoItem);
            pstmt.executeUpdate();
        }
        recalcularTotalPedido(obtenerPedidoIdPorItem(idPedidoItem));
    }

    public static void eliminarPedidoItem(int idPedidoItem) throws SQLException {
    int pedidoId = obtenerPedidoIdPorItem(idPedidoItem);
    String sql = "DELETE FROM pedidos_items WHERE id = ?";
    try (Connection conn = getConnection();
         PreparedStatement pstmt = conn.prepareStatement(sql)) {
        pstmt.setInt(1, idPedidoItem);
        pstmt.executeUpdate();
    }

    // Después de eliminar el item, recalcula el total y verifica si el pedido queda vacío
    recalcularTotalPedido(pedidoId);
    if (contarItemsEnPedido(pedidoId) == 0) {
        eliminarPedido(pedidoId);
        LOGGER.log(Level.INFO, "Pedido #{0} eliminado automáticamente porque no tiene productos.", pedidoId);
    }
}
    private static int contarItemsEnPedido(int pedidoId) throws SQLException {
    String sql = "SELECT COUNT(*) FROM pedidos_items WHERE pedido_id = ?";
    try (Connection conn = getConnection();
         PreparedStatement pstmt = conn.prepareStatement(sql)) {
        pstmt.setInt(1, pedidoId);
        try (ResultSet rs = pstmt.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
    }
    return 0;
}
    
    private static int obtenerPedidoIdPorItem(int idPedidoItem) throws SQLException {
        String sql = "SELECT pedido_id FROM pedidos_items WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, idPedidoItem);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("pedido_id");
                }
            }
        }
        throw new SQLException("No se encontró el pedido para el ítem " + idPedidoItem);
    }
    
    public static double recalcularTotalPedido(int pedidoId) throws SQLException {
        String sql = "SELECT SUM(precio_unitario * cantidad) AS total FROM pedidos_items WHERE pedido_id = ?";
        double nuevoTotal = 0.0;
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, pedidoId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    nuevoTotal = rs.getDouble("total");
                }
            }
        }
        
        updatePedidoTotal(pedidoId, nuevoTotal);
        return nuevoTotal;
    }
    
    public static Map<Integer, List<Map<String, Object>>> getItemsForPedidos(List<Integer> pedidoIds) throws SQLException {
    Map<Integer, List<Map<String, Object>>> itemsPorPedido = new HashMap<>();
    
    if (pedidoIds.isEmpty()) {
        return itemsPorPedido;
    }

    String placeholders = pedidoIds.stream().map(id -> "?").collect(Collectors.joining(", "));
    String sql = "SELECT pi.id, pi.pedido_id, pi.menu_item_id, pi.cantidad, pi.tamano, mi.nombre " +
                 "FROM pedidos_items pi " +
                 "JOIN menu_items mi ON pi.menu_item_id = mi.id " +
                 "WHERE pi.pedido_id IN (" + placeholders + ");";

    try (Connection conn = getConnection();
         PreparedStatement pstmt = conn.prepareStatement(sql)) {
        
        for (int i = 0; i < pedidoIds.size(); i++) {
            pstmt.setInt(i + 1, pedidoIds.get(i));
        }

        try (ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                int pedidoId = rs.getInt("pedido_id");
                
                Map<String, Object> item = new HashMap<>();
                item.put("id_pedido_item", rs.getInt("id"));
                item.put("nombre_producto", rs.getString("nombre"));
                item.put("cantidad", rs.getInt("cantidad"));
                item.put("tamano", rs.getString("tamano"));

                itemsPorPedido.computeIfAbsent(pedidoId, k -> new ArrayList<>()).add(item);
            }
        }
    } catch (SQLException e) {
        LOGGER.log(Level.SEVERE, "Error al obtener los items para los pedidos.", e);
        throw e;
    }
    return itemsPorPedido;
}
}