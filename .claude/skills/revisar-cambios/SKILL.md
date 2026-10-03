---
name: revisar-cambios
description: Revisa los cambios de la rama actual respecto a main con el subagente code-reviewer
argument-hint: [rama-base]
disable-model-invocation: true
allowed-tools: Bash(git diff:*), Bash(git log:*), Bash(git status:*), Bash(git branch:*)
---

## Contexto

- Rama actual: !`git branch --show-current`
- Estado: !`git status --short`
- Commits propios: !`git log --oneline ${ARGUMENTS:-main}..HEAD`

Usa el subagente `code-reviewer` para revisar los cambios de esta rama respecto a
`${ARGUMENTS:-main}` (`git diff ${ARGUMENTS:-main}...HEAD`, más los cambios sin commitear).
Devuélveme su informe tal cual, ordenado por gravedad, y al final dime qué corregirías primero.
