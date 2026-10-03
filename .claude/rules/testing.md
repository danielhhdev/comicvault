---
paths:
  - "src/test/**/*.java"
---

# Estándares de testing

- JUnit 5 + AssertJ (`assertThat`), nada de aserciones de JUnit clásicas.
- Nombre de la clase: `<Clase>Test`. Nombre del método: describe el comportamiento, por ejemplo
  `devuelve404CuandoElComicNoExiste`. Estructura given / when / then.
- Pirámide de tests:
  - **Unitarios** (servicios): Mockito, sin contexto de Spring. Son la mayoría.
  - **Slice web**: `@WebMvcTest` + `MockMvc` para controladores, con `@MockitoBean` para el servicio.
  - **Slice JPA**: `@DataJpaTest` para repositorios y consultas.
  - **Integración completa**: `@SpringBootTest`, muy pocos.
- En Spring Boot 4 se usa `@MockitoBean` (no `@MockBean`, que ya no existe). Si dudas del
  paquete de una anotación de test, copia los imports de un test existente.
- Los tests corren con el perfil `test` (H2 en modo PostgreSQL); no dependen de Docker.
- Un test cubre un comportamiento. Incluye siempre el camino de error (404, validación, conflicto).
- Prohibido `Thread.sleep`, dependencias entre tests o datos compartidos entre métodos.
- No se comenta ni se desactiva (`@Disabled`) un test para que pase el build.
- Antes de dar una tarea por terminada, `mvn test` debe estar en verde.
