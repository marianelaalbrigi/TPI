
package tpiprogramacionii.dao;
import java.sql.Connection;
import java.util.List;

public interface GenericDAO<T> {
    
    // Métodos autónomos (crean su propia conexión)
    void insertar(T entidad) throws Exception;
    void actualizar(T entidad) throws Exception;
    void eliminar(Long id) throws Exception;
    T leer(Long id) throws Exception;
    List<T> leerTodos() throws Exception;
    
    // Métodos transaccionales (reciben conexión externa)
    void insertTx(T entidad, Connection conn) throws Exception;
    void actualizarTx(T entidad, Connection conn) throws Exception;
    void eliminarTx(Long id, Connection conn) throws Exception;

}
