#!/usr/bin/env bash
# PostToolUse (Edit|Write|MultiEdit): formatea con Spotless el archivo .java que se acaba de tocar.
# Nunca bloquea: si el formateo falla, solo avisa.
set -uo pipefail

command -v jq >/dev/null 2>&1 || exit 0
command -v mvn >/dev/null 2>&1 || exit 0

file="$(jq -r '.tool_input.file_path // empty')"
[[ "$file" == *.java ]] || exit 0
[[ -f "$file" ]] || exit 0

cd "${CLAUDE_PROJECT_DIR:-.}" || exit 0

if ! out="$(mvn -q spotless:apply "-DspotlessFiles=$(printf '%s' "$file" | sed 's/[.[\*^$()+?{|]/\\&/g')" 2>&1)"; then
  echo "Aviso: spotless:apply falló para $file (no es bloqueante)." >&2
  echo "$out" | tail -n 5 >&2
fi

exit 0
