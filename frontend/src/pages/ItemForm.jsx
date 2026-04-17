import { useEffect, useState } from 'react'
import { useNavigate, useParams, Link } from 'react-router-dom'
import client, { apiErrorMessage } from '../api/client'
import PageHeader from '../components/PageHeader'

export default function ItemForm() {
  const { id } = useParams()
  const isEdit = !!id
  const navigate = useNavigate()

  const [form, setForm] = useState({ name: '', description: '' })
  const [loading, setLoading] = useState(false)
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState(null)

  useEffect(() => {
    if (isEdit) {
      setLoading(true)
      client
        .get(`/api/items/${id}`)
        .then(({ data }) => setForm({ name: data.name, description: data.description || '' }))
        .catch((e) => setError(apiErrorMessage(e)))
        .finally(() => setLoading(false))
    }
  }, [id, isEdit])

  async function handleSubmit(e) {
    e.preventDefault()
    setSaving(true)
    setError(null)
    try {
      if (isEdit) {
        await client.put(`/api/items/${id}`, form)
      } else {
        await client.post('/api/items', form)
      }
      navigate('/items')
    } catch (err) {
      setError(apiErrorMessage(err))
    } finally {
      setSaving(false)
    }
  }

  return (
    <div>
      <PageHeader icon="bi-list-ul" title={isEdit ? 'Editar producto' : 'Nuevo producto'} />

      <div className="row">
        <div className="col-md-8 col-lg-6">
          {error && <div className="alert alert-danger">{error}</div>}
          {loading ? (
            <div className="spinner-border text-coffee"></div>
          ) : (
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
                      value={form.name}
                      onChange={(e) => setForm({ ...form, name: e.target.value })}
                    />
                  </div>
                  <div className="mb-3">
                    <label className="form-label">Descripción</label>
                    <textarea
                      className="form-control"
                      rows={3}
                      maxLength={500}
                      value={form.description}
                      onChange={(e) => setForm({ ...form, description: e.target.value })}
                    />
                  </div>
                </div>
                <div className="card-footer bg-white d-flex gap-2">
                  <button type="submit" className="btn btn-coffee" disabled={saving}>
                    {saving ? 'Guardando...' : isEdit ? 'Actualizar' : 'Crear'}
                  </button>
                  <Link to="/items" className="btn btn-outline-secondary">
                    Cancelar
                  </Link>
                </div>
              </form>
            </div>
          )}
        </div>
      </div>
    </div>
  )
}
