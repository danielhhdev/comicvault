# Estado de las fases

Punto de retoma del proyecto: lee esto primero y no hace falta revisar el código para saber
por dónde seguir. El detalle de cada fase está en `docs/plan-implementacion.md`.

**Última actualización:** 2026-10-05

## Dónde estamos

| Fase | Contenido | Estado |
|---|---|---|
| 0 | Verificar el build | Hecha |
| 1 | Publisher (editoriales): CRUD, migraciones V1 y V2, 36 tests | Hecha, en `main` |
| 2 | Series: CRUD completo, migración V3, 409 al borrar editorial con series, 114 tests en total | Hecha, en `feature/series` (pendiente de integrar en `main`) |
| 3 | Comic (tomo) | **Siguiente** |
| 4 | Filtros y búsqueda | Pendiente |
| 5 | Estadísticas de la colección | Pendiente |
| 6 | Hardening y documentación | Pendiente |

## Siguiente paso

Integrar `feature/series` en `main` (fast-forward o PR, lo decide el usuario) y después lanzar
`/nueva-feature` para Comic (Fase 3), en la rama `feature/comics` saliendo de `main`.
Remoto `origin`: `https://github.com/danielhhdev/comicvault.git` (enlazado, vacío hasta el primer
push). No hay push ni PR sin que se pida.

## Qué hay que tener en cuenta en la Fase 3

- La siguiente migración es **V4**. Usa el script de `crear-migracion`, no calcules el número.
- Borrar una serie con tomos debe dar 409: hoy `SeriesService.delete` no lo controla (mismo
  patrón que `PublisherService.delete`, con `existsBy...` previo y la FK como red de seguridad).
- Patrón de `Series` como referencia: `title_key` calculado en Java + `UNIQUE (parent_id, key)`,
  comprobación previa + `flush` y traducción de `DataIntegrityViolationException`
  (`SeriesService.integrityFailure` distingue 404 por padre borrado de 409 por duplicado),
  listado con `PagedModel`, `@EntityGraph` y `PATCH` con `null` = sin cambios.
- Referencia a padre inexistente al crear, PUT o PATCH: **404** (`NotFoundException`).
- **No uses `CHECK ... IN (...)` ni `OR` de igualdades en las migraciones**: H2 2.4 los convierte
  en un conjunto constante ligado a la sesión de Flyway (ya cerrada) y los inserts fallan con
  «La base de datos ha sido cerrada». Series no tiene `CHECK` sobre `status`; el enum y la
  validación del DTO lo protegen. Los `CHECK` de comparación (`>= 1`) funcionan bien.
- `PATCH` no puede vaciar un campo opcional (`null` = sin cambios); para eso se usa `PUT`.
- En `@DataJpaTest`, para probar una FK que impide borrar hay que `entityManager.clear()` antes,
  o Hibernate se queja en memoria de la referencia transitoria y no llega a la base de datos.
- Las skills `crear-endpoint` y `commit` ya se pueden invocar desde Claude (se quitó
  `disable-model-invocation`). La skill `commit` pide confirmación antes de cada commit.

## Decisiones abiertas

- **Fase 4:** consultas con JPQL o con `Specification` (por defecto JPQL).
- **Fase 6:** aprobar `springdoc-openapi` como dependencia nueva.
- **Fase 6 (seguridad, auditoría de la Fase 2):** la API no tiene autenticación; `?sort=` acepta
  cualquier propiedad de la entidad (valorar lista blanca) y no hay tope superior en
  `totalVolumes`.
- **Espacios en nombres y títulos:** `"Panini "` y `"Panini"` son editoriales distintas, y lo
  mismo pasa con los títulos de serie. Sin decidir (recortar con `strip()` en las dos entidades
  y en una migración).
- **`.claude/skills/commit/SKILL.md`:** la primera línea es ` ---` (con un espacio delante y
  CRLF), lo que puede romper el frontmatter. Está sin commitear; revisarlo.
- **Ramas viejas:** siguen existiendo las ya integradas en `main`; se pueden borrar con
  `git branch -d`.

## Entorno

- PostgreSQL en Docker publicado en el **5433** (hay un PostgreSQL nativo en el 5432 que tapaba
  el contenedor). `mvn spring-boot:run` arranca sin variables de entorno.
- `mvn verify` y los tests usan H2 y no necesitan Docker.
- Estructura de paquetes por capa en la raíz: `controller/`, `service/`, `repository/`,
  `entity/`, `dto/` (ver `docs/arquitectura.md`).

## Cómo mantener este documento

Al cerrar una fase con `nueva-feature`: marcarla como hecha, mover "Siguiente" a la fase que
toca, actualizar "Qué hay que tener en cuenta" con lo aprendido y revisar las decisiones
abiertas. Se commitea junto al cierre de la fase.
