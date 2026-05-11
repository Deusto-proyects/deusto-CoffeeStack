import { useEffect, useState } from 'react'
import { Link, useNavigate, useParams } from 'react-router-dom'
import client, { apiErrorMessage } from '../api/client'
import PageHeader from '../components/PageHeader'

export default function UsuarioForm() {
  const navigate = useNavigate()
  const { id } = useParams()
  const isEdit = Boolean(id)

  const [form, setForm] = useState({ username: '', password: '', rol: 'EMPLEADO' })
  const [loading, setLoading] = useState(isEdit)
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState(null)

  useEffect(() => {
    if (!isEdit) return
    let cancelled = false
    async function loadUser() {
      try {
        setLoading(true)
        const { data } = await client.get('/api/usuarios')
        const found = data.find((u) => String(u.id) === String(id))
        if (!found) {
          if (!cancelled) setError('Usuario no encontrado')
          return
        }
        if (!cancelled) {
          setForm({ username: found.username, password: '', rol: found.rol })
        }
      } catch (err) {
        if (!cancelled) setError(apiErrorMessage(err))
      } finally {
        if (!cancelled) setLoading(false)
      }
    }
    loadUser()
    return () => { cancelled = true }
  }, [id, isEdit])

  async function handleSubmit(e) {
    e.preventDefault()
    setSaving(true)
    setError(null)
    try {
      if (isEdit) {
        const payload = { username: form.username }
        if (form.password && form.password.length > 0) {
          payload.password = form.password
        }
        await client.put(`/api/usuarios/${id}`, payload)
      } else {
        await client.post('/api/usuarios', form)
      }
      navigate('/usuarios')
    } catch (err) {
      setError(apiErrorMessage(err))
    } finally {
      setSaving(false)
    }
  }

  if (loading) {
    return (
      <div className="text-center py-5">
        <div className="spinner-border text-coffee"></div>
      </div>
    )
  }

  return (
    <div>
      <PageHeader
        icon={isEdit ? 'bi-pencil-square' : 'bi-person-plus'}
        title={isEdit ? 'Editar usuario' : 'Nuevo usuario'}
      />

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
                  <label className="form-label">
                    Contraseña {isEdit ? '(opcional)' : '*'}
                  </label>
                  <input
                    type="password"
                    className="form-control"
                    required={!isEdit}
                    minLength={isEdit ? undefined : 6}
                    maxLength={100}
                    value={form.password}
                    onChange={(e) => setForm({ ...form, password: e.target.value })}
                    placeholder={isEdit ? 'Dejar en blanco para no cambiarla' : ''}
                  />
                  {!isEdit && <small className="text-muted">Mínimo 6 caracteres.</small>}
                  {isEdit && (
                    <small className="text-muted">
                      Si está en blanco se conserva la contraseña actual. Mínimo 6 caracteres si se cambia.
                    </small>
                  )}
                </div>
                {!isEdit && (
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
                )}
                {isEdit && (
                  <div className="mb-3">
                    <label className="form-label">Rol</label>
                    <input className="form-control" value={form.rol} disabled />
                    <small className="text-muted">
                      El rol se cambia desde la tabla con la acción "Rol".
                    </small>
                  </div>
                )}
              </div>
              <div className="card-footer bg-white d-flex gap-2">
                <button type="submit" className="btn btn-coffee" disabled={saving}>
                  {saving ? 'Guardando...' : isEdit ? 'Guardar cambios' : 'Crear usuario'}
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
