#!/usr/bin/env bash
# SessionStart: comprueba las herramientas de las que dependen los demás hooks y avisa si falta alguna.
# Lo que se imprima por stdout se añade como contexto de la sesión. Nunca bloquea.
set -uo pipefail

missing=()
for tool in jq git mvn docker; do
  command -v "$tool" >/dev/null 2>&1 || missing+=("$tool")
done

if ((${#missing[@]} > 0)); then
  echo "Aviso del entorno: faltan herramientas (${missing[*]}). Los hooks que dependen de ellas no harán nada."
fi

cd "${CLAUDE_PROJECT_DIR:-.}" || exit 0
git rev-parse --is-inside-work-tree >/dev/null 2>&1 || echo "Aviso del entorno: esto no es un repositorio git."

exit 0
