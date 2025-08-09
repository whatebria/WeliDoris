package com.welidoris.pedidos.models;

public class Promocion extends MenuItem {
    
    // El constructor ahora recibe el ID y se lo pasa a la clase base
    public Promocion(int id, String nombre, double precio) {
        super(id, nombre, false);
        this.agregarPrecio("unico", precio);
    }
}
