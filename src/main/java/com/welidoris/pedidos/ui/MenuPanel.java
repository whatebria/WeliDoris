package com.welidoris.pedidos.ui;

import com.welidoris.pedidos.db.DatabaseManager;
import com.welidoris.pedidos.models.MenuItem;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.net.URL;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MenuPanel extends JPanel {

    private static final Logger LOGGER = Logger.getLogger(MenuPanel.class.getName());
    private PedidoPanel pedidoPanel;
    private List<MenuItem> productos;
    private int maxColumns = 3; // Valor por defecto
    private final NumberFormat currencyFormatter;

    public MenuPanel(PedidoPanel pedidoPanel) {
        this.pedidoPanel = pedidoPanel;
        // Se configura el formato de moneda para Chile.
        this.currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("es", "CL"));

        // Se inicializa la lista de productos para evitar errores si la base de datos falla
        productos = new ArrayList<>();

        // Se carga la lista de productos dentro de un bloque try-catch en el constructor
        try {
            productos = DatabaseManager.getMenuItems();
            System.out.println("Se cargaron correctamente los productos desde la base de datos.");
        } catch (SQLException e) {
            System.out.println("Error al cargar los productos desde la base de datos.");
            LOGGER.log(Level.SEVERE, "Error al cargar los productos desde la base de datos.", e);
            JOptionPane.showMessageDialog(null, "Error al conectar con la base de datos: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }

        setLayout(new GridBagLayout());

        // Listener para redimensionar la ventana y ajustar las columnas
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

        // Se dibuja el menú inicial
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
            
            // Verificamos si el mapa de precios es null antes de usarlo
            Map<String, Double> precios = p.getPrecios();
            if (precios != null && p.tieneTamanos()) {
                for (String tamano : precios.keySet()) {
                    tamanoCombo.addItem(tamano);
                }
            } else if (precios != null) {
                // Si no tiene tamaños pero sí precios, se agrega el precio único
                 for (String tamano : precios.keySet()) {
                    tamanoCombo.addItem(tamano);
                }
                tamanoCombo.setEnabled(false);
            } else {
                tamanoCombo.addItem("Error");
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
                
                // Muestra un mensaje si el combo box indica un error
                if ("Error".equals(tamano)) {
                    JOptionPane.showMessageDialog(null, "Error: No se pudo obtener el precio del producto.", "Error", JOptionPane.ERROR_MESSAGE);
                } else if (cantidad > 0) {
                    // Llama al método agregarProducto en PedidoPanel, pasando el objeto MenuItem completo
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
        repaint();
    }
}
