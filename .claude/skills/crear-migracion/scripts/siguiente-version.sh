#!/usr/bin/env bash
# Imprime el siguiente número de versión Flyway libre (1 si no hay migraciones).
set -euo pipefail

DIR="${1:-src/main/resources/db/migration}"

max=0
shopt -s nullglob
for f in "$DIR"/V*__*.sql; do
  name="$(basename "$f")"
  num="${name#V}"
  num="${num%%__*}"
  if [[ "$num" =~ ^[0-9]+$ ]] && (( 10#$num > max )); then
    max=$((10#$num))
  fi
done

echo $((max + 1))
