package tpiprogramacionii.service;

import java.util.List;

/**
 * Interfaz genérica para la capa de servicios
 * Define las operaciones CRUD básicas con manejo transaccional
 * Todas las operaciones incluyen validaciones de negocio y control de transacciones
 * 
 * @param <T> Tipo de entidad que maneja el servicio
 * @author jnowell
 */
public interface GenericService<T> {
    
    /**
     * Inserta una nueva entidad en la base de datos
     * Maneja la transacción completa (commit/rollback)
     * @param entidad La entidad a insertar
     * @throws Exception Si falla la validación o la inserción
     */
    void insertar(T entidad) throws Exception;
    
    /**
     * Actualiza una entidad existente en la base de datos
     * Maneja la transacción completa (commit/rollback)
     * @param entidad La entidad a actualizar
     * @throws Exception Si falla la validación o la actualización
     */
    void actualizar(T entidad) throws Exception;
    
    /**
     * Elimina lógicamente una entidad (marca eliminado = true)
     * Maneja la transacción completa (commit/rollback)
     * @param id ID de la entidad a eliminar
     * @throws Exception Si falla la eliminación
     */
    void eliminar(int id) throws Exception;
    
    /**
     * Obtiene una entidad por su ID
     * @param id ID de la entidad a buscar
     * @return La entidad encontrada o null si no existe
     * @throws Exception Si ocurre un error al consultar
     */
    T getById(int id) throws Exception;
    
    /**
     * Obtiene todas las entidades activas (eliminado = false)
     * @return Lista de entidades activas
     * @throws Exception Si ocurre un error al consultar
     */
    List<T> getAll() throws Exception;
    
}
