# Sistema de Gestión de Inventario

## 1. Descripción General
Este proyecto desarrolla un sistema de gestión de inventario orientado a controlar insumos, lotes, stock, mermas, ventas y reposición mediante una evolución incremental por sprints.

El trabajo se enmarca en la asignatura **Proceso de Software y Calidad 2025-26**, cumpliendo los requisitos académicos de arquitectura por capas, metodología SCRUM, planificación iterativa y gestión formal del Product Backlog.

El objetivo del sistema es disponer de una solución cliente-servidor que permita:
- Operación base del inventario con control de acceso por roles.
- Automatización del descuento de stock a partir de ventas y recetas, incluyendo FEFO.
- Soporte a decisiones mediante alertas, sugerencias de compra, forecast y reportes exportables.

## 2. Arquitectura del Sistema
La solución se plantea con arquitectura **cliente-servidor** separando responsabilidades funcionales y técnicas.

### Capa de datos
Responsable de la persistencia de entidades clave del dominio de inventario (insumos, lotes, movimientos, ventas, mermas, etc.).

### Servidor (API REST)
Implementa la lógica de negocio y expone servicios REST para operación, validación de reglas y automatizaciones del dominio.

### Cliente
Interfaz de usuario (web o escritorio) para consumo de la API, ejecución de flujos operativos y visualización de resultados.

### Justificación tecnológica (Java + Spring Boot)
La elección de **Java + Spring Boot** responde a:
- Adecuación al modelo API REST requerido.
- Soporte sólido para separación por capas y mantenibilidad.
- Ecosistema maduro para validación, seguridad, pruebas e integración.

## 3. Metodología de Trabajo
El proyecto se gestiona con **SCRUM**, aplicando planificación iterativa, revisión incremental y mejora continua.

### Roles definidos
- **Product Owner:** responsable de la visión de producto, priorización y gestión del backlog. Durante la planificación inicial fue asumido por el autor del trabajo; la gestión se indica a cargo de Pablo Romero.
- **Scrum Master:** facilitación de ceremonias, seguimiento y gestión de bloqueos; rol rotado entre miembros del equipo.
- **Developers:** implementación técnica y aseguramiento de calidad del incremento.

### Gestión de trabajo con GitHub Projects
Se utiliza **GitHub Projects** como soporte operativo de SCRUM para organizar historias, seguimiento por sprint y trazabilidad de prioridades.

### Organización temporal
- 3 sprints de aproximadamente 12 días.
- Referencia de trabajo con **Ideal Day ~1 h/día por persona** dentro de la capacidad académica definida.

## 4. Planificación de Sprints
| Sprint | Fechas | Objetivo | Historias incluidas | Criterios de validación |
|---|---|---|---|---|---|
| Sprint 1 | 9-25 marzo (review 26 marzo) | Inventario base operable con acceso por roles | CORE-01 a CORE-08, ADM-02 | Alta de insumos, registro de lote, consulta de stock, registro de merma con login |
| Sprint 2 | 13-28 abril (review 29 abril) | Registro de ventas y descuento automático de inventario + alertas | CORE-09 a CORE-13, INT-01, INT-02 | Venta con descuento por receta/FEFO y alertas de stock/caducidad |
| Sprint 3 | 4-18 mayo (review 19 mayo) | Reposición automática, pronóstico y reportes exportables | INT-03 a INT-05, ADM-01, ADM-03, REP-01 a REP-04 | Sugerencia de compra, forecast SES, reportes y exportación CSV |

## 5. Product Backlog
El Product Backlog se estructura mediante historias de usuario con identificador funcional y criterios de negocio.

### Formato de historias
Cada historia incluye al menos:
- Identificador (por ejemplo, `CORE-01`, `INT-03`, `ADM-02`, `REP-01`).
- Descripción funcional.
- Prioridad (`Alta`, `Media`, `Baja`).
- Estimación en horas.

### Priorización (PB Priority)
La priorización se aplica para maximizar valor y reducir riesgo técnico:
- Primero capacidades troncales de inventario.
- Después automatización operativa (ventas, alertas).
- Finalmente capacidades analíticas y de soporte a decisión.

### Organización por etiquetas
- `CORE`: funcionalidades nucleares del inventario.
- `INT`: automatizaciones e inteligencia operativa.
- `ADM`: capacidades administrativas y de gestión.
- `REP`: reporting y exportación.

## 6. Gestión del Tiempo
La dedicación total prevista es de **50 h por persona**.

| Bloque | Horas por persona |
|---|---:|
| Definición de alcance y backlog inicial | 2,5 h |
| Sprint Planning (1,5 h × 3) | 4,5 h |
| Retrospectivas (0,5 h × 3) | 1,5 h |
| Reuniones con profesor (inicial + 3 reviews) | 1,5 h |
| Desarrollo | 40 h |
| **Total** | **50 h** |

### Justificación de capacidad por sprint
- La capacidad de desarrollo se distribuye en tres iteraciones: ~13,33 h por sprint por persona.
- El backlog estimado suma ~80 h de equipo.
- Con 3 personas, la carga estimada de desarrollo por persona ronda 27 h, dejando margen para integración, pruebas, documentación e incidencias dentro de las 40 h disponibles de desarrollo.

### Relación entre estimación y planificación
La planificación mantiene coherencia entre esfuerzo estimado y capacidad real, reservando margen explícito para tareas transversales y riesgo técnico, de acuerdo con el contexto académico.

## 7. Instalación y Ejecución
### Requisitos
- Java (versión compatible con Spring Boot del proyecto).
- Herramienta de construcción Java (según configuración final del backend).
- Entorno de ejecución para cliente web o escritorio (según implementación final).

### Backend
En el estado actual del repositorio no se han publicado todavía los artefactos de implementación del servidor ni su configuración ejecutable.

### Cliente
En el estado actual del repositorio no se han publicado todavía los artefactos de implementación del cliente ni sus scripts de ejecución.

## 8. Organización del Repositorio
Estado actual detectado del repositorio:

| Ruta | Contenido |
|---|---|
| `planificacion.md` | Documento de planificación Scrum, esfuerzo y organización de sprints |
| `README.md` | Documentación general del proyecto |

### Ubicación de backend y frontend
A la fecha de esta versión del README no existen aún carpetas de código para backend/frontend en el repositorio público. Su ubicación se definirá al incorporar la implementación de los incrementos.

### Configuración relevante
La configuración operativa detallada (variables de entorno, perfiles, puertos, base de datos, build y despliegue) se añadirá junto con el código fuente en los sprints correspondientes.

## 9. Estado Actual del Proyecto
### Implementación por sprint
- **Sprint 1:** definido a nivel de planificación funcional (inventario base y acceso por roles).
- **Sprint 2:** definido a nivel de planificación funcional (ventas, descuento automático, alertas).
- **Sprint 3:** definido a nivel de planificación funcional (reposición, forecast, reportes y CSV).

### Situación actual del repositorio
El repositorio contiene actualmente documentación de planificación; la implementación técnica no está aún incorporada en esta versión.

### Próximas mejoras
- Incorporar estructura base de arquitectura cliente-servidor.
- Publicar backend API REST en Spring Boot con modelo de datos inicial.
- Publicar cliente y flujos de validación por sprint.
- Añadir guía de ejecución y despliegue cuando exista código funcional.
