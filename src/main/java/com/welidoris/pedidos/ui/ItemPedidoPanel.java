// ItemPedidoPanel.java
package com.welidoris.pedidos.ui;

import com.welidoris.pedidos.models.PedidoItem;

import javax.swing.*;
import java.awt.*;
import java.text.NumberFormat;
import java.util.Locale;

public class ItemPedidoPanel extends JPanel {

    private PedidoItem item; // Guardamos el objeto PedidoItem
    private JLabel cantidadLabel;
    private Runnable onTotalUpdate; // Para notificar a PedidoPanel sobre el cambio de total
    private Runnable onDelete;

    public ItemPedidoPanel(PedidoItem item, Runnable onTotalUpdate, Runnable onDelete) {
        this.item = item;
        this.onTotalUpdate = onTotalUpdate;
        this.onDelete = onDelete;

        setLayout(new GridBagLayout());
        setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        setMaximumSize(new Dimension(400, 60));
        setAlignmentX(Component.LEFT_ALIGNMENT);

        GridBagConstraints gbc = new GridBagConstraints();
        NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("es", "CL"));

        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));

        JButton minusBtn = new JButton("-");
        styleButton(minusBtn, new Color(200, 200, 200), new Color(43, 43, 43));
        minusBtn.addActionListener(e -> {
            ajustarCantidad(-1);
            if (onTotalUpdate != null) onTotalUpdate.run();
        });

        JButton plusBtn = new JButton("+");
        styleButton(plusBtn, new Color(200, 200, 200), new Color(43, 43, 43));
        plusBtn.addActionListener(e -> {
            ajustarCantidad(1);
            if (onTotalUpdate != null) onTotalUpdate.run();
        });

        cantidadLabel = new JLabel("x" + this.item.getCantidad());
        cantidadLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        
        JButton eliminarBtn = new JButton("X");
        styleButton(eliminarBtn, new Color(200, 50, 50), Color.WHITE);
        eliminarBtn.addActionListener(e -> {
            onDelete.run();
        });

        controlPanel.add(minusBtn);
        controlPanel.add(Box.createHorizontalStrut(5));
        controlPanel.add(cantidadLabel);
        controlPanel.add(Box.createHorizontalStrut(5));
        controlPanel.add(plusBtn);
        controlPanel.add(Box.createHorizontalStrut(10));
        controlPanel.add(eliminarBtn);

        String itemText = String.format("<html>%s (%s) - <b>%s</b></p></html>",
                this.item.getNombre(),
                this.item.getTamano(),
                currencyFormatter.format(this.item.getPrecioTotal()));
                
        JLabel itemLabel = new JLabel(itemText);
        itemLabel.setFont(new Font("SansSerif", Font.PLAIN, 16));

        gbc.gridx = 0; gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.insets = new Insets(0, 0, 0, 10);
        add(itemLabel, gbc);

        gbc.gridx = 1; gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0.0;
        gbc.insets = new Insets(0, 0, 0, 0);
        add(controlPanel, gbc);
    }

    private void ajustarCantidad(int cambio) {
        int nuevaCantidad = item.getCantidad() + cambio;
        if (nuevaCantidad > 0) {
            item.setCantidad(nuevaCantidad);
            // Solo actualizamos el label de la cantidad
            cantidadLabel.setText("x" + item.getCantidad());
        } else {
            // Si la cantidad llega a 0, se llama al método de eliminación
            onDelete.run();
        }
    }
    
    private void styleButton(JButton button, Color background, Color foreground) {
        button.setBackground(background);
        button.setForeground(foreground);
        button.setMargin(new Insets(2, 5, 2, 5));
        button.setPreferredSize(new Dimension(35, 30));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }
}