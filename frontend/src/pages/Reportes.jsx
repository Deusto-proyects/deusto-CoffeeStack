import { useState, useEffect } from 'react'
import { Link } from 'react-router-dom'
import PageHeader from '../components/PageHeader'
import { downloadCsv } from '../utils/csvDownload'
import client, { apiErrorMessage } from '../api/client'

// ─── helpers ──────────────────────────────────────────────────────────────────
function isoToday() {
  return new Date().toISOString().slice(0, 10)
}
function isoDaysAgo(days) {
  const d = new Date()
  d.setDate(d.getDate() - days)
  return d.toISOString().slice(0, 10)
}

// ─── Tarjeta genérica ─────────────────────────────────────────────────────────
function ReportCard({ icon, title, description, children }) {
  return (
    <div className="col-lg-6">
      <div className="card h-100 shadow-sm">
        <div className="card-header d-flex align-items-center gap-2 py-3">
          <i className={`bi ${icon} fs-5 text-coffee`} />
          <span className="fw-semibold">{title}</span>
        </div>
        <div className="card-body d-flex flex-column gap-3">
          <p className="text-muted small mb-0">{description}</p>
          {children}
        </div>
      </div>
    </div>
  )
}

// ─── Alerta de estado ─────────────────────────────────────────────────────────
function StatusAlert({ msg, type, onClose }) {
  if (!msg) return null
  return (
    <div className={`alert alert-${type} alert-dismissible d-flex align-items-center gap-2 py-2`} role="alert">
      <i className={`bi ${type === 'success' ? 'bi-check-circle' : 'bi-exclamation-triangle'}`} />
      <span>{msg}</span>
      <button type="button" className="btn-close ms-auto" onClick={onClose} />
    </div>
  )
}

// ─── Componente: Reporte de Ventas CSV ────────────────────────────────────────
function ReporteVentasCard() {
  const [loading, setLoading] = useState(false)
  const [status, setStatus] = useState(null) // { msg, type }

  async function handleDownload() {
    try {
      setLoading(true)
      setStatus(null)
      await downloadCsv('/api/ventas/reporte/csv', 'reporte_ventas.csv')
      setStatus({ msg: 'Fichero descargado correctamente.', type: 'success' })
    } catch (e) {
      setStatus({ msg: e.message, type: 'danger' })
    } finally {
      setLoading(false)
    }
  }

  return (
    <ReportCard
      icon="bi-receipt-cutoff"
      title="Ventas por día y producto"
      description="Exporta el histórico completo de ventas agregado por día y nombre de producto. Ideal para analizar la demanda y las tendencias de consumo en Excel o Google Sheets."
    >
      <StatusAlert msg={status?.msg} type={status?.type} onClose={() => setStatus(null)} />

      <div className="d-flex align-items-center justify-content-between mt-auto pt-2 border-top">
        <div className="text-muted small">
          <i className="bi bi-file-earmark-spreadsheet me-1 text-success" />
          Columnas: <strong>Fecha · Producto · Unidades Vendidas</strong>
        </div>
        <button
          id="btn-reportes-ventas-csv"
          className="btn btn-coffee"
          onClick={handleDownload}
          disabled={loading}
        >
          {loading ? (
            <><span className="spinner-border spinner-border-sm me-2" />Generando…</>
          ) : (
            <><i className="bi bi-download me-2" />Descargar CSV</>
          )}
        </button>
      </div>
    </ReportCard>
  )
}

