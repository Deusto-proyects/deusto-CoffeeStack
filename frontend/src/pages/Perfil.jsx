import { useEffect, useState } from 'react'
import client, { apiErrorMessage } from '../api/client'
import PageHeader from '../components/PageHeader'
import { useAuth } from '../context/AuthContext'

export default function Perfil() {
  const { user, logout } = useAuth()
  const [me, setMe] = useState(null)
  const [error, setError] = useState(null)

  useEffect(() => {
    client
      .get('/api/auth/me')
      .then((r) => setMe(r.data))
      .catch((e) => setError(apiErrorMessage(e)))
  }, [])

  return (
    <div>
      <PageHeader icon="bi-person-circle" title="Mi perfil" />

      {error && <div className="alert alert-danger">{error}</div>}

      <div className="row">
        <div className="col-md-6">
          <div className="card">
            <div className="card-header">Datos de la cuenta</div>
            <div className="card-body">
              <dl className="row mb-0">
                <dt className="col-sm-4">Usuario</dt>
                <dd className="col-sm-8">{me?.username ?? user?.username}</dd>

                <dt className="col-sm-4">Rol</dt>
                <dd className="col-sm-8">
                  <span className={`badge badge-rol-${me?.role ?? user?.rol}`}>
                    {me?.role ?? user?.rol}
                  </span>
                </dd>
              </dl>
            </div>
            <div className="card-footer bg-white">
              <button className="btn btn-outline-danger" onClick={logout}>
                <i className="bi bi-box-arrow-right me-2"></i>Cerrar sesión
              </button>
            </div>
          </div>
        </div>
        <div className="col-md-6 mt-3 mt-md-0">
          <div className="card">
            <div className="card-header">Permisos de tu rol</div>
            <div className="card-body">
              <PermisosRol rol={me?.role ?? user?.rol} />
            </div>
          </div>
        </div>
      </div>
    </div>
  )
}

function PermisosRol({ rol }) {
  const perms = {
    EMPLEADO: [
      'Consultar stock, insumos, productos y proveedores',
      'Registrar recepción de lotes',
      'Crear proveedores',
      'Ver historial de movimientos',
    ],
    PROPIETARIO: [
      'Todo lo del EMPLEADO',
      'Crear / editar / desactivar insumos',
      'Crear / editar / eliminar productos',
      'Definir recetas de productos',
      'Registrar mermas, roturas y ajustes',
    ],
    ROOT: [
      'Todo lo del PROPIETARIO',
      'Crear / listar / desactivar usuarios',
      'Cambiar roles de usuarios',
    ],
  }

  const lista = perms[rol] || []

  return (
    <ul className="list-unstyled mb-0">
      {lista.map((p, i) => (
        <li key={i} className="mb-2">
          <i className="bi bi-check-circle-fill text-success me-2"></i>
          {p}
        </li>
      ))}
    </ul>
  )
}
