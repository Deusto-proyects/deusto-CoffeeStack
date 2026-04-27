import { useState } from 'react'
import { Link, useNavigate, useLocation } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import { apiErrorMessage } from '../api/client'

export default function Login() {
  const { login, loading } = useAuth()
  const navigate = useNavigate()
  const location = useLocation()
  const [username, setUsername] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState(null)

  async function handleSubmit(e) {
    e.preventDefault()
    setError(null)
    try {
      await login(username, password)
      const redirect = location.state?.from?.pathname || '/'
      navigate(redirect, { replace: true })
    } catch (err) {
      setError(apiErrorMessage(err))
    }
  }

  return (
    <div className="login-wrapper">
      <div className="login-card">
        <div className="text-center mb-4">
          <i className="bi bi-cup-hot-fill" style={{ fontSize: '3rem', color: 'var(--coffee)' }}></i>
          <h2 className="mt-2 mb-1 text-coffee fw-bold">CoffeeStack</h2>
          <p className="text-muted small mb-0">Sistema de Gestión de Inventario</p>
        </div>

        <h4 className="mb-3">Iniciar sesión</h4>

        {error && (
          <div className="alert alert-danger py-2" role="alert">
            <i className="bi bi-exclamation-triangle-fill me-2"></i>
            {error}
          </div>
        )}

        <form onSubmit={handleSubmit}>
          <div className="mb-3">
            <label className="form-label">Usuario</label>
            <div className="input-group">
              <span className="input-group-text">
                <i className="bi bi-person"></i>
              </span>
              <input
                type="text"
                className="form-control"
                required
                value={username}
                onChange={(e) => setUsername(e.target.value)}
                autoFocus
              />
            </div>
          </div>

          <div className="mb-3">
            <label className="form-label">Contraseña</label>
            <div className="input-group">
              <span className="input-group-text">
                <i className="bi bi-lock"></i>
              </span>
              <input
                type="password"
                className="form-control"
                required
                value={password}
                onChange={(e) => setPassword(e.target.value)}
              />
            </div>
          </div>

          <button type="submit" className="btn btn-coffee w-100" disabled={loading}>
            {loading ? (
              <>
                <span className="spinner-border spinner-border-sm me-2"></span>Entrando...
              </>
            ) : (
              <>
                <i className="bi bi-box-arrow-in-right me-2"></i>Entrar
              </>
            )}
          </button>
        </form>

        <hr className="my-4" />
        <p className="text-center mb-0 small">
          ¿No tienes cuenta?{' '}
          <Link to="/register" className="text-coffee fw-semibold">
            Regístrate
          </Link>
        </p>
        <p className="text-center text-muted small mt-3 mb-0">
          <i className="bi bi-info-circle me-1"></i>
          Usuario por defecto: <code>admin</code> / <code>admin123</code>
        </p>
      </div>
    </div>
  )
}
