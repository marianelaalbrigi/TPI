# TPI - Sistema de Gestión de Empleados y Legajos

Trabajo Práctico Integrador - Programación II

## Descripción del Dominio

Este proyecto implementa un sistema de gestión de **Empleados** y **Legajos**, aplicando conceptos de programación orientada a objetos y acceso a base de datos.

El grupo eligió el dominio Empleado – Legajo porque representa un caso realista, sencillo y aplicable a múltiples entornos organizacionales. Este tipo de relación es común en sistemas de gestión de personal, donde cada empleado cuenta con un legajo único que centraliza su información administrativa y laboral.

### Entidades Principales:

- **Empleado**: Representa a un empleado de la organización con sus datos personales (nombre, apellido, DNI, email, fecha de ingreso, área).
- **Legajo**: Representa el legajo administrativo de un empleado (número de legajo, categoría, estado, fecha de alta, observaciones).

### Características del Dominio:

- **Relación uno a uno (1:1)** entre Empleado y Legajo
- **Baja lógica**: Los registros no se eliminan físicamente, sino que se marcan como inactivos
- **Validaciones**: Campos únicos (DNI, email, número de legajo), obligatorios y con restricciones de longitud
- **Estados**: Uso de enumeraciones para el estado del legajo (ACTIVO/INACTIVO)

---

## Requisitos

### Software necesario:

- **Java Development Kit (JDK)**: Versión 17 o superior
- **MySQL**: Versión 8.0 o superior
- **MySQL Connector/J**: Driver JDBC para MySQL (incluir el JAR en el classpath)
- **IDE recomendado**: VS Code con Extension Pack for Java, NetBeans, o IntelliJ IDEA

---

## Configuración Inicial

### 1. Clonar el Repositorio

```bash
git clone https://github.com/JulietaNowell/TPI.git
cd TPI
```

### 2. Configurar la Base de Datos

#### a) Crear la base de datos en MySQL

Ejecuta los siguientes comandos en tu cliente MySQL (MySQL Workbench, terminal, etc.):

```sql
CREATE DATABASE tpintegrador CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE tpintegrador;
```

#### b) Crear las tablas

Ejecuta el archivo SQL proporcionado para crear la estructura de las tablas:

```sql
-- Tabla Empleados
CREATE TABLE empleado (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    apellido VARCHAR(100) NOT NULL,
    dni VARCHAR(20) NOT NULL UNIQUE,
    email VARCHAR(150) UNIQUE,
    fecha_ingreso DATE,
    area VARCHAR(100),
    borrado BOOLEAN DEFAULT FALSE
);

-- Tabla Legajos
CREATE TABLE legajo (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nro_legajo VARCHAR(50) NOT NULL UNIQUE,
    categoria VARCHAR(100),
    estado VARCHAR(20) NOT NULL,
    fecha_alta DATE,
    observaciones TEXT,
    empleado_id BIGINT,
    borrado BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (empleado_id) REFERENCES empleado(id)
);
```

> **Nota**: Si tienes un archivo `.sql` proporcionado por el docente, ejecútalo directamente en MySQL en lugar de crear las tablas manualmente.

### 3. Configurar las Credenciales de la Base de Datos

Una vez creada la base de datos y las tablas, edita el archivo de configuración ubicado en:
```
TPIProgramacionII/src/tpiprogramacionii/config/db.properties
```

Configura las credenciales según tu instalación de MySQL:

```properties
db.host=localhost
db.port=3306
db.database=tpintegrador
db.user=root
db.password=tu_password_mysql
```


### 4. Agregar el Driver MySQL JDBC

Este paso es **obligatorio** para que la aplicación pueda conectarse a la base de datos.

#### Opción A: Usando VS Code
1. Descarga el MySQL Connector/J desde: https://dev.mysql.com/downloads/connector/j/
2. Copia el archivo `.jar` a la carpeta `TPIProgramacionII/lib/`
3. En VS Code, presiona `Cmd+Shift+P` (Mac) o `Ctrl+Shift+P` (Windows/Linux)
4. Escribe "Java: Configure Classpath" y selecciona la opción
5. Agrega el JAR a "Referenced Libraries"

