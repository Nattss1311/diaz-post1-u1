# diaz-post1-u1
Post-contenido — Refactorización SOLID y análisis de patrones GoF en Spring

## Descripción
Repositorio del post-contenido de la Unidad 1 de Patrones de Diseño de Software — Sexto Semestre. Contiene dos partes: refactorización SOLID de un God Object (`parte-1-refactorizacion-solid/`) y análisis de patrones GoF en Spring Framework (`parte-2-analisis-gof-spring/`).

---

## Parte 1 — Refactorización SOLID
Proyecto Maven que refactoriza la clase monolítica `OrderProcessor` aplicando los principios SOLID (SRP, OCP, ISP y DIP) para desacoplar responsabilidades como almacenamiento, notificaciones, reportes y cálculo de impuestos/descuentos. Ver la implementación completa en `parte-1-refactorizacion-solid/` [parte-1-refactorizacion-solid/](./parte-1-refactorizacion-solid/) .


## Análisis de Violaciones SOLID

| Principio | Método/Sección afectada | Descripción de la violación |
|-----------|-------------------------|-----------------------------|
| SRP       | calculateTotal + applyDiscount + saveOrder + sendEmail + printReport | La clase `OrderProcessor` viola el principio SRP al encargarse de cinco tareas completamente distintas: cálculo financiero (calculateTotal), políticas de descuento (applyDiscount), persistencia de datos (saveOrder), notificación al cliente (sendEmail) y presentación de reportes (printReport). Esto provoca que la clase cambie por razones muy variadas al concentrar responsabilidades ajenas, por lo que la solución es delegar cada función a clases independientes. |
| OCP | applyDiscount | El método `applyDiscount` viola el principio OCP al usar estructuras condicionales if para validar las categorías de cliente. Si en el futuro se requiere agregar un nuevo descuento (como 'ESTUDIANTE'), es obligatorio modificar directamente el código interno del método. Para cumplir con OCP, el cálculo de descuentos debe delegarse a clases independientes mediante una interfaz, lo que permitirá incorporar nuevas reglas de negocio en archivos separados sin alterar el código existente. |
| LSP | Toda la clase (ausencia de jerarquía) | No aplica directamente en este punto. El principio LSP evalúa el comportamiento de clases hijas dentro de jerarquías de herencia y, al estar todo implementado dentro de una sola clase concreta sin extender ni implementar contratos, no existen subtipos que puedan romper la sustitución. |
| DIP | Toda la clase (dependencias internas sin abstracciones) | La clase viola el principio DIP porque se conecta directamente a componentes concretos: guarda datos en una lista local en memoria (orders) y simula notificaciones imprimiendo por consola (`System.out.println`). Esto genera un acoplamiento rígido entre la lógica de negocio y los detalles técnicos. Para cumplir con DIP, la clase debe depender de abstracciones (interfaces como OrderRepository o NotificationService), lo que permitirá cambiar la base de datos o el servicio de correo sin modificar la lógica del procesamiento de órdenes. |

---

## Parte 2 — Análisis de Patrones GoF en Spring

| # | Patrón | Categoría | Clase en Spring |
|---|--------|-----------|-----------------|
| 1 | Prototype | Creacional | `AbstractBeanFactory` |
| 2 | Proxy | Estructural | `ProxyFactoryBean` |
| 3 | Chain of Responsibility | Comportamiento | `HandlerExecutionChain` |

Ver el análisis detallado y los fragmentos de código fuente en `parte-2-analisis-gof-spring/documento-analisis.md`. [parte-2-analisis-gof-spring/documento-analisis.md](./parte-2-analisis-gof-spring/documento-analisis.md).

---

## Herramientas utilizadas
- Java 17, Apache Maven, VS Code, Git, GitHub
- Código fuente de Spring Framework (investigación)

## Conclusiones

La refactorización de `OrderProcessor` y el análisis de Spring Framework demuestran que los principios SOLID y los patrones GoF comparten el mismo objetivo: separar responsabilidades, permitir extensiones sin modificar el código base y depender de abstracciones. Spring refleja esta filosofía al usar Prototype para crear instancias independientes, Proxy para agregar comportamientos como seguridad o transacciones, y Chain of Responsibility para ejecutar interceptores de manera desacoplada. Aprender a identificar estas estructuras en frameworks reales y aplicarlas en proyectos propios es lo que permite pasar de escribir código que solo funciona a diseñar sistemas verdaderamente mantenibles. Aunque una arquitectura limpia requiere más tiempo al principio, esa inversión se traduce rápidamente en un software más fácil de probar, escalar y evolucionar con el tiempo.