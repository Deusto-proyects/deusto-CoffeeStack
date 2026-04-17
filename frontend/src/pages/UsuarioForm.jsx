import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import client, { apiErrorMessage } from '../api/client'
import PageHeader from '../components/PageHeader'

export default function UsuarioForm() {
  const navigate = useNavigate()
  const [form, setForm] = useState({ username: '', password: '', rol: 'EMPLEADO' })
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState(null)

  async function handleSubmit(e) {
    e.preventDefault()
    setSaving(true)
    setError(null)
    try {
      await client.post('/api/usuarios', form)
      navigate('/usuarios')
    } catch (err) {
      setError(apiErrorMessage(err))
    } finally {
      setSaving(false)
    }
  }

  return (
    <div>
      <PageHeader icon="bi-person-plus" title="Nuevo usuario" />

      <div className="row">
        <div className="col-md-8 col-lg-6">
          {error && <div className="alert alert-danger">{error}</div>}

          <div className="card">
            <form onSubmit={handleSubmit}>
              <div className="card-body">
                <div className="mb-3">
                  <label className="form-label">Nombre de usuario *</label>
                  <input
                    type="text"
                    className="form-control"
                    required
                    minLength={3}
                    maxLength={60}
                    value={form.username}
                    onChange={(e) => setForm({ ...form, username: e.target.value })}
                  />
                </div>
                <div className="mb-3">
                  <label className="form-label">Contraseña *</label>
                  <input
                    type="password"
                    className="form-control"
                    required
                    minLength={6}
                    maxLength={100}
                    value={form.password}
                    onChange={(e) => setForm({ ...form, password: e.target.value })}
                  />
                  <small className="text-muted">Mínimo 6 caracteres.</small>
                </div>
                <div className="mb-3">
                  <label className="form-label">Rol *</label>
                  <select
                    className="form-select"
                    value={form.rol}
                    onChange={(e) => setForm({ ...form, rol: e.target.value })}
                  >
                    <option value="EMPLEADO">EMPLEADO</option>
                    <option value="PROPIETARIO">PROPIETARIO</option>
                    <option value="ROOT">ROOT</option>
                  </select>
                </div>
              </div>
              <div className="card-footer bg-white d-flex gap-2">
                <button type="submit" className="btn btn-coffee" disabled={saving}>
                  {saving ? 'Creando...' : 'Crear usuario'}
                </button>
                <Link to="/usuarios" className="btn btn-outline-secondary">
                  Cancelar
                </Link>
              </div>
            </form>
          </div>
        </div>
      </div>
    </div>
  )
}
