import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import client, { apiErrorMessage } from '../api/client'
import PageHeader from '../components/PageHeader'
import { useAuth } from '../context/AuthContext'

export default function Usuarios() {
  const { user: currentUser } = useAuth()
  const [users, setUsers] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)

  async function load() {
    try {
      setLoading(true)
      const { data } = await client.get('/api/usuarios')
      setUsers(data)
    } catch (err) {
      setError(apiErrorMessage(err))
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    load()
  }, [])

  async function cambiarRol(id, nuevoRol) {
    try {
      await client.patch(`/api/usuarios/${id}/rol`, { rol: nuevoRol })
      load()
    } catch (err) {
      alert(apiErrorMessage(err))
    }
  }

  async function desactivar(id, username) {
    if (!window.confirm(`¿Desactivar al usuario "${username}"?`)) return
    try {
      await client.delete(`/api/usuarios/${id}`)
      load()
    } catch (err) {
      alert(apiErrorMessage(err))
    }
  }

  return (
    <div>
      <PageHeader
        icon="bi-people-fill"
        title="Usuarios"
        subtitle="Gestión de cuentas y roles (solo ROOT)"
        actions={
          <Link to="/usuarios/nuevo" className="btn btn-coffee">
            <i className="bi bi-person-plus me-1"></i>Nuevo usuario
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
          ) : users.length === 0 ? (
            <div className="empty-state">
              <i className="bi bi-people fs-1"></i>
              <p className="mt-2 mb-0">No hay usuarios.</p>
            </div>
          ) : (
            <div className="table-responsive">
              <table className="table table-hover align-middle">
                <thead>
                  <tr>
                    <th>#</th>
                    <th>Usuario</th>
                    <th>Rol</th>
                    <th>Estado</th>
                    <th className="text-end">Acciones</th>
                  </tr>
                </thead>
                <tbody>
                  {users.map((u) => {
                    const esActual = u.username === currentUser?.username
                    return (
                      <tr key={u.id}>
                        <td className="text-muted">{u.id}</td>
                        <td>
                          <strong>{u.username}</strong>
                          {esActual && <span className="ms-2 badge bg-info">tú</span>}
                        </td>
                        <td>
                          <span className={`badge badge-rol-${u.rol}`}>{u.rol}</span>
                        </td>
                        <td>
                          {u.activo ? (
                            <span className="badge bg-success">Activo</span>
                          ) : (
                            <span className="badge bg-secondary">Inactivo</span>
                          )}
                        </td>
                        <td className="text-end">
                          <div className="btn-group btn-group-sm">
                            <div className="dropdown">
                              <button
                                className="btn btn-outline-coffee dropdown-toggle"
                                data-bs-toggle="dropdown"
                                disabled={esActual}
                              >
                                <i className="bi bi-arrow-repeat me-1"></i>Cambiar rol
                              </button>
                              <ul className="dropdown-menu">
                                {['ROOT', 'PROPIETARIO', 'EMPLEADO']
                                  .filter((r) => r !== u.rol)
                                  .map((r) => (
                                    <li key={r}>
                                      <button
                                        className="dropdown-item"
                                        onClick={() => cambiarRol(u.id, r)}
                                      >
                                        {r}
                                      </button>
                                    </li>
                                  ))}
                              </ul>
                            </div>
                            <button
                              className="btn btn-outline-danger ms-1"
                              onClick={() => desactivar(u.id, u.username)}
                              disabled={esActual || !u.activo}
                            >
                              <i className="bi bi-trash"></i>
                            </button>
                          </div>
                        </td>
                      </tr>
                    )
                  })}
                </tbody>
              </table>
            </div>
          )}
        </div>
      </div>
    </div>
  )
}
