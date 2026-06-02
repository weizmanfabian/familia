# AGENTS.md — Orquestación de Agentes y Skills (Proyecto Familia)

> **Propósito.** Este archivo es el contrato de orquestación que Claude Code debe
> seguir cuando trabaja sobre este repositorio. Define **qué agente y qué skills
> deben activarse en cada fase** del ciclo de vida del cambio (planeación →
> diseño → codificación → revisión → commit). El objetivo es que la calidad no
> dependa de que el usuario "se acuerde" de invocar un experto: el agente
> correcto entra solo en el momento correcto.
>
> **Stack detectado.** Java 21 · Spring Boot 3.3.10 (Jakarta) · PostgreSQL ·
> Maven · Lombok · MapStruct 1.5.5 · Spring Validation · Actuator · Docker
> Compose. Toda recomendación debe respetar estas versiones.

---

## 1. Principio rector

> Antes de ejecutar, planifica. Antes de planificar, entiende. Antes de
> entender, pregunta. Y antes de commitear, audita.

Ningún cambio entra al repositorio sin haber pasado por las cuatro compuertas:

1. **Planeación** — EDT antes del primer archivo tocado.
2. **Diseño** — arquitectura, SOLID, naming y compatibilidad de versión.
3. **Codificación** — Clean Code + SOLID + SonarQube como baseline.
4. **Pre-commit** — auditoría obligatoria del diff antes del git commit.

---

## 2. Matriz de activación: ¿Qué agente/skill para qué momento?

| Momento del trabajo | Disparador típico del usuario | Skill / Agente que DEBE activarse | Modo |
|---|---|---|---|
| Captura del objetivo, alcance ambiguo, proyecto multi-tarea | "quiero implementar X", "necesito planear Y", "voy a refactorizar Z" | `edt-planning-expert` | obligatorio |
| Diseño previo a tocar código (HU nueva, endpoint, feature) | "voy a agregar el endpoint /…", "diseñemos el módulo …" | `quality-code-reviewer` (modo PLANEACIÓN) + `solid-expert` + `clean-code-expert` | obligatorio |
| Escribir o modificar código (cualquier lenguaje) | edición de `.java`, `.sql`, `.yml`, etc. | `clean-code-expert` + `solid-expert` + `sonarqube-expert` | siempre activos como baseline |
| Revisión de un bloque recién escrito | "termine X", "revisa lo que acabo de hacer" | `quality-code-reviewer` (modo REVISIÓN) | obligatorio |
| Pre-commit, pre-push, pre-PR | "haz commit", "sube esto", "abre PR" | `quality-code-reviewer` (modo REVISIÓN sobre diff) → si pasa → `git-expert` | obligatorio y bloqueante |
| Mensajes de commit, ramas, PRs, releases, tags | toda interacción con git | `git-expert` | obligatorio |
| Redacción en español (README, docs, comentarios funcionales, mensajes para el equipo) | texto en español que no sea código | `spanish-writing-expert` | recomendado |

> **Regla de oro.** `clean-code-expert`, `solid-expert` y `sonarqube-expert` no
> son opcionales: son el **estándar mínimo** de cualquier código producido en
> este repositorio. No se activan "cuando aplique" — se aplican siempre.

---

## 3. Flujos canónicos

### 3.1 Flujo "feature nueva o refactor de un módulo"

```
Usuario expresa intención
        │
        ▼
edt-planning-expert  ──►  EDT con fases, paquetes, dependencias, riesgos
        │
        ▼
quality-code-reviewer (PLANEACIÓN)
   + solid-expert + clean-code-expert
        │
        ▼  (plan aprobado por el usuario)
Codificación
   ⇄ clean-code-expert + solid-expert + sonarqube-expert (siempre)
        │
        ▼
quality-code-reviewer (REVISIÓN del diff)
        │
        ├── BLOCKER / CRITICAL  ──►  detener y resolver
        └── OK
                │
                ▼
        git-expert  ──►  commit + branch + PR en español
```

### 3.2 Flujo "fix puntual / bug"

```
Reproducir y entender el bug
        │
        ▼
clean-code-expert (no se "parcha": se corrige con estándar limpio)
   + sonarqube-expert (que el fix no introduzca deuda)
        │
        ▼
quality-code-reviewer (REVISIÓN sobre el diff)
        │
        ▼
git-expert  ──►  commit `fix(<scope>): …`
```

### 3.3 Flujo "commit / push / PR"

