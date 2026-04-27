import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import { apiErrorMessage } from '../api/client'

export default function Register() {
  const { register, loading } = useAuth()
  const navigate = useNavigate()
  const [username, setUsername] = useState('')
  const [password, setPassword] = useState('')
  const [rol, setRol] = useState('EMPLEADO')
  const [error, setError] = useState(null)

  async function handleSubmit(e) {
    e.preventDefault()
    setError(null)
    if (password.length < 6) {
      setError('La contraseña debe tener al menos 6 caracteres.')
      return
    }
    try {
      await register(username, password, rol)
      navigate('/', { replace: true })
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
          <p className="text-muted small mb-0">Crear cuenta</p>
        </div>

        {error && (
          <div className="alert alert-danger py-2">
            <i className="bi bi-exclamation-triangle-fill me-2"></i>
            {error}
          </div>
        )}

        <form onSubmit={handleSubmit}>
          <div className="mb-3">
            <label className="form-label">Usuario</label>
            <input
              type="text"
              className="form-control"
              required
              minLength={3}
              maxLength={60}
              value={username}
              onChange={(e) => setUsername(e.target.value)}
              autoFocus
            />
          </div>
          <div className="mb-3">
            <label className="form-label">Contraseña</label>
            <input
              type="password"
              className="form-control"
              required
              minLength={6}
              value={password}
              onChange={(e) => setPassword(e.target.value)}
            />
            <small className="text-muted">Mínimo 6 caracteres</small>
          </div>
          <div className="mb-3">
            <label className="form-label">Rol</label>
            <select
              className="form-select"
              value={rol}
              onChange={(e) => setRol(e.target.value)}
            >
              <option value="EMPLEADO">EMPLEADO</option>
              <option value="PROPIETARIO">PROPIETARIO</option>
              <option value="ROOT">ROOT</option>
            </select>
            <small className="text-muted">
              Nota: En producción el registro estaría restringido.
            </small>
          </div>

          <button type="submit" className="btn btn-coffee w-100" disabled={loading}>
            {loading ? 'Creando...' : 'Crear cuenta'}
          </button>
        </form>

        <hr className="my-4" />
        <p className="text-center mb-0 small">
          ¿Ya tienes cuenta?{' '}
          <Link to="/login" className="text-coffee fw-semibold">
            Inicia sesión
          </Link>
        </p>
      </div>
    </div>
  )
}
