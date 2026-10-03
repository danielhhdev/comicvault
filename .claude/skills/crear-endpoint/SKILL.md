---
name: crear-endpoint
description: Genera las clases de un recurso REST (DTOs, repositorio, servicio, controlador y tests) con las convenciones de ComicVault. Es un paso de implementación de nueva-feature; úsala suelta solo si piden únicamente el endpoint, sin plan ni revisión.
disable-model-invocation: true
---

# Crear un endpoint REST

Esta skill solo genera código: no crea rama, no planifica, no commitea ni revisa. Para una
funcionalidad nueva usa `nueva-feature`, que la invoca en su paso de implementación.

Sigue estos pasos en orden. Las plantillas de `templates/` son un punto de partida: sustituye
los marcadores `__Recurso__`, `__recurso__` y `__recursos__` por el nombre real (singular
con mayúscula, singular en minúscula y plural en minúscula) y adapta los campos.

## Antes de escribir código

1. Lee `.claude/rules/api-conventions.md` y `.claude/rules/persistence.md`.
2. Comprueba si el recurso ya tiene clases en las capas de `src/main/java/com/example/comicvault/`
   (`controller/`, `service/`, `repository/`, `entity/`, `dto/`)
   y si hace falta una migración (en ese caso usa primero la skill `crear-migracion`).

## Pasos

1. **DTOs** (`dto/`): `Create__Recurso__Request`, `Update__Recurso__Request` (reemplazo completo,
   `PUT`), `Patch__Recurso__Request` (campos opcionales, `null` = sin cambios, `PATCH`) y
   `__Recurso__Response` como `record`, con validaciones Bean Validation en las peticiones.
2. **Repositorio** (`repository/__Recurso__Repository.java`, `public`): extiende `JpaRepository`.
3. **Servicio** (`service/__Recurso__Service.java`, `public` con métodos `public`): `@Service`, inyección por constructor,
   `@Transactional` en las operaciones de escritura; lanza `NotFoundException` si no existe.
   Si hay una restricción única, comprueba antes y usa `saveAndFlush`/`flush` capturando
   `DataIntegrityViolationException` para devolver `ConflictException` (409), no un 500.
4. **Controlador** (`controller/__Recurso__Controller.java`, `package-private`): ruta `/api/v1/__recursos__`,
   devuelve DTOs y los códigos de estado de la regla de API (`201` con `Location`, `204`...).
   Listado con `@PageableDefault(sort = ...)` y `PagedModel` (el JSON de `Page` no es estable).
5. **Tests**: un test unitario del servicio y un `@WebMvcTest` del controlador (usa la plantilla
   `templates/ControllerTest.java.tpl`) que cubran también 400, 404 y, si aplica, 409. Los imports de Spring Boot 4 (`org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest`, `org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest`) ya están en la plantilla.
6. Ejecuta `mvn spotless:apply` y `mvn test`. No termines con los tests en rojo.

## Entrega

Resume los archivos creados, los endpoints expuestos (método y ruta) y el resultado de `mvn test`.
