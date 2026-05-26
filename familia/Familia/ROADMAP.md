# ROADMAP — Refactorización del backend Familia

> **Propósito.** Este archivo es el tablero persistente de la refactorización.
> Las casillas `[ ]` se marcan a `[x]` a medida que se completan las tareas.
> Mantiene contexto entre sesiones para que cualquier agente (o el usuario)
> pueda retomar el trabajo sin re-explorar.
>
> **Última actualización:** 2026-05-25 · **Estado global:** F3 cerrada
> al 100%. Siguiente: F4 (servicios + excepciones + CORS).

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
| D2 | Interfaces de servicio: **reemplazarlas por contratos ISP granulares** (`Listable`, `Readable`, `Creatable`, `Updatable`, `Deletable`). Cada servicio implementa solo los que necesita. `CiudadService` (catálogo solo lectura) implementa solo `Listable`; `PersonaService` implementa los 5. Revisado 2026-05-25 sustituyendo la decisión YAGNI original. | 2026-05-25 |
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

### F1 — Docker DB-only por defecto + perfiles · 0.5 día · [x] Completada (2026-05-23, commit 0f64c0b)

- [x] **F1.1** En `docker-compose.yml`: agregar `profiles: ["full"]` al
      servicio `app`. El servicio `db` se queda sin perfil → arranca por defecto.
- [x] **F1.2** Crear `src/main/resources/application-dev.properties` con
      `spring.datasource.url=jdbc:postgresql://${DB_HOST:localhost}:${DB_PORT_OUT}/${DB_NAME}`.
- [x] **F1.3** Crear `src/main/resources/application-prod.properties` con
      `spring.datasource.url=jdbc:postgresql://db:${DB_PORT_IN}/${DB_NAME}`.
- [x] **F1.4** Ajustar `application.properties`: dejar defaults comunes
      (server.port, context-path, driver, pool) y `spring.profiles.active = dev`
      **literal** (sin placeholder, para que cambiar de perfil sea editar una
      sola línea). En docker-compose `app` se pasa `SPRING_PROFILES_ACTIVE=prod`
      como env var, que tiene mayor precedencia y sobrescribe el archivo.
- [x] **F1.5** `Dockerfile`: cambiar `EXPOSE ${APP_PORT_IN}` por `EXPOSE 8080`
      literal.
- [x] **F1.6** Documentar en `README.md` los dos flujos:
      - `docker compose up` → solo DB (debug local desde IDE).
      - `docker compose --profile full up` → DB + backend.

**Salida obtenida:** `docker compose up` ahora arranca solo PostgreSQL en
`localhost:5439`. El backend se levanta desde IntelliJ con perfil `dev`
(default). El flujo containerizado se invoca con `docker compose --profile
full up`.

**Smoke test pendiente antes del commit (tarea del usuario):**
1. `docker compose up -d` → debe aparecer solo `base_de_datos` en `docker compose ps`.
2. Arrancar `FamiliaApplication` en el IDE → log debe decir
   `The following 1 profile is active: "dev"` y conectarse a
   `localhost:${DB_PORT_OUT}`.
3. `GET http://localhost:${APP_PORT_IN}/familia/ciudades` → 200/204.
4. `docker compose down && docker compose --profile full up --build -d`
   → ambos contenedores arriba; `GET .../ciudades` por
   `localhost:${APP_PORT_OUT}` → 200/204.

---

### F2 — Naming y paquetes (typos + camelCase + verbos + ISP) · 1.0 día · [x] Completada (2026-05-25)

> Usar **IntelliJ Refactor → Rename** (Shift+F6) y **Move** (F6) para que los
> imports se actualicen automáticamente. Commit por sub-bloque.

- [x] **F2.1** Renombrar paquete `com.weiz.Familia.infraestructure` →
      `com.weiz.familia.infrastructure` (también `com.weiz.Familia` →
      `com.weiz.familia` en todo el árbol). Commit `67a6951`.
- [x] **F2.2** Renombrar `util.Enums` → `shared.enums`, `util.Exceptions` →
      `shared.exceptions`. (Borrar `util` si queda vacío.)
- [x] **F2.3** Renombrar `api.controllers.errorHandler` →
      `api.controllers.errorhandling`. `BadRequestController` →
      `GlobalExceptionHandler`. Commit `5588ad5`.
- [x] **F2.4** Renombrar campos en `PersonaEntity`, `PersonaRequest`,
      `PersonaResponse`: `numero_documento` → `numeroDocumento`,
      `fecha_nacimiento` → `fechaNacimiento`, `correo_electronico` →
      `correoElectronico`. Mantener `@Column(name="numero_documento")` etc.
      para preservar BD. Commit `0febcda` (combinado con F2.5).
- [x] **F2.5** Decisión: mantener contrato JSON externo en snake_case.
      Agregado `spring.jackson.property-naming-strategy=SNAKE_CASE` en
      `application.properties`. Java conserva camelCase y Jackson hace la
      traducción automática. Commit `0febcda`.
