# CoffeeStack — Sistema de Gestión de Inventario

Sistema de gestión de inventario para cafeterías, desarrollado como proyecto académico de la asignatura **Proceso de Software y Calidad 2025-26 (Universidad de Deusto)**.

Permite controlar insumos, lotes, stock, mermas y roturas mediante una API REST con autenticación JWT y control de acceso por roles.

## Stack tecnológico

- **Java 21** + **Spring Boot 3.2.5**
- **Spring Security** + JWT (JJWT 0.12.3)
- **Spring Data JPA** + Hibernate + **Flyway** (migraciones)
- **PostgreSQL 16** (perfil `local`, vía Docker) · **MySQL 8** (`dev`/`prod`) · **H2** (tests)
- **SpringDoc OpenAPI** (Swagger UI en `/swagger-ui/index.html`)
- Frontend: **React 19 + Vite + Bootstrap 5 + recharts**

## Quickstart con datos demo

Arranque mínimo para ver todas las funcionalidades con datos ya poblados.

### Requisitos

- Java 21+
- Node 20+
- Docker + docker compose (para PostgreSQL en local)

### Pasos

```bash
# 1. Levantar PostgreSQL en local
docker compose up -d

# 2. Arrancar el backend (perfil 'local' por defecto, aplica V1-V11 incluyendo demo data)
./gradlew bootRun

# 3. En otra terminal, arrancar el frontend
cd frontend
npm install
npm run dev
```

### Credenciales y URLs

| Servicio | URL |
|----------|-----|
| Frontend | http://localhost:5173 |
| Backend  | http://localhost:8080 |
| Swagger  | http://localhost:8080/swagger-ui/index.html |
| Javadoc  | https://deusto-proyects.github.io/deusto-CoffeeStack/javadoc/ |

| Usuario demo | Password | Rol |
|--------------|----------|-----|
| `admin` | `admin123` | ROOT |

El usuario `admin` lo crea automáticamente `DataInitializer` al arrancar.
Crea más usuarios desde `/admin/usuarios` (solo ROOT) o vía `POST /api/usuarios`.

### Qué hay en la BD tras la primera ejecución

La migración `V11__demo_data.sql` (solo en perfiles `local` y `dev`) precarga:
- 2 proveedores, 4 insumos (Café, Leche, Azúcar, Vasos)
- 4 lotes con `precio_compra` para el cálculo de coste en reportes
- 9 movimientos VENTA/MERMA/ROTURA repartidos en los últimos 25 días

Eso permite ver el **Reporte de consumo** (`/reportes/consumo`) con datos no triviales
en el rango por defecto (últimos 30 días).

## Asistente IA (Chatbot)

El proyecto incluye un asistente conversacional **100 % local** (ningún dato sale del equipo) que ayuda al PROPIETARIO/ROOT a tomar decisiones operativas (qué reponer, riesgo de mermas, cobertura de stock, etc.).

### Qué hace falta instalar

- **Ollama** corriendo en `localhost:11434` con el modelo **`qwen2.5:7b`** (descarga ~5 GB, persiste en disco).
- **~6 GB de RAM libres** mientras se usa (recomendado 8 GB+).
- GPU NVIDIA opcional para acelerar respuestas.

Hay dos formas de tener Ollama listo:

**Opción A — Ollama dentro del `docker-compose` (recomendado).** Ya está definido el servicio `ollama` que descarga y precarga el modelo automáticamente al primer arranque:

```bash
docker compose up -d
# Espera 2–5 min en el primer arranque mientras descarga qwen2.5:7b.
# Comprueba que está listo:
docker compose ps   # el servicio ollama debe figurar como (healthy)
```

