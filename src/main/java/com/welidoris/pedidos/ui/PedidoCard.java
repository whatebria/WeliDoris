package com.welidoris.pedidos.ui;

import com.welidoris.pedidos.db.DatabaseManager;
import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.text.NumberFormat;
import java.util.List;
import java.util.Map;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.text.SimpleDateFormat;
import java.util.Date;

public class PedidoCard extends JPanel {

    private static final Logger LOGGER = Logger.getLogger(PedidoCard.class.getName());
    private final Map<String, Object> pedidoData;
    private final Runnable onUpdate;
    private final NumberFormat currencyFormatter;

    public PedidoCard(Map<String, Object> pedidoData, boolean esEditable, Runnable onUpdate) {
        this.pedidoData = pedidoData;
        this.onUpdate = onUpdate;
        this.currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("es", "CL"));

        setLayout(new GridBagLayout());

        int pedidoId = (int) pedidoData.get("id");
        String nombreCliente = (String) pedidoData.get("nombre_cliente");

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

        // --- Lógica condicional para pedidos editables ---
        if (esEditable) {
            boolean isPagado = (boolean) pedidoData.get("pagado");
            boolean isCompletado = (boolean) pedidoData.get("completado");
            String metodoPago = (String) pedidoData.get("metodo_pago");

            JPanel estadoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            estadoPanel.setOpaque(false);
            JCheckBox pagadoCheckBox = new JCheckBox("Pagado", isPagado);
            JCheckBox completadoCheckBox = new JCheckBox("Completado", isCompletado);

            pagadoCheckBox.setEnabled(!isPagado && !"No especificado".equals(metodoPago));
            pagadoCheckBox.addActionListener(e -> {
                if (pagadoCheckBox.isSelected()) {
                    new SwingWorker<Void, Void>() {
                        @Override
                        protected Void doInBackground() throws Exception {
                            DatabaseManager.updateEstadoPago(pedidoId, true);
                            return null;
                        }
                        @Override
                        protected void done() {
                            try {
                                get();
                                pagadoCheckBox.setEnabled(false);
                                completadoCheckBox.setEnabled(true);
                                onUpdate.run();
                                JOptionPane.showMessageDialog(PedidoCard.this, "Pedido #" + pedidoId + " marcado como pagado.");
                            } catch (Exception ex) {
                                LOGGER.log(Level.SEVERE, "Error al actualizar pago.", ex);
                                JOptionPane.showMessageDialog(PedidoCard.this, "Error: " + ex.getMessage());
                            }
                        }
                    }.execute();
                }
            });

            completadoCheckBox.setEnabled(isPagado && !isCompletado);
            completadoCheckBox.addActionListener(e -> {
                if (completadoCheckBox.isSelected()) {
                    new SwingWorker<Void, Void>() {
                        @Override
                        protected Void doInBackground() throws Exception {
                            DatabaseManager.updateEstadoCompletado(pedidoId, true);
                            return null;
                        }
                        @Override
                        protected void done() {
                            try {
                                get();
                                onUpdate.run();
                                JOptionPane.showMessageDialog(PedidoCard.this, "Pedido #" + pedidoId + " marcado como completado.");
                            } catch (Exception ex) {
                                LOGGER.log(Level.SEVERE, "Error al actualizar completado.", ex);
                                JOptionPane.showMessageDialog(PedidoCard.this, "Error: " + ex.getMessage());
                            }
                        }
                    }.execute();
                }
            });

            JComboBox<String> metodoPagoCombo = new JComboBox<>(new String[]{"Efectivo", "Transferencia", "No especificado"});
            metodoPagoCombo.setSelectedItem(metodoPago);
            metodoPagoCombo.addActionListener(e -> {
                String nuevoMetodo = (String) metodoPagoCombo.getSelectedItem();
                new SwingWorker<Void, Void>() {
                    @Override
                    protected Void doInBackground() throws Exception {
                        DatabaseManager.updateMetodoPago(pedidoId, nuevoMetodo);
                        return null;
                    }
                    @Override
                    protected void done() {
                        try {
                            get();
                            pagadoCheckBox.setEnabled(!"No especificado".equals(nuevoMetodo));
                            if (!pagadoCheckBox.isEnabled()) {
                                pagadoCheckBox.setSelected(false);
                            }
                            onUpdate.run();
                        } catch (Exception ex) {
                            LOGGER.log(Level.SEVERE, "Error al actualizar el método de pago.", ex);
                            JOptionPane.showMessageDialog(PedidoCard.this, "Error: " + ex.getMessage());
                        }
                    }
                }.execute();
            });

            estadoPanel.add(pagadoCheckBox);
            estadoPanel.add(completadoCheckBox);
            estadoPanel.add(new JLabel("Método:"));
            estadoPanel.add(metodoPagoCombo);

            gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 3;
            add(estadoPanel, gbc);

        } else {
            // Lógica para pedidos completados (solo información estática)
            Date fecha = (Date) pedidoData.get("fecha");
            String metodoPago = (String) pedidoData.get("metodo_pago");
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm");

            JLabel infoLabel = new JLabel("<html><b>Fecha:</b> " + dateFormat.format(fecha) + " &nbsp;&nbsp;&nbsp; <b>Método:</b> " + metodoPago + "</html>");
            infoLabel.setFont(new Font("Arial", Font.PLAIN, 16));
            gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 3;
            add(infoLabel, gbc);
        }

