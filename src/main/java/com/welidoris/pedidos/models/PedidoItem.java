package com.welidoris.pedidos.models;

/**
 * Clase que representa un item individual en un pedido.
 * Incluye informacion del producto, su tamano, precio y la cantidad.
 */
public class PedidoItem {

    private int menuItemId;
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

    // Getters y Setters
    public int getMenuItemId() {
        return menuItemId;
    }

    public void setMenuItemId(int menuItemId) {
        this.menuItemId = menuItemId;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getTamano() {
        return tamano;
    }

    public void setTamano(String tamano) {
        this.tamano = tamano;
    }

    public double getPrecioUnitario() {
        return precioUnitario;
    }

    public void setPrecioUnitario(double precioUnitario) {
        this.precioUnitario = precioUnitario;
    }

    public int getCantidad() {
        return cantidad;
    }

    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
    }

    /**
     * Calcula y retorna el precio total (subtotal) para este item del pedido.
     * @return El precio unitario multiplicado por la cantidad.
     */
    public double getPrecioTotal() {
        return precioUnitario * cantidad;
    }
}
