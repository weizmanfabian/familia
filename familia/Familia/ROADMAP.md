# ROADMAP — Refactorización del backend Familia

> **Propósito.** Este archivo es el tablero persistente de la refactorización.
> Las casillas `[ ]` se marcan a `[x]` a medida que se completan las tareas.
> Mantiene contexto entre sesiones para que cualquier agente (o el usuario)
> pueda retomar el trabajo sin re-explorar.
>
> **Última actualización:** 2026-05-23 · **Estado global:** Plan aprobado, sin
> ejecutar.

---

## 0. Contexto

- **Stack:** Java 21 · Spring Boot 3.3.10 (Jakarta) · PostgreSQL · Maven ·
  Lombok · MapStruct 1.5.5 (declarado, no usado) · Validation · Actuator.
- **Working directory:** `C:\Users\weizm\Documents\Pprojects\familia\familia\Familia`
- **Rama de trabajo (a crear):** `refactor/familia-clean-arquitectura`
- **Documentos relacionados:**
  - [`AGENTS.md`](./AGENTS.md) — orquestación de agentes y skills, convenciones.
  - `~/.claude/agents/quality-code-reviewer.md` — agente bloqueante pre-commit.

---

## 1. Decisiones ya tomadas (no volver a preguntar)

| # | Decisión | Acordado |
|---|---|---|
| D1 | Alcance arquitectura: **solo corregir typos**, sin Screaming Architecture en esta iteración. | 2026-05-23 |
| D2 | Interfaces de servicio: **eliminarlas (YAGNI)**. Solo hay una impl → `@Service class PersonaService` directo. | 2026-05-23 |
| D3 | `docker compose up` levanta **solo la DB** por defecto. El backend corre desde el IDE para debug. Backend en contenedor se activa con `docker compose --profile full up`. | 2026-05-23 |
| D4 | `application.properties` se separa en perfiles `dev` (DB en `localhost:${DB_PORT_OUT}`) y `prod` (DB en `db:${DB_PORT_IN}`). Default = `dev`. | 2026-05-23 |
| D5 | Mapeo: **MapStruct sustituye `BeanUtils.copyProperties`** en todos los puntos. | 2026-05-23 |
| D6 | Naming Java: **camelCase obligatorio**. `numero_documento` → `numeroDocumento` con `@Column(name="numero_documento")` para preservar BD. | 2026-05-23 |
| D7 | Paquetes corregidos: `infraestructure` → `infrastructure`, `imp` → `impl`, `util.Enums` → `shared.enums`, `util.Exceptions` → `shared.exceptions`, `errorHandler` → `errorhandling`. | 2026-05-23 |
| D8 | Commits y PRs vía `git-expert` (español colombiano, Conventional Commits). Pre-commit obligatorio con `quality-code-reviewer`. | 2026-05-23 |

---

## 2. Hallazgos de la línea base (snapshot 2026-05-23)

| # | Hallazgo | Severidad | Pilar | Fase que lo resuelve |
|---|---|---|---|---|
| 1 | `BeanUtils.copyProperties` para mapear en entidades | CRITICAL | SOLID · Clean Code | F3 |
| 2 | `PersonaEntity.merge()` con reflexión + `printStackTrace` | CRITICAL | SonarQube `java:S1148,S106,S3011` | F3 |
| 3 | `@PostPersist/@PostUpdate` para `esViable` (se ejecuta tarde) | CRITICAL | Correctness | F5 |
| 4 | Campos Java en `snake_case` | MAJOR | Clean Code · `java:S00116` | F2 |
| 5 | Paquetes mal escritos (`infraestructure`, `imp`, `Enums`, `Exceptions`) | MAJOR | Clean Code | F2 |
| 6 | Notación húngara `ICiudadService`, `IPersonaService` | MAJOR | Clean Code | F2 |
| 7 | `update` filtra `InvocationTargetException, IllegalAccessException` | MAJOR | SOLID · Clean Code | F3 + F4 |
| 8 | Métodos sin verbo (`get`, `put` en controller) | MAJOR | Clean Code (bloqueante) | F2 |
| 9 | `EnumValidationException` no extiende `Exception` ni hace `super(msg)` | **BLOCKER** | Correctness | F4 |
| 10 | `BadRequestController` con `Class<Enum>` raw type | MAJOR | SonarQube `java:S3740` | F4 |
| 11 | `delete()` hace `findById` + `deleteById` (doble query) | MINOR | Performance | F4 |
| 12 | `@CrossOrigin(*)` repetido en cada controller | MINOR | DRY · Security | F4 |
| 13 | `TestController` expuesto en `/test` | MINOR | Seguridad | F5 |
| 14 | `application.properties` sin perfiles | MAJOR | Configuración | F1 |
| 15 | `docker compose up` levanta app+db cuando dev solo necesita db | MAJOR | DX | F1 |
| 16 | MapStruct en pom pero sin annotation processor + sin mappers | MAJOR | Build | F0 + F3 |
| 17 | `Dockerfile` `EXPOSE ${APP_PORT_IN}` no interpola | MINOR | Docker | F1 |
| 18 | Cobertura de pruebas = solo `contextLoads()` | MAJOR | Testing | F7 |
| 19 | `.env` con credenciales versionado en repo | **CRITICAL** | Seguridad | F0 |

