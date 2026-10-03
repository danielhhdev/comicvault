# Flujo de Git

- Ramas: `feature/<tema>`, `fix/<tema>`, `chore/<tema>`. Nunca se trabaja directamente sobre `main`.
- Commits con Conventional Commits en español: `feat: añade el listado paginado de cómics`,
  `fix: corrige el 500 al borrar una serie inexistente`, `test:`, `docs:`, `refactor:`, `chore:`.
- Commits pequeños y atómicos: un cambio lógico por commit.
- No hagas `git push --force`, no reescribas historia compartida y no hagas commit de `.env`
  ni de secretos.
- Los PR deben explicar qué cambia y por qué, y cómo se ha probado.
