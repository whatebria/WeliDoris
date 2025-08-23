package com.welidoris.pedidos.ui;

import com.welidoris.pedidos.db.DatabaseManager;
import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.ArrayList;
import java.util.stream.Collectors;

public class PedidosCompletadosPanel extends JPanel {

    private static final Logger LOGGER = Logger.getLogger(PedidosCompletadosPanel.class.getName());
    private JScrollPane scrollPane;
    private JPanel mainPanel;
    private JPanel totalesPanel;
    private final NumberFormat currencyFormatter;

    private JComboBox<String> metodoPagoFilter;

    private JLabel totalEfectivoLabel;
    private JLabel totalTransferenciaLabel;
    private JLabel totalGeneralLabel;

    public PedidosCompletadosPanel() {
        this.currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("es", "CL"));
        setLayout(new BorderLayout());

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

        mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        scrollPane = new JScrollPane(mainPanel);
        scrollPane.getVerticalScrollBar().setUnitIncrement(20);

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

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, scrollPane, totalesPanel);
        splitPane.setResizeWeight(0.7);
        add(splitPane, BorderLayout.CENTER);

        cargarTotalesGenerales();
        cargarPedidos("Todos");
    }
    
    public void actualizarResumenTotales() {
        cargarTotalesGenerales();
    }

    private void cargarTotalesGenerales() {
        // ... (el método no cambia)
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
        }

        double totalGeneral = totalEfectivo + totalTransferencia;

        totalEfectivoLabel.setText("Total Efectivo: " + currencyFormatter.format(totalEfectivo));
        totalTransferenciaLabel.setText("Total Transferencia: " + currencyFormatter.format(totalTransferencia));
        totalGeneralLabel.setText("Total General: " + currencyFormatter.format(totalGeneral));
    }

    public void cargarPedidos(String filtroMetodoPago) {
        mainPanel.removeAll();
        mainPanel.revalidate();
        mainPanel.repaint();

        new SwingWorker<List<Map<String, Object>>, Void>() {
            @Override
            protected List<Map<String, Object>> doInBackground() throws Exception {
                // Se obtienen los pedidos
                List<Map<String, Object>> pedidos = DatabaseManager.getPedidosCompletados(filtroMetodoPago);

                // Si no hay pedidos, se retorna la lista vacía
                if (pedidos.isEmpty()) {
                    return pedidos;
                }
                
                // Se obtienen los IDs de los pedidos
                List<Integer> pedidoIds = pedidos.stream()
                                                 .map(p -> (int) p.get("id"))
                                                 .collect(Collectors.toList());

                // Se obtienen todos los ítems de esos pedidos en una sola consulta
                Map<Integer, List<Map<String, Object>>> itemsPorPedido = DatabaseManager.getItemsForPedidos(pedidoIds);
                
                // Se adjuntan los ítems a cada pedido
                for (Map<String, Object> pedido : pedidos) {
                    int pedidoId = (int) pedido.get("id");
                    pedido.put("items", itemsPorPedido.getOrDefault(pedidoId, new ArrayList<>()));
                }
                
                return pedidos;
            }

            @Override
            protected void done() {
                try {
                    List<Map<String, Object>> pedidos = get();
                    if (pedidos.isEmpty()) {
                        mainPanel.add(new JLabel("No hay pedidos completados con este filtro."));
                    } else {
                        for (Map<String, Object> pedido : pedidos) {
                            // Ahora se usa la clase unificada PedidoCard
                            JPanel pedidoCard = new PedidoCard(pedido, false, null);
                            pedidoCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, pedidoCard.getPreferredSize().height));
                            mainPanel.add(pedidoCard);
                            mainPanel.add(Box.createVerticalStrut(15));
                        }
                    }
                    mainPanel.revalidate();
                    mainPanel.repaint();
                } catch (Exception ex) {
                    LOGGER.log(Level.SEVERE, "Error al cargar los pedidos completados.", ex);
                    JOptionPane.showMessageDialog(PedidosCompletadosPanel.this, "Error al cargar los pedidos: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }
}