package com.welidoris.pedidos.ui;

import com.welidoris.pedidos.db.DatabaseManager;
import javax.swing.*;
import java.awt.*;
import java.util.Enumeration;
import com.formdev.flatlaf.FlatLightLaf;

public class MainFrame extends JFrame {
    
    public MainFrame() {
        setTitle("Antojos Weli Doris");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setLayout(new BorderLayout(10, 10));

        // Instanciamos el nuevo panel de pedidos completados
        PedidosCompletadosPanel pedidosCompletadosPanel = new PedidosCompletadosPanel();
        
        // Ahora instanciamos el panel de pedidos guardados y le pasamos las referencias
        JTabbedPane tabbedPane = new JTabbedPane();
        PedidosGuardadosPanel pedidosGuardadosPanel = new PedidosGuardadosPanel(pedidosCompletadosPanel, tabbedPane);
        
        PedidoPanel pedidoPanel = new PedidoPanel(pedidosGuardadosPanel);
        MenuPanel menuPanel = new MenuPanel(pedidoPanel);


        // JScrollPane para el menú con la barra de desplazamiento
        JScrollPane menuScrollPane = new JScrollPane(menuPanel);
        menuScrollPane.getVerticalScrollBar().setUnitIncrement(20);
        menuScrollPane.setBorder(BorderFactory.createTitledBorder("Menú de Productos"));

        // JTabbedPane para las pestañas
        tabbedPane.addTab("Productos", menuScrollPane);
        tabbedPane.addTab("Pedidos Guardados", pedidosGuardadosPanel);
        tabbedPane.addTab("Pedidos Completados", pedidosCompletadosPanel);
        
        // JSplitPane para dividir la ventana
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, tabbedPane, pedidoPanel);
        splitPane.setResizeWeight(0.8);
        
        add(splitPane, BorderLayout.CENTER);
    }

    public static void main(String[] args) {
        // Asegúrate de tener FlatLaf en el classpath de tu proyecto
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
        } catch (Exception ex) {
            System.err.println("Failed to initialize Look and Feel");
        }

        // Se restablecen las fuentes por si no se aplican correctamente con FlatLaf
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