```
Usuario: "haz commit" | "sube" | "abre PR"
        │
        ▼
La sesión principal corre `git status` + `git diff (--cached)`
        │
        ▼
quality-code-reviewer (REVISIÓN obligatoria sobre archivos staged)
        │
        ├── findings BLOCKER/CRITICAL ──► reportar y NO commitear
        └── verde
                │
                ▼
        git-expert  ──►  mensaje en español, Conventional Commits
```

> **Nota de seguridad.** Nunca se omiten hooks (`--no-verify`) ni firma
> (`--no-gpg-sign`). Si un hook falla, se diagnostica la causa raíz.

### 3.4 Flujo "documentación / README / texto en español"

```
spanish-writing-expert  ──►  reorganiza, corrige y mejora el texto
        │
        ▼
git-expert (si va al repo)  ──►  `docs(<scope>): …`
```

---

## 4. Convenciones específicas de este proyecto

Estas convenciones **complementan** las reglas de los skills. Cuando un skill
sugiera algo que choque con esto, ganan estas reglas (porque son del proyecto).

### 4.1 Naming

- **Métodos**: verbo en infinitivo + sustantivo. `crearPersona`, `consultarCiudades`,
  `validarViabilidad`. Nunca solo sustantivo/adjetivo (`viable()`, `peor()`).
- **Variables**: descriptivas en `camelCase`. Booleanos con prefijo
  `es / tiene / debe / puede / hay`.
- **Java**: `camelCase` en variables y campos. **Prohibido `snake_case` en Java**
  (`numero_documento` → `numeroDocumento`). El mapeo a columnas BD se hace con
  `@Column(name = "numero_documento")`.
- **BD**: `snake_case`, tablas en plural (`personas`, `ciudades`), columnas en
  singular.
- **Paquetes**: en inglés y bien escritos. `infrastructure` (no `infraestructure`),
  `impl` (no `imp`), `enums` (no `Enums`), `exceptions` (no `Exceptions`).
- **Interfaces**: sin prefijo `I` húngaro. La interfaz es `PersonaService` y la
  implementación es `PersonaServiceImpl` (o se prefiere un solo `@Service` sin
  interfaz cuando no hay segunda implementación — YAGNI).

### 4.2 Arquitectura (screaming architecture)

La estructura de paquetes debe **gritar el dominio** (familia, persona, ciudad),
no el framework. Dentro de cada bounded context se separa por capa:

```
com.weiz.familia
 ├── persona
 │    ├── api          (controllers, requests, responses)
 │    ├── application  (services, mappers)
 │    ├── domain       (entities, value objects, repositorio-interfaz)
 │    └── infrastructure  (JPA repository impl si aplica)
 ├── ciudad
 │    └── …
 └── shared
      ├── exceptions
      ├── errorhandling
      └── config
```

Dependencias hacia adentro: `api → application → domain`. `domain` no conoce a
Spring, JPA, ni a `api`.

### 4.3 Mapeo

- **Prohibido `BeanUtils.copyProperties`** en entidades. Es frágil, reflexivo y
  oculta errores de mapeo en runtime.
- **MapStruct** ya está en `pom.xml` 1.5.5 — debe ser el único mecanismo de
  mapeo entre `Request ↔ Entity ↔ Response`.
- Los mappers viven en `application/mappers` y son interfaces `@Mapper(componentModel = "spring")`.

### 4.4 Validación de reglas de negocio

- Las invariantes de negocio (ej. "edad entre 18 y 65 para `esViable`") **no**
  se calculan en `@PostPersist` / `@PostUpdate` (eso ocurre después de
  persistir). Se calculan en el servicio o como método del propio dominio
  invocado antes de `save()`.
- Validaciones de formato → Bean Validation (`@Valid`, `@NotBlank`, `@Pattern`)
  en los DTOs.
- Validaciones de existencia/conflicto → en el servicio, lanzando excepciones
  de dominio.

### 4.5 Excepciones

- Toda excepción de dominio extiende de `RuntimeException` y vive en
  `shared/exceptions`.
- Prohibido `throws InvocationTargetException, IllegalAccessException` filtrando
  hasta el controller — se envuelven en el servicio.
- Prohibido `e.printStackTrace()` y `System.out/err.println` (Sonar `java:S106`,
  `java:S1148`). Se usa SLF4J (`log.error(...)`).

### 4.6 Configuración por perfiles

