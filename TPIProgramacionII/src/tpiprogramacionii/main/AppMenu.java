
package tpiprogramacionii.main;

import java.util.Scanner;
import tpiprogramacionii.dao.EmpleadoDAO;
import tpiprogramacionii.dao.LegajoDAO;
import tpiprogramacionii.service.EmpleadoService;


class AppMenu {

 private final Scanner scanner;
         private final MenuController menuController;
         private boolean running;
       
         public  AppMenu() {
             this.scanner = new Scanner(System.in);
             EmpleadoService empleadoService = createEmpleadoService();
             this.menuController = new MenuController(scanner, empleadoService);
             this.running = true;
         }

       
         public static void main(String[] args) {
               AppMenu app = new AppMenu();
               app.run();
         }

         public void run() {
              while (running) {
              try {
                  MenuDisplay.mostrarMenuPrincipal();
                  int opcion = Integer.parseInt(scanner.nextLine());
                  processOption(opcion);
               } catch (NumberFormatException e) {
                  System.out.println("Entrada invalida. Por favor, ingrese un numero.");
               }
              }
             scanner.close();
         }
          
          
          
         private void processOption(int opcion) {
             switch (opcion) {
                  case 1 -> menuController.crearEmpleado();
                  case 2 -> menuController.buscarEmpleadoPorId();
                  case 3 -> menuController.listarEmpleados();
                  case 4 -> menuController.actualizarAreaEmpleado();
                  case 5 -> menuController.eliminarEmpleadoPorId();
                  case 6 -> menuController.buscarEmpleadoPorDni();
                  case 7 -> menuController.crearLegajoIndependiente();
                  case 8 -> menuController.buscarLegajoPorId();
                  case 9 -> menuController.listarLegajos();
                  case 10 -> menuController.actualizarEstadoLegajo();
                  case 11 -> menuController.eliminarLegajoPorId();
                  case 0 -> {
                  System.out.println("Saliendo...");
                  running = false;
                 }
            default -> System.out.println("Opcion no valida.");
            }
         }    
             
         private EmpleadoService createEmpleadoService() {
                LegajoDAO legajoDAO = new LegajoDAO();
                EmpleadoDAO empleadoDAO = new EmpleadoDAO(legajoDAO);
                return new EmpleadoService(empleadoDAO, legajoDAO);
        }       
}
