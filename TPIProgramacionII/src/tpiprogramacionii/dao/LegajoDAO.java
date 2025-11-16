
package tpiprogramacionii.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import tpiprogramacionii.utils.DataBaseConnection;
import tpiprogramacionii.entities.Estado;
import tpiprogramacionii.entities.Legajo;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.sql.Types;



public class LegajoDAO implements GenericDAO <Legajo>{
     
    //QUERYS: --------------------------------------------------------------------------------------------------
   
        //Insertar legajo (id autoincremental) 
    private static final String INSERT_SQL = "INSERT INTO legajo (nro_legajo, categoria, estado, fecha_alta, observaciones) VALUES (?, ?, ?, ?, ?)";

    //Actualizar categoría en legajo 
    private static final String UPDATE_CATEGORIA = "UPDATE legajo SET categoria = ? WHERE id = ? AND eliminado = FALSE";

    //Actualizar estado en legajo
    private static final String UPDATE_ESTADO = "UPDATE legajo SET estado = ? WHERE id = ? AND eliminado = FALSE";

    //Eliminar legajo (marca eliminado = TRUE)
    private static final String DELETE_SQL = "UPDATE legajo SET eliminado = TRUE WHERE id = ? AND eliminado = FALSE";

    //Buscar legajo por ID
    private static final String SEARCH_BY_ID =  "SELECT id, nro_legajo, categoria, estado, fecha_alta, observaciones " +
                                                "FROM legajo WHERE id = ? AND eliminado = FALSE";

    //Listar todos los legajos activos
    private static final String SELECT_ALL_ACTIVE = "SELECT id, nro_legajo, categoria, estado, fecha_alta, observaciones " +
                                                    "FROM legajo WHERE eliminado = FALSE AND UPPER(estado) = 'ACTIVO'";

    //CONSTRUCTOR ---------------------------------------------------------------------------------------------------
    public LegajoDAO() {} 
        
    //MÉTODOS HEREDADOS ---------------------------------------------------------------------------------------------
   
    /**
    * Inserta un legajo en la base de datos.
    * Crea y cierra la conexión automáticamente y llama a insertTx para realizar la inserción.
    * @param legajo El legajo a insertar.
    * @throws Exception Si ocurre un error al conectar o ejecutar la inserción.
    */
    
    @Override
    public void insertar(Legajo legajo) throws Exception {
        try (Connection conex = DataBaseConnection.getConnection()) {
            insertTx(legajo, conex);
        }
    }
       
    //--------------------------------------------------------------------------------------------------------------    
    /**
    * Inserta un legajo en la base de datos (id de empleado autoincremental) usando una conexión existente.
    * Recupera el Id autogenerado por la BD y lo asigna al objeto legajo
    * @param legajo a insertar.
    * @param conex CConexión activa proporcionada por la transacción.
    * @throws Exception Si ocurre un error SQL durante la inserción.
    */
    
    @Override
    public void insertTx(Legajo legajo, Connection conex) throws Exception {
        try (PreparedStatement stmt = conex.prepareStatement(INSERT_SQL, Statement.RETURN_GENERATED_KEYS)) {
            setLegajoParameters(stmt, legajo);   
            stmt.executeUpdate();
            legajo.setId(recuperarIdGenerado(stmt));
        }
    }
        
    //--------------------------------------------------------------------------------------------------------------    
          
    /**
     * Actualiza la categoría de un legajo en la base de datos (versión autónoma)
     * Crea y cierra su propia conexión
     * @param legajo contiene los valores para la actualización
     * @throws Exception si ocurre un error al conectarse o ejecutar la instrucción SQL
     */
    
    @Override
    public void actualizar(Legajo legajo) throws Exception {
         try (Connection conex = DataBaseConnection.getConnection()) {
            actualizarTx(legajo, conex);
        }
    }
    
    //--------------------------------------------------------------------------------------------------------------    
    
