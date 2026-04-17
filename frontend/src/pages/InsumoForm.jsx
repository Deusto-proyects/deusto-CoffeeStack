import { useEffect, useState } from 'react'
import { useNavigate, useParams, Link } from 'react-router-dom'
import client, { apiErrorMessage } from '../api/client'
import PageHeader from '../components/PageHeader'

export default function InsumoForm() {
  const { id } = useParams()
  const isEdit = !!id
  const navigate = useNavigate()

  const [form, setForm] = useState({
    nombre: '',
    unidadMedida: '',
    stockMinimoAlerta: 0,
  })
  const [loading, setLoading] = useState(false)
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState(null)

  useEffect(() => {
    if (isEdit) {
      setLoading(true)
      client
        .get(`/api/insumos/${id}`)
        .then(({ data }) =>
          setForm({
            nombre: data.nombre,
            unidadMedida: data.unidadMedida,
            stockMinimoAlerta: data.stockMinimoAlerta,
          })
        )
        .catch((e) => setError(apiErrorMessage(e)))
        .finally(() => setLoading(false))
    }
  }, [id, isEdit])

  async function handleSubmit(e) {
    e.preventDefault()
    setError(null)
    setSaving(true)
    try {
      if (isEdit) {
        await client.put(`/api/insumos/${id}`, form)
      } else {
        await client.post('/api/insumos', form)
      }
      navigate('/insumos')
    } catch (err) {
      setError(apiErrorMessage(err))
    } finally {
      setSaving(false)
    }
  }

  return (
    <div>
      <PageHeader
        icon="bi-box-seam"
        title={isEdit ? 'Editar insumo' : 'Nuevo insumo'}
      />

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
                      value={form.nombre}
                      onChange={(e) => setForm({ ...form, nombre: e.target.value })}
                    />
                  </div>
                  <div className="mb-3">
                    <label className="form-label">Unidad de medida *</label>
                    <input
                      type="text"
                      className="form-control"
                      required
                      maxLength={30}
                      placeholder="kg, litros, unidades..."
                      value={form.unidadMedida}
                      onChange={(e) => setForm({ ...form, unidadMedida: e.target.value })}
                    />
                  </div>
                  <div className="mb-3">
                    <label className="form-label">Stock mínimo de alerta *</label>
                    <input
                      type="number"
                      step="0.01"
                      min="0"
                      className="form-control"
                      required
                      value={form.stockMinimoAlerta}
                      onChange={(e) =>
                        setForm({ ...form, stockMinimoAlerta: parseFloat(e.target.value) })
                      }
                    />
                    <small className="text-muted">
                      Cuando el stock total caiga por debajo se mostrará alerta.
                    </small>
                  </div>
                </div>
                <div className="card-footer bg-white d-flex gap-2">
                  <button type="submit" className="btn btn-coffee" disabled={saving}>
                    {saving ? 'Guardando...' : isEdit ? 'Actualizar' : 'Crear'}
                  </button>
                  <Link to="/insumos" className="btn btn-outline-secondary">
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
