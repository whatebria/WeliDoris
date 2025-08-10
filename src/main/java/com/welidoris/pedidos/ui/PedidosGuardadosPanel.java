package com.welidoris.pedidos.ui;

import com.welidoris.pedidos.db.DatabaseManager;
import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PedidosGuardadosPanel extends JPanel {
    
    private static final Logger LOGGER = Logger.getLogger(PedidosGuardadosPanel.class.getName());
    private JScrollPane scrollPane;
    private JPanel mainPanel;
    private PedidosCompletadosPanel pedidosCompletadosPanel;
    private JTabbedPane tabbedPane;
    private final NumberFormat currencyFormatter;

    public PedidosGuardadosPanel(PedidosCompletadosPanel completadosPanel, JTabbedPane tabbedPane) {
        this.pedidosCompletadosPanel = completadosPanel;
        this.tabbedPane = tabbedPane;
        // Se configura el formato de moneda para Chile.
        this.currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("es", "CL"));

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
        mainPanel.revalidate();
        mainPanel.repaint();

        new SwingWorker<List<Map<String, Object>>, Void>() {
            @Override
            protected List<Map<String, Object>> doInBackground() throws Exception {
                // Se obtiene una lista de mapas, donde cada mapa representa un pedido.
                return DatabaseManager.getPedidosGuardados();
            }

            @Override
            protected void done() {
                try {
                    List<Map<String, Object>> pedidos = get();
                    if (pedidos.isEmpty()) {
                        mainPanel.add(new JLabel("No hay pedidos guardados."));
                    } else {
                        for (Map<String, Object> pedido : pedidos) {
                            int pedidoId = (int) pedido.get("id");
                            String nombreCliente = (String) pedido.get("nombre_cliente");
                            boolean pagado = (boolean) pedido.get("pagado");
                            boolean completado = (boolean) pedido.get("completado");
                            String metodoPago = (String) pedido.get("metodo_pago");
                            int total = (int) pedido.get("total");
                            List<Map<String, Object>> items = (List<Map<String, Object>>) pedido.get("items");
                            
                            JPanel pedidoPanel = new JPanel();
                            pedidoPanel.setLayout(new BorderLayout());
                            TitledBorder border = BorderFactory.createTitledBorder("Pedido #" + pedidoId + " - " + nombreCliente + " - " + currencyFormatter.format(total));
                            pedidoPanel.setBorder(BorderFactory.createCompoundBorder(border, BorderFactory.createEmptyBorder(5, 5, 5, 5)));
                            
                            JTextArea detallesArea = new JTextArea();
                            detallesArea.setEditable(false);
                            
                            for (Map<String, Object> item : items) {
                                String nombreProducto = (String) item.get("nombre_producto");
                                int cantidad = (int) item.get("cantidad");
                                String tamano = (String) item.get("tamano");
                                detallesArea.append("  - Producto: " + nombreProducto + " (" + tamano + "), Cantidad: " + cantidad + "\n");
                            }

                            JCheckBox pagadoCheckBox = new JCheckBox("Pagado", pagado);
                            JCheckBox completadoCheckBox = new JCheckBox("Completado", completado);

                            pagadoCheckBox.setEnabled(!pagado);
                            pagadoCheckBox.addActionListener(e -> {
                                if (pagadoCheckBox.isSelected()) {
                                    try {
                                        DatabaseManager.updateEstadoPago(pedidoId, true);
                                        pagadoCheckBox.setEnabled(false);
                                        JOptionPane.showMessageDialog(PedidosGuardadosPanel.this, "El pedido #" + pedidoId + " ha sido marcado como pagado.", "Pago Confirmado", JOptionPane.INFORMATION_MESSAGE);
                                        // Refrescar el estado de los checkboxes
                                        completadoCheckBox.setEnabled(true);
                                    } catch (SQLException ex) {
                                        LOGGER.log(Level.SEVERE, "Error al actualizar el estado de pago del pedido.", ex);
                                        JOptionPane.showMessageDialog(PedidosGuardadosPanel.this, "Error al actualizar el estado de pago: " + ex.getMessage(), "Error de Base de Datos", JOptionPane.ERROR_MESSAGE);
                                    }
                                }
                            });

                            completadoCheckBox.setEnabled(pagado && !completado); // Solo se puede completar si ya está pagado
                            completadoCheckBox.addActionListener(e -> {
                                if (completadoCheckBox.isSelected()) {
                                    try {
                                        DatabaseManager.updateEstadoCompletado(pedidoId, true);
                                        completadoCheckBox.setEnabled(false);
                                        cargarPedidos(); // Refresca este panel para que el pedido desaparezca
                                        pedidosCompletadosPanel.cargarPedidos(); // Refresca el otro panel
                                        tabbedPane.setSelectedIndex(tabbedPane.indexOfTab("Pedidos Completados")); // Mover a la pestaña
                                        
                                        JOptionPane.showMessageDialog(PedidosGuardadosPanel.this, "El pedido #" + pedidoId + " ha sido marcado como completado y entregado.", "Pedido Completado", JOptionPane.INFORMATION_MESSAGE);
                                    } catch (SQLException ex) {
                                        LOGGER.log(Level.SEVERE, "Error al actualizar el estado de completado del pedido.", ex);
                                        JOptionPane.showMessageDialog(PedidosGuardadosPanel.this, "Error al actualizar el estado de completado: " + ex.getMessage(), "Error de Base de Datos", JOptionPane.ERROR_MESSAGE);
                                    }
                                }
                            });

                            JComboBox<String> metodoPagoCombo = new JComboBox<>(new String[]{"Efectivo", "Transferencia", "No especificado"});
                            metodoPagoCombo.setSelectedItem(metodoPago);
                            metodoPagoCombo.setEnabled("No especificado".equals(metodoPago));
                            metodoPagoCombo.addActionListener(e -> {
                                String nuevoMetodo = (String) metodoPagoCombo.getSelectedItem();
                                if (!"No especificado".equals(nuevoMetodo)) {
                                    try {
                                        DatabaseManager.updateMetodoPago(pedidoId, nuevoMetodo);
                                        metodoPagoCombo.setEnabled(false);
                                        JOptionPane.showMessageDialog(PedidosGuardadosPanel.this, "El método de pago para el pedido #" + pedidoId + " ha sido actualizado a: " + nuevoMetodo, "Método de Pago Actualizado", JOptionPane.INFORMATION_MESSAGE);
                                    } catch (SQLException ex) {
                                        LOGGER.log(Level.SEVERE, "Error al actualizar el método de pago del pedido.", ex);
                                        JOptionPane.showMessageDialog(PedidosGuardadosPanel.this, "Error al actualizar el método de pago: " + ex.getMessage(), "Error de Base de Datos", JOptionPane.ERROR_MESSAGE);
                                    }
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
                            mainPanel.add(Box.createRigidArea(new Dimension(0, 10)));
                        }
                    }
                    mainPanel.revalidate();
                    mainPanel.repaint();
                } catch (Exception ex) {
                    LOGGER.log(Level.SEVERE, "Error al cargar los pedidos guardados en SwingWorker.", ex);
                    JOptionPane.showMessageDialog(PedidosGuardadosPanel.this, "Error al cargar los pedidos: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }
}
