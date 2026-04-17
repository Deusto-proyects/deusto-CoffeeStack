import { useEffect, useState } from 'react'
import { Link, useNavigate, useSearchParams } from 'react-router-dom'
import client, { apiErrorMessage } from '../api/client'
import PageHeader from '../components/PageHeader'

export default function AjusteForm() {
  const navigate = useNavigate()
  const [params] = useSearchParams()
  const [insumos, setInsumos] = useState([])
  const [insumoId, setInsumoId] = useState('')
  const [lotes, setLotes] = useState([])
  const [form, setForm] = useState({
    loteId: params.get('loteId') || '',
    tipoMovimiento: 'MERMA',
    cantidad: '',
    motivo: '',
  })
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState(null)

  useEffect(() => {
    client
      .get('/api/insumos', { params: { size: 200 } })
      .then(({ data }) => setInsumos(data.content ?? data))
      .catch((e) => setError(apiErrorMessage(e)))
  }, [])

  useEffect(() => {
    if (!insumoId) {
      setLotes([])
      return
    }
    client
      .get(`/api/lotes/insumo/${insumoId}`)
      .then(({ data }) => setLotes(data))
      .catch((e) => setError(apiErrorMessage(e)))
  }, [insumoId])

  async function handleSubmit(e) {
    e.preventDefault()
    setSaving(true)
    setError(null)
    try {
      await client.post('/api/ajustes', {
        loteId: parseInt(form.loteId, 10),
        tipoMovimiento: form.tipoMovimiento,
        cantidad: parseFloat(form.cantidad),
        motivo: form.motivo,
      })
      navigate('/ajustes')
    } catch (err) {
      setError(apiErrorMessage(err))
    } finally {
      setSaving(false)
    }
  }

  const loteSeleccionado = lotes.find((l) => String(l.id) === String(form.loteId))

  return (
    <div>
      <PageHeader
        icon="bi-clipboard-plus"
        title="Registrar ajuste"
        subtitle="Merma, rotura o corrección manual"
      />

      <div className="row">
        <div className="col-md-8 col-lg-6">
          {error && <div className="alert alert-danger">{error}</div>}

          <div className="card">
            <form onSubmit={handleSubmit}>
              <div className="card-body">
                {!params.get('loteId') && (
                  <div className="mb-3">
                    <label className="form-label">Insumo</label>
                    <select
                      className="form-select"
                      value={insumoId}
                      onChange={(e) => setInsumoId(e.target.value)}
                    >
                      <option value="">— Seleccionar primero un insumo —</option>
                      {insumos.map((i) => (
                        <option key={i.id} value={i.id}>
                          {i.nombre}
                        </option>
                      ))}
                    </select>
                  </div>
                )}

                <div className="mb-3">
                  <label className="form-label">Lote afectado *</label>
                  <select
                    className="form-select"
                    required
                    value={form.loteId}
                    onChange={(e) => setForm({ ...form, loteId: e.target.value })}
                    disabled={!insumoId && !params.get('loteId')}
                  >
                    <option value="">— Seleccionar lote —</option>
                    {lotes.map((l) => (
                      <option key={l.id} value={l.id}>
                        {l.numeroLote} (actual: {l.cantidadActual})
                      </option>
                    ))}
                    {params.get('loteId') && lotes.length === 0 && (
                      <option value={params.get('loteId')}>
                        Lote #{params.get('loteId')}
                      </option>
                    )}
                  </select>
                  {loteSeleccionado && (
                    <small className="text-muted">
                      Cantidad actual en el lote: <strong>{loteSeleccionado.cantidadActual}</strong>
                    </small>
                  )}
                </div>

                <div className="mb-3">
                  <label className="form-label">Tipo de movimiento *</label>
                  <select
                    className="form-select"
                    required
                    value={form.tipoMovimiento}
                    onChange={(e) => setForm({ ...form, tipoMovimiento: e.target.value })}
                  >
                    <option value="MERMA">MERMA (pérdida por caducidad/deterioro)</option>
                    <option value="ROTURA">ROTURA (daño físico)</option>
                    <option value="AJUSTE_POSITIVO">AJUSTE_POSITIVO (suma al stock)</option>
                    <option value="AJUSTE_NEGATIVO">AJUSTE_NEGATIVO (resta al stock)</option>
                  </select>
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
                  <small className="text-muted">
                    Cantidad absoluta afectada (positiva siempre).
                  </small>
                </div>

                <div className="mb-3">
                  <label className="form-label">Motivo *</label>
                  <textarea
                    className="form-control"
                    rows={3}
                    required
                    minLength={5}
                    maxLength={300}
                    placeholder="Ej. Caducidad detectada en inventario físico"
                    value={form.motivo}
                    onChange={(e) => setForm({ ...form, motivo: e.target.value })}
                  />
                </div>
              </div>
              <div className="card-footer bg-white d-flex gap-2">
                <button type="submit" className="btn btn-coffee" disabled={saving}>
                  {saving ? 'Guardando...' : 'Registrar movimiento'}
                </button>
                <Link to="/ajustes" className="btn btn-outline-secondary">
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
