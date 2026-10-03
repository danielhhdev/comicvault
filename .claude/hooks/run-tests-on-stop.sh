#!/usr/bin/env bash
# Stop: cuando Claude va a terminar su turno, ejecuta los tests si hay cambios Java.
# Si fallan, exit 2 devuelve la salida a Claude para que siga y los arregle.
# Desactivar puntualmente:  CLAUDE_SKIP_STOP_TESTS=1 claude
set -uo pipefail

[[ "${CLAUDE_SKIP_STOP_TESTS:-0}" == "1" ]] && exit 0
command -v jq >/dev/null 2>&1 || exit 0
command -v mvn >/dev/null 2>&1 || exit 0

input="$(cat)"

# Evita bucles: si Claude ya está continuando por un bloqueo anterior de este hook, no insistir
[[ "$(jq -r '.stop_hook_active // false' <<<"$input")" == "true" ]] && exit 0

cd "${CLAUDE_PROJECT_DIR:-.}" || exit 0

# Solo si hay cambios en Java/SQL/pom sin commitear
git rev-parse --is-inside-work-tree >/dev/null 2>&1 || exit 0
if ! git status --porcelain | grep -Eq '\.(java|sql|yml|xml)$'; then
  exit 0
fi

if out="$(mvn -q test 2>&1)"; then
  exit 0
fi

{
  echo "Los tests fallan (mvn test). Corrígelos antes de terminar. Últimas líneas:"
  echo "$out" | tail -n 40
} >&2
exit 2
