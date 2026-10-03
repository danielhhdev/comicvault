# ComicVault

API REST para gestionar una colección de cómics. Es un proyecto de aprendizaje de **Claude Code**:
trae ya montadas todas las piezas (CLAUDE.md, rules, permisos, comandos, skills, subagentes,
hooks y automatización) para que puedas practicarlas sobre una app real.

**Stack:** Java 21 · Spring Boot 4.1 · Maven · PostgreSQL (Docker Compose) · Flyway · Spotless

## Requisitos

| Herramienta | Para qué |
|---|---|
| JDK 21 y Maven 3.9+ | compilar y ejecutar |
| Docker (con Compose) | PostgreSQL en local |
| `jq` | lo usan los hooks de `.claude/hooks/` |
| Git | control de versiones y worktrees |
| Claude Code | `npm install -g @anthropic-ai/claude-code` o el instalador oficial |

## Puesta en marcha

```bash
cd comicvault
git init -b main && git add . && git commit -m "chore: esqueleto inicial"

mvn verify               # compila, pasa los tests (H2) y empaqueta
mvn spring-boot:run      # arranca PostgreSQL con compose.yaml y la API en :8080
curl localhost:8080/actuator/health
```

## Empezar con Claude Code

```bash
claude                   # desde la raíz del proyecto
```

Dentro de la sesión, comprueba que todo se ha cargado:

| Comando | Qué deberías ver |
|---|---|
| `/memory` | `CLAUDE.md` y las rules de `.claude/rules/` |
| `/agents` | `code-reviewer`, `test-writer`, `spring-architect` |
| `/hooks` | los hooks de `.claude/settings.json` |
| `/permissions` | las reglas allow/deny |
| `/help` | las skills `/nueva-feature`, `/revisar-cambios`, `/commit`, `/crear-endpoint` y `/crear-migracion` |

## Estructura

```
CLAUDE.md                 instrucciones del proyecto
.claude/
  settings.json           permisos y hooks (compartido con el equipo)
  rules/                  reglas modulares (algunas limitadas por rutas)
  agents/                 subagentes: code-reviewer, test-writer, spring-architect
  skills/                 crear-endpoint, crear-migracion, commit, nueva-feature, revisar-cambios
  hooks/                  scripts: bloqueo, protección, formato, tests, contexto, notificación
scripts/                  review.sh (headless) y worktree.sh (sesiones en paralelo)
.github/workflows/        ci.yml y claude.yml (revisión de PRs y @claude)
docs/                     arquitectura y notas
src/                      código de la aplicación
```

## Configuración personal

Lo que sea solo tuyo va en `.claude/settings.local.json` y `CLAUDE.local.md`; ambos están en
`.gitignore`.
