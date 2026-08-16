# Análisis de Patrones de Diseño GoF y Principios SOLID en el Código Fuente de Spring Framework

---

**Universidad de Santander (UDES)**  
**Programa:** Ingeniería de Sistemas  
**Asignatura:** Patrones de Diseño de Software  
**Unidad:** Unidad 1 — Post Contenido  

> **Datos del Estudiante:**  
> * **Nombre:** Natalia Díaz Villamizar  
> * **Código:** 02240131001  
> * **Fecha de entrega:** 16 de Agosto de 2024  

---

<div style="page-break-after: always;"></div>

## Introducción

Spring Framework ha consolidado su posición como uno de los frameworks más influyentes en el ecosistema Java, no solo por su funcionalidad, sino por la calidad arquitectónica de su implementación (Fowler, 2002). Su código fuente es un repositorio vivo de patrones de diseño Gang of Four (GoF), demostrando cómo los principios SOLID y las mejores prácticas pueden aplicarse en proyectos de escala empresarial (Martin, 2009).

Este documento presenta un análisis detallado de tres patrones de diseño identificados en el código fuente de Spring Framework:
* **Patrón Prototype** (Creacional)
* **Patrón Proxy** (Estructural)
* **Patrón Chain of Responsibility** (Comportamiento)

El propósito de este análisis es examinar cómo Spring Framework implementa estos patrones para resolver problemas específicos de diseño, entender la motivación arquitectónica detrás de cada decisión y reconocer cómo los principios SOLID se reflejan en la práctica (Spring Framework Contributors, 2024a). A través de la investigación del repositorio oficial en GitHub y la revisión de código fuente concreto, se explora la conexión entre teoría y aplicación, proporcionando lecciones valiosas para el desarrollo de software bien estructurado.

---

<div style="page-break-after: always;"></div>

## 1. Análisis de Patrón: Prototype (Creacional)

### Definición y Categoría GoF
El patrón **Prototype** es un patrón de creación de objetos que pertenece a la categoría **Creacional** del catálogo Gang of Four. Su propósito es crear nuevos objetos mediante la clonación de una instancia prototipo existente, en lugar de crear instancias desde cero.

Este enfoque es especialmente útil cuando:
* La creación de un objeto es costosa computacionalmente.
* Se requieren múltiples variaciones de un objeto.
* La configuración de un objeto es compleja.

En términos generales, el patrón Prototype desacopla el código cliente de la lógica específica de creación de objetos, permitiendo flexibilidad en la generación de nuevas instancias.

### Ubicación en Spring Framework
En Spring Framework, el patrón Prototype se implementa a través del scope de bean `"prototype"`.

* **Clase responsable:** `org.springframework.beans.factory.support.AbstractBeanFactory`
* **Módulo:** `spring-beans`

Esta clase abstracta es el corazón del contenedor de inyección de dependencias de Spring y gestiona el ciclo de vida de los beans según su configuración de scope.

### Problema que Resuelve
En aplicaciones complejas, a menudo se requieren múltiples instancias independientes de un objeto durante su ciclo de vida. Si Spring entregara siempre la misma instancia (comportamiento Singleton, que es el default), los cambios de estado realizados por un componente afectarían a todos los demás que utilicen ese bean.

Esto es problemático para objetos que mantienen estado mutable, como controladores de sesión, manejadores de peticiones o servicios que procesan datos específicos del contexto. El patrón Prototype resuelve este problema permitiendo que cada solicitud de un bean configurado con scope `"prototype"` genere una nueva instancia completamente independiente, garantizando así el **aislamiento de estado** entre consumidores.

En un escenario contrafactual, si Spring no implementara el patrón Prototype y forzara el uso de Singletons, se generarían condiciones de carrera (race conditions) y corrupción de datos cuando múltiples hilos intentaran modificar el estado interno de una misma instancia compartida.



