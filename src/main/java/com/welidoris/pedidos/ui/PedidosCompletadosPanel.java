package com.welidoris.pedidos.ui;

import com.welidoris.pedidos.db.DatabaseManager;
import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.border.TitledBorder;

public class PedidosCompletadosPanel extends JPanel {
    
    private JScrollPane scrollPane;
    private JPanel mainPanel;

    public PedidosCompletadosPanel() {
        setLayout(new BorderLayout());

        mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        scrollPane = new JScrollPane(mainPanel);
        scrollPane.getVerticalScrollBar().setUnitIncrement(20);
        add(scrollPane, BorderLayout.CENTER);

        JPanel botonPanel = new JPanel();
        botonPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        
        JButton refreshBtn = new JButton("Actualizar Pedidos Completados");
        refreshBtn.addActionListener(e -> cargarPedidos());
        refreshBtn.setPreferredSize(new Dimension(250, 40));
        
        botonPanel.add(refreshBtn);
        add(botonPanel, BorderLayout.SOUTH);
        
        cargarPedidos();
    }
    
    public void cargarPedidos() {
        mainPanel.removeAll();
        
        // Consulta para obtener solo los pedidos que ya están pagados Y completados
        String sqlPedidos = "SELECT id, nombre_cliente, pagado, completado, metodo_pago " +
                            "FROM pedidos WHERE pagado = TRUE AND completado = TRUE ";

        try (Connection conn = DatabaseManager.getConnection();
             Statement stmtPedidos = conn.createStatement();
             ResultSet rsPedidos = stmtPedidos.executeQuery(sqlPedidos)) {

            while (rsPedidos.next()) {
                int pedidoId = rsPedidos.getInt("id");
                String nombreCliente = rsPedidos.getString("nombre_cliente");
                String metodoPago = rsPedidos.getString("metodo_pago");
                
                JPanel pedidoPanel = new JPanel();
                pedidoPanel.setLayout(new BorderLayout());
                TitledBorder border = BorderFactory.createTitledBorder("Pedido #" + pedidoId + " - " + nombreCliente);
                pedidoPanel.setBorder(BorderFactory.createCompoundBorder(border, BorderFactory.createEmptyBorder(5, 5, 5, 5)));
                
                
                JTextArea detallesArea = new JTextArea();
                detallesArea.setEditable(false);
                detallesArea.append("Método de pago: " + metodoPago + "\n");
                
                String sqlItems = "SELECT menu_item_id, cantidad, tamano FROM pedidos_items WHERE pedido_id = ?;";
                try (PreparedStatement pstmtItems = conn.prepareStatement(sqlItems)) {
                    pstmtItems.setInt(1, pedidoId);
                    try (ResultSet rsItems = pstmtItems.executeQuery()) {
                        while (rsItems.next()) {
                            String nombreProducto = rsItems.getString("menu_item_id");
                            int cantidad = rsItems.getInt("cantidad");
                            String tamano = rsItems.getString("tamano");
                            detallesArea.append("  - Producto: " + nombreProducto + " (" + tamano + "), Cantidad: " + cantidad + "\n");
                        }
                    }
                }
                
                // No se necesitan checkboxes, solo una etiqueta para indicar el estado final
                JLabel estadoLabel = new JLabel("Estado: Pagado y Completado");
                estadoLabel.setFont(new Font("Arial", Font.BOLD, 14));
                
                JPanel estadoPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
                estadoPanel.add(estadoLabel);

                pedidoPanel.add(detallesArea, BorderLayout.CENTER);
                pedidoPanel.add(estadoPanel, BorderLayout.SOUTH);

                mainPanel.add(pedidoPanel);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error al cargar los pedidos completados: " + e.getMessage(), "Error de Base de Datos", JOptionPane.ERROR_MESSAGE);
        }
        
        mainPanel.revalidate();
        mainPanel.repaint();
    }
}