    /**
     * Actualiza la categoría de un legajo usando una conexión existente (versión transaccional)
     * @param legajo contiene los valores para la actualización
     * @param conex Conexión transaccional activa
     * @throws Exception si ocurre un error al ejecutar la actualización
     */
    
    @Override
    public void actualizarTx(Legajo legajo, Connection conex) throws Exception {
        try (PreparedStatement stmt = conex.prepareStatement(UPDATE_CATEGORIA)) {
            stmt.setString(1, legajo.getCategoria());
            stmt.setLong(2, legajo.getId());
            
            int rows = stmt.executeUpdate();
            if (rows == 0) {
                throw new SQLException("No se pudo actualizar la categoria del legajo con ID: " + legajo.getId());
            }
        }
    }
        
    //--------------------------------------------------------------------------------------------------------------    
      
    /**
    * Elimina lógicamente un legajo (versión autónoma)
    * Crea y cierra su propia conexión
    * @param id Id del legajo que se desea eliminar
    * @throws Exception Si ocurre un error de conexión o ejecución
    */
    
    @Override
    public void eliminar(Long id) throws Exception {
        try (Connection conex = DataBaseConnection.getConnection()) {
            eliminarTx(id, conex);
        }
    }
    
    //--------------------------------------------------------------------------------------------------------------    
    
    /**
    * Elimina lógicamente un legajo usando una conexión existente (versión transaccional)
    * Marca como eliminado (baja lógica) el legajo
    * @param id Id del legajo que se desea eliminar
    * @param conex Conexión transaccional activa
    * @throws Exception Si ocurre un error de ejecución
    */
    
