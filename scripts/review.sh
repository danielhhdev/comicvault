#!/usr/bin/env bash
# Revisión automática del diff de la rama actual con Claude Code en modo headless.
# Uso: scripts/review.sh [rama-base]      (por defecto: main)
# Resultado: docs/reviews/<rama>-<fecha>.md
set -euo pipefail

BASE="${1:-main}"
BRANCH="$(git branch --show-current | tr '/' '-')"
OUT="docs/reviews/${BRANCH:-detached}-$(date +%F).md"

mkdir -p docs/reviews

if git diff --quiet "${BASE}...HEAD"; then
  echo "No hay cambios respecto a ${BASE}." >&2
  exit 0
fi

git diff "${BASE}...HEAD" | claude -p \
  "Revisa este diff de un proyecto Spring Boot siguiendo CLAUDE.md y las reglas de .claude/rules/. \
Agrupa los hallazgos en Bloqueantes, Importantes y Sugerencias, con archivo y línea. \
Empieza con un veredicto de una línea." \
  --allowedTools "Read" "Grep" "Glob" \
  --max-turns 10 \
  --output-format text >"$OUT"

echo "Revisión guardada en $OUT"
