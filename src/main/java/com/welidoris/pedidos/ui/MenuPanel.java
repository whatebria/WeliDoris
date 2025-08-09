package com.welidoris.pedidos.ui;

import com.welidoris.pedidos.models.MenuItem;
import com.welidoris.pedidos.models.Producto;
import com.welidoris.pedidos.models.Promocion;
import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class MenuPanel extends JPanel {
    private List<MenuItem> productos;
    private PedidoPanel pedidoPanel;
    private int maxColumns;
    private int nextId = 1;

    public MenuPanel(PedidoPanel pedidoPanel) {
        this.pedidoPanel = pedidoPanel;
        
        setLayout(new GridBagLayout());

        productos = new ArrayList<>();
        
        Producto salchipapas = new Producto(nextId++, "Salchipapas", true);
        salchipapas.agregarPrecio("Chica", 1500.0);
        salchipapas.agregarPrecio("Mediana", 2500.0);
        salchipapas.agregarPrecio("Grande", 3500.0);
        salchipapas.setImagen("salchipapas.jpeg");
        
        Producto completo = new Producto(nextId++, "Completo", true);
        completo.agregarPrecio("Chico", 1600.0);
        completo.agregarPrecio("Grande", 2300.0);
        completo.setImagen("completo.jpeg");
        
        Producto completoItaliano = new Producto(nextId++, "Completo Italiano", true);
        completoItaliano.agregarPrecio("Chico", 1800.0);
        completoItaliano.agregarPrecio("Grande", 2500.0);
        completoItaliano.setImagen("completoItaliano.jpeg");
        
        Producto completoDinamico = new Producto(nextId++, "Completo Dinamico", true);
        completoDinamico.agregarPrecio("Chico", 2000.0);
        completoDinamico.agregarPrecio("Grande", 2700.0);
        completoDinamico.setImagen("completoDinamico.jpg");

        Producto assQueso = new Producto(nextId++, "Ass Queso", true);
        assQueso.agregarPrecio("Chico", 2500.0);
        assQueso.agregarPrecio("Grande", 3200.0);
        assQueso.setImagen("assQueso.jpg");
        
        Producto assItaliano = new Producto(nextId++, "Ass Italiano", true);
        assItaliano.agregarPrecio("Chico", 2800.0);
        assItaliano.agregarPrecio("Grande", 3500.0);
        assItaliano.setImagen("assitaliano.jpg");
        
        Producto assDinamico = new Producto(nextId++, "Ass Dinamico", true);
        assDinamico.agregarPrecio("Chico", 3000.0);
        assDinamico.agregarPrecio("Grande", 3700.0);
        assDinamico.setImagen("assdinamico.png");
        
        Producto papasFritas = new Producto(nextId++, "Papas Fritas", true);
        papasFritas.agregarPrecio("Chica", 1000.0);
        papasFritas.agregarPrecio("Mediana", 2000.0);
        papasFritas.agregarPrecio("Grande", 3000.0);
        papasFritas.setImagen("papasfritas.png");
        
        Producto churrasco = new Producto(nextId++, "Churrasco", false);
        churrasco.agregarPrecio("unico", 3500.0);
        churrasco.setImagen("churrasco.jpg");
        
        Producto churrascoItaliano = new Producto(nextId++, "Churrasco Italiano", false);
        churrascoItaliano.agregarPrecio("unico", 3500.0);
        churrascoItaliano.setImagen("churrascoitaliano.jpeg");
        
        Producto chacarero = new Producto(nextId++, "Chacarero", false);
        chacarero.agregarPrecio("unico", 4000.0);
        chacarero.setImagen("chacarero.jpg");

        Promocion promoChurrasco = new Promocion(nextId++, "Promoción Churrasco (2x)", 6000.0);
        promoChurrasco.setImagen("promocionchurrasco.jpeg");
        
        Promocion promoChurrascoItaliano = new Promocion(nextId++, "Promoción Churrasco Italiano 2x", 6000.0);
        promoChurrascoItaliano.setImagen("promocionchurrascoitaliano.jpeg");
        
        Promocion promoChacarero = new Promocion(nextId++, "Promoción Chacarero 2x", 7000.0);
        promoChacarero.setImagen("promocionchacarero.jpg");
        
        Promocion promoChurrascoMasPapas = new Promocion(nextId++, "Promoción Churrasco + Papas", 5000.0);
        promoChurrascoMasPapas.setImagen("promocionchurrascopapas.jpeg");

        productos.add(salchipapas);
        productos.add(completo);
        productos.add(completoItaliano);
        productos.add(completoDinamico);
        productos.add(assItaliano);
        productos.add(assDinamico);
        productos.add(assQueso);
        productos.add(papasFritas);
        productos.add(churrascoItaliano);
        productos.add(chacarero);
        productos.add(promoChurrasco);
        productos.add(promoChurrascoItaliano);
        productos.add(promoChacarero);
        productos.add(promoChurrascoMasPapas);

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                if (getWidth() > 0) {
                    if (getWidth() <= 800) {
                        maxColumns = 1;
                    } else {
                        maxColumns = 2;
                    }
                    dibujarMenu();
                }
            }
        });

        dibujarMenu();
    }
    
    private void dibujarMenu() {
        removeAll();
        revalidate();
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(15, 15, 15, 15);
        gbc.anchor = GridBagConstraints.CENTER;
        
        int col = 0;
        int row = 0;
        
        for (MenuItem p : productos) {
            JPanel productCard = new JPanel();
            productCard.setLayout(new BoxLayout(productCard, BoxLayout.Y_AXIS));

            TitledBorder titledBorder = BorderFactory.createTitledBorder(p.getNombre());
            titledBorder.setTitleFont(new Font("SansSerif", Font.BOLD, 18));

            productCard.setBorder(BorderFactory.createCompoundBorder(
                titledBorder,
                BorderFactory.createEmptyBorder(15, 15, 15, 15)
            ));
            
            productCard.setPreferredSize(new Dimension(480, 320));
            
            URL urlImagen = getClass().getResource("/images/" + p.getImagen());
            
            if (urlImagen != null) {
                ImageIcon originalIcon = new ImageIcon(urlImagen);
                Image originalImage = originalIcon.getImage();
                Image resizedImage = originalImage.getScaledInstance(200, 150, Image.SCALE_SMOOTH);
                
                JLabel imagenLabel = new JLabel(new ImageIcon(resizedImage));
                imagenLabel.setHorizontalAlignment(JLabel.CENTER);
                imagenLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
                productCard.add(imagenLabel);
            } else {
                JLabel noImagenLabel = new JLabel("No hay imagen");
                noImagenLabel.setHorizontalAlignment(JLabel.CENTER);
                noImagenLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
                productCard.add(noImagenLabel);
            }
            
            productCard.add(Box.createVerticalStrut(15));
            
            JPanel opcionesPanel = new JPanel(new GridBagLayout());
            GridBagConstraints gbcOpciones = new GridBagConstraints();
            gbcOpciones.insets = new Insets(5, 5, 5, 5);

            JComboBox<String> tamanoCombo = new JComboBox<>();
            tamanoCombo.setPreferredSize(new Dimension(100, 50));
            
            if (p.tieneTamanos()) {
                for (String tamano : p.getPrecios().keySet()) {
                    tamanoCombo.addItem(tamano);
                }
            } else {
                tamanoCombo.addItem("Único");
                tamanoCombo.setEnabled(false);
            }
            gbcOpciones.gridx = 0;
            gbcOpciones.gridy = 0;
            gbcOpciones.fill = GridBagConstraints.HORIZONTAL;
            opcionesPanel.add(tamanoCombo, gbcOpciones);
            
            JLabel cantLabel = new JLabel("Cant:");
            gbcOpciones.gridx = 1;
            gbcOpciones.gridy = 0;
            opcionesPanel.add(cantLabel, gbcOpciones);
            
            JSpinner cantidadSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 99, 1));
            cantidadSpinner.setPreferredSize(new Dimension(75, 50));
            gbcOpciones.gridx = 2;
            gbcOpciones.gridy = 0;
            opcionesPanel.add(cantidadSpinner, gbcOpciones);

            JButton agregarBtn = new JButton("Agregar");
            
            agregarBtn.setPreferredSize(new Dimension(120, 50));
            agregarBtn.addActionListener(e -> {
                int cantidad = (Integer) cantidadSpinner.getValue();
                String tamano = (String) tamanoCombo.getSelectedItem();
                if (cantidad > 0) {
                    pedidoPanel.agregarProducto(p.getId(), p, tamano, cantidad);
                    
                    cantidadSpinner.setValue(0);
                }
            });
            gbcOpciones.gridx = 3;
            gbcOpciones.gridy = 0;
            opcionesPanel.add(agregarBtn, gbcOpciones);
            
            productCard.add(opcionesPanel);

            gbc.gridx = col;
            gbc.gridy = row;
            add(productCard, gbc);
            
            col++;
            if (col >= maxColumns) {
                col = 0;
                row++;
            }
        }
        
        revalidate();
        repaint();
    }
}