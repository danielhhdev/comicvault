#!/usr/bin/env bash
# UserPromptSubmit: lo que se imprima por stdout se añade como contexto al prompt.
# Mantenerlo corto: se ejecuta en cada mensaje.
set -uo pipefail

cd "${CLAUDE_PROJECT_DIR:-.}" || exit 0
git rev-parse --is-inside-work-tree >/dev/null 2>&1 || exit 0

branch="$(git branch --show-current 2>/dev/null)"
changed="$(git status --porcelain 2>/dev/null | wc -l | tr -d ' ')"
last="$(git log -1 --pretty=format:'%h %s' 2>/dev/null || true)"

echo "Contexto del repo -> rama: ${branch:-detached} | archivos modificados: ${changed} | último commit: ${last:-ninguno}"

if [[ "$branch" == "main" || "$branch" == "master" ]]; then
  echo "Aviso: estás en ${branch}. Crea una rama feature/... antes de modificar código."
fi

exit 0
