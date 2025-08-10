package com.welidoris.pedidos.ui;

import com.welidoris.pedidos.db.DatabaseManager;
import com.welidoris.pedidos.models.MenuItem;
import com.welidoris.pedidos.models.PedidoItem; 
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public class PedidoPanel extends JPanel {
    private JTextArea areaPedido;
    private JLabel totalLabel;
    
    private Map<String, PedidoItem> itemsEnPedido; 
    
    private JTextField nombreClienteField;
    private JCheckBox pagadoCheckBox;
    private JComboBox<String> metodoPagoCombo;
    
    private JButton limpiarBtn;
    private JButton guardarBtn;

    public PedidoPanel() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createTitledBorder("Tu Pedido"));
        
        itemsEnPedido = new LinkedHashMap<>();

        areaPedido = new JTextArea(10, 30);
        areaPedido.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(areaPedido);
        add(scrollPane, BorderLayout.CENTER);

        JPanel pedidoControls = new JPanel();
        pedidoControls.setLayout(new BoxLayout(pedidoControls, BoxLayout.Y_AXIS));
        pedidoControls.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
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
        
        JPanel southPanel = new JPanel(new BorderLayout());
        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        totalLabel = new JLabel("Total: $0.0");
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

        southPanel.add(leftPanel, BorderLayout.WEST);
        southPanel.add(rightPanel, BorderLayout.EAST);
        
        pedidoControls.add(clientePanel);
        pedidoControls.add(Box.createVerticalStrut(10));
        pedidoControls.add(pagoPanel);
        pedidoControls.add(Box.createVerticalStrut(10));
        pedidoControls.add(southPanel);

        add(pedidoControls, BorderLayout.SOUTH);
    }
    
    public void agregarProducto(int menuItemId, MenuItem item, String tamano, int cantidad) {
        String clave = menuItemId + " (" + tamano + ")";
        
        // Verifica si el mapa de precios no es nulo antes de acceder a el
        if (item.getPrecios() != null) {
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
            // Muestra un mensaje de error si el precio no se pudo obtener
            JOptionPane.showMessageDialog(this, "Error: No se pudo obtener el precio del producto.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private double calcularTotal() {
        double total = 0.0;
        for (PedidoItem item : itemsEnPedido.values()) {
            total += item.getPrecioUnitario() * item.getCantidad();
        }
        return total;
    }

    void actualizarPedido() {
        areaPedido.setText("");
        double total = calcularTotal();
        
        for (PedidoItem item : itemsEnPedido.values()) {
            double subtotal = item.getPrecioUnitario() * item.getCantidad();
            areaPedido.append(String.format("%s (%s) x %d  -> $%.2f\n",
                    item.getNombre(), item.getTamano(), item.getCantidad(), subtotal));
        }
        
        totalLabel.setText(String.format("Total: $%.2f", total));
        System.out.println("Pedido actualizado. Total de items: " + itemsEnPedido.size());
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
            
            JOptionPane.showMessageDialog(this, "El pedido de " + nombreCliente + " se ha guardado correctamente.", "Pedido Guardado", JOptionPane.INFORMATION_MESSAGE);
            
        } else {
            JOptionPane.showMessageDialog(this, "Error al guardar el pedido. No se pudo crear un ID de pedido.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}