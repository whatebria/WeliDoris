package com.welidoris.pedidos.ui;

import com.welidoris.pedidos.db.DatabaseManager;
import com.welidoris.pedidos.models.MenuItem;
import com.welidoris.pedidos.models.PedidoItem;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.sql.SQLException;
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
    private JButton limpiarBtn; // Boton para limpiar todo el pedido
    private JButton guardarBtn;

    /**
     * Constructor que recibe una referencia al panel de pedidos guardados para poder notificarle las actualizaciones.
     * @param pedidosGuardadosPanel La instancia del panel de pedidos guardados.
     */
    public PedidoPanel(PedidosGuardadosPanel pedidosGuardadosPanel) {
        this.pedidosGuardadosPanel = pedidosGuardadosPanel;
        // Se configura el formato de moneda para Chile.
        this.currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("es", "CL"));
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createTitledBorder(
                // Borde y titulo con colores oscuros para el nuevo fondo claro
                BorderFactory.createLineBorder(new Color(150, 150, 150)),
                "Tu Pedido",
                TitledBorder.LEFT, TitledBorder.TOP,
                new Font("SansSerif", Font.BOLD, 14),
                new Color(43, 43, 43)
        ));
        // Fondo claro para el panel principal
        setBackground(new Color(245, 245, 245));
        setForeground(new Color(43, 43, 43));

        itemsEnPedido = new LinkedHashMap<>();

        listaPanel = new JPanel();
        listaPanel.setLayout(new BoxLayout(listaPanel, BoxLayout.Y_AXIS));
        // Fondo blanco para la lista de productos
        listaPanel.setBackground(new Color(255, 255, 255));

        JScrollPane scrollPane = new JScrollPane(listaPanel);
        scrollPane.setPreferredSize(new Dimension(350, 300));
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        add(scrollPane, BorderLayout.CENTER);

        JPanel pedidoControls = new JPanel();
        pedidoControls.setLayout(new BoxLayout(pedidoControls, BoxLayout.Y_AXIS));
        pedidoControls.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        // Fondo claro para el panel de controles
        pedidoControls.setBackground(new Color(245, 245, 245));

        // Panel para el nombre del cliente
        JPanel clientePanel = new JPanel(new GridBagLayout());
        clientePanel.setBackground(new Color(245, 245, 245));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        JLabel nombreClienteLabel = new JLabel("Nombre del cliente:");
        nombreClienteLabel.setForeground(new Color(43, 43, 43));
        gbc.gridx = 0; gbc.gridy = 0;
        clientePanel.add(nombreClienteLabel, gbc);

        gbc.gridx = 1; gbc.gridy = 0; gbc.weightx = 1.0;
        nombreClienteField = new JTextField(20);
        nombreClienteField.setBackground(new Color(255, 255, 255));
        nombreClienteField.setForeground(new Color(43, 43, 43));
        clientePanel.add(nombreClienteField, gbc);

        // Panel para las opciones de pago
        // Se ha cambiado de GridBagLayout a BorderLayout para alinear los elementos a los lados
        JPanel pagoPanel = new JPanel(new BorderLayout());
        pagoPanel.setBackground(new Color(245, 245, 245));

        // Panel izquierdo para el método de pago
        JPanel leftPagoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        leftPagoPanel.setBackground(new Color(245, 245, 245));
        JLabel metodoPagoLabel = new JLabel("Método de pago:");
        metodoPagoLabel.setForeground(new Color(43, 43, 43));
        metodoPagoCombo = new JComboBox<>(new String[]{"Seleccionar método", "Efectivo", "Transferencia"});
        metodoPagoCombo.setBackground(new Color(255, 255, 255));
        metodoPagoCombo.setForeground(new Color(43, 43, 43));
        leftPagoPanel.add(metodoPagoLabel);
        leftPagoPanel.add(metodoPagoCombo);

        // Panel derecho para la casilla de "Pagado"
        JPanel rightPagoPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        rightPagoPanel.setBackground(new Color(245, 245, 245));
        pagadoCheckBox = new JCheckBox("Pagado");
        pagadoCheckBox.setBackground(new Color(245, 245, 245));
        pagadoCheckBox.setForeground(new Color(43, 43, 43));
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

        // Panel para total y botones de accion
        JPanel footerPanel = new JPanel(new BorderLayout());
        footerPanel.setBackground(new Color(245, 245, 245));
        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        leftPanel.setBackground(new Color(245, 245, 245));

        // Color del texto cambiado a oscuro para el fondo claro
        totalLabel = new JLabel("Total: " + currencyFormatter.format(0));
        totalLabel.setFont(new Font("Arial", Font.BOLD, 16));
        totalLabel.setForeground(new Color(0, 0, 0));
        leftPanel.add(totalLabel);

        leftPanel.add(Box.createRigidArea(new Dimension(10, 0)));

        limpiarBtn = new JButton("Limpiar Todo");
        // Color del botón "Limpiar Todo" cambiado a gris claro con texto oscuro
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
        rightPanel.setBackground(new Color(245, 245, 245));

        guardarBtn = new JButton("Guardar Pedido");
        guardarBtn.setBackground(new Color(50, 200, 50)); // Verde para la accion de guardar
        guardarBtn.setForeground(Color.WHITE);
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

    /**
     * Ajusta la cantidad de un producto en el pedido. Si la cantidad llega a 0, lo elimina.
     * @param clave La clave que identifica el producto.
     * @param cambio La cantidad a anadir (positivo) o restar (negativo).
     */
    private void ajustarCantidad(String clave, int cambio) {
        PedidoItem item = itemsEnPedido.get(clave);
        if (item != null) {
            int nuevaCantidad = item.getCantidad() + cambio;
            if (nuevaCantidad > 0) {
                item.setCantidad(nuevaCantidad);
            } else {
                // Si la cantidad llega a 0, se elimina el item
                itemsEnPedido.remove(clave);
            }
            actualizarPedido();
        }
    }

    /**
     * Elimina un producto por completo del pedido, sin importar la cantidad.
     * @param clave La clave que identifica el producto a eliminar.
     */
    private void eliminarProductoCompleto(String clave) {
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

            // Panel para cada item con botones de +/- y X, mas un label
            JPanel itemPanel = new JPanel(new BorderLayout());
            itemPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
            itemPanel.setMaximumSize(new Dimension(400, 40));
            itemPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
            // Fondo blanco para cada item
            itemPanel.setBackground(new Color(255, 255, 255));
            
            // Panel de control de cantidad con espaciado mejorado
            JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
            controlPanel.setBackground(new Color(255, 255, 255));

            // Boton para disminuir la cantidad
            JButton minusBtn = new JButton("-");
            // Botones con color de fondo gris claro y texto oscuro
            minusBtn.setBackground(new Color(200, 200, 200));
            minusBtn.setForeground(new Color(43, 43, 43));
            minusBtn.setMargin(new Insets(2, 5, 2, 5));
            // Se ha hecho el boton mas grande
            minusBtn.setPreferredSize(new Dimension(35, 30));
            minusBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
            minusBtn.addActionListener(e -> ajustarCantidad(clave, -1));

            // Boton para aumentar la cantidad
            JButton plusBtn = new JButton("+");
            // Botones con color de fondo gris claro y texto oscuro
            plusBtn.setBackground(new Color(200, 200, 200));
            plusBtn.setForeground(new Color(43, 43, 43));
            plusBtn.setMargin(new Insets(2, 5, 2, 5));
            // Se ha hecho el boton mas grande
            plusBtn.setPreferredSize(new Dimension(35, 30));
            plusBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
            plusBtn.addActionListener(e -> ajustarCantidad(clave, 1));
            
            // Label de la cantidad
            JLabel cantidadLabel = new JLabel("x" + item.getCantidad());
            cantidadLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
            // Texto oscuro para el fondo claro
            cantidadLabel.setForeground(new Color(43, 43, 43));
            
            // Boton para eliminar el producto por completo
            JButton eliminarBtn = new JButton("X");
            eliminarBtn.setBackground(new Color(200, 50, 50));
            eliminarBtn.setForeground(Color.WHITE);
            eliminarBtn.setMargin(new Insets(2, 5, 2, 5));
            // Se ha hecho el boton mas grande
            eliminarBtn.setPreferredSize(new Dimension(35, 30));
            eliminarBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
            eliminarBtn.addActionListener(e -> eliminarProductoCompleto(clave));
            
            // Se agrega un Box.createHorizontalStrut para agregar espacio entre el label y los botones
            controlPanel.add(minusBtn);
            controlPanel.add(Box.createHorizontalStrut(5));
            controlPanel.add(cantidadLabel);
            controlPanel.add(Box.createHorizontalStrut(5));
            controlPanel.add(plusBtn);
            controlPanel.add(Box.createHorizontalStrut(10)); // Espacio extra para separar del boton de eliminar
            controlPanel.add(eliminarBtn);

            // Texto del item con color oscuro para el fondo claro
            String itemText = String.format("<html><p style='color:#434343;'>%s (%s) - <b>%s</b></p></html>",
                item.getNombre(),
                item.getTamano(),
                currencyFormatter.format(item.getPrecioTotal()));
            
            JLabel itemLabel = new JLabel(itemText);
            itemLabel.setFont(new Font("SansSerif", Font.PLAIN, 16));

            itemPanel.add(itemLabel, BorderLayout.CENTER);
            itemPanel.add(controlPanel, BorderLayout.EAST);

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