- [x] **F2.6** Renombrar métodos handler del controller: `get` →
      `consultarPorDocumento`, `put` → `actualizar`, `delete` → `eliminar`,
      `create` → `crear`, `readAll` → `consultarTodas`. En el servicio ya
      se renombraron como parte de F2.7. Commit `e495d4c`.
- [x] **F2.7** Reemplazar interfaces monolíticas por contratos ISP
      granulares en `infrastructure/services/contracts/`: `Listable`,
      `Readable`, `Creatable`, `Updatable`, `Deletable`. Borrado
      `CrudService`, `IPersonaService`, `ICiudadService`. `PersonaService`
      implementa los 5; `CiudadService` solo `Listable<CiudadResponse>`.
      Métodos del servicio en infinitivo desde el inicio. Adelanto parcial
      de F4.2: quitado `throws InvocationTargetException, IllegalAccessException`
      del `actualizar` y del controller `put`. Commit `aaf5e4e`.

**Salida esperada:** ningún `snake_case` en Java, contratos ISP granulares,
paquetes correctos, métodos con verbos en infinitivo.

---

### F3 — MapStruct sustituye BeanUtils · 1.0 día · [x] Completada (2026-05-25)

> Decisión revisada: los mappers viven en `infrastructure/mappers/`
> (consistente con la organización por tipo técnico del proyecto), no en
> `application/mappers/` como planteaba el borrador inicial.

- [x] **F3.1** Crear `infrastructure/mappers/PersonaMapper`:
      `@Mapper(componentModel="spring", uses=CiudadMapper.class)` con
      `PersonaEntity toEntity(PersonaRequest)` (ignora `ciudad`, `esViable`),
      `PersonaResponse toResponse(PersonaEntity)`,
      `void actualizar(@MappingTarget PersonaEntity, PersonaRequest)` con
      `nullValuePropertyMappingStrategy=IGNORE` (ignora `numeroDocumento`,
      `ciudad`, `esViable`). Commit `594212a`.
- [x] **F3.2** Crear `CiudadMapper` con solo `toResponse(CiudadEntity)`
      (el catálogo es solo lectura, ISP-coherente). Commit `594212a`.
- [x] **F3.3** Eliminar de `PersonaEntity` los métodos estáticos
      `entityToResponse`, `requestToEntity` y el método `merge` con
      reflexión + `printStackTrace`. Commit `594212a`.
- [x] **F3.4** Eliminar de `CiudadEntity` el método `entityToResponse`.
      Commit `594212a`.
- [x] **F3.5** Inyectar `PersonaMapper` y `CiudadMapper` en `PersonaService`
      y `CiudadService` (constructor via Lombok); reemplazar el uso de los
      métodos eliminados. Commit `594212a`.
- [x] **F3.6** Verificado: `mvn clean compile` exitoso, MapStruct genera
      `PersonaMapperImpl` y `CiudadMapperImpl` en
      `target/generated-sources/annotations`.

**Salida obtenida:** entidades sin lógica de mapeo, servicios delegan a
mappers, cero `BeanUtils.copyProperties`, cero reflexión. Cambio de
comportamiento intencional en PUT /personas/{id}: `actualizar()` ahora
solo ignora nulls del request; el comportamiento viejo (ignorar también
strings blank) impedía al cliente vaciar campos string. La semántica
estándar MapStruct elimina ese bug latente.

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
| F1. Docker DB-only | 6/6 | [x] |
| F2. Naming y paquetes | 7/7 | [x] |
| F3. MapStruct | 6/6 | [x] |
| F4. Servicios y excepciones | 0/6 | [ ] |
| F5. Dominio | 0/3 | [ ] |
| F7. Pruebas | 0/4 | [ ] |
| F8. Cierre | 0/4 | [ ] |
| **Total** | **23/40** | **57.5%** |

**Próximo paso sugerido:** F4 — servicios, excepciones y CORS:
1. Corregir el bug BLOCKER de `EnumValidationException` (no extiende Exception ni hace `super(msg)`).
2. Renombrar `IdNotFoundException` → `RegistroNoEncontradoException`.
3. Cambiar `PersonaService.eliminar` para usar `existsById` en vez de `findById + deleteById`.
4. Reescribir `GlobalExceptionHandler` con `instanceof` pattern matching de Java 21 y sin raw types.
5. Centralizar `@CrossOrigin` en un `WebMvcConfigurer` global.

---

## 5. Bitácora (entradas en orden inverso, más reciente arriba)

> Registrar aquí decisiones nuevas, bloqueos, desvíos del plan. Una línea por
> evento, formato: `YYYY-MM-DD — descripción corta`.

