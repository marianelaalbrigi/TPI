
package tpiprogramacionii.main;

import java.util.Date;
import java.util.List;
import java.util.Scanner;
import tpiprogramacionii.entities.Empleado;
import tpiprogramacionii.entities.Estado;
import tpiprogramacionii.entities.Legajo;
import tpiprogramacionii.service.EmpleadoService;
import tpiprogramacionii.service.LegajoService;

/**
 * Constructor de MenuController.
 * Inicializa el controlador del menú con el Scanner para leer
 * desde consola y el EmpleadoService para realizar las operaciones
 * de negocio sobre empleados. Valida que ambos parámetros no sean null.
 */
public class MenuController {
         private final Scanner scanner;  
         private EmpleadoService empleadoService;
         private LegajoService legajoService;
   
   
         public MenuController(Scanner scanner, EmpleadoService empleadoService,LegajoService legajoService) {
              if (scanner == null) {
                 throw new IllegalArgumentException("Scanner no puede ser null");
              }
             if (empleadoService == null) {
                 throw new IllegalArgumentException("EmpleadoService no puede ser null");
               }
             this.scanner = scanner;
             this.empleadoService = empleadoService;
             this.legajoService= legajoService;
        }
         
         
 /**
 * Alta de empleado.
 * Lee los datos desde consola (incluye campos opcionales),
 * crea el objeto Empleado y delega el guardado en EmpleadoService.
 * Muestra mensajes claros de éxito o error.
 */
    public void crearEmpleado(){
               try {
            System.out.print("Nombre del empleado: ");
            String nombre = scanner.nextLine().trim().toUpperCase();
            
            System.out.print("Apellido del empleado: ");
            String apellido = scanner.nextLine().trim().toUpperCase();
            
            System.out.print("DNI del empleado: ");
            String dni = scanner.nextLine().trim();
            
            System.out.print("Email del empleado (opcional, Enter para omitir): ");
            String emailInput = scanner.nextLine().trim();

            String email = emailInput.isEmpty() ? null : emailInput;  //Si el usuario omitio a traves de enter, queda como un string vacio y se setea en NULL. 

            System.out.print("Área del empleado (opcional, Enter para omitir): ");
            String areaInput = scanner.nextLine().trim().toUpperCase();

            String area = areaInput.isEmpty() ? null : areaInput.toUpperCase();
            
            System.out.print("Fecha de ingreso del empleado (opcional, formato yyyy-MM-dd, Enter para omitir): ");
            String fechaStr = scanner.nextLine().trim();

            Date fechaIngreso = null;  // valor por defecto (sin fecha)

             // Valida el formato de fecha ingresado (string), a traves de Date.valueof(), lo transforma a formato Date
             if (!fechaStr.isEmpty()) {
                    try {
                            fechaIngreso = java.sql.Date.valueOf(fechaStr);    // Date.valueOf espera formato: (ej: 2024-11-13)
                    } catch (IllegalArgumentException e) {
                            System.out.println("Fecha inválida.");
                        }
                 }
             
             // PREGUNTAR si quiere editar campos opcionales del legajo
            System.out.print("¿Desea editar los campos opcionales del legajo? (S/N): ");
            String opcion = scanner.nextLine().trim().toUpperCase();

            Legajo legajo;

            if (opcion.equals("S")) {
                legajo = crearLegajo();  // Legajo con campos opcionales
            } else {
                legajo = new Legajo();   // legajo con los valores por defecto
            }

             Empleado empleado = new Empleado(0L,nombre, apellido, dni);
             empleado.setEmail(email);
             empleado.setFechaIngreso(fechaIngreso);
             empleado.setArea(area);
             empleado.setLegajo(legajo); // Asocio el legajo al empleado ANTES de llamar al servicio
             
             empleadoService.insertar(empleado);
             System.out.println("Empleado creado exitosamente, con ID: " + empleado.getId());
            } catch (Exception e) {
            System.err.println("Error al crear persona: " + e.getMessage());
            }
          
        }
    
