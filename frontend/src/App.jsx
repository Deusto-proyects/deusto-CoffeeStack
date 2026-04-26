import { Routes, Route, Navigate } from 'react-router-dom'
import Layout from './components/Layout'
import ProtectedRoute from './components/ProtectedRoute'
import Login from './pages/Login'
import Register from './pages/Register'
import Dashboard from './pages/Dashboard'
import Perfil from './pages/Perfil'
import Insumos from './pages/Insumos'
import InsumoForm from './pages/InsumoForm'
import InsumoStock from './pages/InsumoStock'
import Items from './pages/Items'
import ItemForm from './pages/ItemForm'
import Receta from './pages/Receta'
import Lotes from './pages/Lotes'
import LoteForm from './pages/LoteForm'
import Proveedores from './pages/Proveedores'
import ProveedorForm from './pages/ProveedorForm'
import Ajustes from './pages/Ajustes'
import AjusteForm from './pages/AjusteForm'
import HistorialMovimientos from './pages/HistorialMovimientos'
import Usuarios from './pages/Usuarios'
import UsuarioForm from './pages/UsuarioForm'
import Ventas from './pages/Ventas'
import VentaForm from './pages/VentaForm'
import VentaDetalle from './pages/VentaDetalle'

export default function App() {
  return (
    <Routes>
      <Route path="/login" element={<Login />} />
      <Route path="/register" element={<Register />} />

      <Route
        element={
          <ProtectedRoute>
            <Layout />
          </ProtectedRoute>
        }
      >
        <Route index element={<Dashboard />} />
        <Route path="perfil" element={<Perfil />} />

        <Route path="insumos" element={<Insumos />} />
        <Route
          path="insumos/nuevo"
          element={
            <ProtectedRoute roles={['PROPIETARIO', 'ROOT']}>
              <InsumoForm />
            </ProtectedRoute>
          }
        />
        <Route
          path="insumos/:id/editar"
          element={
            <ProtectedRoute roles={['PROPIETARIO', 'ROOT']}>
              <InsumoForm />
            </ProtectedRoute>
          }
        />
        <Route path="insumos/:id/stock" element={<InsumoStock />} />

        <Route path="items" element={<Items />} />
        <Route
          path="items/nuevo"
          element={
            <ProtectedRoute roles={['PROPIETARIO', 'ROOT']}>
              <ItemForm />
            </ProtectedRoute>
          }
        />
        <Route
          path="items/:id/editar"
          element={
            <ProtectedRoute roles={['PROPIETARIO', 'ROOT']}>
              <ItemForm />
            </ProtectedRoute>
          }
        />
        <Route path="items/:itemId/receta" element={<Receta />} />

        <Route path="lotes" element={<Lotes />} />
        <Route
          path="lotes/nuevo"
          element={
            <ProtectedRoute roles={['EMPLEADO', 'PROPIETARIO', 'ROOT']}>
              <LoteForm />
            </ProtectedRoute>
          }
        />

        <Route path="proveedores" element={<Proveedores />} />
        <Route
          path="proveedores/nuevo"
          element={
            <ProtectedRoute roles={['EMPLEADO', 'PROPIETARIO', 'ROOT']}>
              <ProveedorForm />
            </ProtectedRoute>
          }
        />

        <Route path="ajustes" element={<Ajustes />} />
        <Route
          path="ajustes/nuevo"
          element={
            <ProtectedRoute roles={['PROPIETARIO', 'ROOT']}>
              <AjusteForm />
            </ProtectedRoute>
          }
        />

        <Route
          path="historial"
          element={
            <ProtectedRoute roles={['PROPIETARIO', 'ROOT']}>
              <HistorialMovimientos />
            </ProtectedRoute>
          }
        />

        <Route
          path="usuarios"
          element={
            <ProtectedRoute roles={['ROOT']}>
              <Usuarios />
            </ProtectedRoute>
          }
        />
        <Route
          path="usuarios/nuevo"
          element={
            <ProtectedRoute roles={['ROOT']}>
              <UsuarioForm />
            </ProtectedRoute>
          }
        />

        <Route path="ventas" element={<Ventas />} />
        <Route
          path="ventas/nueva"
          element={
            <ProtectedRoute roles={['EMPLEADO', 'PROPIETARIO', 'ROOT']}>
              <VentaForm />
            </ProtectedRoute>
          }
        />
        <Route path="ventas/:id" element={<VentaDetalle />} />

        <Route path="*" element={<Navigate to="/" replace />} />
      </Route>
    </Routes>
  )
}
