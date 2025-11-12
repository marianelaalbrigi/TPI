
package tpiprogramacionii.dao;
import java.sql.Connection;
import java.util.List;

public interface GenericDAO<T> {
    
    public void insertar(T entidad) throws Exception;
    void insertTx(T entidad, Connection conn) throws Exception;
    void actualizar(T entidad)throws Exception;
    void eliminar(int id)throws Exception;
    T leer(int id)throws Exception;
    List<T> leerTodos()throws Exception;

}
