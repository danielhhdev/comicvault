#!/usr/bin/env bash
# Crea un git worktree aislado con su propia rama para trabajar en paralelo con otra sesión de Claude.
# Uso:    scripts/worktree.sh <nombre-feature>
# Luego:  cd ../comicvault-<nombre-feature> && claude
# Limpia: git worktree remove ../comicvault-<nombre-feature>
set -euo pipefail

if [[ $# -ne 1 ]]; then
  echo "Uso: $0 <nombre-feature>" >&2
  exit 1
fi

NAME="$1"
ROOT="$(git rev-parse --show-toplevel)"
DIR="$(dirname "$ROOT")/$(basename "$ROOT")-${NAME}"
BRANCH="feature/${NAME}"

git worktree add "$DIR" -b "$BRANCH"

cat <<EOF

Worktree creado:
  carpeta: $DIR
  rama:    $BRANCH

Siguiente paso:
  cd "$DIR" && claude

Nota: cada worktree arranca su propia base de datos de tests (H2 en memoria), pero si usas
PostgreSQL en local comparten el mismo contenedor y el mismo puerto 5433.
EOF
