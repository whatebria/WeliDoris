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

public class PedidosGuardadosPanel extends JPanel {
    
    private JScrollPane scrollPane;
    private JPanel mainPanel;
    private PedidosCompletadosPanel pedidosCompletadosPanel;
    private JTabbedPane tabbedPane;

    public PedidosGuardadosPanel(PedidosCompletadosPanel completadosPanel, JTabbedPane tabbedPane) {
        this.pedidosCompletadosPanel = completadosPanel;
        this.tabbedPane = tabbedPane;
        
        setLayout(new BorderLayout());

        mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        scrollPane = new JScrollPane(mainPanel);
        scrollPane.getVerticalScrollBar().setUnitIncrement(20);
        add(scrollPane, BorderLayout.CENTER);

        JPanel botonPanel = new JPanel();
        botonPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        
        JButton refreshBtn = new JButton("Actualizar Pedidos");
        refreshBtn.addActionListener(e -> cargarPedidos());
        refreshBtn.setPreferredSize(new Dimension(200, 40)); 
        
        botonPanel.add(refreshBtn);
        add(botonPanel, BorderLayout.SOUTH);
        
        cargarPedidos();
    }
    
    public void cargarPedidos() {
        mainPanel.removeAll(); 
        
        // Consulta para obtener solo los pedidos que NO están completados
        String sqlPedidos = "SELECT id, nombre_cliente, pagado, completado, metodo_pago " +
                            "FROM pedidos WHERE completado = FALSE ";

        try (Connection conn = DatabaseManager.getConnection();
             Statement stmtPedidos = conn.createStatement();
             ResultSet rsPedidos = stmtPedidos.executeQuery(sqlPedidos)) {

            while (rsPedidos.next()) {
                int pedidoId = rsPedidos.getInt("id");
                Date fecha = rsPedidos.getTimestamp("fecha");
                String nombreCliente = rsPedidos.getString("nombre_cliente");
                boolean pagado = rsPedidos.getBoolean("pagado");
                boolean completado = rsPedidos.getBoolean("completado");
                String metodoPago = rsPedidos.getString("metodo_pago");
                
                JPanel pedidoPanel = new JPanel();
                pedidoPanel.setLayout(new BorderLayout());
                TitledBorder border = BorderFactory.createTitledBorder("Pedido #" + pedidoId + " - " + nombreCliente);
                pedidoPanel.setBorder(BorderFactory.createCompoundBorder(border, BorderFactory.createEmptyBorder(5, 5, 5, 5)));
                
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
                
                JTextArea detallesArea = new JTextArea();
                detallesArea.setEditable(false);
                detallesArea.append("Fecha: " + sdf.format(fecha) + "\n");
                
                String sqlItems = "SELECT nombre_producto, cantidad, tamano FROM pedido_items WHERE pedido_id = ?;";
                try (PreparedStatement pstmtItems = conn.prepareStatement(sqlItems)) {
                    pstmtItems.setInt(1, pedidoId);
                    try (ResultSet rsItems = pstmtItems.executeQuery()) {
                        while (rsItems.next()) {
                            String nombreProducto = rsItems.getString("nombre_producto");
                            int cantidad = rsItems.getInt("cantidad");
                            String tamano = rsItems.getString("tamano");
                            detallesArea.append("  - Producto: " + nombreProducto + " (" + tamano + "), Cantidad: " + cantidad + "\n");
                        }
                    }
                }
                
                JCheckBox pagadoCheckBox = new JCheckBox("Pagado", pagado);
                pagadoCheckBox.setEnabled(!pagado);
                pagadoCheckBox.addActionListener(e -> {
                    if (pagadoCheckBox.isSelected()) {
                        DatabaseManager.updateEstadoPago(pedidoId, true);
                        pagadoCheckBox.setEnabled(false);
                        JOptionPane.showMessageDialog(this, "El pedido #" + pedidoId + " ha sido marcado como pagado.", "Pago Confirmado", JOptionPane.INFORMATION_MESSAGE);
                    }
                });

                JCheckBox completadoCheckBox = new JCheckBox("Completado", completado);
                completadoCheckBox.setEnabled(pagado && !completado); // Solo se puede completar si ya está pagado
                completadoCheckBox.addActionListener(e -> {
                    if (completadoCheckBox.isSelected()) {
                        completadoCheckBox.setEnabled(false);
                        
                        // Actualizar ambos paneles y cambiar de pestaña
                        cargarPedidos(); // Refresca este panel para que el pedido desaparezca
                        pedidosCompletadosPanel.cargarPedidos(); // Refresca el otro panel
                        tabbedPane.setSelectedIndex(tabbedPane.indexOfTab("Pedidos Completados")); // Mover a la pestaña
                        
                        JOptionPane.showMessageDialog(this, "El pedido #" + pedidoId + " ha sido marcado como completado y entregado.", "Pedido Completado", JOptionPane.INFORMATION_MESSAGE);
                    }
                });
                
                JComboBox<String> metodoPagoCombo = new JComboBox<>(new String[]{"Efectivo", "Transferencia", "No especificado"});
                metodoPagoCombo.setSelectedItem(metodoPago);
                metodoPagoCombo.setEnabled("No especificado".equals(metodoPago));
                metodoPagoCombo.addActionListener(e -> {
                    String nuevoMetodo = (String) metodoPagoCombo.getSelectedItem();
                    if (!"No especificado".equals(nuevoMetodo)) {
                        DatabaseManager.updateMetodoPago(pedidoId, nuevoMetodo);
                        metodoPagoCombo.setEnabled(false);
                        JOptionPane.showMessageDialog(this, "El método de pago para el pedido #" + pedidoId + " ha sido actualizado a: " + nuevoMetodo, "Método de Pago Actualizado", JOptionPane.INFORMATION_MESSAGE);
                    }
                });

                JPanel estadoPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
                estadoPanel.add(pagadoCheckBox);
                estadoPanel.add(completadoCheckBox);
                estadoPanel.add(new JLabel("Método:"));
                estadoPanel.add(metodoPagoCombo);

                pedidoPanel.add(detallesArea, BorderLayout.CENTER);
                pedidoPanel.add(estadoPanel, BorderLayout.SOUTH);

                mainPanel.add(pedidoPanel);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error al cargar los pedidos guardados: " + e.getMessage(), "Error de Base de Datos", JOptionPane.ERROR_MESSAGE);
        }
        
        mainPanel.revalidate();
        mainPanel.repaint();
    }
}