        // --- Lógica común de visualización de productos ---
        JPanel productosPanel = new JPanel();
        productosPanel.setLayout(new BoxLayout(productosPanel, BoxLayout.Y_AXIS));
        productosPanel.setOpaque(true);

        JScrollPane productosScrollPane = new JScrollPane(productosPanel);
        // Quitar el borde del JScrollPane para que no tenga líneas
        productosScrollPane.setBorder(null);

        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 3; gbc.weightx = 1.0; gbc.weighty = 1.0; gbc.fill = GridBagConstraints.BOTH;
        add(productosScrollPane, gbc);

        // Carga los productos
        cargarItems(productosPanel, (List<Map<String, Object>>) pedidoData.get("items"), esEditable);

        // --- Panel de total y botón de eliminar ---
        JPanel totalControlPanel = new JPanel(new BorderLayout());
        totalControlPanel.setOpaque(false);
        
        JLabel totalLabel = new JLabel("Total: " + currencyFormatter.format((double) pedidoData.get("total")));
        totalLabel.setFont(new Font("Arial", Font.BOLD, 18));
        totalControlPanel.add(totalLabel, BorderLayout.WEST);

        if (esEditable) {
            JButton eliminarPedidoBtn = new JButton("Eliminar Pedido");
            eliminarPedidoBtn.setBackground(new Color(255, 0, 0));
            eliminarPedidoBtn.setForeground(Color.WHITE);
            eliminarPedidoBtn.setFocusPainted(false);
            eliminarPedidoBtn.setPreferredSize(new Dimension(150, 40));
            eliminarPedidoBtn.addActionListener(e -> {
                int confirm = JOptionPane.showConfirmDialog(this,
                        "¿Estás seguro de que quieres eliminar este pedido completo? Esta acción es irreversible.",
                        "Confirmar Eliminación", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                if (confirm == JOptionPane.YES_OPTION) {
                    new SwingWorker<Void, Void>() {
                        @Override
                        protected Void doInBackground() throws Exception {
                            DatabaseManager.eliminarPedido(pedidoId);
                            return null;
                        }
                        @Override
                        protected void done() {
                            try {
                                get();
                                onUpdate.run();
                                JOptionPane.showMessageDialog(PedidoCard.this, "Pedido #" + pedidoId + " eliminado exitosamente.");
                            } catch (Exception ex) {
                                LOGGER.log(Level.SEVERE, "Error al eliminar el pedido completo.", ex);
                                JOptionPane.showMessageDialog(PedidoCard.this, "Error: " + ex.getMessage());
                            }
                        }
                    }.execute();
                }
            });
            totalControlPanel.add(eliminarPedidoBtn, BorderLayout.EAST);
        }
        
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 3; gbc.weighty = 0.0;
        add(totalControlPanel, gbc);
    }

