---
paths:
  - "src/main/java/**/*.java"
---

# Estilo de código Java

- Java 21: usa `record` para DTOs y valores inmutables, `switch` con patrones, `var` solo
  cuando el tipo es obvio y bloques de texto para SQL/JSON largos.
- Sin Lombok. Getters/setters explícitos solo donde JPA los necesite.
- Inyección **por constructor** (un único constructor, sin `@Autowired`). Dependencias `final`.
- Nunca devuelvas `null` desde un método público: usa `Optional` (solo como retorno, no como
  parámetro ni campo) o lanza una excepción de dominio.
- Nombres en inglés para clases, métodos y variables; mensajes de error y logs en español.
- Una clase pública por archivo; clases pequeñas y con una responsabilidad.
- Logging con SLF4J (`private static final Logger log`), nunca `System.out`. No registres
  datos personales ni secretos.
- Sin comentarios que repitan el código; comenta el "por qué", no el "qué".
- El formato lo impone Spotless: no pelees con él, ejecuta `mvn spotless:apply`.
