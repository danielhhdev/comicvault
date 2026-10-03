# Hooks de Claude Code

Guía para entender qué son los hooks, cómo funcionan y cuáles tiene este proyecto.

## 1. Qué es un hook

Un **hook** es un script (o comando) que Claude Code ejecuta **automáticamente** cuando ocurre un
evento concreto: arrancar una sesión, enviar un mensaje, usar una herramienta, terminar un turno...

La idea clave: **lo ejecuta el programa, no el modelo**. Claude no "decide" lanzarlo ni puede
saltárselo.

### Hooks vs. instrucciones (CLAUDE.md / rules)

| | `CLAUDE.md` y `.claude/rules/` | Hooks |
|---|---|---|
| Naturaleza | Texto que Claude lee | Código que se ejecuta |
| Cumplimiento | Probable, pero no garantizado | Determinista (siempre ocurre) |
| Ejemplo | "No uses `-DskipTests`" | Un script que **bloquea** cualquier comando con `-DskipTests` |

Regla práctica: si algo **debe** cumplirse siempre (seguridad, formato, tests), va en un hook.
Si es una guía o preferencia, va en las rules.

## 2. Para qué sirven

- **Bloquear** acciones peligrosas (`git push --force`, borrar archivos, editar `.env`).
- **Automatizar** tareas repetitivas (formatear código tras cada edición, lanzar tests al terminar).
- **Dar contexto** a Claude (rama actual, estado de git).
- **Avisarte** (notificación cuando Claude espera una respuesta tuya).
- **Reforzar convenciones** del proyecto (sin Lombok, sin `System.out`...).

## 3. Cómo funcionan

### 3.1 Ciclo de vida

```
SessionStart ──► UserPromptSubmit ──► [ PreToolUse ──► (la herramienta se ejecuta) ──► PostToolUse ]* ──► Stop
                  (tu mensaje)           ▲ puede bloquear                                  (cada herramienta)  (fin del turno)

Notification: aparece en cualquier momento (permiso pendiente, Claude esperando)
```

### 3.2 Eventos que usa este proyecto

| Evento | Cuándo se dispara | ¿Puede bloquear? |
|---|---|---|
| `SessionStart` | Al iniciar o reanudar una sesión | No |
| `UserPromptSubmit` | Al enviar un mensaje, antes de que Claude lo procese | Sí (en este repo solo añade contexto) |
| `PreToolUse` | **Antes** de que Claude use una herramienta (Bash, Edit, Write...) | Sí: cancela la acción |
| `PostToolUse` | **Después** de que la herramienta se haya ejecutado | No deshace nada, pero puede avisar a Claude |
| `Stop` | Cuando Claude va a terminar su respuesta | Sí: le obliga a seguir trabajando |
| `Notification` | Cuando Claude necesita tu atención | No |

Existen más eventos (p. ej. `SubagentStop`, `PreCompact`, `SessionEnd`); aquí no se usan.

### 3.3 Cómo se comunica un hook con Claude Code

1. **Entrada:** Claude Code le pasa un **JSON por stdin** con los datos del evento. Por ejemplo,
   en `PreToolUse` con Bash llega algo como:
   ```json
   { "tool_name": "Bash", "tool_input": { "command": "git push --force" } }
   ```
   Los scripts lo leen con `jq` (por eso `jq` es una dependencia; `session-start.sh` avisa si falta).
2. **Salida:** el **código de salida** decide qué pasa:

   | Código | Significado |
   |---|---|
   | `0` | Todo bien. En `SessionStart` y `UserPromptSubmit`, lo que se escriba por **stdout** se añade como contexto para Claude. |
   | `2` | **Bloqueo.** Lo que se escriba por **stderr** se le muestra a Claude para que reaccione (cambie de plan, corrija el código...). |
   | Otro | Error no bloqueante: se registra, pero la acción sigue. |