    private void cargarItems(JPanel productosPanel, List<Map<String, Object>> items, boolean esEditable) {
        productosPanel.removeAll();

        if (items != null && !items.isEmpty()) {
            for (Map<String, Object> itemData : items) {
                String nombreProducto = (String) itemData.get("nombre_producto");
                int cantidad = (int) itemData.get("cantidad");
                String tamano = (String) itemData.get("tamano");

                JPanel itemRow = new JPanel(new GridBagLayout());
                itemRow.setOpaque(true);

                GridBagConstraints gbc = new GridBagConstraints();
                gbc.insets = new Insets(2, 2, 2, 2);
                gbc.anchor = GridBagConstraints.WEST;
                gbc.fill = GridBagConstraints.HORIZONTAL;

                JLabel label = new JLabel(String.format("%s (%s) x %d", nombreProducto, tamano, cantidad));
                gbc.gridx = 0; gbc.weightx = 1.0;
                itemRow.add(label, gbc);

                if (esEditable) {
                    int pedidoItemId = (int) itemData.get("id_pedido_item");

                    JButton minusBtn = new JButton("-");
                    styleButton(minusBtn, new Color(200, 200, 200), Color.BLACK);
                    minusBtn.addActionListener(e -> ajustarCantidad(pedidoItemId, -1));
                    gbc.gridx = 1; gbc.weightx = 0;
                    itemRow.add(minusBtn, gbc);

                    JButton plusBtn = new JButton("+");
                    styleButton(plusBtn, new Color(200, 200, 200), Color.BLACK);
                    plusBtn.addActionListener(e -> ajustarCantidad(pedidoItemId, 1));
                    gbc.gridx = 2; gbc.weightx = 0;
                    itemRow.add(plusBtn, gbc);

                    JButton deleteBtn = new JButton("X");
                    styleButton(deleteBtn, new Color(200, 50, 50), Color.WHITE);
                    deleteBtn.addActionListener(e -> eliminarItem(pedidoItemId));
                    gbc.gridx = 3; gbc.weightx = 0;
                    itemRow.add(deleteBtn, gbc);
                }

                productosPanel.add(itemRow);
            }
        }
        productosPanel.revalidate();
        productosPanel.repaint();
    }

    // Métodos de acción para pedidos editables
    private void ajustarCantidad(int pedidoItemId, int cambio) {
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                DatabaseManager.ajustarCantidadPedidoItem(pedidoItemId, cambio);
                return null;
            }
            @Override
            protected void done() {
                try {
                    get();
                    onUpdate.run();
                } catch (Exception ex) {
                    LOGGER.log(Level.SEVERE, "Error al ajustar cantidad.", ex);
                    JOptionPane.showMessageDialog(PedidoCard.this, "Error: " + ex.getMessage());
                }
            }
        }.execute();
    }

    private void eliminarItem(int pedidoItemId) {
        int confirm = JOptionPane.showConfirmDialog(this, "¿Estás seguro de que quieres eliminar este producto?", "Confirmar Eliminación", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            new SwingWorker<Void, Void>() {
                @Override
                protected Void doInBackground() throws Exception {
                    DatabaseManager.eliminarPedidoItem(pedidoItemId);
                    return null;
                }
                @Override
                protected void done() {
                    try {
                        get();
                        onUpdate.run();
                    } catch (Exception ex) {
                        LOGGER.log(Level.SEVERE, "Error al eliminar producto.", ex);
                        JOptionPane.showMessageDialog(PedidoCard.this, "Error: " + ex.getMessage());
                    }
                }
            }.execute();
        }
    }

    private void styleButton(JButton button, Color background, Color foreground) {
        button.setBackground(background);
        button.setForeground(foreground);
        button.setMargin(new Insets(5, 5, 5, 5));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setFocusPainted(false);
    }
}