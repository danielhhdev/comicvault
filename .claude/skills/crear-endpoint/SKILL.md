---
name: crear-endpoint
description: Crea un endpoint REST completo (controlador, servicio, repositorio, DTOs y tests) siguiendo las convenciones de ComicVault. Úsala cuando se pida añadir un recurso o endpoint nuevo a la API.
---

# Crear un endpoint REST

Sigue estos pasos en orden. Las plantillas de `templates/` son un punto de partida: sustituye
los marcadores `__Recurso__`, `__recurso__` y `__recursos__` por el nombre real (singular
con mayúscula, singular en minúscula y plural en minúscula) y adapta los campos.

## Antes de escribir código

1. Lee `.claude/rules/api-conventions.md` y `.claude/rules/persistence.md`.
2. Comprueba si el recurso ya tiene clases en las capas de `src/main/java/com/example/comicvault/`
   (`controller/`, `service/`, `repository/`, `entity/`, `dto/`)
   y si hace falta una migración (en ese caso usa primero la skill `crear-migracion`).

## Pasos

1. **DTOs** (`dto/`): `Create__Recurso__Request`, `Update__Recurso__Request` y
   `__Recurso__Response` como `record`, con validaciones Bean Validation en las peticiones.
2. **Repositorio** (`repository/__Recurso__Repository.java`, `public`): extiende `JpaRepository`.
3. **Servicio** (`service/__Recurso__Service.java`, `public` con métodos `public`): `@Service`, inyección por constructor,
   `@Transactional` en las operaciones de escritura; lanza `NotFoundException` si no existe.
4. **Controlador** (`controller/__Recurso__Controller.java`, `package-private`): ruta `/api/v1/__recursos__`,
   devuelve DTOs y los códigos de estado de la regla de API (`201` con `Location`, `204`...).
5. **Tests**: un test unitario del servicio y un `@WebMvcTest` del controlador (usa la plantilla
   `templates/ControllerTest.java.tpl`). Los imports de Spring Boot 4 (`org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest`, `org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest`) ya están en la plantilla.
6. Ejecuta `mvn spotless:apply` y `mvn test`. No termines con los tests en rojo.

## Entrega

Resume los archivos creados, los endpoints expuestos (método y ruta) y el resultado de `mvn test`.
