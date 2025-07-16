
# Sistema de Gestión de Libros

[![Java](https://img.shields.io/badge/Java-21-red.svg)](https://www.oracle.com/java/technologies/javase/jdk21-downloads.html)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.10-green.svg)](https://spring.io/projects/spring-boot)
[![Docker](https://img.shields.io/badge/Docker-28.0.1-blue.svg)](https://www.docker.com/)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16.1-336791.svg)](https://www.postgresql.org/)

Sistema 

## Descripción

Este proyecto implementa un sistema backend para personas, utilizando una arquitectura basada en microservicios. La aplicación permite realizar operaciones CRUD (Crear, Leer, Actualizar, Eliminar) sobre personas, con persistencia en una base de datos PostgreSQL. Está construida con Spring Boot y se despliega utilizando Docker para facilitar el desarrollo y la producción.

## Tecnologías Utilizadas

### Backend
- **Java 21**: Aprovecha las últimas características del lenguaje, como Records, Pattern Matching y Sealed Classes.
- **Spring Boot 3.3.10**: Framework principal con soporte para:
  - **Spring Data JPA**: Para la persistencia de datos.
  - **Spring Validation**: Para la validación de datos.
  - **Spring Web**: Para la creación de APIs RESTful.
- **PostgreSQL 16.1**: Sistema de gestión de base de datos robusto y escalable.
- **Docker Compose**: Orquestación de contenedores para desarrollo y producción.
- **Arquitectura de Microservicios**: Implementación modular con comunicación a través de API REST.

### Herramientas Recomendadas
- **IDE Backend**: IntelliJ IDEA 2024.3.5 o versiones posteriores.
- **Gestión de Base de Datos**: DBeaver 23.2.x, DataGrip, o PgAdmin 4.3.x.
- **Pruebas de API**: Postman (para importar y probar la colección de solicitudes).

## Estructura del Proyecto

```
Biblioteca/
├── .env
├── Dockerfile
├── docker-compose.yml
├── pom.xml
├── db/
│   └── sql/
│       ├── create_schema.sql
│       ├── data.sql
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/weiz/Familia/
│   │   │       ├── api/
│   │   │       │   ├── controllers/
│   │   │       │   │   ├── PersonaController.java
│   │   │       │   ├── errorHandler/
│   │   │       │   ├── requests/
│   │   │       │   │   ├── PersonaRequest.java
│   │   │       │   └── responses/
│   │   │       │       ├── errors/
│   │   │       │       ├── CiudadResponse.java
│   │   │       │       └── PersonaResponse.java
│   │   │       ├── domain/
│   │   │       │   ├── entities/
│   │   │       │   │   ├── CiudadEntity.java
│   │   │       │   │   └── PersonaEntity.java
│   │   │       │   └── repositories/
│   │   │       │       ├── CiudadRepository.java
│   │   │       │       └── PersonaRepository.java
│   │   │       ├── infraestructure/
│   │   │       │   ├── abstracts/
│   │   │       │   └── services/
│   │   │       │       └── imp/
│   │   │       │           ├── PersonaService.java
│   │   │       └── util/
│   │   │           └── Exceptions/
│   │   │               ├── CustomException.java
│   │   │               └── EntityNotFoundException.java
│   │   └── resources/
│   │       └── application.properties
│   └── test/
│       ├── java/
│       └── resources/
└── 
```

## Requisitos Previos

Antes de comenzar, asegúrate de tener instalados los siguientes programas:

- **Docker** (versión 28.0.1 o superior) y **Docker Compose**.
- **Git** (para clonar el repositorio).
- Opcional: **IntelliJ IDEA** (para desarrollo local sin Docker).
- Opcional: **Postman** (para probar la API).

## Configuración del Entorno de Desarrollo

### 1. Clonar el Repositorio

Clona el repositorio e ingresa al directorio del proyecto:

```bash
git clone https://github.com/weizmanfabian/familia.git
cd familia/
```

### 2. Configuración del Backend

Una ves en la raiz del proyecto, ingresa al directorio donde se ve toda la estructura previamente expuesta del backend
```bash
cd familia/familia
```

#### 2.1. Verificar el archivo `.env`

En el directorio `familia/`, encontrarás un archivo `.env` que contiene la configuración de la aplicación y la base de datos, incluyendo los puertos internos (`IN`) y los expuestos (`OUT`):

```properties
APP_NAME=Familia
APP_CONTEXT_PATH=/familia
APP_PORT_IN=8080
APP_PORT_OUT=8088

DB_NAME=familia
DB_HOST=localhost
DB_USER=weizman
DB_PASSWORD=YourStrong()Passw0rd
DB_PORT_IN=5432
DB_PORT_OUT=5438
```

**Nota**: Asegúrate de que el puerto `5439` (definido como `DB_PORT_OUT`) no esté ocupado en tu máquina local, ya que será usado para mapear el puerto interno de PostgreSQL (`5439`).

#### 2.2. Construir y Ejecutar los Contenedores con Docker Compose

Desde el directorio `familia/familia/familia` que es el directorio backend, ejecuta el siguiente comando para construir y levantar los contenedores:

```bash
docker-compose up
```

Esto hará lo siguiente:
- Iniciará un contenedor de PostgreSQL (`db`) con la base de datos inicializada mediante los scripts SQL en `db/sql/`.

**Nota**: Si deseas detener y eliminar los contenedores después de probar la aplicación, ejecuta:

```bash
docker-compose down
```

#### 2.2.1 Compilar y ejecutar el proyecto backend

```bash
mvn clean install
mvn spring-boot:run
```

#### 2.3. Acceder a la Aplicación

Una vez que los contenedores estén en ejecución, la aplicación estará disponible en:

- **API REST**: `http://localhost:8089/familia`
- **Base de Datos**: Puedes conectarte a PostgreSQL en `localhost:5439` usando las credenciales definidas en `.env`:
  - Host: `localhost`
  - Puerto: `5439`
  - Usuario: `weizman`
  - Contraseña: `YourStrong()Passw0rd`
  - Base de datos: `familia`

### 3. Probar la API (Opcional)

#### 3.1. Importar la Colección de Postman

1. Abre Postman.
2. Importa el archivo `Familia.postman_collection.json` que se encuentra en el directorio `Familia/`.
3. Ejecuta las solicitudes de la colección para probar los endpoints de la API, como:
  - Crear, leer, actualizar y eliminar personas.

**Ejemplo de Endpoints**:
- `GET /personas`: Obtener todos las personas.
- `POST /personas`: Crear una nueva persona.

## Notas Adicionales

- **Persistencia de Datos**: Los scripts SQL en `db/sql/` inicializan el esquema, datos, funciones y procedimientos almacenados en la base de datos PostgreSQL al iniciar el contenedor `db`.
- **Depuración**: Si encuentras problemas, revisa los logs de los contenedores con:
  ```bash
  docker-compose logs db
  ```
- **Entorno de Producción**: Para un entorno de producción, considera ajustar las variables de entorno en `.env` (por ejemplo, cambiar la contraseña de la base de datos) y usar un volumen persistente para los datos de PostgreSQL.

## Contribuciones

Si deseas contribuir al proyecto, por favor sigue estos pasos:

1. Haz un fork del repositorio.
2. Crea una nueva rama (`git checkout -b feature/nueva-funcionalidad`).
3. Realiza tus cambios y haz commit (`git commit -m "Añadir nueva funcionalidad"`).
4. Sube tus cambios (`git push origin feature/nueva-funcionalidad`).
5. Crea un Pull Request en GitHub.

## Licencia

Este proyecto está licenciado bajo la [MIT License](LICENSE).
