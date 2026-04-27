import { useState, useEffect } from 'react'
import { Link, useNavigate, useParams } from 'react-router-dom'
import client, { apiErrorMessage } from '../api/client'
import PageHeader from '../components/PageHeader'

export default function ProveedorForm() {
  const navigate = useNavigate()
  const { id } = useParams()
  const [form, setForm] = useState({ nombre: '', contacto: '', email: '', telefono: '' })
  const [saving, setSaving] = useState(false)
  const [loading, setLoading] = useState(!!id)
  const [error, setError] = useState(null)

  useEffect(() => {
    if (!id) return
    client
      .get(`/api/proveedores/${id}`)
      .then(({ data }) => {
        setForm({
          nombre: data.nombre || '',
          contacto: data.contacto || '',
          email: data.email || '',
          telefono: data.telefono || '',
        })
      })
      .catch((err) => setError(apiErrorMessage(err)))
      .finally(() => setLoading(false))
  }, [id])

  async function handleSubmit(e) {
    e.preventDefault()
    setSaving(true)
    setError(null)
    const payload = { nombre: form.nombre }
    if (form.contacto) payload.contacto = form.contacto
    if (form.email) payload.email = form.email
    if (form.telefono) payload.telefono = form.telefono
    try {
      if (id) {
        await client.put(`/api/proveedores/${id}`, payload)
      } else {
        await client.post('/api/proveedores', payload)
      }
      navigate('/proveedores')
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
      <PageHeader icon="bi-truck" title={id ? 'Editar proveedor' : 'Nuevo proveedor'} />

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
                  {saving ? 'Guardando...' : id ? 'Actualizar proveedor' : 'Crear proveedor'}
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
