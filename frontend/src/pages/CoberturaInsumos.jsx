import { useEffect, useState } from 'react'
import client, { apiErrorMessage } from '../api/client'
import PageHeader from '../components/PageHeader'

/**
 * Muestra los días de cobertura estimados por insumo con semáforo de riesgo.
 * Solo accesible para PROPIETARIO y ROOT.
 */
export default function CoberturaInsumos() {
  const [cobertura, setCobertura] = useState([])
  const [ventana, setVentana] = useState(30)
  const [loading, setLoading] = useState(true)
  const [calculando, setCalculando] = useState(false)
  const [error, setError] = useState(null)

  async function cargar(ventanaDias) {
    try {
      setCalculando(true)
      setError(null)
      const { data } = await client.get('/api/stock/cobertura', {
        params: { ventana: ventanaDias },
      })
      setCobertura(data)
    } catch (err) {
      setError(apiErrorMessage(err))
    } finally {
      setLoading(false)
      setCalculando(false)
    }
  }

  useEffect(() => {
    cargar(ventana)
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [])

  // ---- helpers de presentación ----

  function badgeRiesgo(nivel) {
    const cfg = {
      CRITICO: { cls: 'badge-riesgo-critico', icon: 'bi-exclamation-triangle-fill', label: 'Crítico' },
      BAJO:    { cls: 'badge-riesgo-bajo',    icon: 'bi-exclamation-circle-fill',    label: 'Bajo' },
      OK:      { cls: 'badge-riesgo-ok',      icon: 'bi-check-circle-fill',          label: 'OK' },
    }
    const { cls, icon, label } = cfg[nivel] ?? cfg['OK']
    return (
      <span className={`badge-riesgo ${cls}`}>
        <i className={`bi ${icon} me-1`}></i>
        {label}
      </span>
    )
  }

  function formatDias(dias) {
    if (!isFinite(dias)) return <span className="text-muted fst-italic">Sin consumo</span>
    return <strong>{dias.toFixed(1)}</strong>
  }

  function rowClass(nivel) {
    if (nivel === 'CRITICO') return 'table-danger'
    if (nivel === 'BAJO')    return 'table-warning'
    return ''
  }

  // ---- resumen de alertas ----
  const nCritico = cobertura.filter(r => r.nivelRiesgo === 'CRITICO').length
  const nBajo    = cobertura.filter(r => r.nivelRiesgo === 'BAJO').length

  return (
    <div>
      <PageHeader
        icon="bi-shield-exclamation"
        title="Días de cobertura por insumo"
        subtitle="Estimación de cuántos días de stock quedan basándose en el consumo medio diario"
      />

      {error && <div className="alert alert-danger">{error}</div>}

      {/* Resumen de alertas */}
      {!loading && (nCritico > 0 || nBajo > 0) && (
        <div className="row g-3 mb-3">
          {nCritico > 0 && (
            <div className="col-auto">
              <div className="alert alert-danger d-flex align-items-center mb-0 py-2 px-3">
                <i className="bi bi-exclamation-triangle-fill me-2 fs-5"></i>
                <div>
                  <strong>{nCritico}</strong> insumo{nCritico > 1 ? 's' : ''} en estado{' '}
                  <strong>CRÍTICO</strong> (&lt; 3 días)
                </div>
              </div>
            </div>
          )}
          {nBajo > 0 && (
            <div className="col-auto">
              <div className="alert alert-warning d-flex align-items-center mb-0 py-2 px-3">
                <i className="bi bi-exclamation-circle-fill me-2 fs-5"></i>
                <div>
                  <strong>{nBajo}</strong> insumo{nBajo > 1 ? 's' : ''} con cobertura{' '}
                  <strong>BAJA</strong> (3 – 6 días)
                </div>
              </div>
            </div>
          )}
        </div>
      )}

      {/* Panel de parámetros */}
      <div className="card mb-3">
        <div className="card-body">
          <div className="row g-3 align-items-end">
            <div className="col-md-4">
              <label className="form-label">Ventana de muestreo (días)</label>
              <input
                type="number"
                className="form-control"
                min="1"
                value={ventana}
                onChange={(e) => setVentana(Number(e.target.value))}
              />
              <div className="form-text">
                Días hacia atrás para calcular el consumo medio diario.
              </div>
            </div>
            <div className="col-md-4">
              <button
                className="btn btn-coffee w-100"
                onClick={() => cargar(ventana)}
                disabled={calculando || ventana < 1}
              >
                {calculando ? (
                  <>
                    <span className="spinner-border spinner-border-sm me-2"></span>
                    Calculando...
                  </>
                ) : (
                  <>
                    <i className="bi bi-arrow-repeat me-1"></i>Recalcular
                  </>
                )}
              </button>
            </div>
            <div className="col-md-4">
              <div className="d-flex gap-2 flex-wrap">
                <span className="badge-riesgo badge-riesgo-critico">
                  <i className="bi bi-exclamation-triangle-fill me-1"></i>Crítico &lt; 3 días
                </span>
                <span className="badge-riesgo badge-riesgo-bajo">
                  <i className="bi bi-exclamation-circle-fill me-1"></i>Bajo 3–6 días
                </span>
                <span className="badge-riesgo badge-riesgo-ok">
                  <i className="bi bi-check-circle-fill me-1"></i>OK ≥ 7 días
                </span>
              </div>
            </div>
          </div>
        </div>
      </div>

      {/* Tabla de resultados */}
      <div className="card">
        <div className="card-header">
          <i className="bi bi-table me-2"></i>Cobertura estimada por insumo
          {!loading && (
            <span className="ms-2 text-muted fw-normal small">
              — ventana: {ventana} días
            </span>
          )}
        </div>
        <div className="card-body p-0">
          {loading ? (
            <div className="text-center py-4">
              <div className="spinner-border text-coffee"></div>
            </div>
          ) : cobertura.length === 0 ? (
            <div className="empty-state">
              <i className="bi bi-inbox fs-1"></i>
              <p className="mt-2 mb-0">No hay insumos registrados.</p>
            </div>
          ) : (
            <div className="table-responsive">
              <table className="table table-hover align-middle mb-0">
                <thead>
                  <tr>
                    <th>Insumo</th>
                    <th>Unidad</th>
                    <th className="text-end">Stock actual</th>
                    <th className="text-end">Consumo medio/día</th>
                    <th className="text-end">Días de cobertura</th>
                    <th className="text-center">Riesgo</th>
                  </tr>
                </thead>
                <tbody>
                  {cobertura.map((r) => (
                    <tr key={r.insumoId} className={rowClass(r.nivelRiesgo)}>
                      <td>
                        <strong>{r.insumoNombre}</strong>
                      </td>
                      <td>{r.unidadMedida}</td>
                      <td className="text-end">
                        {r.stockActual.toFixed(2)} {r.unidadMedida}
                      </td>
                      <td className="text-end">
                        {r.consumoMedioDiario.toFixed(2)} {r.unidadMedida}/día
                      </td>
                      <td className="text-end">
                        {formatDias(r.diasCobertura)}
                      </td>
                      <td className="text-center">
                        {badgeRiesgo(r.nivelRiesgo)}
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
