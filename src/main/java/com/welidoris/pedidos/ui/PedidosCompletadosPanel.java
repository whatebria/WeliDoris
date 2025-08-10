package com.welidoris.pedidos.ui;

import com.welidoris.pedidos.db.DatabaseManager;
import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PedidosCompletadosPanel extends JPanel {

    private static final Logger LOGGER = Logger.getLogger(PedidosCompletadosPanel.class.getName());
    private JScrollPane scrollPane;
    private JPanel mainPanel;
    private JPanel totalesPanel;
    private final NumberFormat currencyFormatter;

    private JComboBox<String> metodoPagoFilter;

    // Etiquetas para mostrar los totales
    private JLabel totalEfectivoLabel;
    private JLabel totalTransferenciaLabel;
    private JLabel totalGeneralLabel;

    public PedidosCompletadosPanel() {
        // Se configura el formato de moneda para Chile.
        this.currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("es", "CL"));
        setLayout(new BorderLayout());

        // --- Panel de filtro ---
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filterPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        filterPanel.add(new JLabel("Filtrar por método de pago:"));

        metodoPagoFilter = new JComboBox<>(new String[]{"Todos", "Efectivo", "Transferencia"});
        filterPanel.add(metodoPagoFilter);

        metodoPagoFilter.addActionListener(e -> {
            String filtroSeleccionado = (String) metodoPagoFilter.getSelectedItem();
            cargarPedidos(filtroSeleccionado);
        });

        add(filterPanel, BorderLayout.NORTH);

        // --- Panel principal para los pedidos ---
        mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        scrollPane = new JScrollPane(mainPanel);
        scrollPane.getVerticalScrollBar().setUnitIncrement(20);

        // --- Panel para los totales ---
        totalesPanel = new JPanel(new GridBagLayout());
        totalesPanel.setBorder(BorderFactory.createTitledBorder("Resumen de Totales"));
        totalesPanel.setPreferredSize(new Dimension(300, 0));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        totalEfectivoLabel = new JLabel("Total Efectivo: " + currencyFormatter.format(0));
        totalEfectivoLabel.setFont(new Font("Arial", Font.BOLD, 14));
        gbc.gridx = 0;
        gbc.gridy = 0;
        totalesPanel.add(totalEfectivoLabel, gbc);

        totalTransferenciaLabel = new JLabel("Total Transferencia: " + currencyFormatter.format(0));
        totalTransferenciaLabel.setFont(new Font("Arial", Font.BOLD, 14));
        gbc.gridy = 1;
        totalesPanel.add(totalTransferenciaLabel, gbc);

        JSeparator separator = new JSeparator(SwingConstants.HORIZONTAL);
        gbc.gridy = 2;
        gbc.insets = new Insets(5, 5, 5, 5);
        totalesPanel.add(separator, gbc);

        totalGeneralLabel = new JLabel("Total General: " + currencyFormatter.format(0));
        totalGeneralLabel.setFont(new Font("Arial", Font.BOLD, 16));
        gbc.gridy = 3;
        gbc.insets = new Insets(10, 10, 10, 10);
        totalesPanel.add(totalGeneralLabel, gbc);

        // --- JSplitPane para dividir la vista ---
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, scrollPane, totalesPanel);
        splitPane.setResizeWeight(0.7); // 70% para el panel de pedidos, 30% para el de totales
        add(splitPane, BorderLayout.CENTER);

        // Llamamos a los métodos de carga
        // Primero cargamos los totales generales
        cargarTotalesGenerales();
        // Luego cargamos los pedidos con el filtro inicial "Todos"
        cargarPedidos("Todos");
    }
    
    // Nuevo método público para que otros paneles puedan solicitar una actualización.
    public void actualizarResumenTotales() {
        cargarTotalesGenerales();
    }

    /**
     * Carga el resumen de totales de TODOS los pedidos completados,
     * sin importar el filtro de método de pago.
     */
    private void cargarTotalesGenerales() {
        double totalEfectivo = 0;
        double totalTransferencia = 0;

        String sqlTotales = "SELECT metodo_pago, SUM(total) as total_por_metodo FROM pedidos " +
                            "WHERE pagado = TRUE AND completado = TRUE GROUP BY metodo_pago;";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmtTotales = conn.prepareStatement(sqlTotales);
             ResultSet rsTotales = pstmtTotales.executeQuery()) {

            while (rsTotales.next()) {
                String metodoPago = rsTotales.getString("metodo_pago");
                double total = rsTotales.getDouble("total_por_metodo");

                if ("Efectivo".equals(metodoPago)) {
                    totalEfectivo = total;
                } else if ("Transferencia".equals(metodoPago)) {
                    totalTransferencia = total;
                }
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al calcular los totales generales.", e);
            // No mostramos un JOptionPane para no interrumpir el flujo si falla el cálculo de totales.
        }

        double totalGeneral = totalEfectivo + totalTransferencia;

        // Actualizamos las etiquetas del resumen
        totalEfectivoLabel.setText("Total Efectivo: " + currencyFormatter.format(totalEfectivo));
        totalTransferenciaLabel.setText("Total Transferencia: " + currencyFormatter.format(totalTransferencia));
        totalGeneralLabel.setText("Total General: " + currencyFormatter.format(totalGeneral));
    }

    /**
     * Carga y muestra los pedidos completados, aplicando el filtro de método de pago.
     */
    public void cargarPedidos(String filtroMetodoPago) {
        mainPanel.removeAll();

        String sqlPedidos;

        if ("Todos".equals(filtroMetodoPago)) {
            sqlPedidos = "SELECT id, fecha, nombre_cliente, metodo_pago, total " +
                         "FROM pedidos WHERE pagado = TRUE AND completado = TRUE ORDER BY fecha DESC;";
        } else {
            sqlPedidos = "SELECT id, fecha, nombre_cliente, metodo_pago, total " +
                         "FROM pedidos WHERE pagado = TRUE AND completado = TRUE AND metodo_pago = ? ORDER BY fecha DESC;";
        }

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmtPedidos = conn.prepareStatement(sqlPedidos)) {

            if (!"Todos".equals(filtroMetodoPago)) {
                pstmtPedidos.setString(1, filtroMetodoPago);
            }

            SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");

            try (ResultSet rsPedidos = pstmtPedidos.executeQuery()) {
                if (!rsPedidos.isBeforeFirst()) {
                    mainPanel.add(new JLabel("No hay pedidos completados con este filtro."));
                } else {
                    while (rsPedidos.next()) {
                        int pedidoId = rsPedidos.getInt("id");
                        Date fecha = rsPedidos.getTimestamp("fecha");
                        String nombreCliente = rsPedidos.getString("nombre_cliente");
                        String metodoPago = rsPedidos.getString("metodo_pago");
                        double total = rsPedidos.getDouble("total");

                        JPanel pedidoPanel = new JPanel();
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

                        JLabel infoLabel = new JLabel("<html><b>Fecha:</b> " + dateFormat.format(fecha) + "<br><b>Método de pago:</b> " + metodoPago + "</html>");
                        infoLabel.setFont(new Font("Arial", Font.PLAIN, 14));
                        gbc.gridx = 0;
                        gbc.gridy = 0;
                        pedidoPanel.add(infoLabel, gbc);

                        JLabel totalLabel = new JLabel("<html><b>Total: " + currencyFormatter.format(total) + "</b></html>");
                        totalLabel.setFont(new Font("Arial", Font.BOLD, 14));
                        gbc.gridx = 1;
                        gbc.gridy = 0;
                        gbc.anchor = GridBagConstraints.EAST;
                        pedidoPanel.add(totalLabel, gbc);

                        DefaultListModel<String> productosListModel = new DefaultListModel<>();

                        String sqlItems = "SELECT mi.nombre, pi.cantidad, pi.tamano " +
                                          "FROM pedidos_items pi " +
                                          "JOIN menu_items mi ON pi.menu_item_id = mi.id " +
                                          "WHERE pi.pedido_id = ?;";

                        try (PreparedStatement pstmtItems = conn.prepareStatement(sqlItems)) {
                            pstmtItems.setInt(1, pedidoId);
                            try (ResultSet rsItems = pstmtItems.executeQuery()) {
                                while (rsItems.next()) {
                                    String nombreProducto = rsItems.getString("nombre");
                                    int cantidad = rsItems.getInt("cantidad");
                                    String tamano = rsItems.getString("tamano");
                                    productosListModel.addElement(" - " + nombreProducto + " (" + tamano + ") x " + cantidad);
                                }
                            }
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
                        pedidoPanel.add(productosScrollPane, gbc);

                        mainPanel.add(pedidoPanel);
                        mainPanel.add(Box.createVerticalStrut(15));
                    }
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al cargar los pedidos completados desde la base de datos.", e);
            JOptionPane.showMessageDialog(this, "Error al cargar los pedidos completados: " + e.getMessage(), "Error de Base de Datos", JOptionPane.ERROR_MESSAGE);
        }

        mainPanel.revalidate();
        mainPanel.repaint();
    }
}
