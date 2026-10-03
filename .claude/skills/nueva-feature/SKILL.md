---
name: nueva-feature
description: Planifica e implementa una funcionalidad nueva en cortes verticales
argument-hint: <descripción de la funcionalidad>
disable-model-invocation: true
---

Quiero implementar esta funcionalidad: $ARGUMENTS

Sigue este proceso:

1. **Diseño**: usa el subagente `spring-architect` para obtener un plan (modelo de datos,
   migración, capas, endpoints, tests y orden de implementación). Muéstrame el plan y las
   preguntas abiertas, y **espera mi aprobación** antes de escribir código.
2. **Implementación**: una vez aprobado, avanza por cortes verticales pequeños. Usa las skills
   `crear-migracion` y `crear-endpoint` cuando correspondan. Ejecuta `mvn test` tras cada corte.
3. **Tests**: si quedan comportamientos sin cubrir, usa el subagente `test-writer`.
4. **Revisión**: al terminar, usa el subagente `code-reviewer` sobre los cambios y corrige los
   hallazgos bloqueantes.
5. **Cierre**: resume lo hecho, los endpoints creados y el resultado final de `mvn test`.
   No hagas commit; propón el mensaje de commit.
