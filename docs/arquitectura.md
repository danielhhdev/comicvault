# Arquitectura de ComicVault

## Modelo de dominio previsto

| Entidad | Campos principales |
|---|---|
| `Publisher` (editorial) | id, name, country |
| `Series` (serie) | id, title, publisher, status (`ONGOING`, `FINISHED`, `CANCELLED`), totalVolumes |
| `Comic` (tomo) | id, series, volumeNumber, title, isbn, releaseDate, readingStatus, rating (1-5), acquiredAt |

`readingStatus`: `WISHLIST`, `UNREAD`, `READING`, `READ`.

Relaciones: una editorial tiene muchas series; una serie tiene muchos tomos.

## Estructura de paquetes

Organización por capa técnica, directamente bajo `com.example.comicvault`. Cada recurso aporta
una clase a cada capa, con el nombre del recurso como prefijo:

```
controller/   PublisherController, SeriesController, ComicController
service/      PublisherService, ...
repository/   PublisherRepository, ...
entity/       Publisher, Series, Comic (y sus enums)
dto/          CreatePublisherRequest, UpdatePublisherRequest, PublisherResponse, ...
common/       error/ (ApiExceptionHandler, NotFoundException, ConflictException)
```

Visibilidad: los controladores son `package-private` (solo los usa Spring MVC). Servicios,
repositorios, entidades y DTOs son `public` porque se usan entre paquetes, pero las entidades
no las usa ningún controlador: solo salen del servicio como DTOs.

Los tests viven en el mismo paquete que la clase que prueban (`controller/PublisherControllerTest`,
`service/PublisherServiceTest`, `repository/PublisherRepositoryTest`).

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
