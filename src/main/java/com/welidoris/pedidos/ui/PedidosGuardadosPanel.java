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

public final class PedidosGuardadosPanel extends JPanel {
    
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

        // El boton de actualizar se ha eliminado para que la carga sea automatica
        
        cargarPedidos();
    }
    
    /**
     * Carga y muestra todos los pedidos que no han sido marcados como completados.
     * Este metodo se puede llamar desde otro panel para forzar una actualizacion.
     */
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
                            
                            JLabel totalLabel = new JLabel("<html><b>Total: " + currencyFormatter.format(total) + "</b></html>");
                            totalLabel.setFont(new Font("Arial", Font.BOLD, 14));
                            gbc.gridx = 0;
                            gbc.gridy = 0;
                            gbc.weightx = 1.0;
                            pedidoPanel.add(totalLabel, gbc);

                            // Panel de estados y método de pago
                            JPanel estadoPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
                            JCheckBox pagadoCheckBox = new JCheckBox("Pagado", pagado);
                            JCheckBox completadoCheckBox = new JCheckBox("Completado", completado);
                            
                            pagadoCheckBox.setEnabled(!pagado);
                            pagadoCheckBox.addActionListener(e -> {
                                if (pagadoCheckBox.isSelected()) {
                                    try {
                                        DatabaseManager.updateEstadoPago(pedidoId, true);
                                        pagadoCheckBox.setEnabled(false);
                                        JOptionPane.showMessageDialog(PedidosGuardadosPanel.this, "El pedido #" + pedidoId + " ha sido marcado como pagado.", "Pago Confirmado", JOptionPane.INFORMATION_MESSAGE);
                                        completadoCheckBox.setEnabled(true);
                                    } catch (SQLException ex) {
                                        LOGGER.log(Level.SEVERE, "Error al actualizar el estado de pago del pedido.", ex);
                                        JOptionPane.showMessageDialog(PedidosGuardadosPanel.this, "Error al actualizar el estado de pago: " + ex.getMessage(), "Error de Base de Datos", JOptionPane.ERROR_MESSAGE);
                                    }
                                }
                            });

                            completadoCheckBox.setEnabled(pagado && !completado); // Solo se puede completar si ya esta pagado
                            completadoCheckBox.addActionListener(e -> {
                                if (completadoCheckBox.isSelected()) {
                                    try {
                                        DatabaseManager.updateEstadoCompletado(pedidoId, true);
                                        completadoCheckBox.setEnabled(false);
                                        cargarPedidos(); // Refresca este panel para que el pedido desaparezca
                                        pedidosCompletadosPanel.cargarPedidos("Todos"); // Refresca el otro panel
                                        tabbedPane.setSelectedIndex(tabbedPane.indexOfTab("Pedidos Completados")); // Mover a la pestana
                                        
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
                                    } catch (SQLException ex) {
                                        LOGGER.log(Level.SEVERE, "Error al actualizar el método de pago del pedido.", ex);
                                        JOptionPane.showMessageDialog(PedidosGuardadosPanel.this, "Error al actualizar el método de pago: " + ex.getMessage(), "Error de Base de Datos", JOptionPane.ERROR_MESSAGE);
                                    }
                                }
                            });
                            
                            estadoPanel.add(pagadoCheckBox);
                            estadoPanel.add(completadoCheckBox);
                            estadoPanel.add(new JLabel("Método:"));
                            estadoPanel.add(metodoPagoCombo);
                            
                            gbc.gridx = 1;
                            gbc.gridy = 0;
                            gbc.weightx = 0.0;
                            pedidoPanel.add(estadoPanel, gbc);
                            
                            // Lista para mostrar los productos
                            DefaultListModel<String> productosListModel = new DefaultListModel<>();
                            for (Map<String, Object> item : items) {
                                String nombreProducto = (String) item.get("nombre_producto");
                                int cantidad = (int) item.get("cantidad");
                                String tamano = (String) item.get("tamano");
                                productosListModel.addElement(" - " + nombreProducto + " (" + tamano + ") x " + cantidad);
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
