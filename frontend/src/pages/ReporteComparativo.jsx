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

// ─── Paleta de colores para las líneas del gráfico ────────────────────────────
const COLORES = [
  '#6f4e37', '#c19a6b', '#3b82f6', '#10b981', '#f59e0b',
  '#ef4444', '#8b5cf6', '#06b6d4', '#ec4899', '#84cc16',
]

function isoToday() {
  return new Date().toISOString().slice(0, 10)
}
function isoDaysAgo(days) {
  const d = new Date()
  d.setDate(d.getDate() - days)
  return d.toISOString().slice(0, 10)
}
function formatEur(value) {
  if (value == null) return '0.00'
  return Number(value).toFixed(2)
}

/**
 * Combina las series de todos los insumos en un array plano para Recharts.
 * Cada objeto tiene 'fecha' y una clave por insumo: { fecha, "Café molido": 5.0, ... }
 */
function combinarSeries(insumos) {
  const mapaFecha = {}
  for (const ins of insumos) {
    for (const p of ins.serie) {
      if (!mapaFecha[p.fecha]) mapaFecha[p.fecha] = { fecha: p.fecha }
      mapaFecha[p.fecha][ins.insumoNombre] = p.cantidad
    }
  }
  return Object.values(mapaFecha).sort((a, b) => a.fecha.localeCompare(b.fecha))
}

