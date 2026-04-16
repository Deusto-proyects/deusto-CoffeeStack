import { useEffect, useState } from 'react'
import { useNavigate, useSearchParams, Link } from 'react-router-dom'
import client, { apiErrorMessage } from '../api/client'
import PageHeader from '../components/PageHeader'

export default function LoteForm() {
  const navigate = useNavigate()
  const [params] = useSearchParams()
  const [insumos, setInsumos] = useState([])
  const [proveedores, setProveedores] = useState([])
  const [form, setForm] = useState({
    insumoId: params.get('insumoId') || '',
    proveedorId: '',
    numeroLote: '',
    cantidad: '',
    fechaVencimiento: '',
  })
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState(null)

  useEffect(() => {
    Promise.all([
      client.get('/api/insumos', { params: { size: 200 } }),
      client.get('/api/proveedores', { params: { size: 200 } }),
    ])
      .then(([r1, r2]) => {
        setInsumos(r1.data.content ?? r1.data)
        setProveedores(r2.data.content ?? r2.data)
      })
      .catch((e) => setError(apiErrorMessage(e)))
  }, [])

  async function handleSubmit(e) {
    e.preventDefault()
    setSaving(true)
    setError(null)
    const payload = {
      insumoId: parseInt(form.insumoId, 10),
      numeroLote: form.numeroLote,
      cantidad: parseFloat(form.cantidad),
    }
    if (form.proveedorId) payload.proveedorId = parseInt(form.proveedorId, 10)
    if (form.fechaVencimiento) payload.fechaVencimiento = form.fechaVencimiento

    try {
      await client.post('/api/lotes', payload)
      navigate('/lotes')
    } catch (err) {
      setError(apiErrorMessage(err))
    } finally {
      setSaving(false)
    }
  }

  return (
    <div>
      <PageHeader icon="bi-boxes" title="Recibir lote" subtitle="Registra una nueva recepción" />

      <div className="row">
        <div className="col-md-8 col-lg-6">
          {error && <div className="alert alert-danger">{error}</div>}

          <div className="card">
            <form onSubmit={handleSubmit}>
              <div className="card-body">
                <div className="mb-3">
                  <label className="form-label">Insumo *</label>
                  <select
                    className="form-select"
                    required
                    value={form.insumoId}
                    onChange={(e) => setForm({ ...form, insumoId: e.target.value })}
                  >
                    <option value="">— Seleccionar —</option>
                    {insumos.map((i) => (
                      <option key={i.id} value={i.id}>
                        {i.nombre} ({i.unidadMedida})
                      </option>
                    ))}
                  </select>
                </div>
                <div className="mb-3">
                  <label className="form-label">Proveedor</label>
                  <select
                    className="form-select"
                    value={form.proveedorId}
                    onChange={(e) => setForm({ ...form, proveedorId: e.target.value })}
                  >
                    <option value="">— Sin proveedor —</option>
                    {proveedores.map((p) => (
                      <option key={p.id} value={p.id}>
                        {p.nombre}
                      </option>
                    ))}
                  </select>
                </div>
                <div className="mb-3">
                  <label className="form-label">Número de lote *</label>
                  <input
                    type="text"
                    className="form-control"
                    required
                    maxLength={60}
                    placeholder="Ej. CAFE-2026-001"
                    value={form.numeroLote}
                    onChange={(e) => setForm({ ...form, numeroLote: e.target.value })}
                  />
                </div>
                <div className="mb-3">
                  <label className="form-label">Cantidad *</label>
                  <input
                    type="number"
                    step="0.001"
                    min="0.001"
                    className="form-control"
                    required
                    value={form.cantidad}
                    onChange={(e) => setForm({ ...form, cantidad: e.target.value })}
                  />
                </div>
                <div className="mb-3">
                  <label className="form-label">Fecha de vencimiento</label>
                  <input
                    type="date"
                    className="form-control"
                    value={form.fechaVencimiento}
                    onChange={(e) => setForm({ ...form, fechaVencimiento: e.target.value })}
                  />
                  <small className="text-muted">Opcional para insumos sin caducidad.</small>
                </div>
              </div>
              <div className="card-footer bg-white d-flex gap-2">
                <button type="submit" className="btn btn-coffee" disabled={saving}>
                  {saving ? 'Guardando...' : 'Registrar lote'}
                </button>
                <Link to="/lotes" className="btn btn-outline-secondary">
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
