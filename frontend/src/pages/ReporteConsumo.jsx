import { useEffect, useState } from 'react'
import client, { apiErrorMessage } from '../api/client'
import PageHeader from '../components/PageHeader'
import {
  CartesianGrid,
  Legend,
  Line,
  LineChart,
  ResponsiveContainer,
  Tooltip,
  XAxis,
  YAxis,
} from 'recharts'

function isoToday() {
  return new Date().toISOString().slice(0, 10)
}

function isoDaysAgo(days) {
  const d = new Date()
  d.setDate(d.getDate() - days)
  return d.toISOString().slice(0, 10)
}

function formatBigDecimal(value) {
  if (value == null) return '0.00'
  const n = Number(value)
  if (Number.isNaN(n)) return String(value)
  return n.toFixed(2)
}

export default function ReporteConsumo() {
  const [insumos, setInsumos] = useState([])
  const [insumoId, setInsumoId] = useState('')
  const [desde, setDesde] = useState(isoDaysAgo(30))
  const [hasta, setHasta] = useState(isoToday())
  const [granularidad, setGranularidad] = useState('DIA')
  const [reporte, setReporte] = useState(null)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState(null)

  useEffect(() => {
    async function loadInsumos() {
      try {
        const { data } = await client.get('/api/insumos', { params: { size: 200 } })
        const lista = data.content ?? data
        setInsumos(lista)
        if (lista.length > 0 && !insumoId) setInsumoId(String(lista[0].id))
      } catch (err) {
        setError(apiErrorMessage(err))
      }
    }
    loadInsumos()
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [])

  async function generar() {
    if (!insumoId) {
      setError('Selecciona un insumo.')
      return
    }
    if (desde > hasta) {
      setError("La fecha 'desde' debe ser anterior o igual a 'hasta'.")
      return
    }
    try {
      setLoading(true)
      setError(null)
      const { data } = await client.get('/api/reportes/consumo', {
        params: { insumoId, desde, hasta, granularidad },
      })
      setReporte(data)
    } catch (err) {
      setReporte(null)
      setError(apiErrorMessage(err))
    } finally {
      setLoading(false)
    }
  }

  const chartData = reporte
    ? reporte.serie.map((p) => ({
        fecha: p.fecha,
        cantidad: p.cantidad,
        coste: Number(p.coste ?? 0),
      }))
    : []

  return (
    <div>
      <PageHeader
        icon="bi-bar-chart-line"
        title="Reporte de consumo"
        subtitle="Cantidad y coste consumido por insumo en un rango de fechas"
      />

      {error && <div className="alert alert-danger">{error}</div>}

      <div className="card mb-3">
        <div className="card-body">
          <div className="row g-3 align-items-end">
            <div className="col-md-3">
              <label className="form-label">Insumo</label>
              <select
                className="form-select"
                value={insumoId}
                onChange={(e) => setInsumoId(e.target.value)}
              >
                <option value="">— Selecciona —</option>
                {insumos.map((i) => (
                  <option key={i.id} value={i.id}>
                    {i.nombre} ({i.unidadMedida})
                  </option>
                ))}
              </select>
            </div>
            <div className="col-md-2">
              <label className="form-label">Desde</label>
              <input
                type="date"
                className="form-control"
                value={desde}
                onChange={(e) => setDesde(e.target.value)}
              />
            </div>
            <div className="col-md-2">
              <label className="form-label">Hasta</label>
              <input
                type="date"
                className="form-control"
                value={hasta}
                onChange={(e) => setHasta(e.target.value)}
              />
            </div>
            <div className="col-md-2">
              <label className="form-label">Granularidad</label>
              <select
                className="form-select"
                value={granularidad}
                onChange={(e) => setGranularidad(e.target.value)}
              >
                <option value="DIA">Día</option>
                <option value="SEMANA">Semana</option>
              </select>
            </div>
            <div className="col-md-3">
              <button className="btn btn-coffee w-100" onClick={generar} disabled={loading}>
                {loading ? (
                  <>
                    <span className="spinner-border spinner-border-sm me-2"></span>
                    Generando...
                  </>
                ) : (
                  <>
                    <i className="bi bi-graph-up me-1"></i>Generar reporte
                  </>
                )}
              </button>
            </div>
          </div>
        </div>
      </div>

      {reporte && (
        <>
          <div className="row g-3 mb-3">
            <div className="col-md-4">
              <div className="card h-100">
                <div className="card-body">
                  <div className="text-muted small">Insumo</div>
                  <div className="fs-5 fw-bold">{reporte.insumoNombre}</div>
                  <div className="text-muted small">
                    Rango: {reporte.desde} → {reporte.hasta} · {reporte.granularidad}
                  </div>
                </div>
              </div>
            </div>
            <div className="col-md-4">
              <div className="card h-100">
                <div className="card-body">
                  <div className="text-muted small">Cantidad consumida</div>
                  <div className="fs-3 fw-bold">
                    {reporte.totalCantidad.toFixed(2)}{' '}
                    <small className="text-muted">{reporte.unidadMedida}</small>
                  </div>
                </div>
              </div>
            </div>
            <div className="col-md-4">
              <div className="card h-100">
                <div className="card-body">
                  <div className="text-muted small">Coste estimado</div>
                  <div className="fs-3 fw-bold">{formatBigDecimal(reporte.costeTotal)} €</div>
                </div>
              </div>
            </div>
          </div>

          <div className="card mb-3">
            <div className="card-header">
              <i className="bi bi-activity me-2"></i>Evolución temporal
            </div>
            <div className="card-body" style={{ height: 320 }}>
              {chartData.length === 0 ? (
                <div className="empty-state">
                  <i className="bi bi-inbox fs-1"></i>
                  <p className="mt-2 mb-0">Sin movimientos en el rango seleccionado.</p>
                </div>
              ) : (
                <ResponsiveContainer width="100%" height="100%">
                  <LineChart data={chartData} margin={{ top: 10, right: 20, left: 0, bottom: 0 }}>
                    <CartesianGrid strokeDasharray="3 3" />
                    <XAxis dataKey="fecha" />
                    <YAxis yAxisId="left" />
                    <YAxis yAxisId="right" orientation="right" />
                    <Tooltip />
                    <Legend />
                    <Line
                      yAxisId="left"
                      type="monotone"
                      dataKey="cantidad"
                      name={`Cantidad (${reporte.unidadMedida})`}
                      stroke="#6f4e37"
                      strokeWidth={2}
                    />
                    <Line
                      yAxisId="right"
                      type="monotone"
                      dataKey="coste"
                      name="Coste (€)"
                      stroke="#c19a6b"
                      strokeWidth={2}
                    />
                  </LineChart>
                </ResponsiveContainer>
              )}
            </div>
          </div>

          <div className="card">
            <div className="card-header">
              <i className="bi bi-pie-chart me-2"></i>Desglose por tipo
            </div>
            <div className="card-body p-0">
              {reporte.desglosePorTipo.length === 0 ? (
                <div className="empty-state">
                  <i className="bi bi-inbox fs-1"></i>
                  <p className="mt-2 mb-0">Sin desglose.</p>
                </div>
              ) : (
                <div className="table-responsive">
                  <table className="table mb-0 align-middle">
                    <thead>
                      <tr>
                        <th>Tipo</th>
                        <th className="text-end">Cantidad</th>
                        <th className="text-end">Coste estimado</th>
                      </tr>
                    </thead>
                    <tbody>
                      {reporte.desglosePorTipo.map((d) => (
                        <tr key={d.tipo}>
                          <td>
                            <span className="badge bg-secondary">{d.tipo}</span>
                          </td>
                          <td className="text-end">
                            {d.cantidad.toFixed(2)} {reporte.unidadMedida}
                          </td>
                          <td className="text-end">{formatBigDecimal(d.coste)} €</td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
              )}
            </div>
          </div>
        </>
      )}
    </div>
  )
}