 /**
 * Búsqueda de empleado por ID.
 * Pide el ID por consola, consulta a EmpleadoService
 * y muestra los datos del empleado si existe.
 */
    public void buscarEmpleadoPorId() {
        
        try {    
            System.out.println("Ingrese el ID del empleado que desea buscar: ");
            long empleadoID = Long.parseLong(scanner.nextLine().trim());
            Empleado e = empleadoService.getById(empleadoID);   
              
             
             if (e == null){
                System.out.println("No se encontró un empleado con ese ID.");
                return;
             }
        
             System.out.println("ID: " + e.getId() +
                      "\nnombre= " + e.getNombre() + 
                      "\napellido= " + e.getApellido() + 
                      "\ndni= " + e.getDni() + 
                      "\nemail= " + e.getEmail() + 
                      "\nfecha_ingreso= " + e.getFechaIngreso() + 
                      "\narea= " + e.getArea());
               
            if (e.getLegajo() != null) {
                  System.out.println("\nnroLegajo= " + e.getLegajo().getNroLegajo() +
                     "\ncategoria= " + e.getLegajo().getCategoria() + 
                     "\nestado= " + e.getLegajo().getEstado() + 
                     "\nfechaAlta= " + e.getLegajo().getFechaAlta());
                }
            
         } catch (NumberFormatException e) {
            System.err.println("Error al buscar empleado: " + e.getMessage());
         } catch (Exception e) {
            System.err.println("Error al buscar empleado: " + e.getMessage());
         }
}
    
  /**
 * Listado de todos los empleados activos.
 * Obtiene la lista desde EmpleadoService y la muestra
 * en consola, incluyendo datos básicos y legajo si aplica.
 */
    public void listarEmpleados(){
        try {
            List<Empleado> empleados = empleadoService.getAll();  
        
            if(empleados.isEmpty()){
                System.out.println("No se encontró ninguna lista de empleados.");
            }
        
            for (Empleado e : empleados) {
                System.out.println("==================================");
                System.out.println("ID: " + e.getId() +
                      "\nnombre= " + e.getNombre() + 
                      "\napellido= " + e.getApellido() + 
                      "\ndni= " + e.getDni() + 
                      "\nemail= " + e.getEmail() + 
                      "\nfecha_ingreso= " + e.getFechaIngreso() + 
                      "\narea= " + e.getArea());
               
            if (e.getLegajo() != null) {
                  System.out.println("\nnroLegajo= " + e.getLegajo().getNroLegajo() +
                     "\ncategoria= " + e.getLegajo().getCategoria() + 
                     "\nestado= " + e.getLegajo().getEstado() + 
                     "\nfechaAlta= " + e.getLegajo().getFechaAlta());
                }
            }   
         } catch (Exception e) {
            System.out.println("Error al listar empleados: " + e.getMessage());
        }
    }
    
   /**
 * Actualización del área de un empleado.
 * Pide el ID, muestra el área actual y permite ingresar
 * un nuevo valor. Delegá la actualización en EmpleadoService.
 */
    public void actualizarAreaEmpleado(){
       try {
        System.out.print("Ingrese el ID del empleado al que desea actualizar su area: ");
        long empleadoId = Long.parseLong(scanner.nextLine().trim());
        Empleado e = empleadoService.getById(empleadoId);
        
        if (e == null) {
            System.out.println("Empleado no encontrado.");
            return;
        }
        String areaActual = e.getArea();

        if (areaActual != null && !areaActual.trim().isEmpty()) {
            System.out.print("¿Desea modificar el área? (s/n): ");
            String respuesta = leerSN();

            if (!"S".equals(respuesta)) {
                System.out.println("Operación cancelada. No se realizaron cambios.");
                return;
        }
                 
        } else {
            System.out.println("Área del empleado vacía.");
        }
         System.out.print("Ingrese el área nueva: ");
         String areaNueva = scanner.nextLine().trim().toUpperCase();
         
         if (areaNueva.trim().isEmpty()) {
            System.out.println("Área nueva vacía. No se realizaron cambios.");
            return;
         }

        e.setArea(areaNueva);
        empleadoService.actualizar(e); 
        System.out.println("Área actualizada correctamente.");
        
      } catch (NumberFormatException e) {
        System.err.println("El ID debe ser un número.");
      } catch (Exception e) {
        System.err.println("Error al actualizar área: " + e.getMessage());
    }
     
    }
    
  /**
 * Baja lógica de un empleado.
 * Pide el ID, muestra los datos para confirmar y, si el
 * usuario acepta, marca al empleado como eliminado.
 */
    public void eliminarEmpleadoPorId(){
        
         try {
                System.out.print("Ingrese el ID del empleado que desea eliminar: ");
                long id = Long.parseLong(scanner.nextLine().trim());

                //Buscar empleado para mostrar info antes de eliminar.
                Empleado empleado = empleadoService.getById(id);
                if (empleado == null) {
                    System.out.println("No se encontró un empleado con ID: " + id);
                    return;
                }

                System.out.println("Empleado encontrado:");
                System.out.println("ID: " + empleado.getId()
                        + "\nNombre: " + empleado.getNombre()
                        + "\nApellido: " + empleado.getApellido()
                        + "\nDNI: " + empleado.getDni());
                            

               System.out.print("¿Desea eliminarlo? (s/n): ");
               String respuesta = leerSN();

               if (!"S".equals(respuesta)) {
                   System.out.println("Operación cancelada. No se realizaron cambios.");
                   return;
               }              
               
                 //Llamar al service, que a su vez llama al DAO.eliminar(id)
                 empleadoService.eliminar(id);

                 System.out.println("Empleado eliminado lógicamente.");

        } catch (NumberFormatException e) {
                System.err.println("El ID debe ser un número entero.");
        } catch (Exception e) {
                 // Acá cae el mensaje del DAO: "ya estaba eliminado o no existe", etc.
                 System.err.println("No se pudo eliminar el empleado: " + e.getMessage());
         }

    }
    