3. **Variable útil:** `$CLAUDE_PROJECT_DIR` es la ruta raíz del proyecto; así los scripts funcionan
   aunque Claude esté en otro directorio.

### 3.4 Configuración en `settings.json`

Los hooks se declaran en `.claude/settings.json` (compartido con el equipo vía git):

```json
"PreToolUse": [
  {
    "matcher": "Bash",
    "hooks": [
      {
        "type": "command",
        "command": "\"$CLAUDE_PROJECT_DIR\"/.claude/hooks/block-dangerous.sh",
        "timeout": 10
      }
    ]
  }
]
```

- **evento** (`PreToolUse`): cuándo se dispara.
- **`matcher`**: filtra por herramienta. Admite regex: `Edit|Write|MultiEdit` = cualquiera de las tres.
  Sin matcher, aplica siempre (como en `Stop`).
- **`command`**: el script a ejecutar.
- **`timeout`**: segundos máximos antes de cancelarlo.

> Tras editar `settings.json`, Claude Code puede pedir revisar/aprobar los cambios en los hooks
> (es una medida de seguridad: un hook ejecuta código en tu máquina).

## 4. Hooks de este proyecto

Todos están en `.claude/hooks/` y registrados en `.claude/settings.json`.

### Resumen

| Script | Evento | Matcher | Qué hace | ¿Bloquea? |
|---|---|---|---|---|
| `session-start.sh` | `SessionStart` | — | Comprueba que existen `jq`, `git`, `mvn`, `docker` y que es un repo git | No |
| `inject-context.sh` | `UserPromptSubmit` | — | Añade rama, nº de archivos modificados y último commit; avisa si estás en `main` | No |
| `block-dangerous.sh` | `PreToolUse` | `Bash` | Bloquea comandos peligrosos | **Sí** |
| `protect-files.sh` | `PreToolUse` | `Edit\|Write\|MultiEdit` | Protege secretos y migraciones ya commiteadas | **Sí** |
| `format-java.sh` | `PostToolUse` | `Edit\|Write\|MultiEdit` | Formatea el `.java` editado con Spotless | No |
| `check-java-style.sh` | `PostToolUse` | `Edit\|Write\|MultiEdit` | Detecta `System.out`, `@Autowired`, Lombok... | Avisa a Claude (exit 2) |
| `run-tests-on-stop.sh` | `Stop` | — | Ejecuta `mvn test` al terminar si hay cambios; si fallan, Claude sigue | **Sí** |
| `notify.sh` | `Notification` | — | Notificación/pitido cuando Claude necesita tu atención | No |

### Detalle

**`session-start.sh`** — Al abrir la sesión verifica las herramientas de las que dependen los demás
hooks. Si falta alguna, lo dice (así sabes que un hook "no hace nada" por falta de `jq`, no porque
funcione bien).

**`inject-context.sh`** — Su stdout se inyecta como contexto en cada mensaje tuyo. Por eso Claude
sabe en qué rama estás sin preguntarlo. Si estás en `main`, recuerda crear una rama `feature/...`
(refuerza `.claude/rules/git-workflow.md`). Debe ser corto porque se ejecuta en **cada** mensaje.

**`block-dangerous.sh`** — Lee el comando de Bash y lo bloquea si coincide con:
- `rm -rf` sobre `/`, `~`, `$HOME` o `.`
- `git push --force` / `-f`, push directo a `main`/`master`, `git reset --hard`
- `DROP DATABASE/SCHEMA`, `TRUNCATE TABLE`, `docker compose down -v`, `docker volume rm/prune`
- `curl ... | sh` (ejecutar scripts descargados)
- `mvn ... -DskipTests` (saltarse tests)

Claude recibe el motivo del bloqueo y debe buscar otra vía.

**`protect-files.sh`** — Antes de editar/escribir un archivo:
- Bloquea `.env`, `.env.*`, `*.pem`, `*.key`.
- Bloquea editar una migración `V*.sql` que **ya está en git** (las migraciones aplicadas son
  inmutables: hay que crear una nueva con la skill `crear-migracion`).

