package com.welidoris.pedidos.ui;

import com.welidoris.pedidos.db.DatabaseManager;
import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PedidosCompletadosPanel extends JPanel {
    
    private static final Logger LOGGER = Logger.getLogger(PedidosCompletadosPanel.class.getName());
    private JScrollPane scrollPane;
    private JPanel mainPanel;
    private final NumberFormat currencyFormatter;
    
    private JComboBox<String> metodoPagoFilter;

    public PedidosCompletadosPanel() {
        // Se configura el formato de moneda para Chile.
        this.currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("es", "CL"));
        setLayout(new BorderLayout());
        
        // --- Panel de filtro ---
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filterPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        filterPanel.add(new JLabel("Filtrar por método de pago:"));
        
        metodoPagoFilter = new JComboBox<>(new String[]{"Todos", "Efectivo", "Transferencia"});
        filterPanel.add(metodoPagoFilter);
        
        // Agregamos un listener al ComboBox para recargar los pedidos al cambiar el filtro
        metodoPagoFilter.addActionListener(e -> {
            String filtroSeleccionado = (String) metodoPagoFilter.getSelectedItem();
            cargarPedidos(filtroSeleccionado);
        });
        
        add(filterPanel, BorderLayout.NORTH);
        // --- Fin del panel de filtro ---

        mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        scrollPane = new JScrollPane(mainPanel);
        scrollPane.getVerticalScrollBar().setUnitIncrement(20);
        add(scrollPane, BorderLayout.CENTER);

        // Llamamos a cargarPedidos con el filtro inicial "Todos"
        cargarPedidos("Todos");
    }
    
    public void cargarPedidos(String filtroMetodoPago) {
        mainPanel.removeAll();
        mainPanel.revalidate();
        mainPanel.repaint();
        
        String sqlPedidos;
        
        // Se construye la consulta SQL según el filtro
        if ("Todos".equals(filtroMetodoPago)) {
            sqlPedidos = "SELECT id, fecha, nombre_cliente, metodo_pago, total " +
                         "FROM pedidos WHERE pagado = TRUE AND completado = TRUE ORDER BY fecha DESC;";
        } else {
            sqlPedidos = "SELECT id, fecha, nombre_cliente, metodo_pago, total " +
                         "FROM pedidos WHERE pagado = TRUE AND completado = TRUE AND metodo_pago = ? ORDER BY fecha DESC;";
        }

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmtPedidos = conn.prepareStatement(sqlPedidos)) {
            
            // Si el filtro no es "Todos", se establece el parámetro
            if (!"Todos".equals(filtroMetodoPago)) {
                pstmtPedidos.setString(1, filtroMetodoPago);
            }

            SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");

            try (ResultSet rsPedidos = pstmtPedidos.executeQuery()) {
                if (!rsPedidos.isBeforeFirst()) {
                    mainPanel.add(new JLabel("No hay pedidos completados con este filtro."));
                } else {
                    while (rsPedidos.next()) {
                        int pedidoId = rsPedidos.getInt("id");
                        Date fecha = rsPedidos.getTimestamp("fecha");
                        String nombreCliente = rsPedidos.getString("nombre_cliente");
                        String metodoPago = rsPedidos.getString("metodo_pago");
                        double total = rsPedidos.getDouble("total");
                        
                        JPanel pedidoPanel = new JPanel();
                        // Se establece un tamaño fijo para cada panel de pedido
                        pedidoPanel.setPreferredSize(new Dimension(1100, 250));
                        pedidoPanel.setMaximumSize(new Dimension(1500, 250));
                        pedidoPanel.setMinimumSize(new Dimension(900, 250));
                        pedidoPanel.setLayout(new GridBagLayout());
                        
                        TitledBorder titledBorder = BorderFactory.createTitledBorder("Pedido #" + pedidoId + " - Cliente: " + nombreCliente);
                        titledBorder.setTitleFont(new Font("Arial", Font.BOLD, 16));
                        pedidoPanel.setBorder(BorderFactory.createCompoundBorder(
                            titledBorder,
                            BorderFactory.createEmptyBorder(10, 10, 10, 10)
                        ));
                        
                        GridBagConstraints gbc = new GridBagConstraints();
                        gbc.insets = new Insets(5, 5, 5, 5);
                        gbc.anchor = GridBagConstraints.WEST;
                        gbc.fill = GridBagConstraints.HORIZONTAL;
                        
                        JLabel infoLabel = new JLabel("<html><b>Fecha:</b> " + dateFormat.format(fecha) + "<br><b>Método de pago:</b> " + metodoPago + "</html>");
                        infoLabel.setFont(new Font("Arial", Font.PLAIN, 14));
                        gbc.gridx = 0;
                        gbc.gridy = 0;
                        pedidoPanel.add(infoLabel, gbc);

                        JLabel totalLabel = new JLabel("<html><b>Total: " + currencyFormatter.format(total) + "</b></html>");
                        totalLabel.setFont(new Font("Arial", Font.BOLD, 14));
                        gbc.gridx = 1;
                        gbc.gridy = 0;
                        gbc.anchor = GridBagConstraints.EAST;
                        pedidoPanel.add(totalLabel, gbc);
                        
                        // Lista para almacenar los detalles de los productos
                        DefaultListModel<String> productosListModel = new DefaultListModel<>();
                        
                        String sqlItems = "SELECT mi.nombre, pi.cantidad, pi.tamano " +
                                          "FROM pedidos_items pi " +
                                          "JOIN menu_items mi ON pi.menu_item_id = mi.id " +
                                          "WHERE pi.pedido_id = ?;";
                        
                        try (PreparedStatement pstmtItems = conn.prepareStatement(sqlItems)) {
                            pstmtItems.setInt(1, pedidoId);
                            try (ResultSet rsItems = pstmtItems.executeQuery()) {
                                while (rsItems.next()) {
                                    String nombreProducto = rsItems.getString("nombre");
                                    int cantidad = rsItems.getInt("cantidad");
                                    String tamano = rsItems.getString("tamano");
                                    productosListModel.addElement(" - " + nombreProducto + " (" + tamano + ") x " + cantidad);
                                }
                            }
                        }
                        
                        JList<String> productosList = new JList<>(productosListModel);
                        productosList.setFont(new Font("Monospaced", Font.PLAIN, 12));
                        
                        JScrollPane productosScrollPane = new JScrollPane(productosList);
                        productosScrollPane.setPreferredSize(new Dimension(300, 100));
                        
                        gbc.gridx = 0;
                        gbc.gridy = 1;
                        gbc.gridwidth = 2; // Ocupa ambas columnas
                        gbc.fill = GridBagConstraints.BOTH;
                        gbc.weightx = 1.0;
                        gbc.weighty = 1.0;
                        pedidoPanel.add(productosScrollPane, gbc);
                        
                        mainPanel.add(pedidoPanel);
                        mainPanel.add(Box.createVerticalStrut(15));
                    }
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al cargar los pedidos completados desde la base de datos.", e);
            JOptionPane.showMessageDialog(this, "Error al cargar los pedidos completados: " + e.getMessage(), "Error de Base de Datos", JOptionPane.ERROR_MESSAGE);
        }
        
        mainPanel.revalidate();
        mainPanel.repaint();
    }
}