  /**
 * Búsqueda de empleado por DNI.
 * Pide el DNI, consulta a EmpleadoService y muestra
 * el empleado encontrado o un mensaje si no existe.
 */
    public void buscarEmpleadoPorDni(){
    
        try {
            System.out.print("Ingrese el DNI del empleado que desea buscar: ");
            String dni = scanner.nextLine().trim();

            Empleado e = empleadoService.buscarPorDni(dni);

             if (e == null) {
                    System.out.println("No se encontró un empleado con DNI." + dni);
                    return;
              }

        
              System.out.println("ID: " + e.getId()
                     + "\nNombre: " + e.getNombre()
                     + "\nApellido: " + e.getApellido()
                     + "\nDNI: " + e.getDni()
                     + "\nÁrea: " + e.getArea());
                     

            if (e.getLegajo() != null) {
                    System.out.println("\nNro Legajo: " + e.getLegajo().getNroLegajo()
                         + "\nCategoría: " + e.getLegajo().getCategoria());
             }

         } catch (Exception ex) {
                System.err.println("Error al buscar empleado por DNI: " + ex.getMessage());
         }
    
    
    }
    
     
    public Legajo crearLegajo(){
        
         //Categoria
        System.out.print("Ingrese la categoria (opcional, Enter para omitir): ");
        String categoriaInput = scanner.nextLine().trim().toUpperCase();
        String categoria = null;
        if (!categoriaInput.isEmpty()) {
            try {
                 categoria = categoriaInput;
            } catch (IllegalArgumentException ex) {
                 System.out.println("Estado inválido, se usará ACTIVO por defecto.");
                 categoria = null; 
            }
        }
  
          //ESTADO
          //Si se ingresa un estado vacio el DAO contiene el ESTADO.ACTIVO por defecto (opcional)
        System.out.print("Ingrese el estado (opcional, Enter para omitir): ");
        String  estadoInput = scanner.nextLine().trim().toUpperCase();
        
        Estado estado = null;
        if (!estadoInput.isEmpty()) {
            try {
                 estado = Estado.valueOf(estadoInput.toUpperCase());
            } catch (IllegalArgumentException ex) {
                 System.out.println("Estado inválido, se usará ACTIVO por defecto.");
                 estado = null; 
            }
        }
        //Observaciones (opcional)
        System.out.print("Observaciones (opcional, Enter para omitir): ");
        String observacionesInput = scanner.nextLine().trim();
        String observaciones = observacionesInput.isEmpty() ? null : observacionesInput;
        
        Legajo legajo = new Legajo();
        legajo.setCategoria(categoria);
        legajo.setEstado(estado);
        legajo.setObservaciones(observaciones);
        
        return legajo;
    }

  /**
 * Búsqueda de legajo por ID.
 * Pide el ID de legajo, consulta a LegajoService
 * y muestra sus datos si existe.
 */
    public void buscarLegajoPorId(){
    
     try {    
            System.out.println("Ingrese el ID del legajo que desea buscar: ");
            long legajoId = Long.parseLong(scanner.nextLine().trim());
            Legajo legajo = legajoService.getById(legajoId);
              
             if (legajo == null){
                System.out.println("No se encontró un legajo con ese ID.");
                return;
             }
    
            System.out.println("ID: " + legajo.getId() +
                     "\nnroLegajo= " + legajo.getNroLegajo() +
                     "\ncategoria= " + legajo.getCategoria() + 
                     "\nestado= " + legajo.getEstado() + 
                     "\nfechaAlta= " + legajo.getFechaAlta());
            
        } catch (NumberFormatException e) {
                System.err.println("El ID debe ser un número entero.");
        } catch (Exception e) {
                System.err.println("Error al buscar legajo: " + e.getMessage());
        }
     
    }
    
