# ComicVault

API REST para gestionar una colección de cómics (series, tomos, editoriales y estado de lectura).
Es un proyecto de aprendizaje: el objetivo real es practicar Claude Code (rules, skills, agentes,
hooks, MCP y automatización), pero el código se trata como un proyecto profesional.

## Stack

- Java 21 y Spring Boot 4.1 (Maven)
- Spring MVC, Spring Data JPA, Bean Validation, Actuator
- PostgreSQL 17 (Docker Compose) y Flyway para migraciones
- Tests: JUnit 5, AssertJ, Mockito y H2 en modo PostgreSQL (perfil `test`)
- Formato: Spotless con google-java-format

## Comandos

```bash
docker compose up -d db          # base de datos local (también la arranca spring-boot-docker-compose)
mvn spring-boot:run              # arrancar la API en http://localhost:8080
mvn test                         # tests (usan H2, no necesitan Docker)
mvn verify                       # compilar + tests + empaquetar
mvn test -Dtest=NombreTest       # una clase de test
mvn spotless:apply               # formatear
mvn spotless:check               # comprobar formato (lo hace el CI)
```

## Arquitectura

Paquetes por funcionalidad (no por capa técnica), bajo `com.example.comicvault`:

```
comic/      controller, service, repository, entity, dto   (una carpeta por funcionalidad)
series/     idem
common/     código compartido (errores, configuración)
```

Flujo: `Controller → Service → Repository`. Los controladores nunca tocan repositorios y
las entidades JPA nunca salen de la capa de servicio (se exponen DTOs).

@docs/arquitectura.md

## Convenciones clave (el detalle está en `.claude/rules/`)

- DTOs como `record`. Sin Lombok. Inyección por constructor.
- API bajo `/api/v1`, nombres de recurso en plural, errores en formato ProblemDetail.
- El esquema solo cambia con migraciones Flyway; `ddl-auto` es `validate`.
- Una migración ya aplicada o commiteada no se edita nunca: se crea otra nueva.
- Todo código nuevo lleva tests; no se da una tarea por terminada con `mvn test` en rojo.

## Forma de trabajar

1. Para cualquier funcionalidad nueva, empieza en **plan mode** y no escribas código hasta
   que el plan esté aprobado.
2. Implementa por cortes verticales pequeños (migración → entidad → repositorio → servicio →
   controlador → tests) y ejecuta `mvn test` al terminar cada corte.
3. Antes de dar algo por cerrado, pide una revisión al subagente `code-reviewer`.
4. Si un cambio es ambiguo, pregunta antes de decidir.

## Qué NO hacer

- No leer ni editar `.env*`; no meter secretos en el código.
- No usar `-DskipTests` para "arreglar" un build.
- No hacer `git push --force` ni push directo a `main`.
- No añadir dependencias sin explicar por qué hacen falta.

## Piezas de Claude Code de este repo

- Rules: `.claude/rules/` · Agentes: `.claude/agents/` · Skills: `.claude/skills/`
- Hooks: `.claude/hooks/` (configurados en `.claude/settings.json`)
- Diario de aprendizaje: `docs/NOTAS.md`
