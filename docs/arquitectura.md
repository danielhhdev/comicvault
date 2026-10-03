# Arquitectura de ComicVault

## Modelo de dominio previsto

| Entidad | Campos principales |
|---|---|
| `Publisher` (editorial) | id, name, country |
| `Series` (serie) | id, title, publisher, status (`ONGOING`, `FINISHED`, `CANCELLED`), totalVolumes |
| `Comic` (tomo) | id, series, volumeNumber, title, isbn, releaseDate, readingStatus, rating (1-5), acquiredAt |

`readingStatus`: `WISHLIST`, `UNREAD`, `READING`, `READ`.

Relaciones: una editorial tiene muchas series; una serie tiene muchos tomos.

## Capas

- **controller**: HTTP, validación de entrada y códigos de estado. Sin lógica de negocio.
- **service**: reglas de negocio y transacciones (`@Transactional`).
- **repository**: acceso a datos con Spring Data JPA.
- **entity**: modelo persistente. No se expone fuera del servicio.
- **dto**: `record` de entrada/salida de la API.

## Errores

Se devuelven como `ProblemDetail` (RFC 9457). `NotFoundException` → 404; los errores de
validación los gestiona `ApiExceptionHandler`.

## Persistencia

PostgreSQL en desarrollo y producción; H2 en modo PostgreSQL en los tests. Las migraciones
(`src/main/resources/db/migration/V<n>__descripcion.sql`) deben ser SQL portable entre ambos.