#### Opción B: Usando NetBeans
1. Click derecho en el proyecto → Properties
2. En "Libraries" → "Compile" → "Add JAR/Folder"
3. Selecciona el archivo `mysql-connector-java-x.x.x.jar`

---

## Compilación y Ejecución

### Desde VS Code:

1. Abre el proyecto en VS Code
2. Asegurarse de tener instalada la extensión "Extension Pack for Java"
3. Abre el archivo `Main.java` ubicado en:
   ```
   TPIProgramacionII/src/tpiprogramacionii/main/Main.java
   ```
4. Presiona el botón "Run" que aparece sobre el método `main()` o presiona `F5`

### Desde NetBeans:

1. Abre el proyecto en NetBeans (File → Open Project)
2. Click derecho en el proyecto → "Run" o presiona `F6`

### Desde Terminal:

```bash
cd TPIProgramacionII/src
javac -cp "../lib/*:." tpiprogramacionii/main/Main.java
java -cp "../lib/*:." tpiprogramacionii.main.Main
```

> **Nota para Windows**: Reemplaza `:` por `;` en el classpath:
> ```
> javac -cp "../lib/*;." tpiprogramacionii/main/Main.java
> ```

---

## Flujo de Uso de la Aplicación

Al ejecutar la aplicación, se mostrará un menú principal con las siguientes opciones:

### 1. Gestión de Empleados
- **Crear empleado**: Registrar un nuevo empleado (requiere: nombre, apellido, DNI)
- **Listar empleados**: Ver todos los empleados activos del sistema
- **Buscar empleado**: Buscar por ID o DNI
- **Modificar empleado**: Actualizar datos de un empleado existente
- **Eliminar empleado**: Realizar baja lógica de un empleado

### 2. Gestión de Legajos
- **Crear legajo**: Asignar un legajo a un empleado (requiere: número de legajo, categoría)
- **Listar legajos**: Ver todos los legajos activos
- **Buscar legajo**: Buscar por ID o número de legajo
- **Modificar legajo**: Actualizar datos de un legajo existente
- **Eliminar legajo**: Realizar baja lógica de un legajo

### Flujo Básico de Ejemplo:

1. **Crear un empleado**:
   - Seleccionar opción "Crear empleado"
   - Ingresar: Nombre, Apellido, DNI (obligatorios)
   - Opcionalmente: email, fecha de ingreso, área

2. **Asignar un legajo al empleado**:
   - Seleccionar opción "Crear legajo"
   - Ingresar: Número de legajo, categoría
   - Seleccionar el empleado al que se asignará el legajo

3. **Consultar información**:
   - Usar las opciones de listado para ver los registros creados
   - Usar búsqueda por ID o DNI/número de legajo para consultas específicas

---

## Estructura del Proyecto

```
TPIProgramacionII/
├── src/
│   └── tpiprogramacionii/
│       ├── config/          # Configuración de BD
│       ├── dao/             # Data Access Objects (acceso a BD)
│       ├── entities/        # Entidades del dominio
│       ├── main/            # Punto de entrada y menú
│       ├── service/         # Lógica de negocio
│       └── utils/           # Utilidades (conexión a BD)
├── lib/                     # Librerías externas (MySQL Connector)
└── build.xml                # Configuración de compilación
```

---

## Link al video 
[Ver video en YouTube](https://www.youtube.com/watch?v=yVh0wiCOpFk)

## Notas Adicionales

- El sistema utiliza **baja lógica**, por lo que los registros eliminados permanecen en la base de datos con el campo `borrado = TRUE`
- Las consultas por defecto solo muestran registros activos (`borrado = FALSE`)
- El DNI y el email del empleado deben ser únicos en el sistema
- El número de legajo debe ser único en el sistema
- Un empleado solo puede tener un legajo asignado (relación 1:1)
