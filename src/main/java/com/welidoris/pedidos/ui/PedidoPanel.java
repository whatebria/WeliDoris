package com.welidoris.pedidos.ui;

import com.welidoris.pedidos.db.DatabaseManager;
import com.welidoris.pedidos.models.MenuItem;
import com.welidoris.pedidos.models.PedidoItem;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.util.LinkedHashMap;
import java.util.Map;
import java.text.NumberFormat;
import java.util.Locale;

/**
 * Panel para la creacion y gestion de un pedido en curso.
 * Incluye la lista de productos, el total, y controles para guardar y limpiar.
 */
public class PedidoPanel extends JPanel {
    private PedidosGuardadosPanel pedidosGuardadosPanel;

    private Map<String, PedidoItem> itemsEnPedido;
    private JPanel listaPanel;
    private JLabel totalLabel;
    private final NumberFormat currencyFormatter;

    private JTextField nombreClienteField;
    private JCheckBox pagadoCheckBox;
    private JComboBox<String> metodoPagoCombo;
    private JButton limpiarBtn;
    private JButton guardarBtn;

    /**
     * Constructor que recibe una referencia al panel de pedidos guardados para poder notificarle las actualizaciones.
     * @param pedidosGuardadosPanel La instancia del panel de pedidos guardados.
     */
    public PedidoPanel(PedidosGuardadosPanel pedidosGuardadosPanel) {
        this.pedidosGuardadosPanel = pedidosGuardadosPanel;
        this.currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("es", "CL"));
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(150, 150, 150)),
                "Tu Pedido",
                TitledBorder.LEFT, TitledBorder.TOP,
                new Font("SansSerif", Font.BOLD, 14)
        ));

        itemsEnPedido = new LinkedHashMap<>();

        listaPanel = new JPanel();
        listaPanel.setLayout(new BoxLayout(listaPanel, BoxLayout.Y_AXIS));

        JScrollPane scrollPane = new JScrollPane(listaPanel);
        scrollPane.setPreferredSize(new Dimension(350, 300));
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        add(scrollPane, BorderLayout.CENTER);

        JPanel pedidoControls = new JPanel();
        pedidoControls.setLayout(new BoxLayout(pedidoControls, BoxLayout.Y_AXIS));
        pedidoControls.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel clientePanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        JLabel nombreClienteLabel = new JLabel("Nombre del cliente:");
        gbc.gridx = 0; gbc.gridy = 0;
        clientePanel.add(nombreClienteLabel, gbc);

        gbc.gridx = 1; gbc.gridy = 0; gbc.weightx = 1.0;
        nombreClienteField = new JTextField(20);
        clientePanel.add(nombreClienteField, gbc);

        JPanel pagoPanel = new JPanel(new BorderLayout());

        JPanel leftPagoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel metodoPagoLabel = new JLabel("Método de pago:");
        metodoPagoCombo = new JComboBox<>(new String[]{"Seleccionar método", "Efectivo", "Transferencia"});
        leftPagoPanel.add(metodoPagoLabel);
        leftPagoPanel.add(metodoPagoCombo);

        JPanel rightPagoPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        pagadoCheckBox = new JCheckBox("Pagado");
        pagadoCheckBox.addActionListener(e -> {
            if (pagadoCheckBox.isSelected()) {
                String metodoSeleccionado = (String) metodoPagoCombo.getSelectedItem();
                if (metodoSeleccionado.equals("Seleccionar método")) {
                    JOptionPane.showMessageDialog(this, "Debe seleccionar un método de pago para marcar como pagado.", "Error de Validación", JOptionPane.WARNING_MESSAGE);
                    pagadoCheckBox.setSelected(false);
                }
            }
        });
        rightPagoPanel.add(pagadoCheckBox);

        pagoPanel.add(leftPagoPanel, BorderLayout.WEST);
        pagoPanel.add(rightPagoPanel, BorderLayout.EAST);

        JPanel footerPanel = new JPanel(new BorderLayout());
        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        totalLabel = new JLabel("Total: " + currencyFormatter.format(0));
        totalLabel.setFont(new Font("Arial", Font.BOLD, 16));
        leftPanel.add(totalLabel);

        leftPanel.add(Box.createRigidArea(new Dimension(10, 0)));

        limpiarBtn = new JButton("Limpiar Todo");
        limpiarBtn.setBackground(new Color(200, 200, 200));
        limpiarBtn.setForeground(new Color(43, 43, 43));
        limpiarBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        limpiarBtn.addActionListener(e -> {
            itemsEnPedido.clear();
            actualizarPedido();
            nombreClienteField.setText("");
            pagadoCheckBox.setSelected(false);
            metodoPagoCombo.setSelectedIndex(0);
        });
        leftPanel.add(limpiarBtn);

        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        JButton guardarBtn = new JButton("Guardar Pedido");
        Color verdeBoton = new Color(34, 139, 34); // Forest Green
        guardarBtn.setBackground(verdeBoton);
        guardarBtn.setForeground(Color.WHITE); // Texto blanco para buen contraste
        guardarBtn.setOpaque(true);
        guardarBtn.setBorderPainted(false);
        guardarBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        guardarBtn.addActionListener(e -> {
            guardarPedidoCompleto();
        });
        rightPanel.add(guardarBtn);

        footerPanel.add(leftPanel, BorderLayout.WEST);
        footerPanel.add(rightPanel, BorderLayout.EAST);

        pedidoControls.add(clientePanel);
        pedidoControls.add(Box.createVerticalStrut(10));
        pedidoControls.add(pagoPanel);
        pedidoControls.add(Box.createVerticalStrut(10));
        pedidoControls.add(footerPanel);

        add(pedidoControls, BorderLayout.SOUTH);
    }

    public void agregarProducto(int menuItemId, MenuItem item, String tamano, int cantidad) {
        String clave = menuItemId + " (" + tamano + ")";

        if (item.getPrecios() != null && item.getPrecios().containsKey(tamano)) {
            double precio = item.getPrecios().get(tamano);

            if (itemsEnPedido.containsKey(clave)) {
                PedidoItem itemExistente = itemsEnPedido.get(clave);
                itemExistente.setCantidad(itemExistente.getCantidad() + cantidad);
            } else {
                PedidoItem nuevoItem = new PedidoItem(menuItemId, item.getNombre(), tamano, precio, cantidad);
                itemsEnPedido.put(clave, nuevoItem);
            }
            actualizarPedido();
        } else {
            JOptionPane.showMessageDialog(this, "Error: No se pudo obtener el precio del producto.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // El método `ajustarCantidad` original ha sido eliminado para evitar duplicación.
    // La lógica ahora está encapsulada en la clase `ItemPedidoPanel`.

    /**
     * Refreshes the list of items in the pedido panel.
     */
    // PedidoPanel.java (método actualizado)
    void actualizarPedido() {
        listaPanel.removeAll();
        for (Map.Entry<String, PedidoItem> entry : itemsEnPedido.entrySet()) {
            String clave = entry.getKey();
            PedidoItem item = entry.getValue();

            JPanel itemPanel = new ItemPedidoPanel(
                item,
                () -> {
                    actualizarTotal();
                },
                () -> {
                    eliminarProductoCompleto(clave);
                }
            );

            listaPanel.add(itemPanel);
            // Agrega un separador después de cada panel de ítem
            listaPanel.add(new JSeparator(SwingConstants.HORIZONTAL));
            // Agrega un pequeño espacio para que no esté pegado
            listaPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        }

        actualizarTotal();

        listaPanel.revalidate();
        listaPanel.repaint();
    }

    /**
     * Removes an item completely from the current order.
     * @param clave The key that identifies the product to be removed.
     */
    private void eliminarProductoCompleto(String clave) {
        itemsEnPedido.remove(clave);
        // The list needs to be rebuilt to show the change
        actualizarPedido(); 
    }

    private void actualizarTotal() {
        double total = calcularTotal();
        totalLabel.setText("Total: " + currencyFormatter.format(total));
    }

    public void guardarPedidoCompleto() {
        String nombreCliente = nombreClienteField.getText();
        if (nombreCliente.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Por favor, ingrese el nombre del cliente.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (itemsEnPedido.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No hay productos en el pedido para guardar.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        String metodoPago = (String) metodoPagoCombo.getSelectedItem();
        if ("Seleccionar método".equals(metodoPago)) {
            metodoPago = "No especificado";
        }

        boolean pagado = pagadoCheckBox.isSelected();

        int nuevoPedidoId = DatabaseManager.saveNewPedido(nombreCliente, pagado, metodoPago);
        if (nuevoPedidoId != -1) {
            double totalPedido = calcularTotal();
            for (PedidoItem item : itemsEnPedido.values()) {
                DatabaseManager.savePedidoItem(nuevoPedidoId, item.getMenuItemId(), item.getCantidad(), item.getTamano());
            }
            DatabaseManager.updatePedidoTotal(nuevoPedidoId, totalPedido);
            
            itemsEnPedido.clear();
            actualizarPedido();
            nombreClienteField.setText("");
            pagadoCheckBox.setSelected(false);
            metodoPagoCombo.setSelectedIndex(0);
            
            if (pedidosGuardadosPanel != null) {
                pedidosGuardadosPanel.cargarPedidos();
            }
            JOptionPane.showMessageDialog(this, "El pedido de " + nombreCliente + " se ha guardado correctamente.", "Pedido Guardado", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this, "Error al guardar el pedido. No se pudo crear un ID de pedido.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private double calcularTotal() {
        double total = 0.0;
        for (PedidoItem item : itemsEnPedido.values()) {
            total += item.getPrecioTotal();
        }
        return total;
    }
}