// ─── Componente: Reporte de Consumo de Insumos CSV ────────────────────────────
function ReporteConsumoCard({ insumos }) {
  const [insumoId, setInsumoId] = useState(insumos.length > 0 ? String(insumos[0].id) : '')
  const [desde, setDesde] = useState(isoDaysAgo(30))
  const [hasta, setHasta] = useState(isoToday())
  const [granularidad, setGranularidad] = useState('DIA')
  const [loading, setLoading] = useState(false)
  const [status, setStatus] = useState(null)

  async function handleDownload() {
    if (!insumoId) {
      setStatus({ msg: 'Selecciona un insumo antes de exportar.', type: 'warning' })
      return
    }
    if (desde > hasta) {
      setStatus({ msg: "La fecha 'Desde' debe ser anterior o igual a 'Hasta'.", type: 'warning' })
      return
    }
    try {
      setLoading(true)
      setStatus(null)
      const nombre = insumos.find(i => String(i.id) === insumoId)?.nombre || 'insumo'
      await downloadCsv(
        '/api/reportes/consumo/csv',
        `consumo_${nombre.replace(/\s+/g, '_')}.csv`,
        { insumoId, desde, hasta, granularidad }
      )
      setStatus({ msg: 'Fichero descargado correctamente.', type: 'success' })
    } catch (e) {
      setStatus({ msg: e.message, type: 'danger' })
    } finally {
      setLoading(false)
    }
  }

  return (
    <ReportCard
      icon="bi-bar-chart-line"
      title="Consumo de insumos"
      description="Exporta la serie temporal del consumo de un insumo en un rango de fechas. Incluye cantidad consumida, coste estimado y desglose por periodo."
    >
      <div className="row g-2">
        <div className="col-12">
          <label className="form-label small mb-1">Insumo</label>
          <select
            className="form-select form-select-sm"
            value={insumoId}
            onChange={e => setInsumoId(e.target.value)}
          >
            <option value="">— Selecciona un insumo —</option>
            {insumos.map(i => (
              <option key={i.id} value={i.id}>
                {i.nombre} ({i.unidadMedida})
              </option>
            ))}
          </select>
        </div>
        <div className="col-6">
          <label className="form-label small mb-1">Desde</label>
          <input
            type="date"
            className="form-control form-control-sm"
            value={desde}
            onChange={e => setDesde(e.target.value)}
          />
        </div>
        <div className="col-6">
          <label className="form-label small mb-1">Hasta</label>
          <input
            type="date"
            className="form-control form-control-sm"
            value={hasta}
            onChange={e => setHasta(e.target.value)}
          />
        </div>
        <div className="col-12">
          <label className="form-label small mb-1">Granularidad</label>
          <select
            className="form-select form-select-sm"
            value={granularidad}
            onChange={e => setGranularidad(e.target.value)}
          >
            <option value="DIA">Por día</option>
            <option value="SEMANA">Por semana</option>
            <option value="MES">Por mes</option>
          </select>
        </div>
      </div>

      <StatusAlert msg={status?.msg} type={status?.type} onClose={() => setStatus(null)} />

      <div className="d-flex align-items-center justify-content-between mt-auto pt-2 border-top">
        <div className="text-muted small">
          <i className="bi bi-file-earmark-spreadsheet me-1 text-success" />
          Columnas: <strong>Fecha · Cantidad · Coste (€)</strong>
        </div>
        <button
          id="btn-reportes-consumo-csv"
          className="btn btn-coffee"
          onClick={handleDownload}
          disabled={loading || !insumoId}
        >
          {loading ? (
            <><span className="spinner-border spinner-border-sm me-2" />Generando…</>
          ) : (
            <><i className="bi bi-download me-2" />Descargar CSV</>
          )}
        </button>
      </div>
    </ReportCard>
  )
}

// ─── Página principal ─────────────────────────────────────────────────────────
export default function Reportes() {
  const [insumos, setInsumos] = useState([])
  const [loadingInsumos, setLoadingInsumos] = useState(true)
  const [errorInsumos, setErrorInsumos] = useState(null)

  useEffect(() => {
    client.get('/api/insumos', { params: { size: 200 } })
      .then(r => setInsumos(r.data.content ?? r.data))
      .catch(e => setErrorInsumos(apiErrorMessage(e)))
      .finally(() => setLoadingInsumos(false))
  }, [])

  return (
    <div>
      <PageHeader
        icon="bi-file-earmark-arrow-down"
        title="Exportar reportes"
        subtitle="Descarga los datos del sistema en formato CSV para analizarlos en Excel, Google Sheets o cualquier herramienta externa."
        actions={
          <div className="d-flex gap-2 flex-wrap">
            <Link to="/reportes/consumo" className="btn btn-outline-secondary btn-sm">
              <i className="bi bi-bar-chart-line me-1" />
              Consumo individual
            </Link>
            <Link to="/reportes/comparativa" className="btn btn-coffee btn-sm">
              <i className="bi bi-bar-chart-steps me-1" />
              Comparativa multi-insumo
            </Link>
          </div>
        }
      />

      {errorInsumos && (
        <div className="alert alert-warning">
          <i className="bi bi-exclamation-triangle me-2" />
          No se pudieron cargar los insumos: {errorInsumos}
        </div>
      )}

      <div className="row g-4">
        <ReporteVentasCard />
        {loadingInsumos ? (
          <div className="col-lg-6 d-flex align-items-center justify-content-center" style={{ minHeight: 200 }}>
            <div className="spinner-border text-secondary" />
          </div>
        ) : (
          <ReporteConsumoCard insumos={insumos} />
        )}
      </div>

      <div className="row mt-4">
        <div className="col">
          <div className="card border-0 bg-light">
            <div className="card-body d-flex align-items-start gap-3">
              <i className="bi bi-info-circle-fill text-primary fs-4 flex-shrink-0 mt-1" />
              <div>
                <p className="mb-1 fw-semibold">Sobre los ficheros CSV</p>
                <p className="mb-0 text-muted small">
                  Todos los ficheros incluyen marca de orden de bytes UTF-8 (BOM) para que Microsoft Excel en Windows
                  abra los caracteres especiales (tildes, ñ) correctamente sin pasos adicionales.
                  En macOS o Linux ábrelos con <em>Importar CSV</em> seleccionando codificación UTF-8.
                </p>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  )
}