**`format-java.sh`** — Tras editar un `.java`, ejecuta `mvn spotless:apply` solo sobre ese archivo.
Nunca bloquea: si falla, solo avisa. Resultado: el código siempre queda formateado sin pedírselo.

**`check-java-style.sh`** — Tras editar código en `src/main/java`, busca con `grep`
`System.out/err`, `printStackTrace()`, `@Autowired` e `import lombok`. Si encuentra algo, devuelve
exit 2 con el listado para que Claude lo corrija (refuerza `.claude/rules/java-style.md`).

**`run-tests-on-stop.sh`** — Cuando Claude va a dar por terminado el turno:
1. Si no hay cambios `.java/.sql/.yml/.xml` sin commitear, no hace nada.
2. Si los hay, ejecuta `mvn -q test`.
3. Si fallan, exit 2 con las últimas 40 líneas: Claude **no puede parar** y debe arreglarlos.
4. Protección anti-bucle: si `stop_hook_active` es `true` (ya está continuando por un bloqueo previo
   de este hook), no insiste.
5. Desactivar puntualmente: `CLAUDE_SKIP_STOP_TESTS=1 claude`.

**`notify.sh`** — Cuando Claude espera un permiso o respuesta, avisa: notificación nativa en macOS y
Linux, pitido en Windows. Nunca falla.

## 5. Ejemplo de flujo completo

Pides: *"añade un endpoint para listar series"*.

1. `SessionStart` → verifica el entorno (una vez).
2. `UserPromptSubmit` → Claude recibe "rama: feature/series, 0 archivos modificados...".
3. Claude va a crear `SeriesController.java` → `PreToolUse` (`protect-files`) comprueba que no sea un
   archivo protegido → permitido.
4. El archivo se crea → `PostToolUse`: `format-java` lo formatea y `check-java-style` revisa que no
   haya `@Autowired`. Si lo hubiera, Claude recibe el aviso y lo corrige.
5. Claude intenta `mvn test -DskipTests` → `PreToolUse` (`block-dangerous`) **lo bloquea**.
6. Claude termina → `Stop` ejecuta `mvn test`. Si hay fallos, sigue trabajando hasta que pasen.

## 6. Cómo probar y depurar un hook

Un hook es un script normal; puedes probarlo a mano simulando el JSON de entrada:

```bash
# Debe bloquear (exit 2) y mostrar el motivo
echo '{"tool_input":{"command":"git push --force"}}' | .claude/hooks/block-dangerous.sh; echo "exit=$?"

# Debe permitir (exit 0)
echo '{"tool_input":{"command":"git status"}}' | .claude/hooks/block-dangerous.sh; echo "exit=$?"
```

Dentro de Claude Code, el comando `/hooks` muestra los hooks cargados.

Problemas típicos:
- **No hace nada:** falta `jq` (mira el aviso de `session-start.sh`) o el script no es ejecutable
  (`chmod +x`).
- **No se dispara:** el `matcher` no coincide con el nombre de la herramienta.
- **Bucle en `Stop`:** olvidar comprobar `stop_hook_active`.
- **Va lento:** un hook en `PostToolUse` se ejecuta tras cada edición; mantenlos rápidos o sube el `timeout`.

## 7. Cómo crear un hook nuevo

1. Crea el script en `.claude/hooks/` (con `#!/usr/bin/env bash`) y hazlo ejecutable.
2. Léete el JSON de stdin con `jq` y decide: `exit 0` (ok) o `exit 2` + mensaje por stderr (bloqueo).
3. Regístralo en `.claude/settings.json` bajo el evento y matcher adecuados.
4. Pruébalo a mano (sección 6) y luego en una sesión real.
5. Apunta lo aprendido en `docs/NOTAS.md`.

> Seguridad: los hooks se ejecutan con tus permisos. Revisa siempre qué hace un hook antes de
> aceptarlo en un repositorio ajeno.
