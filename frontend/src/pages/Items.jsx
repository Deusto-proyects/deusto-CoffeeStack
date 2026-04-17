import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import client, { apiErrorMessage } from '../api/client'
import PageHeader from '../components/PageHeader'
import { useAuth } from '../context/AuthContext'

export default function Items() {
  const { hasRole } = useAuth()
  const [items, setItems] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)
  const puedeEditar = hasRole('PROPIETARIO', 'ROOT')

  async function load() {
    try {
      setLoading(true)
      const { data } = await client.get('/api/items', { params: { size: 100 } })
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

  async function handleDelete(id, name) {
    if (!window.confirm(`¿Eliminar el producto "${name}"?`)) return
    try {
      await client.delete(`/api/items/${id}`)
      load()
    } catch (err) {
      alert(apiErrorMessage(err))
    }
  }

  return (
    <div>
      <PageHeader
        icon="bi-list-ul"
        title="Productos"
        subtitle="Productos del menú y sus recetas"
        actions={
          puedeEditar && (
            <Link to="/items/nuevo" className="btn btn-coffee">
              <i className="bi bi-plus-lg me-1"></i>Nuevo producto
            </Link>
          )
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
              <p className="mt-2">No hay productos.</p>
              {puedeEditar && (
                <Link to="/items/nuevo" className="btn btn-coffee">
                  Crear primer producto
                </Link>
              )}
            </div>
          ) : (
            <div className="table-responsive">
              <table className="table table-hover align-middle">
                <thead>
                  <tr>
                    <th>#</th>
                    <th>Nombre</th>
                    <th>Descripción</th>
                    <th>Creado</th>
                    <th className="text-end">Acciones</th>
                  </tr>
                </thead>
                <tbody>
                  {items.map((i) => (
                    <tr key={i.id}>
                      <td className="text-muted">{i.id}</td>
                      <td>
                        <strong>{i.name}</strong>
                      </td>
                      <td>
                        <small className="text-muted">
                          {i.description || <em>sin descripción</em>}
                        </small>
                      </td>
                      <td>
                        <small>
                          {i.createdAt ? new Date(i.createdAt).toLocaleDateString() : '—'}
                        </small>
                      </td>
                      <td className="text-end">
                        <div className="btn-group btn-group-sm">
                          <Link
                            to={`/items/${i.id}/receta`}
                            className="btn btn-outline-coffee"
                            title="Ver receta"
                          >
                            <i className="bi bi-journal-text me-1"></i>Receta
                          </Link>
                          {puedeEditar && (
                            <>
                              <Link
                                to={`/items/${i.id}/editar`}
                                className="btn btn-outline-coffee"
                              >
                                <i className="bi bi-pencil"></i>
                              </Link>
                              <button
                                className="btn btn-outline-danger"
                                onClick={() => handleDelete(i.id, i.name)}
                              >
                                <i className="bi bi-trash"></i>
                              </button>
                            </>
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
