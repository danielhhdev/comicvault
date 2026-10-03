# Estado de las fases

Punto de retoma del proyecto: lee esto primero y no hace falta revisar el código para saber
por dónde seguir. El detalle de cada fase está en `docs/plan-implementacion.md`.

**Última actualización:** 2026-10-03

## Dónde estamos

| Fase | Contenido | Estado |
|---|---|---|
| 0 | Verificar el build | Hecha |
| 1 | Publisher (editoriales): CRUD, migraciones V1 y V2, 36 tests | Hecha, en `main` |
| 2 | Series | **Siguiente** |
| 3 | Comic (tomo) | Pendiente |
| 4 | Filtros y búsqueda | Pendiente |
| 5 | Estadísticas de la colección | Pendiente |
| 6 | Hardening y documentación | Pendiente |

## Siguiente paso

Fase 2: lanzar `/nueva-feature` para Series, en la rama `feature/series` saliendo de `main`.
Sin remoto: no hay push ni PR, las ramas se integran en `main` con fast-forward cuando se pide.

## Qué hay que tener en cuenta en la Fase 2

- La siguiente migración es **V3**. Usa el script de `crear-migracion`, no calcules el número.
- Borrar una editorial con series debe dar 409: hoy `PublisherService.delete` no lo controla.
- El patrón de `Publisher` es la referencia: unicidad con comprobación previa + `flush` y
  traducción de `DataIntegrityViolationException` a 409, listado con `PagedModel` y orden por
  defecto, y `?sort=` inválido como 400 (ya lo gestiona `ApiExceptionHandler`).
- Series referencia a Publisher con `@ManyToOne(LAZY)` y el listado necesita `@EntityGraph`.

## Decisiones abiertas

- **Fase 4:** consultas con JPQL o con `Specification` (por defecto JPQL).
- **Fase 6:** aprobar `springdoc-openapi` como dependencia nueva.
- **Nombre de editorial:** hoy ignora mayúsculas, pero no recorta espacios (`"Panini "` y
  `"Panini"` son distintos). Sin decidir.
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
