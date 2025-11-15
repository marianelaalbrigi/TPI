
package tpiprogramacionii.entities;

import java.util.Date;
import java.util.Objects;

public class Empleado extends Base {
    private String nombre;
    private String apellido;
    private String dni;
    private String email;
    private Date fechaIngreso;
    private String area;
    private Legajo legajo;

    public Empleado() {
        super();
    }

    //Para crear un empleado, son necesarios los siguientes atributos
    public Empleado(Long id, String nombre, String apellido, String dni) {
        super(id, false);
        this.nombre = nombre;
        this.apellido = apellido;
        this.dni = dni;  
    }
     
    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getApellido() {
        return apellido;
    }

    public void setApellido(String apellido) {
        this.apellido = apellido;
    }

    public String getDni() {
        return dni;
    }

    public void setDni(String dni) {
        this.dni = dni;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Date getFechaIngreso() {
        return fechaIngreso;
    }

    public void setFechaIngreso(Date fechaIngreso) {
        this.fechaIngreso = fechaIngreso;
    }

    public String getArea() {
        return area;
    }

    public void setArea(String area) {
        this.area = area;
    }

    public Legajo getLegajo() {
        return legajo;
    }

    
    //Asociación con legajo
    public void setLegajo(Legajo legajo) {
        this.legajo = legajo;
    }

    @Override
    public String toString() {
        return "Empleado {" + 
                "\nid= "+ getId() +
                "\nnombre= " + nombre + 
                "\napellido= " + apellido + 
                "\ndni= " + dni + 
                "\nemail= " + email + 
                "\nfecha_ingreso= " + fechaIngreso + 
                "\narea= " + area + 
                "\nnro de legajo =" + legajo.getNroLegajo();
    }
    
    //Hashcode basado en el DNI
    @Override
    public int hashCode() {
        int hash = 5;
        hash = 37 * hash + Objects.hashCode(this.dni);
        return hash;
    }

    // Se verifica si existen dos personas iguales comparando sus DNIs      
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
        final Empleado other = (Empleado) obj;
        return Objects.equals(this.dni, other.dni);
    }
}
