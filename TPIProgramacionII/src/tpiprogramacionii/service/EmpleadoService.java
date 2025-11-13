package tpiprogramacionii.service;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import tpiprogramacionii.dao.EmpleadoDAO;
import tpiprogramacionii.dao.LegajoDAO;
import tpiprogramacionii.entities.Empleado;
import tpiprogramacionii.entities.Estado;
import tpiprogramacionii.entities.Legajo;
import tpiprogramacionii.utils.DataBaseConnection;



public class EmpleadoService implements GenericService<Empleado> {
    
    private static final Logger LOGGER = Logger.getLogger(EmpleadoService.class.getName());
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final Pattern DNI_PATTERN = Pattern.compile("^[0-9]{7,8}$");
    
    private final EmpleadoDAO empleadoDAO;
    private final LegajoDAO legajoDAO;
    
    /**
     * Constructor con inyección de dependencias
     * @param empleadoDAO DAO de Empleado
     * @param legajoDAO DAO de Legajo
     */
    public EmpleadoService(EmpleadoDAO empleadoDAO, LegajoDAO legajoDAO) {
        if (empleadoDAO == null) {
            throw new IllegalArgumentException("EmpleadoDAO no puede ser null");
        }
        if (legajoDAO == null) {
            throw new IllegalArgumentException("LegajoDAO no puede ser null");
        }
        this.empleadoDAO = empleadoDAO;
        this.legajoDAO = legajoDAO;
    }
    
