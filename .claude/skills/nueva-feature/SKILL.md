---
name: nueva-feature
description: Planifica e implementa una funcionalidad nueva en cortes verticales
argument-hint: <descripción de la funcionalidad>
---

Quiero implementar esta funcionalidad: $ARGUMENTS

Sigue este proceso:

0. **Rama**: comprueba la rama actual. Si es `main`, o no corresponde a esta funcionalidad,
   crea `feature/<tema>` antes de tocar nada (ver `.claude/rules/git-workflow.md`).
1. **Diseño** (en plan mode): usa el subagente `spring-architect` para obtener un plan (modelo
   de datos, migración, capas, endpoints, tests y orden de implementación). Muéstrame el plan y
   las preguntas abiertas, y **espera mi aprobación** antes de escribir código.
2. **Implementación**: una vez aprobado, avanza por cortes verticales pequeños. Usa las skills
   `crear-migracion` y `crear-endpoint` cuando correspondan. Tras cada corte ejecuta
   `mvn spotless:apply` y `mvn test`, y haz un commit con la skill `commit` (un commit por corte).
3. **Tests**: `crear-endpoint` ya genera los tests básicos. Usa el subagente `test-writer` para
   lo que falte: respuestas 404 y 400, validaciones, y consultas propias del repositorio con
   `@DataJpaTest`. Pásale la lista de archivos creados y el comportamiento esperado.
4. **Revisión**: lanza **en paralelo** (en un mismo mensaje):
   - `code-reviewer`, siempre.
   - `security-auditor`, si la funcionalidad toca entrada de datos, consultas, configuración
     o dependencias.

   A cada agente pásale el plan aprobado y la lista de archivos modificados: parten sin
   contexto y deben revisar contra lo que se pretendía hacer, no solo contra el diff.
   Corrige los hallazgos bloqueantes y vuelve a revisar solo lo corregido. Máximo 2 vueltas:
   si después siguen quedando bloqueantes, para y pregúntame.
5. **Cierre**: resume lo hecho, los endpoints creados, los commits y el resultado final de
   `mvn test`. No hagas push ni abras el PR: eso lo decido yo.
6. **Mini-resumen de piezas de Claude Code**: al terminar la fase, añade una tabla corta para
   ver qué se usó, cuándo y cómo. Solo lo que se usó de verdad en esta fase, sin rellenar:

   | Pieza | Qué | Detalle |
   |---|---|---|
   | Agentes | nombre y modelo (el `model:` de `.claude/agents/<nombre>.md`) | en qué paso y para qué |
   | Skills | `crear-migracion`, `crear-endpoint`, `commit`... | en qué paso |
   | Hooks | script y evento (`PostToolUse`, `PreToolUse`, `Stop`...) | qué hizo (formatear, bloquear, avisar) |

   Para los hooks cita solo los que hayas visto actuar (salida de formato, avisos o bloqueos
   que aparecen en la conversación). Los que corren en silencio no dejan rastro: agrúpalos en
   una línea como "configurados, sin salida visible" en vez de afirmar que se ejecutaron.
   Indica también el modelo de la sesión principal.
