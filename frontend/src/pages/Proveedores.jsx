import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import client, { apiErrorMessage } from '../api/client'
import PageHeader from '../components/PageHeader'

export default function Proveedores() {
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
                        {p.activo ? (
                          <span className="badge bg-success">Activo</span>
                        ) : (
                          <span className="badge bg-secondary">Inactivo</span>
                        )}
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
