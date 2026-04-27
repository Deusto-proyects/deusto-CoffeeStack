import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import client, { apiErrorMessage } from '../api/client'
import PageHeader from '../components/PageHeader'

export default function ProveedorForm() {
  const navigate = useNavigate()
  const [form, setForm] = useState({ nombre: '', contacto: '', email: '', telefono: '' })
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState(null)

  async function handleSubmit(e) {
    e.preventDefault()
    setSaving(true)
    setError(null)
    const payload = { nombre: form.nombre }
    if (form.contacto) payload.contacto = form.contacto
    if (form.email) payload.email = form.email
    if (form.telefono) payload.telefono = form.telefono
    try {
      await client.post('/api/proveedores', payload)
      navigate('/proveedores')
    } catch (err) {
      setError(apiErrorMessage(err))
    } finally {
      setSaving(false)
    }
  }

  return (
    <div>
      <PageHeader icon="bi-truck" title="Nuevo proveedor" />

      <div className="row">
        <div className="col-md-8 col-lg-6">
          {error && <div className="alert alert-danger">{error}</div>}
          <div className="card">
            <form onSubmit={handleSubmit}>
              <div className="card-body">
                <div className="mb-3">
                  <label className="form-label">Nombre *</label>
                  <input
                    type="text"
                    className="form-control"
                    required
                    maxLength={120}
                    value={form.nombre}
                    onChange={(e) => setForm({ ...form, nombre: e.target.value })}
                  />
                </div>
                <div className="mb-3">
                  <label className="form-label">Persona de contacto</label>
                  <input
                    type="text"
                    className="form-control"
                    maxLength={120}
                    value={form.contacto}
                    onChange={(e) => setForm({ ...form, contacto: e.target.value })}
                  />
                </div>
                <div className="mb-3">
                  <label className="form-label">Email</label>
                  <input
                    type="email"
                    className="form-control"
                    maxLength={120}
                    value={form.email}
                    onChange={(e) => setForm({ ...form, email: e.target.value })}
                  />
                </div>
                <div className="mb-3">
                  <label className="form-label">Teléfono</label>
                  <input
                    type="text"
                    className="form-control"
                    maxLength={30}
                    value={form.telefono}
                    onChange={(e) => setForm({ ...form, telefono: e.target.value })}
                  />
                </div>
              </div>
              <div className="card-footer bg-white d-flex gap-2">
                <button type="submit" className="btn btn-coffee" disabled={saving}>
                  {saving ? 'Guardando...' : 'Crear proveedor'}
                </button>
                <Link to="/proveedores" className="btn btn-outline-secondary">
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
