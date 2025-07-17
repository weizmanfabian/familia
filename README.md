# Sistema de Gestión Familiar

[![Java](https://img.shields.io/badge/Java-21-red.svg)](https://www.oracle.com/java/technologies/javase/jdk21-downloads.html)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.10-green.svg)](https://spring.io/projects/spring-boot)
[![Docker](https://img.shields.io/badge/Docker-28.0.1-blue.svg)](https://www.docker.com/)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16.1-336791.svg)](https://www.postgresql.org/)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

Sistema completo de gestión familiar desarrollado con arquitectura de microservicios, implementando las mejores prácticas de desarrollo empresarial y patrones de diseño modernos.

## Tabla de Contenidos

- [Descripción](#descripción)
- [Arquitectura](#arquitectura)
- [Tecnologías](#tecnologías)
- [Requisitos Previos](#requisitos-previos)
- [Instalación](#instalación)
- [Configuración](#configuración)
- [Uso](#uso)
- [API Reference](#api-reference)
- [Testing](#testing)
- [Despliegue](#despliegue)
- [Contribución](#contribución)
- [Licencia](#licencia)

## Descripción

Este proyecto implementa un sistema backend robusto para la gestión de personas y familias, utilizando una arquitectura basada en microservicios con Spring Boot. La aplicación permite realizar operaciones CRUD completas sobre entidades familiares, con persistencia en PostgreSQL y despliegue containerizado con Docker.

### Características Principales

- **API REST**: Endpoints completamente documentados con OpenAPI/Swagger
- **Arquitectura Limpia**: Separación clara entre capas de dominio, aplicación e infraestructura
- **Containerización**: Despliegue completo con Docker Compose
- **Base de Datos**: Persistencia robusta con PostgreSQL y migraciones automatizadas
- **Validación**: Validación de datos integral con Spring Validation
- **Manejo de Errores**: Sistema de excepciones personalizado y responses estandarizados

## Arquitectura

### Stack Tecnológico

#### Backend
- **Java 21**: Aprovecha las últimas características del lenguaje incluyendo Records, Pattern Matching y Sealed Classes
- **Spring Boot 3.3.10**: Framework principal con módulos:
  - Spring Data JPA para persistencia
  - Spring Validation para validación de datos
  - Spring Web para APIs RESTful
- **PostgreSQL 16.1**: Sistema de gestión de base de datos empresarial
- **Docker Compose**: Orquestación de contenedores para desarrollo y producción

#### Arquitectura de Capas
```
┌─────────────────────────────────────────────────────────────┐
│                    API Layer                                │
├─────────────────────────────────────────────────────────────┤
│  Controllers │ Requests │ Responses │ Error Handlers        │
├─────────────────────────────────────────────────────────────┤
│                 Application Layer                           │
├─────────────────────────────────────────────────────────────┤
│              Services │ Use Cases                           │
├─────────────────────────────────────────────────────────────┤
│                  Domain Layer                               │
├─────────────────────────────────────────────────────────────┤
│           Entities │ Repositories │ Abstractions            │
├─────────────────────────────────────────────────────────────┤
│                Infrastructure Layer                         │
├─────────────────────────────────────────────────────────────┤
│              Database │ External Services                   │
└─────────────────────────────────────────────────────────────┘
```

## Tecnologías

### Herramientas de Desarrollo
| Herramienta | Versión | Descripción |
|-------------|---------|-------------|
| Java | 21+ | Lenguaje principal de desarrollo |
| Spring Boot | 3.3.10 | Framework de aplicación |
| PostgreSQL | 16.1 | Sistema de base de datos |
| Docker | 28.0.1 | Containerización |
| Maven | 3.9+ | Gestión de dependencias |

### IDEs Recomendados
- **Backend**: IntelliJ IDEA 2024.3.5+ (Ultimate Edition)
- **Base de Datos**: DBeaver 23.2.x, DataGrip, PgAdmin 4.3.x
- **API Testing**: Postman, Insomnia

## Requisitos Previos

Antes de comenzar, asegúrate de tener instalado:

- **Docker Desktop** (versión 28.0.1 o superior)
- **Git** (para clonar el repositorio)
- **Postman** (opcional, para pruebas de API)

### Verificación de Requisitos
```bash
# Verificar Docker
docker --version
docker-compose --version

# Verificar Git
git --version
```

## Instalación

### 1. Clonar el Repositorio
```bash
git clone https://github.com/weizmanfabian/familia.git
cd familia/familia/familia
```

### 2. Estructura del Proyecto
```
Familia/
├── .env                          # Variables de entorno
├── Dockerfile                    # Configuración de contenedor
├── docker-compose.yml           # Orquestación de servicios
├── pom.xml                      # Configuración Maven
├── Familia.postman_collection.json  # Colección de pruebas
├── db/
│   └── sql/
│       ├── create_schema.sql    # Esquema de base de datos
│       └── data.sql             # Datos iniciales
├── src/
│   ├── main/
│   │   ├── java/com/weiz/Familia/
│   │   │   ├── api/
│   │   │   │   ├── controllers/     # Controladores REST
│   │   │   │   ├── requests/        # DTOs de entrada
│   │   │   │   ├── responses/       # DTOs de salida
│   │   │   │   └── errorHandler/    # Manejo de errores
│   │   │   ├── domain/
│   │   │   │   ├── entities/        # Entidades JPA
│   │   │   │   └── repositories/    # Interfaces de repositorio
│   │   │   ├── infraestructure/
│   │   │   │   ├── abstracts/       # Abstracciones
│   │   │   │   └── services/        # Implementaciones
│   │   │   └── util/
│   │   │       └── Exceptions/      # Excepciones personalizadas
│   │   └── resources/
│   │       └── application.properties
│   └── test/
└── README.md
```

## Configuración

### Variables de Entorno
El archivo `.env` contiene la configuración principal:

```properties
# Configuración de la Aplicación
APP_NAME=Familia
APP_CONTEXT_PATH=/familia
APP_PORT_IN=8080
APP_PORT_OUT=8088

# Configuración de Base de Datos
DB_NAME=familia
DB_HOST=localhost
DB_USER=weizman
DB_PASSWORD=YourStrong()Passw0rd
DB_PORT_IN=5432
DB_PORT_OUT=5438
```

### Configuración de Base de Datos
La base de datos se inicializa automáticamente con:
- Esquema completo en `db/sql/create_schema.sql`
- Datos de prueba en `db/sql/data.sql`

## Uso

### Iniciar la Aplicación
```bash
# Construir y ejecutar todos los servicios
docker-compose up --build

# Ejecutar en segundo plano
docker-compose up -d --build

# Ver logs
docker-compose logs -f
```

### Detener la Aplicación
```bash
# Detener servicios
docker-compose down

# Detener y eliminar volúmenes
docker-compose down -v
```

## API Reference

### Endpoints Principales

#### Gestión de Personas
```http
GET    /familia/personas           # Obtener todas las personas
POST   /familia/personas           # Crear nueva persona
GET    /familia/personas/{id}      # Obtener persona por ID
PUT    /familia/personas/{id}      # Actualizar persona
DELETE /familia/personas/{id}      # Eliminar persona
```

#### Gestión de Ciudades
```http
GET    /familia/ciudades           # Obtener todas las ciudades
```

### Ejemplo de Request
```json
POST /familia/personas
{
    "numero_documento": "1105062032",
    "nombre": "Weizman",
    "apellidos": "Castañeda",
    "fecha_nacimiento": "1998-05-27",
    "correo_electronico": "weizman@correo.com",
    "telefono": "3111111111",
    "ocupacion": "INDEPENDIENTE",
    "idCiudad": 1
}
```

### Ejemplo de Response
```json
{
    "numero_documento": "1105062032",
    "nombre": "Weizman",
    "apellidos": "Castañeda",
    "fecha_nacimiento": "1998-05-27",
    "correo_electronico": "weizman@correo.com",
    "telefono": "3111111111",
    "ocupacion": "INDEPENDIENTE",
    "ciudad": {
        "id": 1,
        "nombre": "Medellín",
        "departamento": "Antioquia"
    },
    "esViable": true
}
```

## Testing

### Pruebas con Postman
1. Importar `Familia.postman_collection.json` en Postman
2. Configurar variables de entorno si es necesario
3. Ejecutar la colección completa o requests individuales

### Conexión a Base de Datos
```properties
Host: localhost
Puerto: 5438
Usuario: weizman
Contraseña: YourStrong()Passw0rd
Base de datos: familia
```

### Verificación de Servicios
```bash
# Verificar que la aplicación esté corriendo
curl http://localhost:8088/familia/personas

# Verificar conexión a base de datos
docker-compose exec db psql -U weizman -d familia -c "SELECT * FROM personas LIMIT 5;"
```

## Despliegue

### Desarrollo
```bash
docker-compose up --build
```

### Producción
Para producción, considere:
- Usar variables de entorno más seguras
- Implementar volúmenes persistentes
- Configurar reverse proxy (Nginx)
- Implementar SSL/TLS
- Configurar logging centralizado

### Monitoreo
```bash
# Ver logs de la aplicación
docker-compose logs app

# Ver logs de base de datos
docker-compose logs db

# Monitorear recursos
docker stats
```

# Despliegue del front
en la raiz del proyecto ingresamos al directorio **familia-front**
```bash
cd familia-front
```
### inicialización y despliegue
```bash
npm install

ng serve
```
El front estará disponible en la ruta 
```bash
http://localhost:4200/
```

---

## Contribución

### Flujo de Trabajo
1. Fork el repositorio
2. Crear rama feature (`git checkout -b feature/nueva-funcionalidad`)
3. Commit cambios (`git commit -m 'Add: nueva funcionalidad'`)
4. Push a la rama (`git push origin feature/nueva-funcionalidad`)
5. Crear Pull Request

### Estándares de Código
- Seguir convenciones de Java y Spring Boot
- Documentar métodos públicos
- Escribir tests unitarios
- Usar commits descriptivos

### Reportar Issues
Para reportar problemas o solicitar características:
- Usar el sistema de Issues de GitHub
- Incluir logs relevantes
- Describir pasos para reproducir el problema

## Licencia

Este proyecto está licenciado bajo la [MIT License](LICENSE).

---

**Desarrollado por**: [weizmanfabian](https://github.com/weizmanfabian)  
**Versión**: 1.0.0  
**Última actualización**: Enero 2025