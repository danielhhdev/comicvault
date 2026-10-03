#!/usr/bin/env bash
# Notification: avisa con una notificación del sistema cuando Claude necesita tu atención
# (permiso pendiente o espera de respuesta). Multiplataforma y nunca falla.
set -uo pipefail

msg="Claude Code necesita tu atención"
if command -v jq >/dev/null 2>&1; then
  msg="$(jq -r '.message // "Claude Code necesita tu atención"' 2>/dev/null || echo "$msg")"
fi

case "$(uname -s)" in
  Darwin)
    osascript -e "display notification \"${msg//\"/\\\"}\" with title \"Claude Code\"" >/dev/null 2>&1 || true
    ;;
  Linux)
    if command -v notify-send >/dev/null 2>&1; then
      notify-send "Claude Code" "$msg" >/dev/null 2>&1 || true
    fi
    ;;
  MINGW* | MSYS* | CYGWIN*)
    powershell.exe -NoProfile -Command "[console]::beep(800,200)" >/dev/null 2>&1 || true
    ;;
esac

exit 0
