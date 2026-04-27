import { useEffect, useState, useCallback } from 'react'
import client, { apiErrorMessage } from '../api/client'
import PageHeader from '../components/PageHeader'

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

function exportCSV(movs) {
  const header = ['Fecha/Hora', 'Insumo', 'Lote', 'Tipo', 'Cantidad', 'Motivo', 'Usuario']
  const rows = movs.map((m) => [
    new Date(m.fechaHora).toLocaleString(),
    m.insumoNombre,
    m.numeroLote,
    m.tipoMovimiento,
    m.cantidad,
    `"${(m.motivo || '').replace(/"/g, '""')}"`,
    m.usuario,
  ])
  const csv = [header, ...rows].map((r) => r.join(',')).join('\n')
  const blob = new Blob(['\uFEFF' + csv], { type: 'text/csv;charset=utf-8;' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = `historial_movimientos_${new Date().toISOString().slice(0, 10)}.csv`
  a.click()
  URL.revokeObjectURL(url)
}

export default function HistorialMovimientos() {
  const [insumos, setInsumos] = useState([])
  const [movs, setMovs] = useState([])
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState(null)

  const [filters, setFilters] = useState({
    insumoId: '',
    tipo: '',
    desde: '',
    hasta: '',
  })
  const [applied, setApplied] = useState(filters)

  // Load insumos for the selector
  useEffect(() => {
    client
      .get('/api/insumos', { params: { size: 500 } })
      .then(({ data }) => setInsumos(data.content ?? data))
      .catch(() => {})
  }, [])

  const fetchMovimientos = useCallback(async (f) => {
    setLoading(true)
    setError(null)
    try {
      const params = {}
      if (f.insumoId) params.insumoId = f.insumoId
      if (f.tipo) params.tipo = f.tipo
      if (f.desde) params.desde = f.desde
      if (f.hasta) params.hasta = f.hasta
      const { data } = await client.get('/api/movimientos', { params })
      setMovs(data)
    } catch (err) {
      setError(apiErrorMessage(err))
    } finally {
      setLoading(false)
    }
  }, [])

  // Load on mount with no filters
  useEffect(() => {
    fetchMovimientos(applied)
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [])

  function handleApply(e) {
    e.preventDefault()
    setApplied(filters)
    fetchMovimientos(filters)
  }

  function handleClear() {
    const empty = { insumoId: '', tipo: '', desde: '', hasta: '' }
    setFilters(empty)
    setApplied(empty)
    fetchMovimientos(empty)
  }

  const hasActiveFilters =
    applied.insumoId || applied.tipo || applied.desde || applied.hasta

  // Stats derived from current data
  const stats = movs.reduce((acc, m) => {
    acc[m.tipoMovimiento] = (acc[m.tipoMovimiento] || 0) + 1
    return acc
  }, {})

  return (
    <div>
      <PageHeader
        icon="bi-journal-text"
        title="Historial de movimientos"
        subtitle="Auditoría completa de todos los movimientos de inventario"
        actions={
          movs.length > 0 && (
            <button
              className="btn btn-outline-secondary"
              onClick={() => exportCSV(movs)}
              title="Exportar como CSV"
            >
              <i className="bi bi-download me-1" />
              Exportar CSV
            </button>
          )
        }
      />

      {/* Filter panel */}
      <div className="card mb-4" style={{ borderLeft: '4px solid var(--coffee)' }}>
        <div className="card-body">
          <form onSubmit={handleApply}>
            <div className="row g-3 align-items-end">
              <div className="col-md-3">
                <label className="form-label fw-semibold">
                  <i className="bi bi-box-seam me-1" />
                  Insumo
                </label>
                <select
                  id="historial-filter-insumo"
                  className="form-select"
                  value={filters.insumoId}
                  onChange={(e) => setFilters({ ...filters, insumoId: e.target.value })}
                >
                  <option value="">Todos los insumos</option>
                  {insumos.map((i) => (
                    <option key={i.id} value={i.id}>
                      {i.nombre}
                    </option>
                  ))}
                </select>
              </div>

              <div className="col-md-2">
                <label className="form-label fw-semibold">
                  <i className="bi bi-tag me-1" />
                  Tipo
                </label>
                <select
                  id="historial-filter-tipo"
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

              <div className="col-md-2">
                <label className="form-label fw-semibold">
                  <i className="bi bi-calendar-event me-1" />
                  Desde
                </label>
                <input
                  id="historial-filter-desde"
                  type="date"
                  className="form-control"
                  value={filters.desde}
                  onChange={(e) => setFilters({ ...filters, desde: e.target.value })}
                />
              </div>

              <div className="col-md-2">
                <label className="form-label fw-semibold">
                  <i className="bi bi-calendar-check me-1" />
                  Hasta
                </label>
                <input
                  id="historial-filter-hasta"
                  type="date"
                  className="form-control"
                  value={filters.hasta}
                  onChange={(e) => setFilters({ ...filters, hasta: e.target.value })}
                />
              </div>

              <div className="col-md-3 d-flex gap-2">
                <button id="historial-btn-aplicar" type="submit" className="btn btn-coffee flex-grow-1">
                  <i className="bi bi-search me-1" />
                  Aplicar
                </button>
                {hasActiveFilters && (
                  <button
                    id="historial-btn-limpiar"
                    type="button"
                    className="btn btn-outline-secondary"
                    onClick={handleClear}
                    title="Limpiar filtros"
                  >
                    <i className="bi bi-x-lg" />
                  </button>
                )}
              </div>
            </div>
          </form>
        </div>
      </div>

      {error && <div className="alert alert-danger">{error}</div>}

      {/* Stats strip */}
      {!loading && movs.length > 0 && (
        <div className="row g-2 mb-4">
          <div className="col-auto">
            <div
              className="d-flex align-items-center gap-2 px-3 py-2 rounded"
              style={{ background: '#f0e6d8', border: '1px solid #e9dccd' }}
            >
              <i className="bi bi-list-ol text-coffee" />
              <span className="fw-semibold text-coffee">{movs.length}</span>
              <span className="text-muted small">movimientos</span>
            </div>
          </div>
          {Object.entries(stats).map(([tipo, count]) => (
            <div key={tipo} className="col-auto">
              <div
                className="d-flex align-items-center gap-2 px-3 py-2 rounded"
                style={{ background: '#f8f8f8', border: '1px solid #eee' }}
              >
                <TipoBadge tipo={tipo} />
                <span className="fw-semibold">{count}</span>
              </div>
            </div>
          ))}
        </div>
      )}

      {/* Table */}
      <div className="card">
        <div className="card-body p-0">
          {loading ? (
            <div className="text-center py-5">
              <div className="spinner-border text-coffee" />
              <p className="mt-3 text-muted">Cargando historial…</p>
            </div>
          ) : movs.length === 0 ? (
            <div className="empty-state">
              <i className="bi bi-clock-history fs-1" />
              <p className="mt-3 mb-1 fw-semibold">Sin movimientos</p>
              <p className="text-muted small mb-0">
                {hasActiveFilters
                  ? 'No hay movimientos que coincidan con los filtros aplicados.'
                  : 'Aún no se han registrado movimientos de inventario.'}
              </p>
            </div>
          ) : (
            <div className="table-responsive">
              <table className="table table-hover align-middle mb-0">
                <thead>
                  <tr>
                    <th style={{ whiteSpace: 'nowrap' }}>Fecha / Hora</th>
                    <th>Insumo</th>
                    <th>Lote</th>
                    <th>Tipo</th>
                    <th className="text-end">Cantidad</th>
                    <th>Motivo</th>
                    <th>Usuario</th>
                  </tr>
                </thead>
                <tbody>
                  {movs.map((m) => (
                    <tr key={m.id}>
                      <td style={{ whiteSpace: 'nowrap' }}>
                        <small className="text-muted">
                          {new Date(m.fechaHora).toLocaleDateString()}
                        </small>
                        <br />
                        <small>{new Date(m.fechaHora).toLocaleTimeString()}</small>
                      </td>
                      <td>
                        <strong>{m.insumoNombre}</strong>
                      </td>
                      <td>
                        <span className="badge bg-light text-dark border" style={{ fontWeight: 500 }}>
                          {m.numeroLote}
                        </span>
                      </td>
                      <td>
                        <TipoBadge tipo={m.tipoMovimiento} />
                      </td>
                      <td className="text-end fw-semibold">{m.cantidad}</td>
                      <td>
                        <small
                          style={{
                            maxWidth: '260px',
                            display: 'block',
                            overflow: 'hidden',
                            textOverflow: 'ellipsis',
                            whiteSpace: 'nowrap',
                          }}
                          title={m.motivo}
                        >
                          {m.motivo}
                        </small>
                      </td>
                      <td>
                        <span className="d-flex align-items-center gap-1">
                          <i className="bi bi-person-circle text-muted" style={{ fontSize: '0.85rem' }} />
                          <small>{m.usuario}</small>
                        </span>
                      </td>
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
