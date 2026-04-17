# CoffeeStack · Frontend

Interfaz web para el sistema de gestión de inventario **CoffeeStack**.

## Stack

- React 19 + Vite
- React Router v7
- Axios (con interceptor JWT)
- Bootstrap 5 + Bootstrap Icons

## Requisitos

- Node.js 18+
- Backend CoffeeStack corriendo en `http://localhost:8080`

## Desarrollo

```bash
cd frontend
npm install
npm run dev
```

La app arranca en `http://localhost:5173`.

## Producción

```bash
npm run build     # genera dist/
npm run preview   # sirve la build en http://localhost:4173
```

## Configuración

Por defecto apunta a `http://localhost:8080`. Para cambiarlo, crea `.env.local`:

```
VITE_API_BASE=http://mi-backend:8080
```

## Usuario por defecto

- Usuario: `admin`
- Contraseña: `admin123`
- Rol: `ROOT`

## Rutas de la aplicación

| Ruta | Descripción | Rol requerido |
|------|-------------|---------------|
| `/login` | Inicio de sesión | — |
| `/register` | Registro de nueva cuenta | — |
| `/` | Dashboard (stock + alertas) | Cualquiera |
| `/perfil` | Datos del usuario actual | Cualquiera |
| `/insumos` | Listado de insumos | Cualquiera |
| `/insumos/nuevo` | Crear insumo | PROPIETARIO, ROOT |
| `/insumos/:id/editar` | Editar insumo | PROPIETARIO, ROOT |
| `/insumos/:id/stock` | Stock detallado + lotes + historial | Cualquiera |
| `/items` | Listado de productos | Cualquiera |
| `/items/nuevo` | Crear producto | PROPIETARIO, ROOT |
| `/items/:id/editar` | Editar producto | PROPIETARIO, ROOT |
| `/items/:itemId/receta` | Ver / editar receta | Ver: cualquiera · Editar: PROPIETARIO, ROOT |
| `/lotes` | Listado de lotes por insumo | Cualquiera |
| `/lotes/nuevo` | Registrar recepción de lote | EMPLEADO+ |
| `/proveedores` | Listado de proveedores | Cualquiera |
| `/proveedores/nuevo` | Crear proveedor | EMPLEADO+ |
| `/ajustes` | Historial de movimientos | Cualquiera |
| `/ajustes/nuevo` | Registrar merma / rotura / ajuste | PROPIETARIO, ROOT |
| `/usuarios` | Listado de usuarios | ROOT |
| `/usuarios/nuevo` | Crear usuario | ROOT |
