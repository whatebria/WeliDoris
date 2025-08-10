package com.welidoris.pedidos.models;

import java.util.Map;

// La clase MenuItem ahora representa tanto productos como promociones.
public class MenuItem {

    // Identificador único del menú.
    private int id;
    
    // Nombre del artículo.
    private String nombre;
    
    // Nombre del archivo de imagen (e.g., "salchipapas.jpeg").
    private String imagen;
    
    // Tipo de artículo (e.g., "Comida", "Bebida", "Promocion").
    private String tipo;
    
    // Indica si el artículo tiene diferentes tamaños.
    private boolean tieneTamanos;
    
    // Almacena los precios por tamaño, cargados desde la base de datos
    private Map<String, Double> precios;

    // Constructor para crear un nuevo item (sin ID, ya que la DB lo asigna).
    public MenuItem(String nombre, String imagen, String tipo, boolean tieneTamanos) {
        this.nombre = nombre;
        this.imagen = imagen;
        this.tipo = tipo;
        this.tieneTamanos = tieneTamanos;
    }
    
    // Constructor para cargar un item desde la base de datos (con ID).
    public MenuItem(int id, String nombre, String imagen, String tipo, boolean tieneTamanos) {
        this.id = id;
        this.nombre = nombre;
        this.imagen = imagen;
        this.tipo = tipo;
        this.tieneTamanos = tieneTamanos;
    }

    // --- Getters y Setters ---

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getImagen() {
        return imagen;
    }

    public void setImagen(String imagen) {
        this.imagen = imagen;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public boolean tieneTamanos() {
        return tieneTamanos;
    }

    public void setTieneTamanos(boolean tieneTamanos) {
        this.tieneTamanos = tieneTamanos;
    }

    // Se agrega un método para establecer los precios del ítem.
    public void setPrecios(Map<String, Double> precios) {
        this.precios = precios;
    }

    // Nuevo método para obtener los precios que ya no usa una lista de strings
    public Map<String, Double> getPrecios() {
        return precios;
    }
}
