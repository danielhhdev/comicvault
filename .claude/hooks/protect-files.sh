#!/usr/bin/env bash
# PreToolUse (Edit|Write|MultiEdit): protege archivos que no deben tocarse.
#  - Secretos (.env, claves).
#  - Migraciones Flyway que ya están en git (son inmutables).
set -uo pipefail

command -v jq >/dev/null 2>&1 || exit 0

file="$(jq -r '.tool_input.file_path // empty')"
[[ -z "$file" ]] && exit 0

block() {
  echo "Bloqueado por .claude/hooks/protect-files.sh: $1" >&2
  exit 2
}

case "$file" in
  */.env | */.env.* | *.pem | *.key)
    block "'$file' es un archivo de secretos."
    ;;
esac

# Migraciones ya commiteadas: nunca se editan, se crea una nueva
if [[ "$file" == */src/main/resources/db/migration/V*.sql ]]; then
  if git -C "${CLAUDE_PROJECT_DIR:-.}" ls-files --error-unmatch -- "$file" >/dev/null 2>&1; then
    block "la migración '$(basename "$file")' ya está en git y no se puede modificar. Crea una migración nueva (skill crear-migracion)."
  fi
fi

exit 0
