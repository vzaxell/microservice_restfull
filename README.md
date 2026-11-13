# Microservicio CRUD con NoSQL y Calidad de Código

Práctica de laboratorio **HU-01** — Universidad Tecnológica de Chihuahua (UTCH), Desarrollo y Gestión de Software.

Microservicio RESTful con **Spring Boot 3** y **MongoDB** que implementa un CRUD sobre la entidad `Libro`, con
validaciones, excepciones personalizadas, manejador global de errores, pruebas unitarias y de integración,
cobertura con **JaCoCo** y análisis estático con **SonarQube**.

## Tecnologías

Java 21 · Spring Boot 3.5 · Maven · MongoDB 7 · JUnit 5 · Mockito · Testcontainers · JaCoCo · SonarQube · Swagger UI · Docker

## Requisitos

- JDK 21
- Docker Desktop (MongoDB, SonarQube y pruebas de integración)
- Maven (se incluye `mvnw`, no es necesario instalarlo)

## Ejecución

```bash
# 1. Levantar MongoDB (y SonarQube)
docker compose up -d mongodb

# 2. Ejecutar el microservicio
./mvnw spring-boot:run          # Windows PowerShell: .\mvnw.cmd spring-boot:run
```

- API: http://localhost:8080/api/libros
- Swagger UI: http://localhost:8080/swagger-ui.html

## Endpoints

| Método | Ruta               | Descripción            | Respuestas        |
|--------|--------------------|------------------------|-------------------|
| POST   | `/api/libros`      | Crear libro            | 201, 400, 409     |
| GET    | `/api/libros`      | Listar libros          | 200               |
| GET    | `/api/libros/{id}` | Obtener libro por id   | 200, 404          |
| PUT    | `/api/libros/{id}` | Actualizar libro       | 200, 400, 404, 409|
| DELETE | `/api/libros/{id}` | Eliminar libro         | 204, 404          |

Ejemplo de cuerpo (POST/PUT):

```json
{
  "titulo": "Clean Code",
  "autor": "Robert C. Martin",
  "isbn": "9780132350884",
  "precio": 450.00,
  "anioPublicacion": 2008
}
```

Ejemplo de error (`404`):

```json
{
  "timestamp": "2026-11-12T10:30:00",
  "status": 404,
  "error": "Not Found",
  "mensaje": "No se encontró el libro con id 123",
  "ruta": "/api/libros/123"
}
```

En errores de validación (`400`) se incluye además el arreglo `detalles` con `campo: mensaje`.

## Pruebas y cobertura (JaCoCo)

```bash
./mvnw clean verify
```

- Pruebas unitarias (servicio, Mockito): `LibroServiceTest`
- Pruebas web (controlador + manejador de errores, MockMvc): `LibroControllerTest`
- Pruebas de integración (flujo completo con MongoDB real en Testcontainers): `LibroIntegrationTest`
  (requieren Docker; se omiten si no hay Docker disponible)
- Reporte HTML: `target/site/jacoco/index.html`
- El build falla si la cobertura de líneas o ramas es menor al **80 %**.

## Análisis estático (SonarQube)

```bash
docker compose up -d sonarqube      # http://localhost:9000  (admin / admin, cambiar contraseña)
```

1. Crear un proyecto local en SonarQube con la clave `micro_restfull` y generar un token.
2. Ejecutar:

```bash
./mvnw clean verify sonar:sonar -Dsonar.host.url=http://localhost:9000 -Dsonar.token=TU_TOKEN
```

## Estructura

```
src/main/java/microservices_crud/micro_restfull
├── controller   # LibroController
├── service      # LibroService
├── repository   # LibroRepository (MongoRepository)
├── model        # Libro (@Document)
├── dto          # LibroRequest, LibroResponse, ErrorResponse
└── exception    # RecursoNoEncontradoException, DatoDuplicadoException, GlobalExceptionHandler
```
