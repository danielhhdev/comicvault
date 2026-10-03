#!/usr/bin/env bash
# PreToolUse (Bash): bloquea comandos peligrosos antes de ejecutarlos.
# Entrada: JSON por stdin. Salida: exit 2 + mensaje en stderr = bloqueo (Claude lo lee).
set -uo pipefail

command -v jq >/dev/null 2>&1 || exit 0 # sin jq no se puede inspeccionar: no bloquear

cmd="$(jq -r '.tool_input.command // empty')"
[[ -z "$cmd" ]] && exit 0

block() {
  echo "Bloqueado por .claude/hooks/block-dangerous.sh: $1" >&2
  exit 2
}

# Borrados masivos
grep -Eq 'rm[[:space:]]+(-[a-zA-Z]*r[a-zA-Z]*f|-[a-zA-Z]*f[a-zA-Z]*r)[[:space:]]+(/|~|\$HOME|\.)([[:space:]]|$)' <<<"$cmd" &&
  block "borrado recursivo de una ruta raíz o del directorio actual."

# Historia compartida
grep -Eq 'git[[:space:]]+push.*(--force|[[:space:]]-f([[:space:]]|$))' <<<"$cmd" &&
  block "git push --force no está permitido."
grep -Eq 'git[[:space:]]+push[[:space:]]+[^[:space:]]+[[:space:]]+(main|master)([[:space:]]|$)' <<<"$cmd" &&
  block "no se hace push directo a main/master; usa una rama y un PR."
grep -Eq 'git[[:space:]]+reset[[:space:]]+--hard' <<<"$cmd" &&
  block "git reset --hard descartaría cambios sin guardar."

# Base de datos y volúmenes
grep -Eiq 'drop[[:space:]]+(database|schema)|truncate[[:space:]]+table' <<<"$cmd" &&
  block "operación destructiva sobre la base de datos."
grep -Eq 'docker[[:space:]]+(compose[[:space:]]+down.*(-v|--volumes)|volume[[:space:]]+(rm|prune))' <<<"$cmd" &&
  block "eliminaría los volúmenes (datos) de Docker."

# Ejecución remota sin revisar
grep -Eq '(curl|wget)[^|]*\|[[:space:]]*(sudo[[:space:]]+)?(ba|z)?sh' <<<"$cmd" &&
  block "ejecutar scripts descargados con una tubería a shell no está permitido."

# Saltarse los tests para "arreglar" el build
grep -Eq 'mvn.*(-DskipTests|-Dmaven\.test\.skip)' <<<"$cmd" &&
  block "no se saltan los tests (-DskipTests); arregla el fallo."

exit 0
