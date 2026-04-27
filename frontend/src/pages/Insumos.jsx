import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import client, { apiErrorMessage } from '../api/client'
import PageHeader from '../components/PageHeader'
import { useAuth } from '../context/AuthContext'

export default function Insumos() {
  const { hasRole } = useAuth()
  const [items, setItems] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)
  const [search, setSearch] = useState('')
  const puedeEditar = hasRole('PROPIETARIO', 'ROOT')

  async function load() {
    try {
      setLoading(true)
      const { data } = await client.get('/api/insumos', { params: { size: 100 } })
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
    if (!window.confirm(`¿Desactivar el insumo "${nombre}"?`)) return
    try {
      await client.delete(`/api/insumos/${id}`)
      load()
    } catch (err) {
      alert(apiErrorMessage(err))
    }
  }

  const filtrados = items.filter((i) =>
    i.nombre.toLowerCase().includes(search.toLowerCase())
  )

  return (
    <div>
      <PageHeader
        icon="bi-box-seam"
        title="Insumos"
        subtitle="Materias primas e ingredientes disponibles"
        actions={
          puedeEditar && (
            <Link to="/insumos/nuevo" className="btn btn-coffee">
              <i className="bi bi-plus-lg me-1"></i>Nuevo insumo
            </Link>
          )
        }
      />

      {error && <div className="alert alert-danger">{error}</div>}

      <div className="card">
        <div className="card-body">
          <div className="mb-3">
            <div className="input-group">
              <span className="input-group-text">
                <i className="bi bi-search"></i>
              </span>
              <input
                type="text"
                className="form-control"
                placeholder="Buscar por nombre..."
                value={search}
                onChange={(e) => setSearch(e.target.value)}
              />
            </div>
          </div>

          {loading ? (
            <div className="text-center py-4">
              <div className="spinner-border text-coffee"></div>
            </div>
          ) : filtrados.length === 0 ? (
            <div className="empty-state">
              <i className="bi bi-inbox fs-1"></i>
              <p className="mt-2">No hay insumos.</p>
              {puedeEditar && (
                <Link to="/insumos/nuevo" className="btn btn-coffee">
                  Crear primer insumo
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
                    <th>Unidad</th>
                    <th className="text-end">Mínimo alerta</th>
                    <th>Estado</th>
                    <th className="text-end">Acciones</th>
                  </tr>
                </thead>
                <tbody>
                  {filtrados.map((i) => (
                    <tr key={i.id}>
                      <td className="text-muted">{i.id}</td>
                      <td>
                        <strong>{i.nombre}</strong>
                      </td>
                      <td>{i.unidadMedida}</td>
                      <td className="text-end">{i.stockMinimoAlerta}</td>
                      <td>
                        {i.activo ? (
                          <span className="badge bg-success">Activo</span>
                        ) : (
                          <span className="badge bg-secondary">Inactivo</span>
                        )}
                      </td>
                      <td className="text-end">
                        <div className="btn-group btn-group-sm">
                          <Link
                            to={`/insumos/${i.id}/stock`}
                            className="btn btn-outline-coffee"
                            title="Ver stock"
                          >
                            <i className="bi bi-bar-chart"></i>
                          </Link>
                          {puedeEditar && (
                            <>
                              <Link
                                to={`/insumos/${i.id}/editar`}
                                className="btn btn-outline-coffee"
                                title="Editar"
                              >
                                <i className="bi bi-pencil"></i>
                              </Link>
                              <button
                                className="btn btn-outline-danger"
                                onClick={() => handleDelete(i.id, i.nombre)}
                                title="Desactivar"
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
