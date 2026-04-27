import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import client, { apiErrorMessage } from '../api/client'
import PageHeader from '../components/PageHeader'
import { useAuth } from '../context/AuthContext'

export default function Proveedores() {
  const { hasRole } = useAuth()
  const puedeEditar = hasRole('EMPLEADO', 'PROPIETARIO', 'ROOT')
  const puedeDesactivar = hasRole('PROPIETARIO', 'ROOT')
  
  const [items, setItems] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)

  async function load() {
    try {
      setLoading(true)
      const { data } = await client.get('/api/proveedores', { params: { size: 100 } })
      setItems(data.content ?? data)
    } catch (err) {
      setError(apiErrorMessage(err))
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    load()
  }, [])

  async function handleDelete(id, nombre) {
    if (!window.confirm(`¿Seguro que deseas eliminar permanentemente el proveedor "${nombre}"?`)) return
    try {
      await client.delete(`/api/proveedores/${id}`)
      load()
    } catch (err) {
      alert(apiErrorMessage(err))
    }
  }

  async function handleToggleStatus(p) {
    if (!puedeDesactivar) return
    const newStatus = !p.activo
    const actionName = newStatus ? 'activar' : 'desactivar'
    if (!window.confirm(`¿Seguro que deseas ${actionName} el proveedor "${p.nombre}"?`)) return
    try {
      await client.patch(`/api/proveedores/${p.id}/estado?activo=${newStatus}`)
      load()
    } catch (err) {
      alert(apiErrorMessage(err))
    }
  }

  return (
    <div>
      <PageHeader
        icon="bi-truck"
        title="Proveedores"
        actions={
          <Link to="/proveedores/nuevo" className="btn btn-coffee">
            <i className="bi bi-plus-lg me-1"></i>Nuevo proveedor
          </Link>
        }
      />

      {error && <div className="alert alert-danger">{error}</div>}

      <div className="card">
        <div className="card-body">
          {loading ? (
            <div className="text-center py-4">
              <div className="spinner-border text-coffee"></div>
            </div>
          ) : items.length === 0 ? (
            <div className="empty-state">
              <i className="bi bi-inbox fs-1"></i>
              <p className="mt-2">No hay proveedores registrados.</p>
              <Link to="/proveedores/nuevo" className="btn btn-coffee">
                Crear primer proveedor
              </Link>
            </div>
          ) : (
            <div className="table-responsive">
              <table className="table table-hover align-middle">
                <thead>
                  <tr>
                    <th>#</th>
                    <th>Nombre</th>
                    <th>Contacto</th>
                    <th>Email</th>
                    <th>Teléfono</th>
                    <th>Estado</th>
                    <th className="text-end">Acciones</th>
                  </tr>
                </thead>
                <tbody>
                  {items.map((p) => (
                    <tr key={p.id}>
                      <td className="text-muted">{p.id}</td>
                      <td>
                        <strong>{p.nombre}</strong>
                      </td>
                      <td>{p.contacto || <span className="text-muted">—</span>}</td>
                      <td>
                        {p.email ? (
                          <a href={`mailto:${p.email}`} className="text-coffee">
                            {p.email}
                          </a>
                        ) : (
                          <span className="text-muted">—</span>
                        )}
                      </td>
                      <td>{p.telefono || <span className="text-muted">—</span>}</td>
                      <td>
                        <span 
                          className={`badge bg-${p.activo ? 'success' : 'secondary'} ${puedeDesactivar ? 'cursor-pointer' : ''}`}
                          onClick={() => handleToggleStatus(p)}
                          style={puedeDesactivar ? { cursor: 'pointer' } : {}}
                          title={puedeDesactivar ? 'Clic para cambiar estado' : ''}
                        >
                          {p.activo ? 'Activo' : 'Inactivo'}
                        </span>
                      </td>
                      <td className="text-end">
                        <div className="btn-group btn-group-sm">
                          {puedeEditar && (
                            <Link
                              to={`/proveedores/${p.id}/editar`}
                              className="btn btn-outline-coffee"
                              title="Editar"
                            >
                              <i className="bi bi-pencil"></i>
                            </Link>
                          )}
                          {puedeDesactivar && (
                            <button
                              className="btn btn-outline-danger"
                              onClick={() => handleDelete(p.id, p.nombre)}
                              title="Eliminar"
                            >
                              <i className="bi bi-trash"></i>
                            </button>
                          )}
                        </div>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </div>
      </div>
    </div>
  )
}