**Opción B — Ollama nativo en tu equipo** (https://ollama.com/download):

```bash
ollama pull qwen2.5:7b
ollama serve
```

### Cómo usarlo

Tras arrancar backend y frontend (`./gradlew bootRun` + `npm run dev`), inicia sesión y entra en **"Asistente IA"** en la barra de navegación (solo visible para PROPIETARIO/ROOT). El primer turno tarda 30–60 s porque el modelo se carga en memoria; los siguientes son más rápidos (5–15 s en CPU, <2 s con GPU).

Endpoints REST:

| Método | Ruta | Acceso | Descripción |
|--------|------|--------|-------------|
| `POST` | `/api/chatbot/preguntar` | PROPIETARIO/ROOT | Enviar una pregunta y obtener respuesta |
| `GET` | `/api/chatbot/snapshot` | PROPIETARIO/ROOT | Ver el snapshot exacto que se envía al modelo (debug) |

Variables relevantes en `application.yml`:

| Clave | Default | Para qué |
|-------|---------|----------|
| `spring.ai.ollama.base-url` | `http://localhost:11434` | URL de Ollama (sobrescribible con `OLLAMA_URL`) |
| `spring.ai.ollama.chat.options.model` | `qwen2.5:7b` | Modelo a utilizar |
| `ai.chatbot.read-timeout-seconds` | `120` | Tiempo máx. de espera por respuesta |

Documentación detallada (arquitectura, snapshot, limitaciones): [`docs/CHATBOT.md`](docs/CHATBOT.md).

## Compilación y ejecución (avanzado)

### Perfiles disponibles

| Perfil | Base de datos | Datos demo |
|--------|---------------|------------|
| `local` (default) | PostgreSQL via docker-compose | Sí |
| `dev`             | MySQL local                   | Sí |
| `prod`            | MySQL (vars de entorno)       | No |
| `test`            | H2 en memoria (MODE=MySQL)    | No |

### Perfil `dev` (MySQL local)

```bash
./gradlew bootRun --args='--spring.profiles.active=dev'
```

Variables de entorno (con defaults):

| Variable | Default |
|----------|---------|
| `DB_USER` | `root` |
| `DB_PASS` | `password` |

La base `coffeestack` debe existir en `localhost:3306`.

### Producción

```bash
java -jar build/libs/coffeestack-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod
```

Variables de entorno **obligatorias**: `DB_URL`, `DB_USER`, `DB_PASS`.

### Tests

```bash
./gradlew test            # unitarios (*Test)
./gradlew integrationTest # de integración (*IT)
./gradlew check           # ambos + JaCoCo
```

### Generar la documentación localmente

```bash
./gradlew javadoc
# salida en build/docs/javadoc/index.html
```

El workflow `.github/workflows/docs.yml` la publica automáticamente en GitHub Pages
tras cada push a `main`.

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
| `GET` | `/api/usuarios` | ROOT | Listar usuarios (con auditoría createdAt/by, updatedAt/by) |
| `PUT` | `/api/usuarios/{id}` | ROOT | Editar username y/o password (password opcional) |
| `PATCH` | `/api/usuarios/{id}/rol` | ROOT | Cambiar el rol |
| `PATCH` | `/api/usuarios/{id}/activar` | ROOT | Reactivar un usuario desactivado |
| `DELETE` | `/api/usuarios/{id}` | ROOT | Desactivar usuario |

### Reportes — `/api/reportes`

| Método | Ruta | Acceso | Descripción |
|--------|------|--------|-------------|
| `GET` | `/api/reportes/consumo` | PROPIETARIO/ROOT | Reporte de consumo de un insumo en un rango de fechas. Parámetros: `insumoId`, `desde` (YYYY-MM-DD), `hasta` (YYYY-MM-DD), `granularidad` (DIA\|SEMANA, default DIA). Devuelve cantidad y coste estimado totales, desglose por tipo de movimiento y serie temporal para gráfico. |

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
├── db/migration/
│   ├── mysql/                    V1-V10 schema (MySQL/H2)
│   └── postgresql/               V1-V10 schema (PostgreSQL)
└── db/seed/
    ├── mysql/V11__demo_data.sql       Demo data (perfiles local/dev)
    └── postgresql/V11__demo_data.sql  Demo data (perfiles local/dev)
```

## Planificación de sprints

| Sprint | Fechas | Objetivo | Estado |
|--------|--------|----------|--------|
| 1 | 9–25 marzo | Inventario base, roles, login | Completado |
| 2 | 13–28 abril | Ventas, descuento automático FIFO, alertas, CI | Completado |
| 3 | 4–18 mayo | Gestión usuarios, reporte consumo, PostgreSQL, Pages | Completado |

## Sprint 3 — Entregables (4–18 mayo)

### Historias de usuario completadas

- **HU-Usuarios** — *Como administrador quiero poder gestionar usuarios (alta/baja/activar/desactivar) para administrar el acceso al sistema.*
  - Endpoints nuevos: `PATCH /api/usuarios/{id}/activar`, `PUT /api/usuarios/{id}` (editar username/password).
  - Auditoría JPA: `Usuario` registra `createdAt/by` y `updatedAt/by` automáticamente vía `AuditorAware` (lee el SecurityContext).
  - Vista `/admin/usuarios` con filtros (activos/inactivos/todos), botones contextuales y formulario reutilizable en modo "nuevo" y "editar".
- **HU-Reporte consumo** — *Como propietario quiero poder ver un reporte de consumo por insumo en un rango de fechas para entender patrones y costes operativos.*
  - Endpoint `GET /api/reportes/consumo` con `insumoId`, `desde`, `hasta`, `granularidad=DIA|SEMANA`.
  - Coste estimado calculado como `cantidad × Lote.precioCompra` (nuevo campo añadido en V10).
  - Desglose por `TipoMovimiento` (VENTA/MERMA/ROTURA/AJUSTE_NEGATIVO) + serie temporal agrupada por día o por lunes ISO.
  - Vista `/reportes/consumo` con tarjetas de resumen, tabla de desglose y gráfico dual-axis con **recharts**.

### Infraestructura y procesos

- **PostgreSQL en local** vía `docker-compose.yml` (perfil `local` por defecto). MySQL se mantiene en `dev`/`prod`.
- **Flyway dual-vendor**: `db/migration/mysql/V1-V10` y `db/migration/postgresql/V1-V10` se traducen entre dialectos. Test `FlywayVendorParityTest` falla si las carpetas divergen.
- **Datos de demo** en `db/seed/{mysql,postgresql}/V11__demo_data.sql` (perfiles `local` y `dev`) para que el reporte de consumo tenga datos visibles desde el arranque.
- **Documentación automática**: workflow `.github/workflows/docs.yml` publica el Javadoc en GitHub Pages tras cada push a `main`. Swagger UI sigue disponible en la app arrancada.
- **Tests añadidos**: `SpringSecurityAuditorAwareTest`, `UsuarioServiceImplTest`, `ReporteConsumoServiceImplTest` (unitarios) y nuevos casos en `UsuarioControllerIT` + `ReporteControllerIT` (integración).

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
