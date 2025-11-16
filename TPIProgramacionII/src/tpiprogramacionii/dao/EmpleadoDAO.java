
package tpiprogramacionii.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.sql.Types;
import tpiprogramacionii.entities.Empleado;
import tpiprogramacionii.entities.Estado;
import tpiprogramacionii.entities.Legajo;
import tpiprogramacionii.utils.DataBaseConnection;

public class EmpleadoDAO implements GenericDAO<Empleado> {
        //QUERYS IMPLEMENTADAS: ---------------------------------------------------------------------------------------------
    
    //Insertar de empleado (id autoincremental)
    private static final String INSERT_SQL = "INSERT INTO empleado (nombre, apellido, dni, email, fecha_ingreso, area) VALUES (?, ?, ?, ?, ?, ?)";
    
    //Actualizar area del empleado 
    private static final String UPDATE_AREA = "UPDATE empleado SET area = ? WHERE id = ?";
    
    //Eliminar empleado (marca eliminado = TRUE)
    private static final String DELETE_SQL = "UPDATE empleado "+
                                             "SET eliminado = TRUE "+
                                             "WHERE id = ? AND eliminado = FALSE";
    
    //Buscar empleado por ID
    private static final String SEARCH_BY_ID = "SELECT e.id, e.nombre, e.apellido, e.dni, e.email, e.fecha_ingreso, e.area, " +
                                                "l.id AS legajo_id, l.nro_legajo, l.categoria, l.estado, l.fecha_alta, l.observaciones " +
                                                "FROM empleado e " +
                                                "LEFT JOIN legajo l ON e.legajo_id = l.id " +
                                                "WHERE e.id = ? AND e.eliminado = FALSE";
    
    //Buscar empleado activo por dni
    private static final String SEARCH_BY_DNI = "SELECT e.id, e.nombre, e.apellido, e.dni, e.email, e.fecha_ingreso, e.area, " +
                                                "l.id AS legajo_id, l.nro_legajo, l.categoria, l.estado, l.fecha_alta, l.observaciones " +
                                                "FROM empleado e " +
                                                "LEFT JOIN legajo l ON e.legajo_id = l.id " +
            
                                                "WHERE e.eliminado = FALSE AND e.dni = ?";
    
    //Listar a todos los empleados activos.  
    private static final String SELECT_ALL_ACTIVE = "SELECT e.id, e.nombre, e.apellido, e.dni, e.email, e.fecha_ingreso, e.area, " +
                                                    "l.id AS legajo_id, l.nro_legajo, l.categoria, l.estado, l.fecha_alta, l.observaciones " +
                                                    "FROM empleado AS e " +
                                                    "LEFT JOIN legajo AS l ON e.legajo_id = l.id " +
                                                    "WHERE e.eliminado = FALSE AND UPPER(l.estado) = 'ACTIVO'";

    
    private static final String UPDATE_LEGAJO_ID = "UPDATE empleado SET legajo_id = ? WHERE id = ?";
    
    
    private final LegajoDAO legajoDAO; 

    //CONSTRUCTOR
    public EmpleadoDAO(LegajoDAO legajoDAO) {
        if (legajoDAO == null) {
            throw new IllegalArgumentException("LegajoDAO no puede ser null");
        }
        this.legajoDAO = legajoDAO;
    }
       
    
    //MÉTODOS HEREDADOS --------------------------------------------------------------------------------------------------

   /**
    * Inserta un empleado y crea automáticamente su legajo asociado en la misma transacción   
    * Primero inserta el empleado y obtiene su ID generado, con el cual genera el numero de legajo
    * @param empleado a insertar (nombre, apellido y DNI requeridos).
    * @throws Exception Si falla la inserción del empleado o del legajo.
    */
    
    @Override
    public void insertar(Empleado empleado) throws Exception {
        try (Connection conex = DataBaseConnection.getConnection()){
            insertTx(empleado, conex);
        }
    }    
    
    //--------------------------------------------------------------------------------------------------------------    
     
    /**
    * Inserta un empleado usando una conexión existente (No la crea ni la cierra)
    * Recupera el id de empleado y lo asigna 
    * @param empleado a insertar
    * @param conex Conexión transaccional activa
    * @throws SQLException si ocurre un error al ejecutar la inserción
    */
    
    @Override
    public void insertTx(Empleado empleado, Connection conex) throws Exception {
        try (PreparedStatement stmt = conex.prepareStatement(INSERT_SQL, Statement.RETURN_GENERATED_KEYS)) {
            setEmpleadoParameters(stmt, empleado);
            stmt.executeUpdate();
            setIdGenerado(stmt, empleado);
        }
    }
        
    //------------------------------------------------------------------------------------------------------------------  
     /**
     * Actualiza el área de un empleado existente en la base de datos (versión autónoma)
     * Crea y cierra su propia conexión
     * @param empleado instancia de Empleado del cual se extrae el Id y área
     * @throws Exception en caso de error de conexión o de ejecución 
     */    
    
