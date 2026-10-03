# Primera implementación: gestión de series y tomos

Objetivo: ejercitar el flujo completo (plan mode → subagente arquitecto → skills → tests →
revisión) con una funcionalidad real y no demasiado grande.

## Paso 0. Prepara la sesión

```bash
git checkout -b feature/series-y-comics
claude
```

Pulsa **Shift+Tab** hasta entrar en **plan mode**.

## Paso 1. Pide el plan

Pega este prompt (o usa `/nueva-feature` con la misma descripción):

```
/nueva-feature CRUD de series y tomos de cómics. Una serie tiene título, editorial, estado
(ONGOING, FINISHED, CANCELLED) y número total de tomos. Un tomo pertenece a una serie y tiene
número de volumen, título, ISBN opcional, fecha de salida, estado de lectura (WISHLIST, UNREAD,
READING, READ) y valoración opcional de 1 a 5. Quiero listados paginados, filtrar los tomos por
estado de lectura y que no se pueda duplicar el número de volumen dentro de una serie (409).
```

Qué observar:
- Que lea `CLAUDE.md` y `docs/arquitectura.md` antes de proponer nada.
- Que use el subagente `spring-architect` y te devuelva un plan con migración, capas y tests.
- Que **no escriba código** hasta que apruebes el plan.

## Paso 2. Aprueba y deja que implemente

- Revisa el plan; si algo no te convence, corrígelo en lenguaje natural antes de aprobar.
- Fíjate en que use las skills `crear-migracion` (números de versión correctos) y `crear-endpoint`.
- Mira cómo actúan los hooks: formatea cada `.java` que edita, bloquea editar una migración ya
  commiteada y ejecuta `mvn test` al terminar el turno.

## Paso 3. Cierra

1. Deja que `test-writer` complete los tests que falten.
2. Ejecuta `/revisar-cambios` y comprueba el informe de `code-reviewer`.
3. Commit con mensaje en Conventional Commits y abre el PR; mira qué comenta la revisión
   automática de GitHub Actions.

## Experimentos para entender cada pieza

- Desactiva una rule (renombra el archivo) y repite una tarea: ¿qué cambia?
- Pídele que edite una migración ya commiteada: debe saltar `protect-files.sh`.
- Pídele que haga `git push --force`: lo frenan los permisos y `block-dangerous.sh`.
- Lanza `scripts/review.sh` y compara su salida con la de `/revisar-cambios`.
- Usa `scripts/worktree.sh` para hacer a la vez otra mini-funcionalidad en paralelo.