---

## 3. Fases (camino crítico)

> Marcar las casillas a medida que se completen. Cada fase debe pasar por
> `quality-code-reviewer` antes de hacer commit, y por `git-expert` para el
> mensaje y la rama.

### F0 — Preparación y línea base · 0.5 día · [x] Completada (2026-05-23)

- [x] **F0.1** Crear rama `refactor/familia-clean-arquitectura` desde `master`.
- [x] **F0.2** Sacar `.env` del repo: borrar tracking, agregar a `.gitignore`,
      crear `.env.example` con las claves (sin valores). *Rotar password de
      PostgreSQL si el repo es público.*
- [x] **F0.3** Configurar `maven-compiler-plugin` con `annotationProcessorPaths`
      para **Lombok + MapStruct 1.5.5 + lombok-mapstruct-binding 0.2.0** (el
      orden importa: Lombok va primero).
- [x] **F0.4** `mvn clean compile` verde como línea base (registrado en
      `docs/baseline-build.log`, ignorado por git).

**Salida obtenida:** rama `refactor/familia-clean-arquitectura` activa, `.env`
fuera del index (queda en disco para uso local), `.gitignore` cubre
`.env*` excepto `.env.example`, `pom.xml` con los tres annotation processors
en orden correcto, build verde.

---

### F1 — Docker DB-only por defecto + perfiles · 0.5 día · [ ] No iniciada

- [ ] **F1.1** En `docker-compose.yml`: agregar `profiles: ["full"]` al
      servicio `app`. El servicio `db` se queda sin perfil → arranca por defecto.
- [ ] **F1.2** Crear `src/main/resources/application-dev.properties` con
      `spring.datasource.url=jdbc:postgresql://${DB_HOST:localhost}:${DB_PORT_OUT}/${DB_NAME}`.
- [ ] **F1.3** Crear `src/main/resources/application-prod.properties` con
      `spring.datasource.url=jdbc:postgresql://db:${DB_PORT_IN}/${DB_NAME}`.
- [ ] **F1.4** Ajustar `application.properties`: dejar solo defaults comunes y
      `spring.profiles.active=${SPRING_PROFILES_ACTIVE:dev}`. En docker-compose
      `app` pasar `SPRING_PROFILES_ACTIVE=prod`.
- [ ] **F1.5** `Dockerfile`: cambiar `EXPOSE ${APP_PORT_IN}` por `EXPOSE 8080`
      literal.
- [ ] **F1.6** Documentar en `README.md` los dos flujos:
      - `docker compose up` → solo DB (debug local desde IDE).
      - `docker compose --profile full up` → DB + backend.

**Salida esperada:** `docker compose up` levanta solo PostgreSQL en
`localhost:5439`, el backend se debuggea desde IntelliJ con perfil `dev`.

---

### F2 — Naming y paquetes (typos + camelCase + verbos + sin interfaces) · 1.0 día · [ ] No iniciada

> Usar **IntelliJ Refactor → Rename** (Shift+F6) y **Move** (F6) para que los
> imports se actualicen automáticamente. Commit por sub-bloque.

- [ ] **F2.1** Renombrar paquete `com.weiz.Familia.infraestructure` →
      `com.weiz.Familia.infrastructure`.
- [ ] **F2.2** Renombrar `util.Enums` → `shared.enums`, `util.Exceptions` →
      `shared.exceptions`. (Borrar `util` si queda vacío.)
