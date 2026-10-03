# Roadmap de aprendizaje de Claude Code

Marca cada fase cuando hayas hecho el ejercicio. En cada una se indica **dónde hay ya un
ejemplo en este repo** para que lo abras, lo entiendas y lo rompas a propósito.

## Fase 1. CLAUDE.md y plan mode
- [ ] Lee `CLAUDE.md` y modifícalo: añade una convención nueva y comprueba que Claude la respeta.
- [ ] Usa `/init` en una carpeta de prueba y compara su resultado con este `CLAUDE.md`.
- [ ] Practica `/clear`, `/compact`, `/context` y `/rewind` en una sesión larga.
- [ ] Prueba el import `@docs/arquitectura.md` (ya está en `CLAUDE.md`) y `CLAUDE.local.md`.

## Fase 2. Rules (`.claude/rules/`)
- [ ] Revisa cuáles tienen `paths:` y cuál es global (`git-workflow.md`).
- [ ] Edita un `Controller` y comprueba que se carga `api-conventions.md`; edita un test y
      comprueba que se carga `testing.md`.
- [ ] Crea una rule nueva (por ejemplo `logging.md`) que resuelva un problema real.

## Fase 3. Permisos y settings
- [ ] Abre `.claude/settings.json` y entiende `allow` y `deny`.
- [ ] Intenta que Claude lea `.env`: debe fallar. Comprueba con `/permissions`.
- [ ] Crea tu `.claude/settings.local.json` con algún permiso personal.

## Fase 4. Slash commands y skills
- [ ] Ejecuta `/nueva-feature` (ver `docs/primera-implementacion.md`).
- [ ] Ejecuta `/revisar-cambios` y mira cómo inyecta salida de comandos con `!`.
- [ ] Observa cuándo Claude carga solo las skills `crear-endpoint` y `crear-migracion`.
- [ ] Crea una skill propia, por ejemplo `añadir-test-de-integracion`, con una plantilla.
- [ ] Diferencia clave: el comando lo invocas tú; la skill la carga Claude si su descripción encaja.

## Fase 5. Subagentes (`.claude/agents/`)
- [ ] Abre `/agents` y lee los tres. Fíjate en `tools:` y `model:` de cada uno.
- [ ] Lanza `code-reviewer` y `test-writer` en una misma tarea y compara sus contextos.
- [ ] Crea un cuarto subagente (por ejemplo `docs-writer` o `security-auditor`).

## Fase 6. Hooks (`.claude/hooks/`)
- [ ] `UserPromptSubmit` → `inject-context.sh`: mira qué contexto añade.
- [ ] `PreToolUse` → `block-dangerous.sh` y `protect-files.sh`: provócalos a propósito.
- [ ] `PostToolUse` → `format-java.sh`: edita un `.java` mal formateado y míralo corregirse.
- [ ] `Stop` → `run-tests-on-stop.sh`: rompe un test y mira cómo Claude se ve obligado a arreglarlo.
- [ ] `Notification` → `notify.sh`: comprueba el aviso cuando espera un permiso.
- [ ] Escribe un hook nuevo (por ejemplo, bloquear `System.out.println` en código de producción).
- [ ] Depura con `claude --debug` y `/hooks`.

## Fase 7. MCP
- [ ] Conecta GitHub: `claude mcp add --transport http github https://api.githubcopilot.com/mcp/`
      y autoriza desde `/mcp`. Pídele que liste los issues abiertos.
- [ ] Conecta un MCP de base de datos de solo lectura contra el PostgreSQL local y pídele
      consultas sobre tu colección.
- [ ] Anota en `docs/NOTAS.md` qué aporta un MCP frente a un comando de shell.

## Fase 8. Automatización
- [ ] Headless: `scripts/review.sh` (revisión del diff) y variantes con `--output-format json`.
- [ ] Worktrees: `scripts/worktree.sh` para dos funcionalidades en paralelo.
- [ ] GitHub Actions: ejecuta `/install-github-app`, añade el secreto y abre un PR con un bug
      a propósito para ver la revisión de `.github/workflows/claude.yml`.
- [ ] Opcional: Claude Agent SDK para montar tu propio agente.
- [ ] Opcional: empaquetar rules, agentes, skills y hooks como un **plugin** reutilizable.

## Cómo sacarle partido
- Haz que cada fase resuelva un dolor real del proyecto (si los tests se olvidan, hook; si se
  rompen las convenciones, rules).
- Apunta en `docs/NOTAS.md` qué funcionó y qué no: es tu material para contenido técnico.
