package com.welidoris.pedidos.models;

public class PedidoItem {
    // Los campos deben ser privados para encapsulamiento
    private int menuItemId; // Nuevo campo para el ID del item del menu
    private String nombre;
    private String tamano;
    private double precioUnitario;
    private int cantidad;

    public PedidoItem(int menuItemId, String nombre, String tamano, double precioUnitario, int cantidad) {
        this.menuItemId = menuItemId;
        this.nombre = nombre;
        this.tamano = tamano;
        this.precioUnitario = precioUnitario;
        this.cantidad = cantidad;
    }

    // Métodos getter para acceder a los campos
    public int getMenuItemId() {
        return menuItemId;
    }
    
    public String getNombre() {
        return nombre;
    }

    public String getTamano() {
        return tamano;
    }

    public double getPrecioUnitario() {
        return precioUnitario;
    }

    public int getCantidad() {
        return cantidad;
    }

    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
    }
}