- `application-dev.properties` → conecta a la DB del contenedor expuesta en
  `localhost:${DB_PORT_OUT}` (para debug con Java corriendo localmente en el IDE).
- `application-prod.properties` → conecta a `db:${DB_PORT_IN}` (red de Docker).
- `application.properties` → solo defaults comunes y `spring.profiles.active`
  por defecto = `dev`.
- `.env` **no se commitea**; sí se commitea `.env.example` con los nombres.

### 4.7 Docker

- `docker compose up` (sin perfil) levanta **solo la DB** — porque el flujo
  diario es debuggear el backend desde el IDE.
- `docker compose --profile full up` (o `docker compose up app db`) levanta DB
  + backend para escenarios de integración o producción local.
- El `Dockerfile` es multi-stage (ya lo es) y debe usar `EXPOSE 8080` literal
  (no `${APP_PORT_IN}`: `EXPOSE` no interpola variables de runtime).

### 4.8 Git

- **Idioma**: español colombiano. Solo los keywords de Conventional Commits
  quedan en inglés (`feat`, `fix`, `docs`, `style`, `refactor`, `perf`, `test`,
  `build`, `ci`, `chore`, `revert`).
- **Asunto**: imperativo, minúscula, sin punto final, ≤ 50 caracteres,
  ≤ 72 totales con `type(scope):`.
- **Scope sugeridos**: `persona`, `ciudad`, `docker`, `config`, `docs`, `api`,
  `domain`, `infra`.
- **Ramas**: `feat/<scope>-<descripcion-corta>`, `fix/<scope>-<descripcion>`,
  `refactor/<scope>-<descripcion>`.
- **Pre-commit**: pasa siempre por `quality-code-reviewer` (no es opcional).

---

## 5. Lecturas obligatorias por skill (pre-flight)

Cuando el agente correspondiente entra en escena, **siempre** debe leer
primero (no asumir contenido por memoria):

| Skill / Agente | Ruta a leer primero |
|---|---|
| `clean-code-expert` | `~/.claude/skills/clean-code-expert/SKILL.md` + `references/clean-code-philosophy.md` |
| `solid-expert` | `~/.claude/skills/solid-expert/SKILL.md` |
| `sonarqube-expert` | `~/.claude/skills/sonarqube-expert/SKILL.md` |
| `git-expert` | `~/.claude/skills/git-expert/SKILL.md` |
| `edt-planning-expert` | `~/.claude/skills/edt-planning-expert/SKILL.md` |
| `spanish-writing-expert` | `~/.claude/skills/spanish-writing-expert/SKILL.md` |
| `quality-code-reviewer` | sus tres SKILL.md de calidad + `pom.xml` del proyecto |

Detección de versión: **siempre** leer `pom.xml` antes de proponer APIs. Este
proyecto es **Java 21** + **Spring Boot 3.3.10 (Jakarta)**. Records, sealed
classes, pattern matching, virtual threads, `List.of`, text blocks: todo
disponible.

---

## 6. Reglas de "no se puede"

- ❌ No introducir código sin haber leído el SKILL correspondiente en esta sesión.
- ❌ No commitear sin haber pasado por `quality-code-reviewer`.
- ❌ No usar `BeanUtils.copyProperties` en entidades nuevas.
- ❌ No usar `snake_case` en identificadores Java.
- ❌ No filtrar checked exceptions de reflexión/IO al controller.
- ❌ No silenciar errores con `printStackTrace` ni `System.out`.
- ❌ No declarar interfaces con prefijo `I` (notación húngara).
- ❌ No mantener paquetes mal escritos en inglés (`infraestructure`, `imp`).
- ❌ No correr el backend dentro de `docker compose up` por defecto: ese flujo
  se reserva al perfil `full`.

---

## 7. Cómo "recordarle" a Claude que use esto

Este archivo se llama `AGENTS.md` por convención de Anthropic (lo carga
automáticamente Claude Code en sesiones sobre este directorio). Para reforzar:

- Mantener este archivo en la raíz del módulo Java (`familia/Familia/AGENTS.md`).
- Si en algún momento se quiere endurecer la regla con un hook real
  (pre-commit que lance `quality-code-reviewer`), eso vive en
  `~/.claude/settings.json` (skill `update-config`), no en este documento.
- Cuando un nuevo skill se agregue a `~/.claude/skills/`, este archivo se
  actualiza para incluirlo en la matriz de la sección 2.
