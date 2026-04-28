# CoffeeStack — Sistema de Gestión de Inventario

Sistema de gestión de inventario para cafeterías, desarrollado como proyecto académico de la asignatura **Proceso de Software y Calidad 2025-26 (Universidad de Deusto)**.

Permite controlar insumos, lotes, stock, mermas y roturas mediante una API REST con autenticación JWT y control de acceso por roles.

## Stack tecnológico

- **Java 21** + **Spring Boot 3.2.5**
- **Spring Security** + JWT (JJWT 0.12.3)
- **Spring Data JPA** + Hibernate + **Flyway** (migraciones)
- **MySQL** (producción/dev) · **H2** (local/tests)
- **SpringDoc OpenAPI** (Swagger UI en `/swagger-ui/index.html`)

## Compilación y ejecución

### Requisitos previos

- Java 21+
- No es necesario instalar Gradle; el proyecto incluye el wrapper `./gradlew`

### Compilar

```bash
./gradlew build
```

### Ejecutar en local (H2 en memoria, sin base de datos externa)

```bash
./gradlew bootRun
```

La aplicación arranca en `http://localhost:8080`.
Consola H2 disponible en `http://localhost:8080/h2-console`.

### Ejecutar con perfil dev (MySQL local)

```bash
./gradlew bootRun --args='--spring.profiles.active=dev'
```

Variables de entorno necesarias (o usa los valores por defecto):

| Variable | Default |
|----------|---------|
| `DB_USER` | `root` |
| `DB_PASS` | `password` |

La base de datos debe existir: `coffeestack` en `localhost:3306`.

### Ejecutar en producción

```bash
java -jar build/libs/coffeestack-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod
```

Variables de entorno requeridas: `DB_URL`, `DB_USER`, `DB_PASS`.

### Ejecutar los tests

```bash
./gradlew test
```

Los tests usan H2 en memoria automáticamente (perfil `test`).

## Roles del sistema

| Rol | Permisos |
|-----|----------|
| `EMPLEADO` | Consultar stock, registrar lotes |
| `PROPIETARIO` | Todo lo anterior + gestionar insumos, registrar ajustes/mermas |
| `ROOT` | Acceso total, gestión de usuarios |

## API REST

Documentación interactiva completa en `http://localhost:8080/swagger-ui/index.html`.

Todas las rutas protegidas requieren cabecera `Authorization: Bearer <token>`.

### Auth — `/api/auth`

| Método | Ruta | Acceso | Descripción |
|--------|------|--------|-------------|
| `POST` | `/api/auth/login` | Público | Obtener token JWT |
| `POST` | `/api/auth/register` | Público | Registrar nuevo usuario |
| `GET` | `/api/auth/me` | Autenticado | Datos del usuario actual |

### Insumos — `/api/insumos`

| Método | Ruta | Acceso | Descripción |
|--------|------|--------|-------------|
| `GET` | `/api/insumos` | Autenticado | Listar insumos (paginado) |
| `GET` | `/api/insumos/{id}` | Autenticado | Detalle de un insumo |
| `POST` | `/api/insumos` | PROPIETARIO/ROOT | Crear insumo |
| `PUT` | `/api/insumos/{id}` | PROPIETARIO/ROOT | Editar insumo |
| `DELETE` | `/api/insumos/{id}` | PROPIETARIO/ROOT | Desactivar insumo (soft delete) |

### Lotes — `/api/lotes`

| Método | Ruta | Acceso | Descripción |
|--------|------|--------|-------------|
| `POST` | `/api/lotes` | EMPLEADO+ | Registrar recepción de lote |
| `GET` | `/api/lotes/insumo/{insumoId}` | Autenticado | Lotes de un insumo |
| `GET` | `/api/lotes/{id}` | Autenticado | Detalle de un lote |

### Stock — `/api/stock`

| Método | Ruta | Acceso | Descripción |
|--------|------|--------|-------------|
| `GET` | `/api/stock/insumos` | Autenticado | Resumen de stock de todos los insumos |
| `GET` | `/api/stock/insumos/{id}` | Autenticado | Stock detallado de un insumo (con lotes) |

### Ajustes — `/api/ajustes`

| Método | Ruta | Acceso | Descripción |
|--------|------|--------|-------------|
| `POST` | `/api/ajustes` | PROPIETARIO/ROOT | Registrar merma, rotura o ajuste manual |
| `GET` | `/api/ajustes` | Autenticado | Historial completo de movimientos |
| `GET` | `/api/ajustes/insumo/{insumoId}` | Autenticado | Movimientos de un insumo concreto |

### Proveedores — `/api/proveedores`

| Método | Ruta | Acceso | Descripción |
|--------|------|--------|-------------|
| `GET` | `/api/proveedores` | Autenticado | Listar proveedores |
| `GET` | `/api/proveedores/{id}` | Autenticado | Detalle de un proveedor |
| `POST` | `/api/proveedores` | EMPLEADO+ | Crear proveedor |

