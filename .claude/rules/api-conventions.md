---
paths:
  - "src/main/java/**/*Controller.java"
  - "src/main/java/**/dto/**/*.java"
---

# Convenciones de la API REST

- Todas las rutas cuelgan de `/api/v1` y usan sustantivos en plural: `/api/v1/comics`,
  `/api/v1/series/{id}/comics`.
- Códigos de estado:
  - `200` lectura/actualización correcta · `201` creación (con cabecera `Location`)
  - `204` borrado correcto · `400` validación · `404` no existe · `409` conflicto
- Los controladores reciben y devuelven **DTOs** (`record`), nunca entidades JPA.
- Validación con Bean Validation en el DTO de entrada (`@NotBlank`, `@Size`, `@Min`...) y
  `@Valid` en el parámetro del controlador.
- Listados paginados con `Pageable` (`page`, `size`, `sort`); tamaño máximo por defecto 50.
- Actualización parcial con `PATCH`, reemplazo completo con `PUT`.
- Los errores salen siempre como `ProblemDetail`; no inventes formatos de error propios.
- Los controladores no contienen lógica de negocio ni acceden a repositorios.
- Nombres de DTO: `CreateComicRequest`, `UpdateComicRequest` (`PUT`), `PatchComicRequest`
  (`PATCH`, campos opcionales) y `ComicResponse`.
