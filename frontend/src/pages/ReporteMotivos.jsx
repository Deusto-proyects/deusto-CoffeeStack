import { useCallback, useEffect, useMemo, useState } from 'react'
import {
  BarChart,
  Bar,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  ResponsiveContainer,
  Legend,
} from 'recharts'
import client, { apiErrorMessage } from '../api/client'
import PageHeader from '../components/PageHeader'
import { downloadCsv } from '../utils/csvDownload'

/**
 * Reporte agregado de movimientos (mermas, roturas, ajustes) agrupados por
 * motivo + tipo. Cubre la issue #24 — permite detectar patrones recurrentes
 * de desperdicio y enfocar mejoras de proceso.
 *
 * Solo accesible para PROPIETARIO y ROOT.
 */
const TIPOS = [
  { value: '', label: 'Todos los tipos' },
  { value: 'MERMA', label: 'MERMA' },
  { value: 'ROTURA', label: 'ROTURA' },
  { value: 'AJUSTE_POSITIVO', label: 'AJUSTE POSITIVO' },
  { value: 'AJUSTE_NEGATIVO', label: 'AJUSTE NEGATIVO' },
  { value: 'VENTA', label: 'VENTA' },
]

const BADGE_STYLES = {
  MERMA: { bg: '#f59e0b', color: '#1c1c1c' },
  ROTURA: { bg: '#ef4444', color: '#fff' },
  AJUSTE_POSITIVO: { bg: '#22c55e', color: '#fff' },
  AJUSTE_NEGATIVO: { bg: '#64748b', color: '#fff' },
  VENTA: { bg: '#6f4e37', color: '#fff' },
}

function TipoBadge({ tipo }) {
  const style = BADGE_STYLES[tipo] || { bg: '#64748b', color: '#fff' }
  return (
    <span
      style={{
        backgroundColor: style.bg,
        color: style.color,
        padding: '2px 10px',
        borderRadius: '999px',
        fontSize: '0.75rem',
        fontWeight: 600,
        letterSpacing: '0.03em',
        whiteSpace: 'nowrap',
      }}
    >
      {tipo}
    </span>
  )
}

function fmtFecha(iso) {
  if (!iso) return '—'
  return new Date(iso).toLocaleDateString()
}

function fmtNum(n) {
  if (typeof n !== 'number' || Number.isNaN(n)) return '—'
  return n.toLocaleString(undefined, { maximumFractionDigits: 2 })
}

function ultimosNDias(n) {
  const hasta = new Date()
  const desde = new Date()
  desde.setDate(hasta.getDate() - n)
  const iso = (d) => d.toISOString().slice(0, 10)
  return { desde: iso(desde), hasta: iso(hasta) }
}