- [ ] **F2.3** Renombrar `api.controllers.errorHandler` →
      `api.controllers.errorhandling`. `BadRequestController` →
      `GlobalExceptionHandler`.
- [ ] **F2.4** Renombrar campos en `PersonaEntity`, `PersonaRequest`,
      `PersonaResponse`: `numero_documento` → `numeroDocumento`,
      `fecha_nacimiento` → `fechaNacimiento`, `correo_electronico` →
      `correoElectronico`. Mantener `@Column(name="numero_documento")` etc.
      para preservar BD.
- [ ] **F2.5** Decidir estrategia JSON: agregar
      `spring.jackson.property-naming-strategy=SNAKE_CASE` en
      `application.properties` si la API debe seguir exponiendo `snake_case`,
      o documentar el cambio si pasa a `camelCase` en el contrato externo.
- [ ] **F2.6** Renombrar métodos del controller: `get` →
      `consultarPorDocumento`, `put` → `actualizar`, `delete` → `eliminar`,
      `create` → `crear`, `readAll` → `consultarTodas`. Idem en el servicio.
- [ ] **F2.7** Eliminar interfaces `ICiudadService`, `IPersonaService`,
      `CrudService` (decisión D2 — YAGNI). Borrar paquete `services/imp`.
      `@Service class PersonaService` se inyecta directamente.

**Salida esperada:** ningún `snake_case` en Java, ningún `I*Service`,
paquetes correctos, métodos con verbos en infinitivo.

---

### F3 — MapStruct sustituye BeanUtils · 1.0 día · [ ] No iniciada

- [ ] **F3.1** Crear `application/mappers/PersonaMapper`:
      `@Mapper(componentModel="spring", uses=CiudadMapper.class)` con
      `PersonaEntity toEntity(PersonaRequest)`,
      `PersonaResponse toResponse(PersonaEntity)`,
      `void actualizar(@MappingTarget PersonaEntity, PersonaRequest)`.
- [ ] **F3.2** Crear `CiudadMapper` análogo.
- [ ] **F3.3** Eliminar de `PersonaEntity` los métodos estáticos
      `entityToResponse`, `requestToEntity` y el método `merge` con reflexión.
- [ ] **F3.4** Eliminar de `CiudadEntity` el método `entityToResponse`.
- [ ] **F3.5** Inyectar `PersonaMapper` y `CiudadMapper` en `PersonaService`
      y `CiudadService`; reemplazar el uso de los métodos eliminados.
- [ ] **F3.6** Verificar que MapStruct genera correctamente (mirar
      `target/generated-sources/annotations`).

**Salida esperada:** entidades sin lógica de mapeo, servicios delegan a
mappers, cero `BeanUtils.copyProperties`, cero reflexión.

---

### F4 — Servicios + excepciones + CORS · 1.0 día · [ ] No iniciada

- [ ] **F4.1** **BUG BLOCKER #9**: corregir `EnumValidationException` para
      extender `RuntimeException` y llamar `super(msg)`.
- [ ] **F4.2** Quitar `throws InvocationTargetException, IllegalAccessException`
      del controller, del servicio y de la interfaz CRUD (la interfaz ya no
      existe tras F2.7).
- [ ] **F4.3** Renombrar `IdNotFoundException` →
      `RegistroNoEncontradoException`.
- [ ] **F4.4** `PersonaService.eliminar`: reemplazar `findById + deleteById`
      por `existsById` o `try { deleteById } catch (EmptyResultDataAccessException)`.
- [ ] **F4.5** Reescribir `GlobalExceptionHandler` con tipos genéricos
      seguros (sin cast raw `Class<Enum>`) y `instanceof` pattern matching
      (Java 21).
- [ ] **F4.6** Mover `@CrossOrigin` a un `WebMvcConfigurer` global
      (`shared.config.WebConfig`). Quitar de cada controller.

**Salida esperada:** sin checked exceptions filtradas, sin raw types, CORS
centralizado, bug BLOCKER cerrado.

---

### F5 — Dominio y reglas de negocio · 0.5 día · [ ] No iniciada

- [ ] **F5.1** Mover el cálculo de `esViable` de `@PostPersist/@PostUpdate`
      a un método del dominio `calcularViabilidad()` invocado en
      `PersonaService.crear` y `PersonaService.actualizar` **antes** de
      `save()`.