### Evidencia de Código
En la clase ``AbstractBeanFactory` (líneas 359-370), se encuentra la siguiente implementación:

```java
else if (mbd.isPrototype()) { 
    // It's a prototype -> create a new instance.
    Object prototypeInstance = null;
    try {
        beforePrototypeCreation(beanName);
        prototypeInstance = createBean(beanName, mbd, args);
    }
    finally {
        afterPrototypeCreation(beanName);
    }
    beanInstance = getObjectForBeanInstance(prototypeInstance, requiredType, name, beanName, mbd);
}
```
Este fragmento es la evidencia clave: cuando Spring evalúa que el bean tiene scope "prototype" (mbd.isPrototype()), no utiliza ninguna copia en caché. En su lugar, llama directamente a createBean(), fabricando una instancia completamente nueva con cada petición. Los métodos beforePrototypeCreation() y afterPrototypeCreation() son hooks que permiten a Spring ejecutar lógica adicional antes y después de la creación, manteniendo la coherencia del contenedor.

### Principio SOLID Reforzado

El patrón Prototype, tal como se implementa en Spring, refuerza principalmente:


* **Principio de Responsabilidad Única (SRP):** La clase `AbstractBeanFactory` delega la responsabilidad de crear instancias nuevas a un método especializado `(createBean())`, en lugar de mantener directamente esa lógica. Esto permite que la responsabilidad de "decidir si un bean es singleton o prototype" esté separada de la responsabilidad de "cómo crear una instancia".

* **Principio de Inversión de Dependencias (DIP):** Al permitir que los objetos cliente soliciten nuevas instancias sin conocer los detalles internos de creación, Spring implementa el Principio de Inversión de Dependencias (DIP), donde los clientes dependen de la abstracción `(BeanFactory)` en lugar de implementaciones concretas.

<div style="page-break-after: always;"></div>

## 2. Análisis de Patrón: Proxy (Estructural)

### Definición y Categoría GoF
El patrón **Proxy** es un patrón de estructura que pertenece a la categoría **Estructural** del catálogo Gang of Four. Su objetivo es proporcionar un sustituto o marcador de posición para otro objeto, permitiendo controlar el acceso a él.

El proxy actúa como intermediario entre el cliente y el objeto real, interceptando llamadas para ejecutar lógica adicional como validación, caché, logging, control de acceso o modificación de comportamiento, sin que el cliente sea consciente de estos detalles. El patrón Proxy es fundamental en arquitecturas que requieren separación de *concerns* y aplicación transversal de comportamientos.

### Ubicación en Spring Framework
En Spring Framework, el patrón Proxy se implementa de manera sofisticada a través del módulo **Spring AOP** *(Aspect-Oriented Programming)*.

* **Clase responsable:** `org.springframework.aop.framework.ProxyFactoryBean`
* **Módulo:** `spring-aop`

Esta clase es una *factory* que crea dinámicamente objetos proxy que envuelven un objeto objetivo (*target object*) y aplican aspectos (*advice*) a sus métodos. Internamente, Spring utiliza `JdkDynamicAopProxy` y `CglibAopProxy` como implementaciones de proxy específicas, dependiendo del tipo de objeto a proxificar.

### Problema que Resuelve
En aplicaciones empresariales, es común necesitar aplicar comportamientos transversales (*cross-cutting concerns*) como transacciones de base de datos, autenticación, autorización, logging o caché a múltiples métodos sin modificar el código del objeto original. 

Si estos comportamientos se implementaran directamente en cada método de cada servicio, resultaría en duplicación masiva de código, acoplamiento innecesario y dificultad para mantener la lógica centralizada. El patrón Proxy, implementado a través de Spring AOP, resuelve este problema permitiendo que se "enganchen" comportamientos adicionales de forma declarativa (`@Transactional`, `@Cacheable`, `@Secured`, etc.) sin alterar la clase original. El proxy intercepta las llamadas, ejecuta el *advice* (comportamiento adicional) y delega la invocación real al objeto objetivo.

Si Spring AOP no utilizara el patrón Proxy, la lógica de infraestructura (transacciones, seguridad o caché) tendría que acoplarse e invocarse manualmente dentro de cada clase de negocio, violando el principio SRP y duplicando código en toda la aplicación.


### Evidencia de Código
En la clase `ProxyFactoryBean` (líneas 236-256), se encuentra la siguiente implementación del método `getObject()`:

```java
@Override 
public @Nullable Object getObject() throws BeansException { 
    initializeAdvisorChain(); 
    if (isSingleton()) { 
        return getSingletonInstance(); 
    } 
    else { 
        if (this.targetName == null) { 
            logger.info("Using non-singleton proxies with singleton targets " + 
                        "is often undesirable. Enable prototype proxies by " + 
                        "setting the 'targetName' property."); 
        } 
        return newPrototypeInstance(); 
    } 
}
```

El método `getObject()` es invocado por Spring cuando un cliente solicita un bean de tipo *ProxyFactoryBean*. Como se observa en el código, este método inicializa primero la cadena de advisors `(initializeAdvisorChain())`, que contiene los comportamientos adicionales (advice) a aplicar. Luego, determina si debe devolver una instancia singleton del proxy o crear una nueva instancia (prototipo). En ambos casos, el resultado es un objeto proxy que envuelve el target object. Cuando el cliente invoca métodos en este proxy, el proxy intercepta las llamadas, aplica los advisors (transacciones, seguridad, etc.) y finalmente delega la ejecución real al objeto objetivo. El código comentado en la salida de log revela la intención arquitectónica: advertir a los desarrolladores sobre configuraciones que pueden causar comportamientos inesperados.
### Principio SOLID Reforzado
El patrón Proxy en Spring AOP refuerza principalmente los siguientes principios:

1. **Principio Abierto/Cerrado (OCP):** A través de los proxies, es posible añadir nuevas funcionalidades (como caché o logging) sin modificar el código de la clase original, manteniéndola "cerrada para modificación pero abierta para extensión" (Martin, 2009).
2. **Principio de Responsabilidad Única (SRP):** Separa la responsabilidad de "ejecutar la lógica de negocio" de la responsabilidad de "aplicar comportamientos transversales". El proxy asume una responsabilidad clara: interceptar y delegar, mientras que el objeto objetivo se enfoca únicamente en su lógica de negocio.
3. **Separación de Intereses (Separation of Concerns):** Desacopla la lógica de infraestructura y aspectos transversales de la lógica de dominio principal de la aplicación.

---



## 3. Análisis de Patrón: Chain of Responsibility (Comportamiento)

### Definición y Categoría GoF
El patrón **Chain of Responsibility** es un patrón de comportamiento que pertenece a la categoría Comportamiento del catálogo Gang of Four (Gamma et al., 1994).. Su propósito es construir una cadena de objetos manejadores (*handlers*) de manera que una solicitud pueda pasarse a lo largo de la cadena.

Cada manejador decide si procesa la solicitud o si la pasa al siguiente manejador en la cadena. Este patrón desacopla el remitente de una solicitud de sus receptores, permitiendo que múltiples objetos tengan la oportunidad de manejar la solicitud sin que el cliente conozca la estructura interna de la cadena.

### Ubicación en Spring Framework
En Spring Framework, el patrón Chain of Responsibility se implementa de manera prominente en el módulo **Spring Web MVC**.

* **Clase responsable:** `org.springframework.web.servlet.HandlerExecutionChain`
* **Módulo:** `spring-webmvc`

Esta clase gestiona una cadena de interceptores `(HandlerInterceptor)` que procesan las solicitudes HTTP antes de que lleguen al controlador (handler) y después de que el controlador retorna su respuesta. Adicionalmente, Spring Security implementa una cadena similar a través de FilterChain (Spring Boot Contributors, 2024).

### Problema que Resuelve
En aplicaciones web, es necesario ejecutar múltiples procesadores de solicitud en un orden específico: verificar autenticación, validar autorización, loguear la solicitud, aplicar transformaciones, etc. 

Si esta lógica se implementara dentro del controlador o en un único manejador centralizado, resultaría en un código fuertemente acoplado, difícil de mantener y poco flexible. El patrón Chain of Responsibility, implementado a través de `HandlerExecutionChain` en Spring MVC, resuelve este problema permitiendo que cada interceptor (como un filtro de autenticación, un logger, un validador, etc.) sea un eslabón independiente en la cadena. 

Cuando llega una solicitud, pasa a través de la cadena de forma ordenada: cada interceptor puede realizar su lógica, permitir que la solicitud continúe o detenerla si algo falla (por ejemplo, autenticación rechazada). Este enfoque es altamente flexible y escalable: nuevos interceptores pueden añadirse sin modificar los existentes.

De no emplearse el patrón Chain of Responsibility, Spring MVC tendría que concentrar todas las validaciones previas y posteriores a una solicitud dentro de un único bloque rígido o dentro de cada controlador, imposibilitando la adición, remoción o reordenamiento dinámico de filtros de seguridad e interceptores.


### Evidencia de Código
En la clase `HandlerExecutionChain` (líneas 136-152), se encuentra la siguiente implementación del método `applyPreHandle()`:

```java
boolean applyPreHandle(HttpServletRequest request, HttpServletResponse response) throws Exception { 
    for (int i = 0; i < this.interceptorList.size(); i++) { 
        HandlerInterceptor interceptor = this.interceptorList.get(i);
        if (!interceptor.preHandle(request, response, this.handler)) { 
            triggerAfterCompletion(request, response, null);
            return false; 
        } 
        this.interceptorIndex = i; 
    } 
    return true; 
}
```
Este fragmento es la evidencia central del patrón: Spring mantiene una lista de interceptores `(interceptorList)`. El método `applyPreHandle()` itera sobre esta lista de forma ordenada. Para cada interceptor, invoca el método `preHandle()`, que ejecuta la lógica del interceptor y retorna un booleano indicando si la solicitud debe continuar (true) o detenerse (false). Si algún interceptor retorna false (por ejemplo, porque la autenticación falló), se invoca triggerAfterCompletion() para ejecutar lógica de limpieza y se retorna false, deteniendo la cadena. De lo contrario, se continúa con el siguiente interceptor. Este mecanismo es el corazón del patrón *Chain of Responsibility en Spring:* una solicitud viaja a través de una cadena de manejadores, y cualquier manejador puede interrumpir la cadena si lo considera necesario.

### Principio SOLID Reforzado
El patrón Chain of Responsibility en Spring refuerza principalmente los siguientes principios:

1. **Principio de Responsabilidad Única (SRP):** Cada interceptor es responsable de una única tarea bien definida (validación de seguridad, logging, transformación de datos, etc.), sin necesidad de conocer los detalles de otros interceptores ni de la cadena completa.
2. **Principio Abierto/Cerrado (OCP):** La arquitectura está abierta a extensión, permitiendo añadir nuevos interceptores sin modificar los existentes ni alterar la clase `HandlerExecutionChain`.
3. **Principio de Inversión de Dependencias (DIP):** `HandlerExecutionChain` depende de la interfaz HandlerInterceptor (una abstracción) y no de implementaciones concretas específicas (Martin, 2009).

## Conclusiones

A través de este análisis, se ha demostrado que Spring Framework no es solo una herramienta funcional, sino un ejemplo sobresaliente de arquitectura orientada a objetos solidificada en código. Los tres patrones investigados —**Prototype**, **Proxy** y **Chain of Responsibility**— no existen de manera aislada; forman parte de una visión cohesiva de cómo construir sistemas flexibles, mantenibles y escalables:

* **Patrón Prototype:** Permite que Spring cree instancias independientes de beans evitando conflictos de estado.
* **Patrón Proxy:** A través de AOP, posibilita la aplicación de comportamientos transversales sin contaminar la lógica de negocio.
* **Patrón Chain of Responsibility:** Estructura el procesamiento de solicitudes HTTP de manera ordenada y segura, permitiendo que múltiples filtros colaboren sin acoplamiento innecesario.

La implementación de estos patrones en Spring está profundamente alineada con los principios **SOLID**, lo que se refleja en el éxito y adopción del framework. Para estudiantes e ingenieros que desean mejorar sus habilidades de diseño, el código fuente de Spring Framework es una cantera de aprendizaje invaluable. 

No basta con entender los patrones de forma abstracta; es esencial estudiar cómo frameworks probados en producción los aplican para resolver problemas reales. Esta investigación ilustra la conexión vital entre teoría arquitectónica y práctica de ingeniería, proporcionando una base sólida para tomar decisiones de diseño más informadas en proyectos futuros.

---

## Referencias

Fowler, M. (2002). *Patterns of enterprise application architecture*. Addison-Wesley Professional.

Gamma, E., Helm, R., Johnson, R., & Vlissides, J. (1994). *Design patterns: Elements of reusable object-oriented software*. Addison-Wesley.

Martin, R. C. (2009). *Clean code: A handbook of agile software craftsmanship*. Prentice Hall.

Refactoring.Guru. (2024). *Design patterns*. Recuperado de https://refactoring.guru/design-patterns

Spring Boot Contributors. (2024). *Spring boot reference documentation*. Recuperado de https://docs.spring.io/spring-boot/reference/

Spring Framework Contributors. (2024). *Spring framework reference documentation*. Recuperado de https://docs.spring.io/spring-framework/reference/

Spring Framework Contributors. (2024). *Spring framework source code repository*. Recuperado de https://github.com/spring-projects/spring-framework
