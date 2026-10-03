package cl.duoc.model;

public class Pedido {
    private int id;
    private String direccion;
    private String tipo;   // COMIDA, ENCOMIENDA, EXPRESS
    private String estado; // PENDIENTE, EN_REPARTO, ENTREGADO

    public Pedido() {}

    public Pedido(int id, String direccion, String tipo, String estado) {
        this.id = id;
        this.direccion = direccion;
        this.tipo = tipo;
        this.estado = estado;
    }

    public Pedido(String direccion, String tipo, String estado) {
        this.direccion = direccion;
        this.tipo = tipo;
        this.estado = estado;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    @Override
    public String toString() {
        return id + " - " + direccion + " (" + tipo + ")";
    }
}