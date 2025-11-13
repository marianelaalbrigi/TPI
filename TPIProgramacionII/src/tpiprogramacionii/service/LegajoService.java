package tpiprogramacionii.service;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import tpiprogramacionii.dao.LegajoDAO;
import tpiprogramacionii.entities.Estado;
import tpiprogramacionii.entities.Legajo;
import tpiprogramacionii.utils.DataBaseConnection;

/**
 * Servicio para la gestión de Legajos
 * Implementa la lógica de negocio y control transaccional para operaciones CRUD
 * Todas las operaciones de escritura (insert/update/delete) manejan transacciones
 * 
 * @author jnowell
 */
public class LegajoService implements GenericService<Legajo> {
    
    private static final Logger LOGGER = Logger.getLogger(LegajoService.class.getName());
    private final LegajoDAO legajoDAO;
    
    /**
     * Constructor con inyección de dependencia
     * @param legajoDAO DAO de Legajo para acceso a datos
     */
    public LegajoService(LegajoDAO legajoDAO) {
        if (legajoDAO == null) {
            throw new IllegalArgumentException("LegajoDAO no puede ser null");
        }
        this.legajoDAO = legajoDAO;
    }
    
    /**
     * Inserta un nuevo legajo con validaciones y control transaccional
     * @param legajo Legajo a insertar
     * @throws Exception Si falla la validación o la transacción
     */
    @Override
    public void insertar(Legajo legajo) throws Exception {
        // Validaciones de negocio
        validarLegajo(legajo);
        
        Connection conn = null;
        try {
            conn = DataBaseConnection.getConnection();
            conn.setAutoCommit(false);
            
            LOGGER.log(Level.INFO, "Iniciando transacción para insertar legajo: {0}", legajo.getNroLegajo());
            
            // Ejecutar inserción
            legajoDAO.insertTx(legajo, conn);
            
            // Commit exitoso
            conn.commit();
            LOGGER.log(Level.INFO, "Legajo insertado exitosamente con ID: {0}", legajo.getId());
            
        } catch (SQLException e) {
            // Rollback en caso de error SQL
            if (conn != null) {
                try {
                    conn.rollback();
                    LOGGER.log(Level.SEVERE, "Rollback ejecutado por error en inserción de legajo", e);
                } catch (SQLException rollbackEx) {
                    LOGGER.log(Level.SEVERE, "Error al hacer rollback", rollbackEx);
                }
            }
            throw new Exception("Error al insertar legajo: " + e.getMessage(), e);
            
        } catch (Exception e) {
            // Rollback en caso de cualquier otro error
            if (conn != null) {
                try {
                    conn.rollback();
                    LOGGER.log(Level.SEVERE, "Rollback ejecutado por error inesperado", e);
                } catch (SQLException rollbackEx) {
                    LOGGER.log(Level.SEVERE, "Error al hacer rollback", rollbackEx);
                }
            }
            throw new Exception("Error inesperado al insertar legajo: " + e.getMessage(), e);
            
        } finally {
            // Restaurar autoCommit y cerrar conexión
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    LOGGER.log(Level.WARNING, "Error al cerrar conexión", e);
                }
            }
        }
    }
    
    /**
     * Actualiza un legajo existente con validaciones y control transaccional
     * @param legajo Legajo con datos actualizados
     * @throws Exception Si falla la validación o la transacción
     */
    @Override
    public void actualizar(Legajo legajo) throws Exception {
        // Validaciones de negocio
        validarLegajoParaActualizar(legajo);
        
        Connection conn = null;
        try {
            conn = DataBaseConnection.getConnection();
            conn.setAutoCommit(false);
            
            LOGGER.log(Level.INFO, "Iniciando transacción para actualizar legajo ID: {0}", legajo.getId());
            
            // Verificar que el legajo existe
            Legajo legajoExistente = legajoDAO.leer(legajo.getId());
            if (legajoExistente == null) {
                throw new IllegalArgumentException("El legajo con ID " + legajo.getId() + " no existe");
            }
            
            // Ejecutar actualización
            legajoDAO.actualizarTx(legajo, conn);
            
            // Commit exitoso
            conn.commit();
            LOGGER.log(Level.INFO, "Legajo actualizado exitosamente ID: {0}", legajo.getId());
            
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                    LOGGER.log(Level.SEVERE, "Rollback ejecutado por error en actualización de legajo", e);
                } catch (SQLException rollbackEx) {
                    LOGGER.log(Level.SEVERE, "Error al hacer rollback", rollbackEx);
                }
            }
            throw new Exception("Error al actualizar legajo: " + e.getMessage(), e);
            
        } catch (Exception e) {
            if (conn != null) {
                try {
                    conn.rollback();
                    LOGGER.log(Level.SEVERE, "Rollback ejecutado por error inesperado", e);
                } catch (SQLException rollbackEx) {
                    LOGGER.log(Level.SEVERE, "Error al hacer rollback", rollbackEx);
                }
            }
            throw new Exception("Error inesperado al actualizar legajo: " + e.getMessage(), e);
            
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    LOGGER.log(Level.WARNING, "Error al cerrar conexión", e);
                }
            }
        }
    }
    
    /**
     * Elimina lógicamente un legajo (marca eliminado = true)
     * @param id ID del legajo a eliminar
     * @throws Exception Si falla la transacción
     */
    @Override
    public void eliminar(int id) throws Exception {
        if (id <= 0) {
            throw new IllegalArgumentException("El ID del legajo debe ser mayor a 0");
        }
        
        Connection conn = null;
        try {
            conn = DataBaseConnection.getConnection();
            conn.setAutoCommit(false);
            
            LOGGER.log(Level.INFO, "Iniciando transacción para eliminar legajo ID: {0}", id);
            
            // Verificar que el legajo existe
            Legajo legajoExistente = legajoDAO.leer(id);
            if (legajoExistente == null) {
                throw new IllegalArgumentException("El legajo con ID " + id + " no existe o ya está eliminado");
            }
            
            // Ejecutar eliminación lógica
            legajoDAO.eliminarTx(id, conn);
            
            // Commit exitoso
            conn.commit();
            LOGGER.log(Level.INFO, "Legajo eliminado exitosamente ID: {0}", id);
            
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                    LOGGER.log(Level.SEVERE, "Rollback ejecutado por error en eliminación de legajo", e);
                } catch (SQLException rollbackEx) {
                    LOGGER.log(Level.SEVERE, "Error al hacer rollback", rollbackEx);
                }
            }
            throw new Exception("Error al eliminar legajo: " + e.getMessage(), e);
            
        } catch (Exception e) {
            if (conn != null) {
                try {
                    conn.rollback();
                    LOGGER.log(Level.SEVERE, "Rollback ejecutado por error inesperado", e);
                } catch (SQLException rollbackEx) {
                    LOGGER.log(Level.SEVERE, "Error al hacer rollback", rollbackEx);
                }
            }
            throw new Exception("Error inesperado al eliminar legajo: " + e.getMessage(), e);
            
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    LOGGER.log(Level.WARNING, "Error al cerrar conexión", e);
                }
            }
        }
    }
    
    /**
     * Obtiene un legajo por su ID
     * @param id ID del legajo a buscar
     * @return Legajo encontrado o null
     * @throws Exception Si ocurre un error al consultar
     */
    @Override
    public Legajo getById(int id) throws Exception {
        if (id <= 0) {
            throw new IllegalArgumentException("El ID del legajo debe ser mayor a 0");
        }
        
        try {
            LOGGER.log(Level.INFO, "Consultando legajo ID: {0}", id);
            return legajoDAO.leer(id);
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al consultar legajo por ID", e);
            throw new Exception("Error al obtener legajo: " + e.getMessage(), e);
        }
    }
    
    /**
     * Obtiene todos los legajos activos
     * @return Lista de legajos activos
     * @throws Exception Si ocurre un error al consultar
     */
    @Override
    public List<Legajo> getAll() throws Exception {
        try {
            LOGGER.log(Level.INFO, "Consultando todos los legajos activos");
            return legajoDAO.leerTodos();
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al consultar todos los legajos", e);
            throw new Exception("Error al obtener lista de legajos: " + e.getMessage(), e);
        }
    }
    
    /**
     * Cambia el estado de un legajo (ACTIVO/INACTIVO)
     * @param id ID del legajo
     * @param nuevoEstado Nuevo estado a asignar
     * @throws Exception Si falla la operación
     */
    public void cambiarEstado(int id, Estado nuevoEstado) throws Exception {
        if (id <= 0) {
            throw new IllegalArgumentException("El ID del legajo debe ser mayor a 0");
        }
        if (nuevoEstado == null) {
            throw new IllegalArgumentException("El estado no puede ser null");
        }
        
        Connection conn = null;
        try {
            conn = DataBaseConnection.getConnection();
            conn.setAutoCommit(false);
            
            LOGGER.log(Level.INFO, "Iniciando transacción para cambiar estado de legajo ID: {0} a {1}", 
                    new Object[]{id, nuevoEstado});
            
            // Verificar que el legajo existe
            Legajo legajoExistente = legajoDAO.leer(id);
            if (legajoExistente == null) {
                throw new IllegalArgumentException("El legajo con ID " + id + " no existe");
            }
            
            // Cambiar estado
            legajoDAO.cambiarEstado(id, nuevoEstado);
            
            // Commit exitoso
            conn.commit();
            LOGGER.log(Level.INFO, "Estado de legajo cambiado exitosamente ID: {0}", id);
            
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                    LOGGER.log(Level.SEVERE, "Rollback ejecutado por error al cambiar estado", e);
                } catch (SQLException rollbackEx) {
                    LOGGER.log(Level.SEVERE, "Error al hacer rollback", rollbackEx);
                }
            }
            throw new Exception("Error al cambiar estado del legajo: " + e.getMessage(), e);
            
        } catch (Exception e) {
            if (conn != null) {
                try {
                    conn.rollback();
                    LOGGER.log(Level.SEVERE, "Rollback ejecutado por error inesperado", e);
                } catch (SQLException rollbackEx) {
                    LOGGER.log(Level.SEVERE, "Error al hacer rollback", rollbackEx);
                }
            }
            throw new Exception("Error inesperado al cambiar estado: " + e.getMessage(), e);
            
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    LOGGER.log(Level.WARNING, "Error al cerrar conexión", e);
                }
            }
        }
    }
    
    // ============= MÉTODOS DE VALIDACIÓN =============
    
    /**
     * Valida los campos obligatorios de un legajo antes de insertar
     * @param legajo Legajo a validar
     * @throws IllegalArgumentException Si falla alguna validación
     */
    private void validarLegajo(Legajo legajo) throws IllegalArgumentException {
        if (legajo == null) {
            throw new IllegalArgumentException("El legajo no puede ser null");
        }
        
        if (legajo.getNroLegajo() == null || legajo.getNroLegajo().trim().isEmpty()) {
            throw new IllegalArgumentException("El número de legajo es obligatorio");
        }
        
        if (legajo.getCategoria() == null || legajo.getCategoria().trim().isEmpty()) {
            throw new IllegalArgumentException("La categoría del legajo es obligatoria");
        }
        
        if (legajo.getEstado() == null) {
            throw new IllegalArgumentException("El estado del legajo es obligatorio");
        }
    }
    
    /**
     * Valida los campos de un legajo antes de actualizar
     * @param legajo Legajo a validar
     * @throws IllegalArgumentException Si falla alguna validación
     */
    private void validarLegajoParaActualizar(Legajo legajo) throws IllegalArgumentException {
        if (legajo == null) {
            throw new IllegalArgumentException("El legajo no puede ser null");
        }
        
        if (legajo.getId() <= 0) {
            throw new IllegalArgumentException("El ID del legajo debe ser mayor a 0");
        }
        
        if (legajo.getCategoria() == null || legajo.getCategoria().trim().isEmpty()) {
            throw new IllegalArgumentException("La categoría del legajo es obligatoria");
        }
    }
}