export default function ReporteComparativo() {
  const [insumos, setInsumos] = useState([])
  const [seleccionados, setSeleccionados] = useState([])
  const [desde, setDesde] = useState(isoDaysAgo(30))
  const [hasta, setHasta] = useState(isoToday())
  const [granularidad, setGranularidad] = useState('DIA')
  const [reporte, setReporte] = useState(null)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState(null)

  // Carga lista de insumos al montar
  useEffect(() => {
    client.get('/api/insumos', { params: { size: 200 } })
      .then(r => {
        const lista = r.data.content ?? r.data
        setInsumos(lista)
      })
      .catch(e => setError(apiErrorMessage(e)))
  }, [])

  function toggleInsumo(id) {
    setSeleccionados(prev =>
      prev.includes(id) ? prev.filter(i => i !== id) : [...prev, id]
    )
  }

  function seleccionarTodos() {
    setSeleccionados(insumos.map(i => i.id))
  }

  function limpiarSeleccion() {
    setSeleccionados([])
  }

  async function generar() {
    if (desde > hasta) {
      setError("La fecha 'Desde' debe ser anterior o igual a 'Hasta'.")
      return
    }
    try {
      setLoading(true)
      setError(null)
      const params = new URLSearchParams()
      params.append('desde', desde)
      params.append('hasta', hasta)
      params.append('granularidad', granularidad)
      // insumoIds como parámetros repetidos (Spring acepta List<Long>)
      for (const id of seleccionados) params.append('insumoIds', id)

      const { data } = await client.get('/api/reportes/consumo/comparativa', { params })
      setReporte(data)
    } catch (e) {
      setReporte(null)
      setError(apiErrorMessage(e))
    } finally {
      setLoading(false)
    }
  }

  const chartData = reporte ? combinarSeries(reporte.insumos) : []
  const insumosConsumo = reporte
    ? [...reporte.insumos].sort((a, b) => b.totalCantidad - a.totalCantidad)
    : []

  return (
    <div>
      <PageHeader
        icon="bi-bar-chart-steps"
        title="Comparativa de consumo"
        subtitle="Compara el consumo de múltiples insumos en el mismo rango de fechas para identificar patrones y costes operativos"
      />

      {error && <div className="alert alert-danger">{error}</div>}

      {/* ── Filtros ─────────────────────────────────────────────────────────── */}
      <div className="card mb-3">
        <div className="card-header">
          <i className="bi bi-funnel me-2" />Parámetros del reporte
        </div>
        <div className="card-body">
          <div className="row g-3 align-items-end">
            <div className="col-md-2">
              <label className="form-label">Desde</label>
              <input
                id="input-comparativo-desde"
                type="date"
                className="form-control"
                value={desde}
                onChange={e => setDesde(e.target.value)}
              />
            </div>
            <div className="col-md-2">
              <label className="form-label">Hasta</label>
              <input
                id="input-comparativo-hasta"
                type="date"
                className="form-control"
                value={hasta}
                onChange={e => setHasta(e.target.value)}
              />
            </div>
            <div className="col-md-2">
              <label className="form-label">Granularidad</label>
              <select
                id="select-comparativo-granularidad"
                className="form-select"
                value={granularidad}
                onChange={e => setGranularidad(e.target.value)}
              >
                <option value="DIA">Día</option>
                <option value="SEMANA">Semana</option>
                <option value="MES">Mes</option>
              </select>
            </div>
            <div className="col-md-4">
              <div className="d-flex gap-2">
                <button
                  id="btn-comparativo-generar"
                  className="btn btn-coffee flex-grow-1"
                  onClick={generar}
                  disabled={loading}
                >
                  {loading ? (
                    <><span className="spinner-border spinner-border-sm me-2" />Generando...</>
                  ) : (
                    <><i className="bi bi-graph-up me-1" />Generar comparativa</>
                  )}
                </button>
              </div>
            </div>
          </div>

          {/* Selector de insumos */}
          <div className="mt-3">
            <div className="d-flex align-items-center justify-content-between mb-2">
              <label className="form-label mb-0 fw-semibold">
                Insumos a comparar
                <span className="text-muted fw-normal ms-1 small">
                  (vacío = todos los activos)
                </span>
              </label>
              <div className="btn-group btn-group-sm">
                <button className="btn btn-outline-secondary" onClick={seleccionarTodos}>
                  Todos
                </button>
                <button className="btn btn-outline-secondary" onClick={limpiarSeleccion}>
                  Ninguno
                </button>
              </div>
            </div>
            <div
              className="border rounded p-2"
              style={{ maxHeight: 200, overflowY: 'auto', display: 'flex', flexWrap: 'wrap', gap: '0.4rem' }}
            >
              {insumos.map(ins => (
                <div key={ins.id} className="form-check form-check-inline mb-0">
                  <input
                    className="form-check-input"
                    type="checkbox"
                    id={`chk-insumo-${ins.id}`}
                    checked={seleccionados.includes(ins.id)}
                    onChange={() => toggleInsumo(ins.id)}
                  />
                  <label className="form-check-label small" htmlFor={`chk-insumo-${ins.id}`}>
                    {ins.nombre} <span className="text-muted">({ins.unidadMedida})</span>
                  </label>
                </div>
              ))}
            </div>
          </div>
        </div>
      </div>

      {/* ── Resultados ──────────────────────────────────────────────────────── */}
      {reporte && (
        <>
          {/* KPI global */}
          <div className="row g-3 mb-3">
            <div className="col-md-4">
              <div className="card h-100 border-0 bg-light">
                <div className="card-body">
                  <div className="text-muted small">Rango analizado</div>
                  <div className="fs-6 fw-bold">
                    {reporte.desde} → {reporte.hasta}
                  </div>
                  <div className="text-muted small">{reporte.granularidad} · {reporte.insumos.length} insumo(s)</div>
                </div>
              </div>
            </div>
            <div className="col-md-4">
              <div className="card h-100 border-0 bg-light">
                <div className="card-body">
                  <div className="text-muted small">Coste total estimado</div>
                  <div className="fs-3 fw-bold text-danger">
                    {formatEur(reporte.costeTotalGlobal)} €
                  </div>
                </div>
              </div>
            </div>
            <div className="col-md-4">
              <div className="card h-100 border-0 bg-light">
                <div className="card-body">
                  <div className="text-muted small">Insumo con mayor consumo</div>
                  {insumosConsumo.length > 0 ? (
                    <>
                      <div className="fs-5 fw-bold">{insumosConsumo[0].insumoNombre}</div>
                      <div className="text-muted small">
                        {insumosConsumo[0].totalCantidad.toFixed(2)} {insumosConsumo[0].unidadMedida}
                      </div>
                    </>
                  ) : (
                    <div className="text-muted small">Sin datos</div>
                  )}
                </div>
              </div>
            </div>
          </div>

          {/* Gráfico comparativo */}
          <div className="card mb-3">
            <div className="card-header">
              <i className="bi bi-graph-up me-2" />Evolución temporal comparada (cantidad consumida)
            </div>
            <div className="card-body" style={{ height: 380 }}>
              {chartData.length === 0 ? (
                <div className="empty-state">
                  <i className="bi bi-inbox fs-1" />
                  <p className="mt-2 mb-0">Sin movimientos en el rango seleccionado.</p>
                </div>
              ) : (
                <ResponsiveContainer width="100%" height="100%">
                  <LineChart data={chartData} margin={{ top: 10, right: 30, left: 0, bottom: 0 }}>
                    <CartesianGrid strokeDasharray="3 3" />
                    <XAxis dataKey="fecha" tick={{ fontSize: 11 }} />
                    <YAxis tick={{ fontSize: 11 }} />
                    <Tooltip />
                    <Legend />
                    {reporte.insumos.map((ins, idx) => (
                      <Line
                        key={ins.insumoId}
                        type="monotone"
                        dataKey={ins.insumoNombre}
                        stroke={COLORES[idx % COLORES.length]}
                        strokeWidth={2}
                        dot={false}
                      />
                    ))}
                  </LineChart>
                </ResponsiveContainer>
              )}
            </div>
          </div>

          {/* Tabla resumen por insumo */}
          <div className="card">
            <div className="card-header">
              <i className="bi bi-table me-2" />Resumen por insumo
            </div>
            <div className="card-body p-0">
              {insumosConsumo.length === 0 ? (
                <div className="empty-state">
                  <i className="bi bi-inbox fs-1" />
                  <p className="mt-2 mb-0">Sin datos.</p>
                </div>
              ) : (
                <div className="table-responsive">
                  <table className="table mb-0 align-middle">
                    <thead>
                      <tr>
                        <th>#</th>
                        <th>Insumo</th>
                        <th className="text-end">Cantidad total</th>
                        <th className="text-end">Coste estimado</th>
                        <th>Distribución</th>
                      </tr>
                    </thead>
                    <tbody>
                      {insumosConsumo.map((ins, idx) => {
                        const costsNum = Number(reporte.costeTotalGlobal) || 1
                        const pct = Number(ins.costeTotal) / costsNum * 100
                        return (
                          <tr key={ins.insumoId}>
                            <td>
                              <span
                                className="badge rounded-circle d-inline-flex align-items-center justify-content-center"
                                style={{
                                  background: COLORES[idx % COLORES.length],
                                  width: 24, height: 24, fontSize: 11,
                                }}
                              >
                                {idx + 1}
                              </span>
                            </td>
                            <td>
                              <div className="fw-semibold">{ins.insumoNombre}</div>
                              <div className="text-muted small">{ins.unidadMedida}</div>
                            </td>
                            <td className="text-end">
                              {ins.totalCantidad.toFixed(2)}{' '}
                              <small className="text-muted">{ins.unidadMedida}</small>
                            </td>
                            <td className="text-end fw-semibold">
                              {formatEur(ins.costeTotal)} €
                            </td>
                            <td style={{ minWidth: 120 }}>
                              <div className="progress" style={{ height: 6 }}>
                                <div
                                  className="progress-bar"
                                  style={{
                                    width: `${pct.toFixed(1)}%`,
                                    background: COLORES[idx % COLORES.length],
                                  }}
                                />
                              </div>
                              <div className="text-muted small mt-1">{pct.toFixed(1)}%</div>
                            </td>
                          </tr>
                        )
                      })}
                    </tbody>
                    <tfoot className="table-light">
                      <tr>
                        <td colSpan={3} className="fw-bold text-end">Total</td>
                        <td className="fw-bold text-end text-danger">
                          {formatEur(reporte.costeTotalGlobal)} €
                        </td>
                        <td />
                      </tr>
                    </tfoot>
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
