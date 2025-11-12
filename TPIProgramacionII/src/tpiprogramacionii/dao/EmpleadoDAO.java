
package tpiprogramacionii.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import tpiprogramacionii.entities.Empleado;
import tpiprogramacionii.entities.Legajo;
import tpiprogramacionii.utils.DataBaseConnection;

public class EmpleadoDAO implements GenericDAO<Empleado> {
        //QUERYS IMPLEMENTADAS: ---------------------------------------------------------------------------------------------
    
    //Insertar de empleado (id autoincremental)
    private static final String INSERT_SQL = "INSERT INTO empleado (nombre, apellido, dni) VALUES (?, ?, ?)";
    
    //Actualizar area del empleado 
    private static final String UPDATE_AREA = "UPDATE empleado SET area = ? WHERE id = ?";
    
    //Eliminar empleado (marca eliminado = TRUE)
    private static final String DELETE_SQL = "UPDATE empleado "+
                                             "SET eliminado = TRUE "+
                                             "WHERE id = ? AND eliminado = FALSE";

    
    //Buscar empleado por ID
    private static final String SEARCH_BY_ID = "SELECT e.id, e.eliminado, e.nombre, e.apellido, e.dni, e.area, e.fechaIngreso, e.legajo_id, "+
                                               "l.nro_legajo, l.categoria "+
                                               "FROM empleado AS e "+
                                               "LEFT JOIN legajo AS l ON e.legajo_id = l.id "+
                                               "WHERE e.id = ? AND e.eliminado = FALSE";
    
    //Buscar empleado activo por dni
    private static final String SEARCH_BY_DNI = "SELECT e.id, e.nombre, e.apellido, e.dni, e.area, l.nro_legajo, l.categoria "+
                                                "FROM empleado AS e "+
                                                "LEFT JOIN legajo AS l ON e.legajo_id = l.id "+
                                                "WHERE e.eliminado = FALSE AND dni = ?";
    
    //Listar a todos los empleados activos.   
    private static final String SELECT_ALL_ACTIVE = "SELECT e.id, e.nombre, e.apellido, e.dni, e.area, "+
                                                   "l.nro_legajo, l.categoria "+
                                                   "FROM empleado AS e "+
                                                   "LEFT JOIN legajo AS l ON e.legajo_id = l.id "+
                                                   "WHERE e.eliminado = FALSE AND UPPER(l.estado) = 'ACTIVO'";
    
    /*QUERYS ALTERNATIVAS NO IMPLEMENTADAS:
    
    //Actualizar el email del empleado 
    private static final String UPDATE_EMAIL = "UPDATE empleado SET email = ? WHERE id = ?";
    
    //Listar empleados activos por area
    private static final String SEARCH_BY_AREA_SQL = "SELECT e.id, e.nombre, e.apellido, e.dni, l.nro_legajo, l.categoria "+
                                                     "FROM empleado AS e "+
                                                     "LEFT JOIN legajo AS l ON e.legajo_id = l.id "+
                                                     "WHERE e.eliminado = FALSE AND e.area = ?";
    
    
    //Listar a todos los empleados que no están activos y que no fueron borrados (logicamente).   
    private static final String SELECT_ALL_INACTIV = "SELECT e.id, e.nombre, e.apellido, e.dni, e.area, "+
                                                     "l.nro_legajo, l.categoria "+
                                                     "FROM empleado AS e "+
                                                     "LEFT JOIN legajo AS l ON e.legajo_id = l.id "+
                                                     "WHERE e.eliminado = FALSE AND l.estado = 'INACTIVO'";
    
    //Listar a todos los empleados borrados (logicamente).
    private static final String SELECT_ALL_ELIMINADOS = "SELECT e.id, e.nombre, e.apellido, e.dni, e.area, "+
                                                        "l.nro_legajo, l.categoria, l.estado "+
                                                        "FROM empleado AS e "+
                                                        "LEFT JOIN legajo AS l ON e.legajo_id = l.id "+
                                                        "WHERE e.eliminado = TRUE";
    */    
    
    
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
            
             conex.setAutoCommit(false);

            try {
                insertTx(empleado, conex);

                // Crear Legajo automáticamente usando el id generado del empleado
                Legajo legajo = new Legajo();
                legajo.setNroLegajo("LEG" + String.format("%06d", empleado.getId()));
                legajo.setEstado(tpiprogramacionii.entities.Estado.ACTIVO);
                
                legajoDAO.insertTx(legajo, conex);
                empleado.setLegajo(legajo);

                conex.commit();

            } catch (Exception e) {
                conex.rollback();
                throw e;
            } finally {
                conex.setAutoCommit(true);
            }
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
     * Actualiza el área de un empleado existente en la base de datos.
     * Verifica si la actualización afectó al menos una fila; si no hay actualización lanza una SQLException
     * @param empleado instancia de Empleado del cual se extrae el Id y área
     * @throws Exception en caso de error de conexión o de ejecución 
     */    
    
    @Override
    public void actualizar(Empleado empleado) throws Exception {
        try (Connection conn = DataBaseConnection.getConnection();
             PreparedStatement stmtArea = conn.prepareStatement(UPDATE_AREA)) {
            
            stmtArea.setString(1, empleado.getArea());
            stmtArea.setInt(2, empleado.getId());
            stmtArea.executeUpdate();
            
            int rowsAffected = stmtArea.executeUpdate();
            if (rowsAffected == 0) {
                throw new SQLException("No se pudo actualizar el área de empleado con ID: " + empleado.getId());
            }
        }
    }

    //--------------------------------------------------------------------------------------------------------------    
        
    /**
    * Elimina lógicamente un empleado y su legajo asociado en la base de datos.
    * Marca como eliminado (Baja lógica) tanto al empleado como al legajo relacionado.
    * @param id del empleado que se desea eliminar.
    * @throws Exception en caso de error de conexión o de ejecución.
    */
    
    @Override
    public void eliminar(int id) throws Exception {
        try (Connection conex = DataBaseConnection.getConnection();
                PreparedStatement stmt = conex.prepareStatement(DELETE_SQL)) {

            stmt.setInt(1, id);
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
    public Empleado leer(int id) throws Exception {
        try (Connection conex = DataBaseConnection.getConnection();
                PreparedStatement stmt = conex.prepareStatement(SEARCH_BY_ID)) {
            stmt.setInt(1, id);
            
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
     * @param stmt PreparedStatement donde se van a asignar los valores
     * @param empleado contiene los valores necesarios para cualquier operación (INSERT/UPDATE)
     * @throws SQLException si hay error al asignar los parámetros
     */
    
    private void setEmpleadoParameters(PreparedStatement stmt, Empleado empleado) throws SQLException {
        stmt.setString(1, empleado.getNombre());
        stmt.setString(2, empleado.getApellido());
        stmt.setString(3, empleado.getDni());               
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
                empleado.setId(generatedKeys.getInt(1));
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
        empleado.setId(rs.getInt("id"));
        empleado.setNombre(rs.getString("nombre"));
        empleado.setApellido(rs.getString("apellido"));
        empleado.setDni(rs.getString("dni"));

        int idLegajo = rs.getInt("id_legajo");
        if (idLegajo > 0 && !rs.wasNull()) {
            Legajo legajo = new Legajo();
            legajo.setNroLegajo(rs.getString("nro_legajo"));
            legajo.setCategoria(rs.getString("categoria"));
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
}
