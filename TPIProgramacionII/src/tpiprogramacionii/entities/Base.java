
package tpiprogramacionii.entities;

public abstract class Base {
    
    private Long id;
    private boolean eliminado;

    protected Base() {
        this.eliminado = false;        
    }

    protected Base(Long id, boolean eliminado) {
        this.id = id;
        this.eliminado = eliminado;
    }
           
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public boolean isEliminado() {
        return eliminado;
    }

    public void setEliminado(boolean eliminado) {
        this.eliminado = eliminado;
    }
            
}