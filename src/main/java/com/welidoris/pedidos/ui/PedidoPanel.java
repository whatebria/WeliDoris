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

public class PedidoPanel extends JPanel {

    private Map<String, PedidoItem> itemsEnPedido;
    private JPanel listaPanel;
    private JLabel totalLabel;
    private final NumberFormat currencyFormatter;

    private JTextField nombreClienteField;
    private JCheckBox pagadoCheckBox;
    private JComboBox<String> metodoPagoCombo;
    private JButton limpiarBtn;
    private JButton guardarBtn;

    public PedidoPanel() {
        // Se configura el formato de moneda para Chile.
        this.currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("es", "CL"));
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createTitledBorder("Tu Pedido"));

        itemsEnPedido = new LinkedHashMap<>();

        // Usamos un JPanel con BoxLayout para poder agregar botones de eliminar
        listaPanel = new JPanel();
        listaPanel.setLayout(new BoxLayout(listaPanel, BoxLayout.Y_AXIS));
        
        // Se anade un JScrollPane para que el contenido sea desplazable
        JScrollPane scrollPane = new JScrollPane(listaPanel);
        scrollPane.setPreferredSize(new Dimension(350, 300));
        add(scrollPane, BorderLayout.CENTER);
        
        JPanel pedidoControls = new JPanel();
        pedidoControls.setLayout(new BoxLayout(pedidoControls, BoxLayout.Y_AXIS));
        pedidoControls.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Panel para el nombre del cliente
        JPanel clientePanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridx = 0; gbc.gridy = 0;
        clientePanel.add(new JLabel("Nombre del cliente:"), gbc);
        
        gbc.gridx = 1; gbc.gridy = 0; gbc.weightx = 1.0;
        nombreClienteField = new JTextField(20);
        clientePanel.add(nombreClienteField, gbc);
        
        // Panel para las opciones de pago
        JPanel pagoPanel = new JPanel(new GridBagLayout());
        gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.weightx = 0;
        
        gbc.gridx = 0; gbc.gridy = 0;
        pagoPanel.add(new JLabel("Método de pago:"), gbc);
        
        gbc.gridx = 1; gbc.gridy = 0;
        metodoPagoCombo = new JComboBox<>(new String[]{"Seleccionar método", "Efectivo", "Transferencia"});
        pagoPanel.add(metodoPagoCombo, gbc);
        
        gbc.gridx = 2; gbc.gridy = 0;
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
        pagoPanel.add(pagadoCheckBox, gbc);
        
        // Panel para total y botones de accion
        JPanel footerPanel = new JPanel(new BorderLayout());
        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        totalLabel = new JLabel("Total: " + currencyFormatter.format(0));
        totalLabel.setFont(new Font("Arial", Font.BOLD, 16));
        limpiarBtn = new JButton("Limpiar");
        limpiarBtn.addActionListener(e -> {
            itemsEnPedido.clear();
            actualizarPedido();
            nombreClienteField.setText("");
            pagadoCheckBox.setSelected(false);
            metodoPagoCombo.setSelectedIndex(0);
        });
        leftPanel.add(totalLabel);
        leftPanel.add(Box.createRigidArea(new Dimension(10, 0)));
        leftPanel.add(limpiarBtn);
        
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        guardarBtn = new JButton("Guardar");
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
    
    private void eliminarProducto(String clave) {
        itemsEnPedido.remove(clave);
        actualizarPedido();
    }

    void actualizarPedido() {
        listaPanel.removeAll();
        double total = 0.0;
        
        for (Map.Entry<String, PedidoItem> entry : itemsEnPedido.entrySet()) {
            String clave = entry.getKey();
            PedidoItem item = entry.getValue();
            
            total += item.getPrecioTotal();
            
            // Panel para cada item con un boton de eliminar
            JPanel itemPanel = new JPanel(new BorderLayout());
            itemPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
            
            // Establecer un tamaño máximo para que el panel no se estire horizontalmente
            itemPanel.setMaximumSize(new Dimension(400, 40));
            // Alinear el panel a la izquierda del BoxLayout
            itemPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
            
            // Texto del item
            String itemText = String.format("<html>%s (%s) x %d - <b>%s</b></html>",
                item.getNombre(),
                item.getTamano(),
                item.getCantidad(),
                currencyFormatter.format(item.getPrecioTotal()));
            
            JLabel itemLabel = new JLabel(itemText);
            itemLabel.setFont(new Font("SansSerif", Font.PLAIN, 16));
            
            // Boton de eliminar
            JButton deleteBtn = new JButton("X");
            deleteBtn.setForeground(Color.RED);
            deleteBtn.setMargin(new Insets(2, 5, 2, 5));
            deleteBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
            deleteBtn.setPreferredSize(new Dimension(30, 25));
            deleteBtn.addActionListener(e -> eliminarProducto(clave));
            
            itemPanel.add(itemLabel, BorderLayout.CENTER);
            itemPanel.add(deleteBtn, BorderLayout.EAST);
            
            listaPanel.add(itemPanel);
            listaPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        }
        
        totalLabel.setText("Total: " + currencyFormatter.format(total));
        
        listaPanel.revalidate();
        listaPanel.repaint();
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
        
        try {
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
                
                JOptionPane.showMessageDialog(this, "El pedido de " + nombreCliente + " se ha guardado correctamente.", "Pedido Guardado", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Error al guardar el pedido. No se pudo crear un ID de pedido.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
             JOptionPane.showMessageDialog(this, "Error al guardar el pedido: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
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
