package com.welidoris.pedidos.ui;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLightLaf;
import com.welidoris.pedidos.db.DatabaseManager;
import javax.swing.*;
import java.awt.*;
import java.util.Enumeration;

public class MainFrame extends JFrame {
    private boolean isFlatDark = false;
    private JButton themeToggleButton;

    public MainFrame() {
        setTitle("Antojos Weli Doris");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);

        // Contenedor principal con pestañas
        JTabbedPane tabbedPane = new JTabbedPane();

        // 1. Instanciamos los paneles principales
        PedidosCompletadosPanel pedidosCompletadosPanel = new PedidosCompletadosPanel();
        PedidosGuardadosPanel pedidosGuardadosPanel = new PedidosGuardadosPanel(pedidosCompletadosPanel, tabbedPane);

        // 2. Paneles específicos para la pestaña de "Productos"
        PedidoPanel pedidoPanel = new PedidoPanel(pedidosGuardadosPanel);
        MenuPanel menuPanel = new MenuPanel(pedidoPanel);

        // --- Configuración de las pestañas ---

        // Pestaña "Productos": MenuPanel + PedidoPanel en un JSplitPane
        JScrollPane menuScrollPane = new JScrollPane(menuPanel);
        menuScrollPane.getVerticalScrollBar().setUnitIncrement(20);
        menuScrollPane.setBorder(BorderFactory.createTitledBorder("Menú de Productos"));

        JSplitPane productosSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, menuScrollPane, pedidoPanel);
        productosSplitPane.setResizeWeight(0.8);
        tabbedPane.addTab("Productos", productosSplitPane);

        // Pestaña "Pedidos Guardados": Solo el PedidosGuardadosPanel
        JScrollPane pedidosGuardadosScrollPane = new JScrollPane(pedidosGuardadosPanel);
        pedidosGuardadosScrollPane.getVerticalScrollBar().setUnitIncrement(20);
        tabbedPane.addTab("Pedidos Guardados", pedidosGuardadosScrollPane);

        // Pestaña "Pedidos Completados": Solo el PedidosCompletadosPanel
        tabbedPane.addTab("Pedidos Completados", pedidosCompletadosPanel);
        
        // Añadimos el JTabbedPane al centro del JFrame
        add(tabbedPane, BorderLayout.CENTER);

        // --- Panel inferior con el botón de cambio de tema ---
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        themeToggleButton = new JButton("Cambiar a tema Oscuro");
        themeToggleButton.setFocusPainted(false);
        themeToggleButton.addActionListener(e -> toggleTheme());
        bottomPanel.add(themeToggleButton);
        
        // Añadimos el panel inferior al JFrame
        add(bottomPanel, BorderLayout.SOUTH);
    }

    private void toggleTheme() {
        try {
            if (isFlatDark) {
                FlatLightLaf.setup();
                isFlatDark = false;
                themeToggleButton.setText("Cambiar a tema Oscuro");
            } else {
                FlatDarkLaf.setup();
                isFlatDark = true;
                themeToggleButton.setText("Cambiar a tema Claro");
            }
            SwingUtilities.updateComponentTreeUI(this);
        } catch (Exception ex) {
            System.err.println("Failed to initialize Look and Feel");
            ex.printStackTrace();
        }
    }

    public static void main(String[] args) {
        try {
            // Se inicializa con el tema claro por defecto
            FlatLightLaf.setup();
        } catch (Exception ex) {
            System.err.println("Failed to initialize Look and Feel");
        }

        int nuevoTamanoFuente = 15;
        Enumeration<Object> keys = UIManager.getDefaults().keys();
        while (keys.hasMoreElements()) {
            Object key = keys.nextElement();
            Object value = UIManager.get(key);
            if (value instanceof Font originalFont) {
                Font nuevaFuente = new Font(originalFont.getName(), originalFont.getStyle(), nuevoTamanoFuente);
                UIManager.put(key, nuevaFuente);
            }
        }
        
        SwingUtilities.invokeLater(() -> {
            DatabaseManager.initializeDatabase();
            new MainFrame().setVisible(true);
        });
    }
}