export default function ReporteMotivos() {
  const initial = useMemo(() => {
    const { desde, hasta } = ultimosNDias(90)
    return { tipo: '', desde, hasta }
  }, [])

  const [filters, setFilters] = useState(initial)
  const [applied, setApplied] = useState(initial)
  const [filas, setFilas] = useState([])
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState(null)

  const cargar = useCallback(async (params) => {
    setLoading(true)
    setError(null)
    try {
      const { data } = await client.get('/api/ajustes/reporte-motivos', {
        params: {
          tipo: params.tipo || undefined,
          desde: params.desde || undefined,
          hasta: params.hasta || undefined,
        },
      })
      setFilas(data)
    } catch (err) {
      setError(apiErrorMessage(err))
      setFilas([])
    } finally {
      setLoading(false)
    }
  }, [])

  useEffect(() => {
    cargar(applied)
  }, [applied, cargar])

  function aplicar(e) {
    e?.preventDefault()
    setApplied(filters)
  }

  function reset() {
    setFilters(initial)
    setApplied(initial)
  }

  // ── métricas resumen ────────────────────────────────────────────────────
  const totales = useMemo(() => {
    const desperdicio = filas
      .filter((f) => f.tipoMovimiento === 'MERMA' || f.tipoMovimiento === 'ROTURA')
      .reduce((acc, f) => acc + (f.cantidadTotal || 0), 0)
    const incidenciasDesperdicio = filas
      .filter((f) => f.tipoMovimiento === 'MERMA' || f.tipoMovimiento === 'ROTURA')
      .reduce((acc, f) => acc + (f.numIncidencias || 0), 0)
    const incidenciasTotales = filas.reduce((acc, f) => acc + (f.numIncidencias || 0), 0)
    const motivosUnicos = new Set(filas.map((f) => f.motivo)).size
    return { desperdicio, incidenciasDesperdicio, incidenciasTotales, motivosUnicos }
  }, [filas])

  // Preparamos datos para el gráfico: limitamos a los 10 con mayor cantidad
  const chartData = useMemo(() => {
    return [...filas]
      .sort((a, b) => b.cantidadTotal - a.cantidadTotal)
      .slice(0, 10)
      .map((f) => ({
        name: f.motivo.length > 20 ? f.motivo.substring(0, 20) + '...' : f.motivo,
        tipo: f.tipoMovimiento,
        cantidad: f.cantidadTotal,
        incidencias: f.numIncidencias,
        fullName: f.motivo
      }))
  }, [filas])

  return (
    <div>
      <PageHeader
        icon="bi-clipboard-data-fill"
        title="Reporte por motivo"
        subtitle="Agrega mermas, roturas y ajustes por motivo para detectar patrones de desperdicio"
      />

      {error && <div className="alert alert-danger">{error}</div>}

      {/* Resumen */}
      <div className="row g-3 mb-3">
        <div className="col-md-3 col-sm-6">
          <div className="card h-100">
            <div className="card-body">
              <div className="text-muted small text-uppercase">Motivos distintos</div>
              <div className="fs-3 fw-bold text-coffee">{totales.motivosUnicos}</div>
            </div>
          </div>
        </div>
        <div className="col-md-3 col-sm-6">
          <div className="card h-100">
            <div className="card-body">
              <div className="text-muted small text-uppercase">Incidencias totales</div>
              <div className="fs-3 fw-bold text-coffee">{totales.incidenciasTotales}</div>
            </div>
          </div>
        </div>
        <div className="col-md-3 col-sm-6">
          <div className="card h-100">
            <div className="card-body">
              <div className="text-muted small text-uppercase">Incidencias de desperdicio</div>
              <div className="fs-3 fw-bold text-danger">{totales.incidenciasDesperdicio}</div>
              <div className="form-text">MERMA + ROTURA</div>
            </div>
          </div>
        </div>
        <div className="col-md-3 col-sm-6">
          <div className="card h-100">
            <div className="card-body">
              <div className="text-muted small text-uppercase">Cantidad desperdiciada</div>
              <div className="fs-3 fw-bold text-danger">{fmtNum(totales.desperdicio)}</div>
              <div className="form-text">Suma de MERMA + ROTURA en sus unidades</div>
            </div>
          </div>
        </div>
      </div>

      {/* Filtros */}
      <form className="card mb-3" onSubmit={aplicar}>
        <div className="card-body">
          <div className="row g-3 align-items-end">
            <div className="col-md-3">
              <label className="form-label">Tipo</label>
              <select
                className="form-select"
                value={filters.tipo}
                onChange={(e) => setFilters({ ...filters, tipo: e.target.value })}
              >
                {TIPOS.map((t) => (
                  <option key={t.value} value={t.value}>
                    {t.label}
                  </option>
                ))}
              </select>
            </div>
            <div className="col-md-3">
              <label className="form-label">Desde</label>
              <input
                type="date"
                className="form-control"
                value={filters.desde}
                onChange={(e) => setFilters({ ...filters, desde: e.target.value })}
              />
            </div>
            <div className="col-md-3">
              <label className="form-label">Hasta</label>
              <input
                type="date"
                className="form-control"
                value={filters.hasta}
                onChange={(e) => setFilters({ ...filters, hasta: e.target.value })}
              />
            </div>
            <div className="col-md-3 d-flex gap-2">
              <button type="submit" className="btn btn-coffee" disabled={loading}>
                {loading ? (
                  <>
                    <span className="spinner-border spinner-border-sm me-2"></span>Cargando
                  </>
                ) : (
                  <>
                    <i className="bi bi-funnel me-1"></i>Aplicar
                  </>
                )}
              </button>
              <button type="button" className="btn btn-outline-secondary" onClick={reset} disabled={loading}>
                <i className="bi bi-arrow-counterclockwise me-1"></i>Reset
              </button>
              <button
                type="button"
                className="btn btn-outline-coffee ms-auto"
                onClick={async () => {
                  try {
                    const params = {}
                    if (filters.tipo) params.tipo = filters.tipo
                    if (filters.desde) params.desde = filters.desde
                    if (filters.hasta) params.hasta = filters.hasta
                    await downloadCsv(
                      '/api/reportes/motivos/csv',
                      `reporte_motivos_${new Date().toISOString().slice(0, 10)}.csv`,
                      params
                    )
                  } catch (err) {
                    setError(err.message)
                  }
                }}
                disabled={loading || filas.length === 0}
                title="Descargar CSV"
              >
                <i className="bi bi-file-earmark-arrow-down me-1"></i>CSV
              </button>
            </div>
          </div>
        </div>
      </form>

      {/* Gráfico */}
      {!loading && filas.length > 0 && (
        <div className="card mb-3">
          <div className="card-header">
            <i className="bi bi-bar-chart-fill me-2"></i>Top 10 motivos por cantidad total
          </div>
          <div className="card-body">
            <div style={{ width: '100%', height: 300 }}>
              <ResponsiveContainer>
                <BarChart data={chartData} margin={{ top: 20, right: 30, left: 20, bottom: 25 }}>
                  <CartesianGrid strokeDasharray="3 3" vertical={false} />
                  <XAxis 
                    dataKey="name" 
                    tick={{ fontSize: 12 }} 
                    interval={0}
                    angle={-25}
                    textAnchor="end"
                  />
                  <YAxis />
                  <Tooltip 
                    formatter={(value, name) => [
                      fmtNum(value), 
                      name === 'cantidad' ? 'Cantidad Total' : name
                    ]}
                    labelFormatter={(label, payload) => {
                      if (payload && payload.length > 0) {
                        return payload[0].payload.fullName + ' (' + payload[0].payload.tipo + ')'
                      }
                      return label
                    }}
                  />
                  <Legend wrapperStyle={{ paddingTop: '20px' }} />
                  <Bar dataKey="cantidad" name="Cantidad Total" fill="#6f4e37" radius={[4, 4, 0, 0]} />
                </BarChart>
              </ResponsiveContainer>
            </div>
          </div>
        </div>
      )}

      {/* Tabla */}
      <div className="card">
        <div className="card-header">
          <i className="bi bi-table me-2"></i>Detalle por motivo
          {!loading && (
            <span className="ms-2 text-muted fw-normal small">
              — {filas.length} fila{filas.length === 1 ? '' : 's'}
            </span>
          )}
        </div>
        <div className="card-body p-0">
          {loading ? (
            <div className="text-center py-4">
              <div className="spinner-border text-coffee"></div>
            </div>
          ) : filas.length === 0 ? (
            <div className="empty-state">
              <i className="bi bi-inbox fs-1"></i>
              <p className="mt-2 mb-0">No hay movimientos para los filtros aplicados.</p>
            </div>
          ) : (
            <div className="table-responsive">
              <table className="table table-hover align-middle mb-0">
                <thead>
                  <tr>
                    <th>Motivo</th>
                    <th>Tipo</th>
                    <th className="text-end">Nº incidencias</th>
                    <th className="text-end">Cantidad total</th>
                    <th>Primera fecha</th>
                    <th>Última fecha</th>
                  </tr>
                </thead>
                <tbody>
                  {filas.map((f, i) => (
                    <tr key={`${f.motivo}-${f.tipoMovimiento}-${i}`}>
                      <td>{f.motivo}</td>
                      <td>
                        <TipoBadge tipo={f.tipoMovimiento} />
                      </td>
                      <td className="text-end fw-bold">{f.numIncidencias}</td>
                      <td className="text-end">{fmtNum(f.cantidadTotal)}</td>
                      <td>{fmtFecha(f.primeraFecha)}</td>
                      <td>{fmtFecha(f.ultimaFecha)}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </div>
      </div>
    </div>
  )
}
