#!/usr/bin/env bash
# PostToolUse (Edit|Write|MultiEdit): vigila las reglas de java-style.md en código de producción.
# exit 2 devuelve el aviso a Claude para que lo corrija.
set -uo pipefail

if ! command -v jq >/dev/null 2>&1; then
  echo "AVISO: jq no está instalado; check-java-style.sh NO está comprobando nada." >&2
  exit 0
fi

file="$(jq -r '.tool_input.file_path // empty')"
[[ "$file" == */src/main/java/*.java || "$file" == *\src\main\java\*.java ]] || exit 0
[[ -f "$file" ]] || exit 0

problems="$(grep -nE 'System\.(out|err)\.|printStackTrace\(|@Autowired|import lombok\.' "$file" || true)"
[[ -z "$problems" ]] && exit 0

{
  echo "Incumples .claude/rules/java-style.md en $file:"
  echo "$problems"
  echo "Usa SLF4J en vez de System.out/printStackTrace, inyección por constructor en vez de @Autowired y sin Lombok."
} >&2
exit 2