    @Override
    public void actualizar(Empleado empleado) throws Exception {
        try (Connection conn = DataBaseConnection.getConnection()) {
            actualizarTx(empleado, conn);
        }
    }
    
    //--------------------------------------------------------------------------------------------------------------    
    
    /**
     * Actualiza el área de un empleado usando una conexión existente (versión transaccional)
     * @param empleado instancia de Empleado del cual se extrae el Id y área
     * @param conn Conexión transaccional activa
     * @throws Exception en caso de error de ejecución 
     */    
    
    @Override
    public void actualizarTx(Empleado empleado, Connection conn) throws Exception {
        try (PreparedStatement stmtArea = conn.prepareStatement(UPDATE_AREA)) {
            stmtArea.setString(1, empleado.getArea());
            stmtArea.setLong(2, empleado.getId());
                        
            int rowsAffected = stmtArea.executeUpdate();
            if (rowsAffected == 0) {
                throw new SQLException("No se pudo actualizar el área de empleado con ID: " + empleado.getId());
            }
        }
    }

    //--------------------------------------------------------------------------------------------------------------    
        
    /**
    * Elimina lógicamente un empleado (versión autónoma)
    * Crea y cierra su propia conexión
    * @param id del empleado que se desea eliminar
    * @throws Exception en caso de error de conexión o de ejecución
    */
    
    @Override
    public void eliminar(Long id) throws Exception {
        try (Connection conex = DataBaseConnection.getConnection()) {
            eliminarTx(id, conex);
        }
    }
    
    //--------------------------------------------------------------------------------------------------------------    
    
    /**
    * Elimina lógicamente un empleado usando una conexión existente (versión transaccional)
    * Marca como eliminado (Baja lógica) el empleado
    * @param id del empleado que se desea eliminar
    * @param conex Conexión transaccional activa
    * @throws Exception en caso de error de ejecución
    */
    
