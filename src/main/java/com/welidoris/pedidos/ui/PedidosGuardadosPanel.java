// PedidoItemPanel.java
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

/**
 * Componente de UI para mostrar un único pedido con sus detalles y controles.
 * Encapsula toda la lógica de un solo pedido.
 */
public class PedidoItemPanel extends JPanel {

    private static final Logger LOGGER = Logger.getLogger(PedidosGuardadosPanel.class.getName());
    private final NumberFormat currencyFormatter;
    private final PedidosGuardadosPanel parentPanel;

    public PedidoItemPanel(Map<String, Object> pedido, PedidosGuardadosPanel parentPanel) {
        this.parentPanel = parentPanel;
        this.currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("es", "CL"));
        
        // Se establecen tamaños preferidos para que el layout sea flexible pero con límites.
        setPreferredSize(new Dimension(1100, 250));
        setMaximumSize(new Dimension(1500, 250));
        setMinimumSize(new Dimension(900, 250));
        
        setLayout(new GridBagLayout());

        // Obtiene los datos del mapa del pedido
        int pedidoId = (int) pedido.get("id");
        String nombreCliente = (String) pedido.get("nombre_cliente");
        boolean pagado = (boolean) pedido.get("pagado");
        boolean completado = (boolean) pedido.get("completado");
        String metodoPago = (String) pedido.get("metodo_pago");
        int total = (int) pedido.get("total");
        List<Map<String, Object>> items = (List<Map<String, Object>>) pedido.get("items");

        // Borde con título del pedido
        TitledBorder titledBorder = BorderFactory.createTitledBorder("Pedido #" + pedidoId + " - Cliente: " + nombreCliente);
        titledBorder.setTitleFont(new Font("Arial", Font.BOLD, 16));
        setBorder(BorderFactory.createCompoundBorder(
            titledBorder,
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Etiqueta del total
        JLabel totalLabel = new JLabel("<html><b>Total: " + currencyFormatter.format(total) + "</b></html>");
        totalLabel.setFont(new Font("Arial", Font.BOLD, 14));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        add(totalLabel, gbc);

        // Panel de estados y método de pago
        JPanel estadoPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JCheckBox pagadoCheckBox = new JCheckBox("Pagado", pagado);
        JCheckBox completadoCheckBox = new JCheckBox("Completado", completado);

        pagadoCheckBox.setEnabled(!pagado);
        completadoCheckBox.setEnabled(pagado && !completado);

        // Lógica para el checkbox "Pagado"
        pagadoCheckBox.addActionListener(e -> {
            if (pagadoCheckBox.isSelected()) {
                try {
                    DatabaseManager.updateEstadoPago(pedidoId, true);
                    pagadoCheckBox.setEnabled(false);
                    completadoCheckBox.setEnabled(true);
                    JOptionPane.showMessageDialog(this, "El pedido #" + pedidoId + " ha sido marcado como pagado.", "Pago Confirmado", JOptionPane.INFORMATION_MESSAGE);
                } catch (SQLException ex) {
                    LOGGER.log(Level.SEVERE, "Error al actualizar el estado de pago del pedido.", ex);
                    JOptionPane.showMessageDialog(this, "Error al actualizar el estado de pago: " + ex.getMessage(), "Error de Base de Datos", JOptionPane.ERROR_MESSAGE);
                    // Revertir el estado del checkbox si hay un error
                    pagadoCheckBox.setSelected(false);
                }
            }
        });

        // Lógica para el checkbox "Completado"
        completadoCheckBox.addActionListener(e -> {
            if (completadoCheckBox.isSelected()) {
                try {
                    DatabaseManager.updateEstadoCompletado(pedidoId, true);
                    // --- AQUÍ SE NOTIFICA AL PANEL PADRE DEL CAMBIO ---
                    parentPanel.notifyPedidoCompleted();
                    JOptionPane.showMessageDialog(this, "El pedido #" + pedidoId + " ha sido marcado como completado y entregado.", "Pedido Completado", JOptionPane.INFORMATION_MESSAGE);
                } catch (SQLException ex) {
                    LOGGER.log(Level.SEVERE, "Error al actualizar el estado de completado del pedido.", ex);
                    JOptionPane.showMessageDialog(this, "Error al actualizar el estado de completado: " + ex.getMessage(), "Error de Base de Datos", JOptionPane.ERROR_MESSAGE);
                    completadoCheckBox.setSelected(false);
                }
            }
        });

        // ComboBox del método de pago
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
                    JOptionPane.showMessageDialog(this, "Error al actualizar el método de pago: " + ex.getMessage(), "Error de Base de Datos", JOptionPane.ERROR_MESSAGE);
                    metodoPagoCombo.setSelectedItem(metodoPago); // Revertir selección
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
        add(estadoPanel, gbc);

        // Lista de productos
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
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        add(productosScrollPane, gbc);
    }
}