- `2026-05-25 — F3 commiteada como 594212a (refactor(mapping): introduce MapStruct y elimina BeanUtils y reflexion). Mappers ubicados en infrastructure/mappers/ (no en application/mappers/ como planteaba el borrador original). CiudadMapper solo expone toResponse (catalogo solo lectura, ISP-coherente). PersonaMapper expone toEntity/toResponse/actualizar(@MappingTarget) con nullValuePropertyMappingStrategy=IGNORE. PersonaEntity queda sin metodos estaticos ni merge() con reflexion; CiudadEntity sin entityToResponse. Cambio de comportamiento en PUT: actualizar ahora solo ignora nulls, no strings blank.`
- `2026-05-25 — F2.6 commiteada como e495d4c (refactor(controllers): renombra handlers HTTP a verbos en infinitivo). Cierra la fase F2 al 100%. PersonaController y CiudadController quedan con readAll/get/create/put/delete renombrados a consultarTodas/consultarPorDocumento/crear/actualizar/eliminar. Las rutas HTTP y placeholders no cambian.`
- `2026-05-25 — F2.7 commiteada como aaf5e4e (refactor(services): reemplaza interfaces monoliticas por ISP granular). Cambio D2: en vez de eliminar interfaces (YAGNI) se sustituyen por 5 contratos granulares en infrastructure/services/contracts/ (Listable, Readable, Creatable, Updatable, Deletable). CiudadService solo implementa Listable (catalogo solo GET); PersonaService los 5. Metodos del servicio en infinitivo desde el inicio. Adelanto parcial de F4.2: quitado throws InvocationTargetException/IllegalAccessException del actualizar y del put.`
- `2026-05-25 — F2.4 + F2.5 commiteadas como 0febcda (refactor(persona): migra campos a camelCase y mantiene contrato JSON con Jackson SNAKE_CASE). Campos numero_documento/fecha_nacimiento/correo_electronico renombrados; @Column(name=...) preserva BD; spring.jackson.property-naming-strategy=SNAKE_CASE preserva contrato externo.`
- `2026-05-25 — F2.3 commiteada como 5588ad5 (refactor(error-handling): renombra paquete errorHandler a errorhandling y clase a GlobalExceptionHandler). Rename quirurgico, similarity 98%.`
- `2026-05-25 — Cambio de criterio en pre-commit: NO lanzar quality-code-reviewer antes de commits/push/PR. Los skills cargados al inicio (clean-code-expert, solid-expert, sonarqube-expert) garantizan calidad al escribir. Guardado en memoria (feedback-no-audit-precommit).`
- `2026-05-23 — Cambio de criterio en perfiles: 'spring.profiles.active = dev' literal en application.properties (no placeholder). Cambiar de perfil = editar esa línea. La env var de docker-compose sigue overrideando para el contenedor. README simplificado. Patrón guardado en memoria (feedback-perfiles-spring-simples).`
- `2026-05-25 — chore(gitignore) commiteado como 5a456a1: excluye carpeta .run/ de IntelliJ y destrackea FamiliaApplication.run.xml.`
- `2026-05-25 — fix(packages) commiteado como 757fbe3: el commit 67a6951 solo movio archivos sin actualizar package/import; este fix completa esa parte. Auditoría: APROBADA.`
- `2026-05-23 — F2.1+F2.2 commiteadas como 67a6951 (refactor(packages): corrige typos y normaliza paquetes a minuscula). Auditoría pre-commit: VERDE. Renames hechos desde IntelliJ. Build verde con 26 fuentes. Previo: el commit acab7f2 mezclaba ROADMAP + renames; se hizo reset --soft y re-split en dos commits limpios. NOTA: 67a6951 quedo INCOMPLETO — los renames de paquetes capturaron el rename pero no los package declarations; corregido en commit 757fbe3.`
- `2026-05-23 — F1 commiteada como 0f64c0b (chore(docker): separa perfiles dev y prod y deja db sola por defecto). Auditoría pre-commit: 0 BLOCKER/CRITICAL/MAJOR, 3 MINOR (2 aplicadas como Boy Scout: newline final Dockerfile + limpieza de logging comentado; la 3ra del formato '=' se deja con espacios para coincidir con el patrón InspektorRestApi).`
- `2026-05-23 — F1 smoke test exitoso (flujos A y B). Implementación lista para commit.`
- `2026-05-23 — F1 implementada: perfil 'full' en docker-compose, application-dev/prod.properties, EXPOSE literal en Dockerfile, README con los dos flujos.`
- `2026-05-23 — F0 commiteada como 2c46ca7 (chore(setup): prepara fase 0 con mapstruct y excluye .env). Auditoría pre-commit por quality-code-reviewer: APPROVED (solo INFO).`
- `2026-05-23 — F0 completada. Rama refactor/familia-clean-arquitectura creada. .env fuera del index (CRITICAL #19 resuelto). pom.xml con MapStruct + Lombok + binding configurados. mvn clean compile = BUILD SUCCESS (línea base).`
- `2026-05-23 — Plan aprobado. F6 descartada. Interfaces de servicio se eliminan (YAGNI). Roadmap creado.`
- `2026-05-23 — AGENTS.md creado con matriz de activación de skills y convenciones del proyecto.`
- `2026-05-23 — Exploración inicial completa. 19 hallazgos identificados, 1 BLOCKER (EnumValidationException) y 2 CRITICAL en código (BeanUtils, reflexión).`
