import { Navigate, useLocation } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'

export default function ProtectedRoute({ roles, children }) {
  const { user } = useAuth()
  const location = useLocation()

  if (!user) {
    return <Navigate to="/login" state={{ from: location }} replace />
  }

  if (roles && roles.length > 0 && !roles.includes(user.rol)) {
    return (
      <div className="container mt-5">
        <div className="alert alert-danger">
          <h5 className="alert-heading">
            <i className="bi bi-shield-lock-fill me-2"></i>Acceso denegado
          </h5>
          <p className="mb-0">
            No tienes permisos para ver esta página. Se requiere rol:{' '}
            <strong>{roles.join(' o ')}</strong>.
          </p>
        </div>
      </div>
    )
  }

  return children
}