- [ ] **F5.2** Extraer constantes mágicas: `EDAD_VIABLE_MIN = 18`,
      `EDAD_VIABLE_MAX = 65` (en `PersonaEntity` o en una clase
      `ReglasViabilidad`).
- [ ] **F5.3** Decidir destino de `TestController`: borrarlo, o moverlo a
      `/actuator/health/familia` con `@Endpoint`.

**Salida esperada:** la viabilidad se calcula antes de persistir, sin
listeners JPA. Sin endpoints de prueba expuestos.

---

### F7 — Pruebas · 1.5 días · [ ] No iniciada

> Numerado como F7 porque la F6 (Screaming Architecture) fue descartada (D1).

- [ ] **F7.1** Unit tests de `PersonaService` con Mockito: happy paths para
      `crear`, `actualizar`, `consultarPorDocumento`, `eliminar`,
      `consultarTodas`. Error paths: persona no existe, ciudad no existe,
      documento duplicado.
- [ ] **F7.2** Unit tests del `PersonaMapper`: verificar el mapeo
      `Request → Entity`, `Entity → Response`, y `actualizar` con
      `@MappingTarget`.
- [ ] **F7.3** Test parametrizado de `PersonaEntity.calcularViabilidad`:
      edades 17 → false, 18 → true, 65 → true, 66 → false.
- [ ] **F7.4** Integration test `@SpringBootTest + @AutoConfigureMockMvc`
      para `POST /personas` con `@Testcontainers` (PostgreSQL real). Agregar
      dep `org.testcontainers:postgresql` en `pom.xml` con scope `test`.

**Salida esperada:** cobertura significativa de los flujos principales,
sin tests integrados a una BD compartida.

---

### F8 — Cierre de calidad · 0.5 día · [ ] No iniciada

- [ ] **F8.1** `mvn clean verify` verde.
- [ ] **F8.2** Correr `quality-code-reviewer` sobre el diff completo de la
      rama. Resolver findings BLOCKER y CRITICAL antes de continuar.
- [ ] **F8.3** Actualizar `README.md` con los nuevos flujos (docker dev-only-db,
      perfiles, comandos para correr desde IDE).
- [ ] **F8.4** Abrir PR `refactor: aplica clean code, solid y arquitectura
      limpia en backend Familia` vía `git-expert` (mensaje en español, body
      explicando el porqué de cada fase, footer con `Refs:` si hay issue).

**Salida esperada:** PR listo para revisión humana, build verde, roadmap
con todas las casillas marcadas.

---

## 4. Estado global

| Fase | Casillas | Estado |
|---|---|---|
| F0. Preparación | 4/4 | [x] |
| F1. Docker DB-only | 0/6 | [ ] |
| F2. Naming y paquetes | 0/7 | [ ] |
| F3. MapStruct | 0/6 | [ ] |
| F4. Servicios y excepciones | 0/6 | [ ] |
| F5. Dominio | 0/3 | [ ] |
| F7. Pruebas | 0/4 | [ ] |
| F8. Cierre | 0/4 | [ ] |
| **Total** | **4/40** | **10%** |

**Próximo paso sugerido:** decidir si commitear F0 ahora (vía `quality-code-reviewer` → `git-expert`) o seguir con F1.1 (perfil `full` en `docker-compose.yml`).

---

## 5. Bitácora (entradas en orden inverso, más reciente arriba)

> Registrar aquí decisiones nuevas, bloqueos, desvíos del plan. Una línea por
> evento, formato: `YYYY-MM-DD — descripción corta`.

- `2026-05-23 — F0 completada. Rama refactor/familia-clean-arquitectura creada. .env fuera del index (CRITICAL #19 resuelto). pom.xml con MapStruct + Lombok + binding configurados. mvn clean compile = BUILD SUCCESS (línea base).`
- `2026-05-23 — Plan aprobado. F6 descartada. Interfaces de servicio se eliminan (YAGNI). Roadmap creado.`
- `2026-05-23 — AGENTS.md creado con matriz de activación de skills y convenciones del proyecto.`
- `2026-05-23 — Exploración inicial completa. 19 hallazgos identificados, 1 BLOCKER (EnumValidationException) y 2 CRITICAL en código (BeanUtils, reflexión).`
