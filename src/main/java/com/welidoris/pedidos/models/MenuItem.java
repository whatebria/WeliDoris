// Archivo: com/welidoris/pedidos/models/MenuItem.java
package com.welidoris.pedidos.models;

import java.util.HashMap;
import java.util.Map;

public abstract class MenuItem {

    protected int id;
    protected String nombre;
    protected String imagen;
    protected boolean tieneTamanos;
    protected Map<String, Double> precios;

    public MenuItem(int id, String nombre, boolean tieneTamanos) {
        this.id = id;
        this.nombre = nombre;
        this.tieneTamanos = tieneTamanos;
        this.precios = new HashMap<>();
    }

    public int getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public String getImagen() {
        return imagen;
    }

    public void setImagen(String imagen) {
        this.imagen = imagen;
    }

    public boolean tieneTamanos() {
        return tieneTamanos;
    }
    
    public void agregarPrecio(String tamano, double precio) {
        precios.put(tamano, precio);
    }
    
    public Map<String, Double> getPrecios() {
        return precios;
    }
}
