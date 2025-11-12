
package tpiprogramacionii.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import tpiprogramacionii.utils.DataBaseConnection;
import tpiprogramacionii.entities.Empleado;
import tpiprogramacionii.entities.Estado;
import tpiprogramacionii.entities.Legajo;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;



public class LegajoDAO implements GenericDAO <Legajo>{
     
    //QUERYS: --------------------------------------------------------------------------------------------------
   
    //Insertar de legajo (id autoincremental) 
    private static final String INSERT_SQL = "INSERT INTO legajo (nro_legajo, categoria, estado) VALUES (?, ?, ?)";
   
    //Actualizar categoria en legajo 
    private static final String UPDATE_CATEGORIA = "UPDATE legajo SET categoria = ? WHERE id = ?";
    
    //Actualizar estado en legajo
    private static final String  UPDATE_ESTADO = "UPDATE legajo SET estado = ? WHERE id = ? AND eliminado = FALSE";
    
    //Eliminar legajo (marca eliminado = TRUE)
    private static final String DELETE_SQL = "UPDATE legajo "+
                                             "SET eliminado = TRUE "+
                                             "WHERE id = ? AND eliminado = FALSE";

    //Buscar legajo por ID
    private static final String SEARCH_BY_ID = "SELECT id, nro_legajo, categoria, estado "+
                                               "FROM legajo "+
                                               "WHERE id = ? AND eliminado = FALSE";

    
    //Listar a todos los legajos activos.   
    private static final String SELECT_ALL_ACTIV = "SELECT id, nro_legajo, categoria "+
                                                   "FROM legajo "+
                                                   "WHERE eliminado = FALSE AND UPPER(estado) = 'ACTIVO'";
    
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
     * @param conex Conexión activa proporcionada por la transacción.
     * @throws Exception si ocurre un error al conectarse o ejecutar la instrucción SQL.
     */
             
    @Override
    public void insertTx(Legajo legajo, Connection conex) throws Exception {
        try (PreparedStatement stmt = conex.prepareStatement(INSERT_SQL, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setString(1, legajo.getNroLegajo());
            stmt.setString(2, legajo.getCategoria());
            stmt.setString(3, legajo.getEstado().name());
            stmt.executeUpdate();
            
            legajo.setId(recuperarIdGenerado(stmt));
              
        }
    }
        
    //--------------------------------------------------------------------------------------------------------------    
          
    /**
     * Inserta de Legajo en la base de datos (id Legajo autoincremental)
     * Verifica que la operación afecte al menos una fila
     * @param legajo(instancia de Legajo) contiene los valores para la inserción
     * @throws Exception si ocurre un error al conectarse o ejecutar la instrucción SQL
     */
    
    @Override
    public void actualizar(Legajo legajo) throws Exception {
         try (Connection conex = DataBaseConnection.getConnection();
            PreparedStatement stmt = conex.prepareStatement(UPDATE_CATEGORIA)) {
            stmt.setString(1, legajo.getCategoria());
            stmt.setInt(2, legajo.getId());
            
            int rows = stmt.executeUpdate();
            if (rows == 0) {
                throw new SQLException("No se pudo actualizar la categoria del legajo con ID: " + legajo.getId());
            }
        }
    }
        
    //--------------------------------------------------------------------------------------------------------------    
      
    /**
    * Elimina lógicamente un legajo y su empleado asociado
    * Marca como eliminado (baja lógica) tanto al legajo como al empleado relacionado
    * Valida que el legajo no esté eliminado previamente
    * @param id Id del legajo que se desea eliminar
    * @throws SQLException Si ocurre un error de conexión o ejecución
    * @throws IllegalStateException Si el legajo ya estaba eliminado
    */
    
    @Override
    public void eliminar(int id) throws SQLException {
        try (Connection conex = DataBaseConnection.getConnection();
             PreparedStatement stmt = conex.prepareStatement(DELETE_SQL)) {

            stmt.setInt(1, id);
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
    public Legajo leer(int id) throws Exception {
       
        try (Connection conex = DataBaseConnection.getConnection();
                PreparedStatement stmt = conex.prepareStatement(SEARCH_BY_ID)) {

            stmt.setInt(1, id);

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
                PreparedStatement stmt = conex.prepareStatement(SELECT_ALL_ACTIV);
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
     * Recupera el Id autogenerado por la BD del registro recién creado
     * @param stmt PreparedStatement a través del cual se obtiene el Id
     * @return el Id generado por la BD.
     * @throws SQLException si hay un error al recuperar el Id 
     */
    
    private int recuperarIdGenerado(PreparedStatement stmt) throws SQLException {
        try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
            if (generatedKeys.next()) {
                return generatedKeys.getInt(1);
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
    
    private Legajo mapResultSetToLegajo(ResultSet rs) throws SQLException {
        return new Legajo(
            rs.getInt("id"),
            rs.getString("nro_legajo"),
            rs.getString("categoria")  
        );
    }
        
    //--------------------------------------------------------------------------------------------------------------    
       
    /**
    * Cambia el estado de un legajo 
    * @param id Id del legajo a modificar
    * @param nuevoEstado Estado deseado (ACTIVO o INACTIVO)
    * @throws SQLException si hay error en la conexión o ejecución
    * @throws IllegalArgumentException si el estado no es válido
    */
    public void cambiarEstado(int id, Estado nuevoEstado) throws SQLException {
      
       try (Connection conex = DataBaseConnection.getConnection();
               PreparedStatement stmt = conex.prepareStatement(UPDATE_ESTADO)) {

            stmt.setString(1, nuevoEstado.name());
            stmt.setInt(2, id);

            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected == 0) {
                throw new SQLException("No se pudo actualizar el estado del legajo. Puede que no exista o esté eliminado.");
            }
        }
    }    
}