    /**
     * Inserta un nuevo empleado junto con su legajo en una transacción atómica
     * Garantiza la relación 1:1 (un empleado tiene exactamente un legajo)
     * Secuencia: 1) Insertar Empleado, 2) Crear Legajo con ID del empleado, 3) Asociar
     * 
     * @param empleado Empleado a insertar (debe tener nombre, apellido y DNI)
     * @throws Exception Si falla la validación o la transacción
     */
    @Override
    public void insertar(Empleado empleado) throws Exception {
        // Validaciones de negocio
        validarEmpleado(empleado);
        validarDniUnico(empleado.getDni());
        
        Connection conn = null;
        try {
            conn = DataBaseConnection.getConnection();
            conn.setAutoCommit(false);
            
            LOGGER.log(Level.INFO, "Iniciando transacción para insertar empleado: {0} {1}", 
                    new Object[]{empleado.getNombre(), empleado.getApellido()});
            
            // 1. Insertar Empleado (obtiene ID autogenerado)
            empleadoDAO.insertTx(empleado, conn);
            LOGGER.log(Level.INFO, "Empleado insertado con ID: {0}", empleado.getId());
            
            // 2. Crear Legajo automáticamente con el ID del empleado
            Legajo legajo = new Legajo();
            legajo.setNroLegajo("LEG" + String.format("%06d", empleado.getId()));
            legajo.setCategoria("JUNIOR"); // Categoría por defecto
            legajo.setEstado(Estado.ACTIVO);
            legajo.setFechaAlta(new java.util.Date());
            
            // 3. Insertar Legajo
            legajoDAO.insertTx(legajo, conn);
            LOGGER.log(Level.INFO, "Legajo creado con número: {0}", legajo.getNroLegajo());
            
            // 4. Asociar legajo al empleado (relación 1:1)
            empleado.setLegajo(legajo);
            
            // Commit exitoso
            conn.commit();
            LOGGER.log(Level.INFO, "Empleado y Legajo insertados exitosamente - Transacción completada");
            
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                    LOGGER.log(Level.SEVERE, "Rollback ejecutado por error SQL en inserción de empleado", e);
                } catch (SQLException rollbackEx) {
                    LOGGER.log(Level.SEVERE, "Error crítico al hacer rollback", rollbackEx);
                }
            }
            throw new Exception("Error al insertar empleado: " + e.getMessage(), e);
            
        } catch (Exception e) {
            if (conn != null) {
                try {
                    conn.rollback();
                    LOGGER.log(Level.SEVERE, "Rollback ejecutado por error inesperado", e);
                } catch (SQLException rollbackEx) {
                    LOGGER.log(Level.SEVERE, "Error crítico al hacer rollback", rollbackEx);
                }
            }
            throw new Exception("Error inesperado al insertar empleado: " + e.getMessage(), e);
            
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
     * Actualiza el área de un empleado existente con control transaccional
     * @param empleado Empleado con datos actualizados (requiere ID y área válidos)
     * @throws Exception Si falla la validación o la transacción
     */
    @Override
    public void actualizar(Empleado empleado) throws Exception {
        // Validaciones de negocio
        validarEmpleadoParaActualizar(empleado);
        
        Connection conn = null;
        try {
            conn = DataBaseConnection.getConnection();
            conn.setAutoCommit(false);
            
            LOGGER.log(Level.INFO, "Iniciando transacción para actualizar empleado ID: {0}", empleado.getId());
            
            // Verificar que el empleado existe
            Empleado empleadoExistente = empleadoDAO.leer(empleado.getId());
            if (empleadoExistente == null) {
                throw new IllegalArgumentException("El empleado con ID " + empleado.getId() + " no existe");
            }
            
            // Ejecutar actualización
            empleadoDAO.actualizarTx(empleado, conn);
            
            // Commit exitoso
            conn.commit();
            LOGGER.log(Level.INFO, "Empleado actualizado exitosamente ID: {0}", empleado.getId());
            
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                    LOGGER.log(Level.SEVERE, "Rollback ejecutado por error en actualización", e);
                } catch (SQLException rollbackEx) {
                    LOGGER.log(Level.SEVERE, "Error al hacer rollback", rollbackEx);
                }
            }
            throw new Exception("Error al actualizar empleado: " + e.getMessage(), e);
            
        } catch (Exception e) {
            if (conn != null) {
                try {
                    conn.rollback();
                    LOGGER.log(Level.SEVERE, "Rollback ejecutado por error inesperado", e);
                } catch (SQLException rollbackEx) {
                    LOGGER.log(Level.SEVERE, "Error al hacer rollback", rollbackEx);
                }
            }
            throw new Exception("Error inesperado al actualizar empleado: " + e.getMessage(), e);
            
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
     * Elimina lógicamente un empleado y su legajo asociado en una transacción atómica
     * Garantiza que ambas entidades se eliminen o ninguna (integridad 1:1)
     * 
     * @param id ID del empleado a eliminar
     * @throws Exception Si falla la transacción
     */
    @Override
    public void eliminar(int id) throws Exception {
        if (id <= 0) {
            throw new IllegalArgumentException("El ID del empleado debe ser mayor a 0");
        }
        
        Connection conn = null;
        try {
            conn = DataBaseConnection.getConnection();
            conn.setAutoCommit(false);
            
            LOGGER.log(Level.INFO, "Iniciando transacción para eliminar empleado ID: {0}", id);
            
            // Verificar que el empleado existe
            Empleado empleadoExistente = empleadoDAO.leer(id);
            if (empleadoExistente == null) {
                throw new IllegalArgumentException("El empleado con ID " + id + " no existe o ya está eliminado");
            }
            
            // Obtener el legajo asociado para eliminarlo también
            Legajo legajoAsociado = empleadoExistente.getLegajo();
            
            // 1. Eliminar empleado (baja lógica)
            empleadoDAO.eliminarTx(id, conn);
            LOGGER.log(Level.INFO, "Empleado marcado como eliminado ID: {0}", id);
            
            // 2. Si tiene legajo asociado, también eliminarlo (mantener consistencia 1:1)
            if (legajoAsociado != null && legajoAsociado.getId() > 0) {
                legajoDAO.eliminarTx(legajoAsociado.getId(), conn);
                LOGGER.log(Level.INFO, "Legajo asociado marcado como eliminado ID: {0}", legajoAsociado.getId());
            }
            
            // Commit exitoso
            conn.commit();
            LOGGER.log(Level.INFO, "Empleado y Legajo eliminados exitosamente - Transacción completada");
            
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                    LOGGER.log(Level.SEVERE, "Rollback ejecutado por error en eliminación", e);
                } catch (SQLException rollbackEx) {
                    LOGGER.log(Level.SEVERE, "Error al hacer rollback", rollbackEx);
                }
            }
            throw new Exception("Error al eliminar empleado: " + e.getMessage(), e);
            
        } catch (Exception e) {
            if (conn != null) {
                try {
                    conn.rollback();
                    LOGGER.log(Level.SEVERE, "Rollback ejecutado por error inesperado", e);
                } catch (SQLException rollbackEx) {
                    LOGGER.log(Level.SEVERE, "Error al hacer rollback", rollbackEx);
                }
            }
            throw new Exception("Error inesperado al eliminar empleado: " + e.getMessage(), e);
            
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
     * Obtiene un empleado por su ID
     * @param id ID del empleado a buscar
     * @return Empleado encontrado con su legajo asociado, o null si no existe
     * @throws Exception Si ocurre un error al consultar
     */
    @Override
    public Empleado getById(int id) throws Exception {
        if (id <= 0) {
            throw new IllegalArgumentException("El ID del empleado debe ser mayor a 0");
        }
        
        try {
            LOGGER.log(Level.INFO, "Consultando empleado ID: {0}", id);
            return empleadoDAO.leer(id);
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al consultar empleado por ID", e);
            throw new Exception("Error al obtener empleado: " + e.getMessage(), e);
        }
    }
    
    /**
     * Obtiene todos los empleados activos con sus legajos
     * @return Lista de empleados activos
     * @throws Exception Si ocurre un error al consultar
     */
    @Override
    public List<Empleado> getAll() throws Exception {
        try {
            LOGGER.log(Level.INFO, "Consultando todos los empleados activos");
            return empleadoDAO.leerTodos();
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al consultar todos los empleados", e);
            throw new Exception("Error al obtener lista de empleados: " + e.getMessage(), e);
        }
    }
    
    /**
     * Busca un empleado por su DNI
     * @param dni DNI del empleado a buscar
     * @return Empleado encontrado o null
     * @throws Exception Si ocurre un error al consultar
     */
    public Empleado buscarPorDni(String dni) throws Exception {
        if (dni == null || dni.trim().isEmpty()) {
            throw new IllegalArgumentException("El DNI no puede estar vacío");
        }
        
        if (!DNI_PATTERN.matcher(dni.trim()).matches()) {
            throw new IllegalArgumentException("El DNI debe tener 7 u 8 dígitos numéricos");
        }
        
        try {
            LOGGER.log(Level.INFO, "Buscando empleado por DNI: {0}", dni);
            return empleadoDAO.buscarPorDni(dni);
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al buscar empleado por DNI", e);
            throw new Exception("Error al buscar empleado por DNI: " + e.getMessage(), e);
        }
    }
    
    /**
     * Actualiza la categoría del legajo de un empleado
     * Operación transaccional que garantiza consistencia
     * 
     * @param idEmpleado ID del empleado
     * @param nuevaCategoria Nueva categoría para el legajo
     * @throws Exception Si falla la operación
     */
    public void actualizarCategoriaLegajo(int idEmpleado, String nuevaCategoria) throws Exception {
        if (idEmpleado <= 0) {
            throw new IllegalArgumentException("El ID del empleado debe ser mayor a 0");
        }
        if (nuevaCategoria == null || nuevaCategoria.trim().isEmpty()) {
            throw new IllegalArgumentException("La categoría no puede estar vacía");
        }
        
        Connection conn = null;
        try {
            conn = DataBaseConnection.getConnection();
            conn.setAutoCommit(false);
            
            LOGGER.log(Level.INFO, "Iniciando transacción para actualizar categoría de legajo del empleado ID: {0}", 
                    idEmpleado);
            
            // Obtener el empleado con su legajo
            Empleado empleado = empleadoDAO.leer(idEmpleado);
            if (empleado == null) {
                throw new IllegalArgumentException("El empleado con ID " + idEmpleado + " no existe");
            }
            
            if (empleado.getLegajo() == null) {
                throw new IllegalStateException("El empleado no tiene un legajo asociado");
            }
            
            // Actualizar categoría del legajo
            Legajo legajo = empleado.getLegajo();
            legajo.setCategoria(nuevaCategoria.trim().toUpperCase());
            legajoDAO.actualizarTx(legajo, conn);
            
            // Commit exitoso
            conn.commit();
            LOGGER.log(Level.INFO, "Categoría de legajo actualizada exitosamente");
            
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                    LOGGER.log(Level.SEVERE, "Rollback ejecutado por error", e);
                } catch (SQLException rollbackEx) {
                    LOGGER.log(Level.SEVERE, "Error al hacer rollback", rollbackEx);
                }
            }
            throw new Exception("Error al actualizar categoría: " + e.getMessage(), e);
            
        } catch (Exception e) {
            if (conn != null) {
                try {
                    conn.rollback();
                    LOGGER.log(Level.SEVERE, "Rollback ejecutado por error inesperado", e);
                } catch (SQLException rollbackEx) {
                    LOGGER.log(Level.SEVERE, "Error al hacer rollback", rollbackEx);
                }
            }
            throw new Exception("Error inesperado: " + e.getMessage(), e);
            
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
     * Valida los campos obligatorios de un empleado antes de insertar
     * @param empleado Empleado a validar
     * @throws IllegalArgumentException Si falla alguna validación
     */
    private void validarEmpleado(Empleado empleado) throws IllegalArgumentException {
        if (empleado == null) {
            throw new IllegalArgumentException("El empleado no puede ser null");
        }
        
        // Validar nombre
        if (empleado.getNombre() == null || empleado.getNombre().trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre del empleado es obligatorio");
        }
        if (empleado.getNombre().trim().length() < 2) {
            throw new IllegalArgumentException("El nombre debe tener al menos 2 caracteres");
        }
        
        // Validar apellido
        if (empleado.getApellido() == null || empleado.getApellido().trim().isEmpty()) {
            throw new IllegalArgumentException("El apellido del empleado es obligatorio");
        }
        if (empleado.getApellido().trim().length() < 2) {
            throw new IllegalArgumentException("El apellido debe tener al menos 2 caracteres");
        }
        
        // Validar DNI
        if (empleado.getDni() == null || empleado.getDni().trim().isEmpty()) {
            throw new IllegalArgumentException("El DNI del empleado es obligatorio");
        }
        if (!DNI_PATTERN.matcher(empleado.getDni().trim()).matches()) {
            throw new IllegalArgumentException("El DNI debe contener 7 u 8 dígitos numéricos");
        }
        
        // Validar email (si está presente)
        if (empleado.getEmail() != null && !empleado.getEmail().trim().isEmpty()) {
            if (!EMAIL_PATTERN.matcher(empleado.getEmail().trim()).matches()) {
                throw new IllegalArgumentException("El formato del email no es válido");
            }
        }
        
        // Validar área (si está presente)
        if (empleado.getArea() != null && !empleado.getArea().trim().isEmpty()) {
            if (empleado.getArea().trim().length() < 2) {
                throw new IllegalArgumentException("El área debe tener al menos 2 caracteres");
            }
        }
    }
    
    /**
     * Valida los campos de un empleado antes de actualizar
     * @param empleado Empleado a validar
     * @throws IllegalArgumentException Si falla alguna validación
     */
    private void validarEmpleadoParaActualizar(Empleado empleado) throws IllegalArgumentException {
        if (empleado == null) {
            throw new IllegalArgumentException("El empleado no puede ser null");
        }
        
        if (empleado.getId() <= 0) {
            throw new IllegalArgumentException("El ID del empleado debe ser mayor a 0");
        }
        
        // Validar área (campo que se actualiza)
        if (empleado.getArea() == null || empleado.getArea().trim().isEmpty()) {
            throw new IllegalArgumentException("El área es obligatoria para actualizar");
        }
        if (empleado.getArea().trim().length() < 2) {
            throw new IllegalArgumentException("El área debe tener al menos 2 caracteres");
        }
    }
    
    /**
     * Valida que el DNI no esté duplicado en la base de datos
     * Regla de negocio: DNI único por empleado
     * 
     * @param dni DNI a validar
     * @throws Exception Si el DNI ya existe
     */
    private void validarDniUnico(String dni) throws Exception {
        Empleado empleadoExistente = empleadoDAO.buscarPorDni(dni);
        if (empleadoExistente != null) {
            throw new IllegalArgumentException("Ya existe un empleado con el DNI " + dni);
        }
    }
}
