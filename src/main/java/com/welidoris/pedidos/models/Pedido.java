package com.welidoris.pedidos.models;

import java.util.Date;

public class Pedido {
    private int id;
    private Date fecha;
    private String nombreCliente;
    private boolean pagado;
    private String metodoPago;
    private double total;

    public Pedido(int id, Date fecha, String nombreCliente, boolean pagado, String metodoPago, double total, String guardado) {
        this.id = id;
        this.fecha = fecha;
        this.nombreCliente = nombreCliente;
        this.pagado = pagado;
        this.metodoPago = metodoPago;
        this.total = total;
    }

    public int getId() {
        return id;
    }

    public Date getFecha() {
        return fecha;
    }
    
    public String getNombreCliente() {
        return nombreCliente;
    }

    public boolean isPagado() {
        return pagado;
    }
    
    public String getMetodoPago() {
        return metodoPago;
    }

    public double getTotal() {
        return total;
    }
}