    @Override
    public void eliminarTx(Long id, Connection conex) throws Exception {
        try (PreparedStatement stmt = conex.prepareStatement(DELETE_SQL)) {
            stmt.setLong(1, id);
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected == 0) {
                throw new SQLException("El empleado con ID " + id + " ya estaba eliminado o no existe.");
            }
        }
    }
    
    //--------------------------------------------------------------------------------------------------------------    
    
    /**
    * Obtiene un empleado por su Id.
    * Recupera el empleado de la base de datos junto con su legajo si existe
    * @param id del empleado a buscar
    * @return objeto Empleado si se encuentra, o null si no existe
    * @throws Exception en caso de error de conexión o de ejecución de la consulta
    */
    
    @Override
    public Empleado leer(Long id) throws Exception {
        try (Connection conex = DataBaseConnection.getConnection();
                PreparedStatement stmt = conex.prepareStatement(SEARCH_BY_ID)) {
            stmt.setLong(1, id);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToEmpleado(rs);
                }
            }
        } catch (SQLException e) {
            throw new Exception("Error al obtener empleado por ID: " + e.getMessage(), e);
        }
        return null;
    }

    //--------------------------------------------------------------------------------------------------------------    
        
    /**
    * Recupera todos los empleados activos de la base de datos
    * Para cada empleado, se recuperan los campos: Id (empleado), nombre, apellido, dni, 
    * Id del legajo, nro_legajo y área.
    * @return lista de los empleados activos
    * @throws Exception en caso de error de conexión o de ejecución de la consulta
    */
    
    @Override
    public List<Empleado> leerTodos() throws Exception {
        List<Empleado> listaEmpleados = new ArrayList<>();
        try(Connection conex = DataBaseConnection.getConnection();
               PreparedStatement stmt = conex.prepareStatement(SELECT_ALL_ACTIVE);
               ResultSet rs = stmt.executeQuery()){
               
            while (rs.next()) {
                listaEmpleados.add(mapResultSetToEmpleado(rs));
            }
        } catch (SQLException e) {
            throw new Exception("Error al obtener el listado de empleados: " + e.getMessage(), e);
        }
        return listaEmpleados;
    }
        
    //--------------------------------------------------------------------------------------------------------------    
         
    /**
     * Asigna los valores de un empleado a los parámetros del PreparedStatement
     * Verifica los campos nulos antes de asignarlos.
     * @param stmt PreparedStatement donde se van a asignar los valores
     * @param empleado contiene los valores necesarios para cualquier operación (INSERT/UPDATE)
     * @throws SQLException si hay error al asignar los parámetros
     */
    
    private void setEmpleadoParameters(PreparedStatement stmt, Empleado empleado) throws SQLException {
        stmt.setString(1, empleado.getNombre());
        stmt.setString(2, empleado.getApellido());
        stmt.setString(3, empleado.getDni());  
       
        if (empleado.getEmail() != null) {
            stmt.setString(4, empleado.getEmail());
        } else {
            stmt.setNull(4, Types.VARCHAR);
        }

        if (empleado.getFechaIngreso() != null) {
            stmt.setDate(5, new java.sql.Date(empleado.getFechaIngreso().getTime()));
        } else {
            stmt.setNull(5, Types.DATE);
        }

        if (empleado.getArea() != null) {
            stmt.setString(6, empleado.getArea());
        } else {
            stmt.setNull(6, Types.VARCHAR);
        }
    }    
    
    //--------------------------------------------------------------------------------------------------------------    
    
    /**
     * Toma el Id de Empleado autogenerado por la BD y lo asigna al objeto Empleado.
     * @param stmt
     * @param empleado instancia a la que se le asignará el Id de la BD
     * @throws SQLException si hay un error al recuperar el Id 
     */
    private void setIdGenerado(PreparedStatement stmt, Empleado empleado) throws SQLException {
        try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
            if (generatedKeys.next()) {
                empleado.setId(generatedKeys.getLong(1));
            } else {
                throw new SQLException("La inserción de la persona falló, no se obtuvo ID generado");
            }
        }
    }  
    
    //--------------------------------------------------------------------------------------------------------------    
    
    /**
    * Crea una instancia de empleado a partir de ResultSet
    * Asigna al objeto los valores Id, nombre, apellido y dni de ResultSet
    * Se recupera su id_legajo, nro_legajo y categoria y se asocia esta info al objeto
    * @param rs ResultSet obtenido de la consulta a la base de datos.
    * @return empleado con su info
    * @throws Exception en caso de error de conexión o de ejecución de la consulta.
    */
    
    private Empleado mapResultSetToEmpleado(ResultSet rs) throws SQLException {
        Empleado empleado = new Empleado();
        empleado.setId(rs.getLong("id"));
        empleado.setNombre(rs.getString("nombre"));
        empleado.setApellido(rs.getString("apellido"));
        empleado.setDni(rs.getString("dni"));
        empleado.setEmail(rs.getString("email"));
        empleado.setFechaIngreso(rs.getDate("fecha_ingreso"));
        empleado.setArea(rs.getString("area"));
        
        long idLegajo = rs.getLong("legajo_id");

        if (idLegajo > 0 && !rs.wasNull()) {
            Legajo legajo = new Legajo();

            legajo.setId((long) idLegajo); 
            legajo.setNroLegajo(rs.getString("nro_legajo"));
            legajo.setCategoria(rs.getString("categoria"));
            
            String estadoStr = rs.getString("estado");
            legajo.setEstado(estadoStr != null ? Estado.valueOf(estadoStr) : Estado.ACTIVO);
           
            java.sql.Date fechaAltaSql = rs.getDate("fecha_alta");
            if (fechaAltaSql != null) {
                legajo.setFechaAlta(new java.sql.Date(fechaAltaSql.getTime()));
            } else {
                legajo.setFechaAlta(null);
            }

            legajo.setObservaciones(rs.getString("observaciones"));
            
            empleado.setLegajo(legajo);
        }
       
        return empleado;
    }
    
    //--------------------------------------------------------------------------------------------------------------    
    
    /**
    * Busca un empleado activo por su DNI.
    * @param dni del empleado a buscar.
    * @return objeto Empleado si se encuentra, o null si no existe.
    * @throws Exception en caso de error de conexión o ejecución de la consulta.
    */ 
    
    public Empleado buscarPorDni(String dni) throws SQLException {
        if (dni == null || dni.trim().isEmpty()) {
            throw new IllegalArgumentException("Debe ingresar un DNI.");
        }
        try(Connection conex = DataBaseConnection.getConnection();
            PreparedStatement stmt = conex.prepareStatement(SEARCH_BY_DNI)) {
                   
            stmt.setString(1, dni.trim());

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToEmpleado(rs);
                }
            }
        }
        return null;
    }
    
    /**
    * Actualiza la relación entre un empleado y su legajo en la base de datos.
    * El campo`legajo_id en la tabla `empleado` se actualiza con el id de legajo 
    * asignándole un nuevo legajo al empleado indicado.
    *
    * @param empleadoId ID del empleado al que se desea actualizar el legajo.
    * @param legajoId Nuevo ID de legajo que se asignará al empleado.
    * @param conex Conexión activa a la base de datos (no debe ser null).
    * @throws SQLException si ocurre un error al ejecutar la sentencia SQL.
    */
    
    public void actualizarLegajoId (Long empleadoId, Long legajoId, Connection conex)throws SQLException {
        try (PreparedStatement stmt = conex.prepareStatement(UPDATE_LEGAJO_ID)){
            stmt.setLong(1, legajoId);
            stmt.setLong(2, empleadoId);
            stmt.executeUpdate();
        }
    }
}