   /**
 * Listado de legajos activos.
 * Recupera todos los legajos no eliminados desde
 * LegajoService y los muestra por consola.
 */
    public void listarLegajos(){
        try {
        List<Legajo> legajos = legajoService.getAll(); 

        if (legajos == null || legajos.isEmpty()) {
            System.out.println("No hay legajos activos.");
            return;
        }

        for (Legajo l : legajos) {
            System.out.println("==================================");
            System.out.println("ID: " + l.getId()
                    + "\nNro legajo: " + l.getNroLegajo()
                    + "\nCategoría: " + l.getCategoria());
            
        }
    } catch (Exception e) {
        System.err.println("Error al listar legajos: " + e.getMessage());
    }
    }
    
  /**
 * Actualización del estado de un legajo.
 * Pide ID de legajo y nuevo estado (ACTIVO/INACTIVO),
 * valida la entrada y delega la actualización en LegajoService.
 */
    public void actualizarEstadoLegajo() { 
        
        try {
        System.out.print("Ingrese el ID del legajo que desea actualizar: ");
        long idLegajo = Long.parseLong(scanner.nextLine().trim());

       
        Legajo legajo = legajoService.getById(idLegajo);  
        if (legajo == null) {
            System.out.println("No se encontró un legajo con ese ID.");
            return;
        }

        System.out.println("Legajo encontrado:");
        System.out.println("ID: " + legajo.getId()
                + "\nNro legajo: " + legajo.getNroLegajo()
                + "\nCategoría: " + legajo.getCategoria()
                + "\nEstado actual: " + legajo.getEstado());

       
        System.out.print("\nIngrese el nuevo estado (ACTIVO / INACTIVO): ");
        String estadoStr = scanner.nextLine().trim().toUpperCase();

        Estado nuevoEstado;
        try {
            nuevoEstado = Estado.valueOf(estadoStr);   
        } catch (IllegalArgumentException ex) {
            System.out.println("Estado inválido. Debe ser ACTIVO o INACTIVO.");
            return;
        }

        
        System.out.print("¿Confirma el cambio de estado? (s/n): ");
        String resp = leerSN();
        if (!"S".equals(resp)) {
            System.out.println("Operación cancelada. No se realizaron cambios.");
            return;
        }


        legajoService.cambiarEstado(idLegajo, nuevoEstado);
        System.out.println("Estado del legajo actualizado correctamente.");

    } catch (NumberFormatException e) {
        System.err.println("El ID debe ser un número entero.");
    } catch (Exception e) {
        System.err.println("Error al actualizar estado del legajo: " + e.getMessage());
    }
}
    
  /**
 * Baja lógica de un legajo.
 * Pide el ID, muestra información para confirmar y, si el
 * usuario acepta, marca el legajo como eliminado.
 */
    public void eliminarLegajoPorId(){
    
        try {
            System.out.println("Ingrese el ID del legajo que desea eliminar: ");
            long legajoId = Long.parseLong(scanner.nextLine().trim());
            
            Legajo legajo = legajoService.getById(legajoId);
            if(legajo == null) {
                System.out.println("Legajo no encontrado.");
                return;
            }
              
            System.out.println("Legajo encontrado.");
            System.out.println("ID: " + legajo.getId() +
                         "\nCategoria: " + legajo.getCategoria()+
                         "\nEstado: " + legajo.getEstado()+
                         "\nFecha de alta: " + legajo.getFechaAlta()+
                         "\nObservaciones: " + legajo.getObservaciones());
            
              System.out.print("¿Desea eliminarlo? (s/n): ");
              String respuesta = leerSN();
              
              if (!"S".equals(respuesta)) {
                  System.out.println("Operación cancelada. No se realizaron cambios.");
                  return;
              }
                //Llamar al service, que a su vez llama al DAO.eliminar(id)
              legajoService.eliminar(legajoId);

               System.out.println("Empleado eliminado lógicamente.");

        } catch (NumberFormatException e) {
                System.err.println("El ID debe ser un número entero.");
        } catch (Exception e) {
                 System.err.println("No se pudo eliminar el empleado: " + e.getMessage());
         }
    }
      
   /**
 * Metodo privado 
 * Para las consulta por si o por no de la interfaz (s/n).
 */ 
    private String leerSN() {
    return scanner.nextLine().trim().toUpperCase();
    }    
    }
     

        
  
        
        
        
        
        
        
        
        
        
        
        
        
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    

         
         
         
         
         
         
         
         
         
         
         
         
         
         
         
         
         
         
         
         
         
         
         
         
         
         
         
         
         
         
         
         
         
         
         
         
         
         
         
         
         
         
         
         
         
         
         
         
         
         
         
         
         
         
         
         
         
         
         
         
         
         
         
         
         
         
         
         
         
         
         
         
         
         
         
         
         
         
         
         
         
         
         
         
         
         
         
         
         
         
         
         
         
         
         
         
         
         
         
         
         
         
         
         
         
         
         
         
         
         
         
         
         
         
         
         
         
         
         
         
         
         
         
         
         

