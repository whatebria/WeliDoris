package com.welidoris.pedidos.ui;

import com.welidoris.pedidos.db.DatabaseManager;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class PedidosGuardadosPanel extends JPanel {
    private static final Logger LOGGER = Logger.getLogger(PedidosGuardadosPanel.class.getName());
    private JScrollPane scrollPane;
    private JPanel mainPanel;
    private PedidosCompletadosPanel pedidosCompletadosPanel;
    private int maxColumns = 1;

    public PedidosGuardadosPanel(PedidosCompletadosPanel completadosPanel, JTabbedPane tabbedPane) {
        this.pedidosCompletadosPanel = completadosPanel;
        NumberFormat.getCurrencyInstance(new Locale("es", "CL"));

        setLayout(new BorderLayout());

        mainPanel = new JPanel();
        mainPanel.setLayout(new GridBagLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        scrollPane = new JScrollPane(mainPanel);
        scrollPane.getVerticalScrollBar().setUnitIncrement(20);
        
        mainPanel.addMouseWheelListener(new MouseWheelListener() {
            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                JScrollBar verticalScrollBar = scrollPane.getVerticalScrollBar();
                int unitsToScroll = e.getUnitsToScroll();
                int newPosition = verticalScrollBar.getValue() + unitsToScroll * verticalScrollBar.getUnitIncrement();
                verticalScrollBar.setValue(newPosition);
            }
        });
        
        add(scrollPane, BorderLayout.CENTER);
        
        cargarPedidos();
    }
    
    public void cargarPedidos() {
        mainPanel.removeAll();
        mainPanel.revalidate();
        mainPanel.repaint();

        new SwingWorker<List<Map<String, Object>>, Void>() {
            @Override
            protected List<Map<String, Object>> doInBackground() throws Exception {
                return DatabaseManager.getPedidosGuardados();
            }

            @Override
            protected void done() {
                try {
                    List<Map<String, Object>> pedidos = get();
                    if (pedidos.isEmpty()) {
                        mainPanel.add(new JLabel("No hay pedidos guardados."));
                    } else {
                        int col = 0;
                        int row = 0;
                        GridBagConstraints gbcMain = new GridBagConstraints();
                        gbcMain.insets = new Insets(10, 10, 10, 10);
                        gbcMain.anchor = GridBagConstraints.NORTHWEST;
                        gbcMain.fill = GridBagConstraints.HORIZONTAL;
                        gbcMain.weightx = 1.0; 

                        for (Map<String, Object> pedido : pedidos) {
                            // Ahora se usa la clase unificada PedidoCard
                            JPanel pedidoCard = new PedidoCard(
                                pedido,
                                true, // Es editable
                                () -> {
                                    cargarPedidos();
                                    pedidosCompletadosPanel.cargarPedidos("Todos");
                                    pedidosCompletadosPanel.actualizarResumenTotales();
                                }
                            );

                            gbcMain.gridx = col;
                            gbcMain.gridy = row;
                            mainPanel.add(pedidoCard, gbcMain);

                            col++;
                            if (col >= maxColumns) {
                                col = 0;
                                row++;
                            }
                        }
                        
                        GridBagConstraints gbcFiller = new GridBagConstraints();
                        gbcFiller.gridx = 0;
                        gbcFiller.gridy = row + 1;
                        gbcFiller.weighty = 1.0; 
                        gbcFiller.fill = GridBagConstraints.VERTICAL;
                        mainPanel.add(Box.createVerticalStrut(1), gbcFiller);
                    }
                    mainPanel.revalidate();
                    mainPanel.repaint();
                } catch (Exception ex) {
                    LOGGER.log(Level.SEVERE, "Error al cargar los pedidos guardados.", ex);
                    JOptionPane.showMessageDialog(PedidosGuardadosPanel.this, "Error al cargar los pedidos: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }
}