### Usuarios — `/api/usuarios`

| Método | Ruta | Acceso | Descripción |
|--------|------|--------|-------------|
| `POST` | `/api/usuarios` | ROOT | Crear usuario |
| `GET` | `/api/usuarios` | ROOT | Listar usuarios |
| `DELETE` | `/api/usuarios/{id}` | ROOT | Desactivar usuario |

### Items — `/api/items`

| Método | Ruta | Acceso | Descripción |
|--------|------|--------|-------------|
| `GET` | `/api/items` | Autenticado | Listar ítems (paginado) |
| `GET` | `/api/items/{id}` | Autenticado | Detalle de un ítem |
| `POST` | `/api/items` | PROPIETARIO/ROOT | Crear ítem |
| `PUT` | `/api/items/{id}` | PROPIETARIO/ROOT | Editar ítem |
| `DELETE` | `/api/items/{id}` | PROPIETARIO/ROOT | Eliminar ítem |

### Recetas — `/api/items/{itemId}/receta`

| Método | Ruta | Acceso | Descripción |
|--------|------|--------|-------------|
| `GET` | `/api/items/{itemId}/receta` | Autenticado | Consultar receta de un ítem |
| `PUT` | `/api/items/{itemId}/receta` | PROPIETARIO/ROOT | Crear o actualizar la receta (insumos y cantidades) |
| `DELETE` | `/api/items/{itemId}/receta` | PROPIETARIO/ROOT | Eliminar la receta |

### Ventas — `/api/ventas`

| Método | Ruta | Acceso | Descripción |
|--------|------|--------|-------------|
| `POST` | `/api/ventas` | EMPLEADO+ | Registrar una venta y descontar inventario automáticamente (FIFO) |
| `GET` | `/api/ventas` | Autenticado | Listar ventas registradas |
| `GET` | `/api/ventas/{id}` | Autenticado | Detalle de una venta con sus líneas |

## Estructura del proyecto

```
src/main/java/com/deusto/coffeestack/
├── controller/      REST endpoints
├── service/         Lógica de negocio
├── repository/      Acceso a datos (JPA)
├── domain/          Entidades JPA
├── dto/             Objetos de transferencia
├── security/        Filtro JWT
└── config/          Seguridad, OpenAPI, inicialización de datos
src/main/resources/
├── application.yml               Configuración multi-perfil (local/dev/prod)
└── db/migration/                 Scripts Flyway (V1–V6)
```

## Planificación de sprints

| Sprint | Fechas | Objetivo | Estado |
|--------|--------|----------|--------|
| 1 | 9–25 marzo | Inventario base, roles, login | Completado |
| 2 | 13–28 abril | Ventas, descuento automático FIFO, alertas, CI | Completado |
| 3 | 4–18 mayo | Reposición, forecast SES, reportes CSV | Pendiente |

## Sprint 2 — Entregables (13–28 abril)

### Historias de usuario completadas

- **CORE-10** — *Como propietario quiero poder definir recetas (insumos y cantidades) por producto para descontar automáticamente inventario al vender.*
  - Entidad `Receta` y endpoints `/api/items/{itemId}/receta`.
- **HU-13 / Ventas** — *Como empleado quiero registrar una venta y que el inventario se descuente automáticamente.*
  - Entidades `Venta` y `VentaLinea`, servicio con lógica **FIFO**, controlador `/api/ventas` y nuevo tipo `VENTA` en `TipoMovimiento`.
- **Alertas de caducidad** — *Como propietario quiero ver alertas de lotes próximos a caducar para priorizar su uso y reducir merma.*
- **Frontend de ventas** — Página `Ventas` con lista, detalle y formulario dinámico para registrar nuevas ventas; integrada en la barra de navegación.

### Calidad y procesos

- **Tests unitarios y de integración** separados en Gradle:
  - `./gradlew test` ejecuta unitarios.
  - `./gradlew integrationTest` ejecuta los `*IT`.
  - `./gradlew build` y `./gradlew check` ejecutan ambos.
- **Mockito** incorporado para tests con dobles de prueba.
- **JaCoCo** para informes de cobertura (`build/reports/jacoco/test/html/index.html`).
- **SLF4J** sustituyendo `System.out.println` en el código de producción.

### Integración Continua

- Workflow de **GitHub Actions** ([`.github/workflows/ci.yml`](.github/workflows/ci.yml)) con dos jobs:
  - `build-backend`: compila el backend con JDK 21 y ejecuta `./gradlew build` (tests unitarios + integración).
  - `build-frontend`: instala dependencias y compila el proyecto React con Node 20.
- Se dispara en cada `push` y `pull_request` sobre `main` / `master`.
