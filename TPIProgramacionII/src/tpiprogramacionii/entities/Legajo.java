
package tpiprogramacionii.entities;

import java.util.Date;
import java.util.Objects;

public class Legajo extends Base  {
    private String nroLegajo;
    private String categoria;
    private Estado estado;
    private Date fechaAlta;
    private String observaciones;

    public Legajo() {
        super();
    }

    public Legajo(Long id, String nroLegajo, String categoria) {
        super(id, false);
        this.nroLegajo = nroLegajo;
        this.categoria = categoria;
        this.estado = estado.ACTIVO;
    }

    public String getNroLegajo() {
        return nroLegajo;
    }

    public void setNroLegajo(String nroLegajo) {
        this.nroLegajo = nroLegajo;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public Estado getEstado() {
        return estado;
    }

    public void setEstado(Estado estado) {
        this.estado = estado;
    }

    public Date getFechaAlta() {
        return fechaAlta;
    }

    public void setFechaAlta(Date fechaAlta) {
        this.fechaAlta = fechaAlta;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    @Override
    public String toString() {
        return "Legajo {"+
                "\nid= "+ getId() + 
                "\nnroLegajo= " + nroLegajo +
                "\ncategoria= " + categoria + 
                "\nestado= " + estado + 
                "\nfechaAlta= " + fechaAlta + 
                "\nobservaciones= " + observaciones + '}';
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 59 * hash + Objects.hashCode(this.nroLegajo);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Legajo other = (Legajo) obj;
        return this.nroLegajo == other.nroLegajo;
    }
        
}
