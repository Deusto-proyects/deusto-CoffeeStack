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

| Sprint | Fechas | Objetivo |
|--------|--------|----------|
| 1 | 9–25 marzo | Inventario base, roles, login |
| 2 | 13–28 abril | Ventas, descuento automático FEFO, alertas |
| 3 | 4–18 mayo | Reposición, forecast SES, reportes CSV |