    @Override
    public void eliminarTx(Long id, Connection conex) throws Exception {
        try (PreparedStatement stmt = conex.prepareStatement(DELETE_SQL)) {
            stmt.setLong(1, id);
            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected == 0) {
                throw new IllegalStateException("El legajo con ID " + id + " ya estaba eliminado o no existe.");
            }
        }
    }
  
    //--------------------------------------------------------------------------------------------------------------    
    
    /**
    * Recupera un legajo activo por su ID.
    * @param id ID del legajo a buscar
    * @return objeto Legajo o null si no existe
    * @throws Exception si ocurre un error de conexión o ejecución
    */
    
    @Override
    public Legajo leer(Long id) throws Exception {
       
        try (Connection conex = DataBaseConnection.getConnection();
                PreparedStatement stmt = conex.prepareStatement(SEARCH_BY_ID)) {

            stmt.setLong(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToLegajo(rs);
                }
            }
            
        } catch (SQLException e) {
            throw new Exception("Error al obtener legajo por ID: " + e.getMessage(), e);
        } 
        return null;
    }

    //--------------------------------------------------------------------------------------------------------------    
       
    /**
    * Lista todos los legajos activos en la base de datos.
    * Solo devuelve legajos donde eliminado = FALSE y estado = ACTIVO.
    * @return Lista de objetos Legajo.
    * @throws Exception si ocurre un error de conexión o ejecución de la consulta.
    */
    
    @Override
    public List<Legajo> leerTodos() throws Exception {
        List<Legajo> listaLegajos = new ArrayList<>();
        try(Connection conex = DataBaseConnection.getConnection(); 
                PreparedStatement stmt = conex.prepareStatement(SELECT_ALL_ACTIVE);
                ResultSet rs = stmt.executeQuery()){
               
            while (rs.next()) {
                listaLegajos.add(mapResultSetToLegajo(rs));
            }
            
        } catch (SQLException e) {
            throw new Exception("Error al obtener el listado de legajos: " + e.getMessage(), e);
        }
        return listaLegajos;
    }
    
    
    //METODOS PROPIOS ----------------------------------------------------------------------------------------------
         
    /**
     * Asigna los valores de un legajo a los parámetros del PreparedStatement
     * Verifica los campos nulos antes de asignarlos.
     * @param stmt PreparedStatement donde se van a asignar los valores
     * @param legajo contiene los valores necesarios para cualquier operación (INSERT/UPDATE)
     * @throws SQLException si hay error al asignar los parámetros
    */        
    
    private void setLegajoParameters(PreparedStatement stmt, Legajo legajo) throws SQLException {
        
        stmt.setString(1, legajo.getNroLegajo());

        if (legajo.getCategoria() != null) {
            stmt.setString(2, legajo.getCategoria());
        } else {
            stmt.setNull(2, Types.VARCHAR);
        }

        stmt.setString(3, legajo.getEstado() != null ? legajo.getEstado().name() : Estado.ACTIVO.name());

        if (legajo.getFechaAlta() != null) {
            stmt.setDate(4, new java.sql.Date(legajo.getFechaAlta().getTime()));
        } else {
            stmt.setNull(4, Types.DATE);
        }
        
        if (legajo.getObservaciones() != null) {
            stmt.setString(5, legajo.getObservaciones());
        } else {
            stmt.setNull(5, Types.VARCHAR);
        }
    }
    
    //--------------------------------------------------------------------------------------------------------------    
    
    /**
     * Recupera el Id autogenerado por la BD del registro recién creado
     * @param stmt PreparedStatement a través del cual se obtiene el Id
     * @return el Id generado por la BD.
     * @throws SQLException si hay un error al recuperar el Id 
     */
    
    private Long recuperarIdGenerado(PreparedStatement stmt) throws SQLException {
        try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
            if (generatedKeys.next()) {
                return generatedKeys.getLong(1);
            } else {
                throw new SQLException("No pudo obtener el ID del legajo");
            }
        }
    }  

    //--------------------------------------------------------------------------------------------------------------    
       
    /**
    * Crea una instancia de legajo a partir de los valores de ResultSet
    * @param rs ResultSet obtenido de la consulta a la base de datos.
    * @return legajo
    * @throws Exception en caso de error de conexión o de ejecución de la consulta.
    */
    
    /*private Legajo mapResultSetToLegajo(ResultSet rs) throws SQLException {
        return new Legajo(
            rs.getLong("id"),
            rs.getString("nro_legajo"),
            rs.getString("categoria")            
        );
    }*/
            
    private Legajo mapResultSetToLegajo(ResultSet rs) throws SQLException {
        Legajo legajo = new Legajo(
            rs.getLong("id"),
            rs.getString("nro_legajo"),
            rs.getString("categoria")
        );
        
        String estadoStr = rs.getString("estado");
        if (estadoStr != null) {
            legajo.setEstado(Estado.valueOf(estadoStr));
        } else {
            legajo.setEstado(Estado.ACTIVO);
        }
        
        java.sql.Date fechaAltaSql = rs.getDate("fecha_alta");
        if (fechaAltaSql != null) {
            legajo.setFechaAlta(new java.util.Date(fechaAltaSql.getTime()));
        }
        
        
        String observaciones = rs.getString("observaciones");
        if (observaciones != null) {
            legajo.setObservaciones(observaciones);
        }

        return legajo;        
    }
    
    
    //--------------------------------------------------------------------------------------------------------------    
       
    /**
    * Cambia el estado de un legajo 
    * @param id Id del legajo a modificar
    * @param nuevoEstado Estado deseado (ACTIVO o INACTIVO)
    * @throws SQLException si hay error en la conexión o ejecución
    * @throws IllegalArgumentException si el estado no es válido
    */
    public void cambiarEstado(Long id, Estado nuevoEstado) throws SQLException {
      
       try (Connection conex = DataBaseConnection.getConnection();
               PreparedStatement stmt = conex.prepareStatement(UPDATE_ESTADO)) {

            stmt.setString(1, nuevoEstado.name());
            stmt.setLong(2, id);

            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected == 0) {
                throw new SQLException("No se pudo actualizar el estado del legajo. Puede que no exista o esté eliminado.");
            }
        }
    }    
}