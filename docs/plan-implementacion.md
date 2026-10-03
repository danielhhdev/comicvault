# Plan de implementación de ComicVault

## Context

ComicVault es una API REST para gestionar una colección de cómics. Hoy solo existe el esqueleto:
`ComicvaultApplication`, el manejo de errores en `common/error/` (`ApiExceptionHandler`,
`NotFoundException`, `ConflictException`), la configuración (`application.yml`,
`application-test.yml`, `compose.yaml`), CI y las piezas de Claude Code. No hay migraciones,
entidades, repositorios, servicios ni controladores (`db/migration/` solo tiene `.gitkeep`).

Objetivo: implementar el dominio de `docs/arquitectura.md` (Publisher, Series, Comic) con CRUD
completo, más filtros, estadísticas y hardening ligero. Alcance elegido: dominio previsto +
extras ligeros, sin autenticación.

Reglas que condicionan todo el plan (de `CLAUDE.md` y `.claude/rules/`):
- Un corte vertical por funcionalidad: migración → entidad → repositorio → servicio →
  controlador → tests. `mvn spotless:apply` y `mvn test` al terminar cada corte.
- Una rama por bloque (`feature/<tema>`), un commit por corte (skill `commit`, Conventional
  Commits en español). Nada de trabajar sobre `main`.
- Antes de cerrar cada bloque: `code-reviewer`, y `security-auditor` si toca entrada de datos
  o consultas.
- No se añaden dependencias sin justificarlas.

## Fase 0 · Verificar el build

Rama `chore/verificar-build`. El README admite que el esqueleto nunca se compiló.

1. `mvn verify` y arreglar lo que falle (versiones, starters de Spring Boot 4, Flyway).
2. `mvn spotless:check`.
3. Comprobar que `ComicvaultApplicationTests` arranca con el perfil `test` (H2) sin migraciones.
4. Quitar del README la nota de "no se ha podido compilar" si todo pasa.

Salida: `mvn verify` en verde. Sin él no se empieza la Fase 1.

## Fase 1 · Publisher (editorial)

Rama `feature/publishers`. Es el recurso más simple y fija el patrón para los demás.

- Migración `V1__create_publishers_table.sql` (`id`, `name` único, `country`, `created_at`).
- `publisher/entity/Publisher`, `PublisherRepository`, `PublisherService`, `PublisherController`.
- DTOs: `CreatePublisherRequest`, `UpdatePublisherRequest`, `PublisherResponse`.
- Endpoints bajo `/api/v1/publishers`: `GET` paginado, `GET /{id}`, `POST` (201 + `Location`),
  `PUT /{id}`, `DELETE /{id}` (204).
- Reglas: nombre duplicado → `ConflictException` (409); id inexistente → `NotFoundException` (404).
- Tests: servicio (Mockito), `@WebMvcTest` del controlador (404, 400, 409) y `@DataJpaTest` del
  repositorio.

Reutiliza `NotFoundException`, `ConflictException` y `ApiExceptionHandler` de
`common/error/`. Se puede apoyar en las skills `crear-migracion` y `crear-endpoint`.

## Fase 2 · Series

Rama `feature/series`.

- Migración `V2__create_series_table.sql` (`title`, `publisher_id` FK, `status`,
  `total_volumes`, `created_at`).
- Entidad `Series` con `@ManyToOne(LAZY)` a `Publisher` y enum `SeriesStatus`
  (`ONGOING`, `FINISHED`, `CANCELLED`).
- Endpoints `/api/v1/series` (CRUD) con `PATCH` para actualización parcial.
- Listado con `@EntityGraph` sobre `publisher` para evitar N+1.
- Reglas: la editorial debe existir (404); `totalVolumes >= 1`; no se borra una editorial con
  series (409 en `PublisherService`).
- Tests: servicio, web y JPA, incluida la comprobación de N+1.

## Fase 3 · Comic (tomo)

Rama `feature/comics`.

- Migración `V3__create_comics_table.sql` (`series_id` FK, `volume_number`, `title`, `isbn`,
  `release_date`, `reading_status`, `rating`, `acquired_at`, `created_at`).
  Restricción única `(series_id, volume_number)`; `isbn` único si no es nulo.
- Entidad `Comic` y enum `ReadingStatus` (`WISHLIST`, `UNREAD`, `READING`, `READ`).
- Endpoints: `/api/v1/comics` (CRUD + `PATCH`) y `GET /api/v1/series/{id}/comics`.
- Validaciones: `rating` entre 1 y 5, y solo si el estado es `READ`; `volumeNumber >= 1`;
  tomo duplicado en la serie → 409.
- Tests: servicio (reglas de `rating` y duplicados), web (400/404/409) y JPA.

## Fase 4 · Filtros y búsqueda

Rama `feature/filtros-comics`.

- `GET /api/v1/comics?readingStatus=&seriesId=&publisherId=&q=` con paginación y orden.
- Consultas en el repositorio con `@Query` JPQL con parámetros nombrados (sin concatenar) o
  `Specification`. Decisión al empezar la fase; por defecto JPQL, que es más simple.
- Tests `@DataJpaTest` de cada combinación de filtros.
- `security-auditor` obligatorio: toca entrada de datos y consultas.

## Fase 5 · Estadísticas de la colección

Rama `feature/estadisticas`.

- `GET /api/v1/stats` → total de tomos, tomos por `readingStatus`, series completas frente a
  incompletas (tomos poseídos vs `totalVolumes`) y nota media.
- Agregaciones en el repositorio con proyecciones o consultas `GROUP BY`, expuestas como
  `record` `CollectionStatsResponse`.
- Tests de servicio y de repositorio con datos conocidos.

## Fase 6 · Hardening y documentación

Rama `chore/hardening`.

- Tests de integración `@SpringBootTest` (pocos): flujo completo editorial → serie → tomo →
  estadísticas contra H2.
- Comprobar que las migraciones aplican igual en PostgreSQL real (`docker compose up -d db` +
  `mvn spring-boot:run`) y no solo en H2.
- OpenAPI con `springdoc-openapi`. **Es una dependencia nueva**: hay que justificarla y que la
  apruebes antes de añadirla.
- Actualizar `README.md`, `docs/arquitectura.md` y `docs/NOTAS.md`.
- Revisar que CI (`spotless:check` + `mvn verify`) pasa en la rama.

## Orden y dependencias

```
Fase 0 → Fase 1 → Fase 2 → Fase 3 → Fase 4
                                  └→ Fase 5
                                        └→ Fase 6
```

Las fases 4 y 5 solo dependen de la 3 y podrían ir en paralelo (worktrees con
`scripts/worktree.sh`). Por defecto van en secuencia.

## Verificación

Por corte: `mvn spotless:apply` y `mvn test` en verde. Por fase: `code-reviewer` (y
`security-auditor` en las fases 4 y 6). Al final de las fases 1, 3 y 6:
`mvn verify`, y arrancar contra PostgreSQL en Docker y probar a mano con `curl`
(`/actuator/health` y el CRUD de cada recurso).

## Fuera de alcance

Autenticación y autorización, Dockerfile y despliegue, importación o exportación masiva de
datos, e imágenes de portada.

## Decisiones pendientes

- Fase 4: JPQL o `Specification`.
- Fase 6: aprobar `springdoc-openapi` como dependencia nueva.
- Cada fase termina en un PR cuyo push y apertura decides tú.
