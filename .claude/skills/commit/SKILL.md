---
name: commit
description: Propone y crea un commit con Conventional Commits en español a partir de los cambios pendientes. Úsala cuando se pida hacer commit.
disable-model-invocation: true
allowed-tools: Bash(git status:*), Bash(git diff:*), Bash(git log:*), Bash(git add:*), Bash(git commit:*)
---

# Hacer commit

## Contexto

- Rama: !`git branch --show-current`
- Estado: !`git status --short`
- Cambios: !`git diff HEAD --stat`
- Estilo reciente: !`git log --oneline -5`

## Pasos

1. Si la rama es `main` o `master`, **para** y propón crear antes `feature/`, `fix/` o `chore/`
   (ver `.claude/rules/git-workflow.md`).
2. Lee el diff completo (`git diff HEAD`). Si mezcla cambios lógicos distintos, propón dividirlos
   en varios commits atómicos y añade solo los archivos de cada uno (nunca `git add -A` a ciegas;
   no incluyas `.env`, claves ni salidas de `docs/reviews/`).
3. Redacta el mensaje: `<tipo>: <resumen en minúsculas, imperativo, en español>` con tipo
   `feat`, `fix`, `test`, `docs`, `refactor` o `chore`. Cuerpo opcional con el porqué.
4. **Enséñame el mensaje y los archivos y espera mi confirmación** antes de ejecutar el commit.
5. Termina el mensaje con la línea `Co-Authored-By` que indique el sistema.
6. No hagas push.
