import { useEffect, useState } from 'react'
import client, { apiErrorMessage } from '../api/client'
import PageHeader from '../components/PageHeader'

/**
 * Sugerencias de reposición por insumo: stock + lead time + cobertura → cantidad
 * sugerida a comprar y nivel de urgencia (OK / ATENCION / URGENTE).
 * Solo accesible para PROPIETARIO y ROOT (la decisión de compra es del propietario).
 */
export default function SugerenciasReposicion() {
  const [sugerencias, setSugerencias] = useState([])
  const [ventana, setVentana] = useState(30)
  const [soloAlertas, setSoloAlertas] = useState(false)
  const [loading, setLoading] = useState(true)
  const [calculando, setCalculando] = useState(false)
  const [error, setError] = useState(null)

  async function cargar(ventanaDias) {
    try {
      setCalculando(true)
      setError(null)
      const { data } = await client.get('/api/insumos/sugerencias-reposicion', {
        params: { ventana: ventanaDias },
      })
      setSugerencias(data)
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

  // ---- helpers ----

  function badgeUrgencia(nivel) {
    if (nivel === 'URGENTE') {
      return (
        <span className="badge bg-danger">
          <i className="bi bi-exclamation-triangle-fill me-1"></i>Urgente
        </span>
      )
    }
    if (nivel === 'ATENCION') {
      return (
        <span className="badge bg-warning text-dark">
          <i className="bi bi-exclamation-circle-fill me-1"></i>Atención
        </span>
      )
    }
    return (
      <span className="badge bg-success">
        <i className="bi bi-check-circle-fill me-1"></i>OK
      </span>
    )
  }

  function formatDiasRestantes(dias) {
    if (dias < 0) {
      return <span className="text-muted fst-italic">Sin consumo</span>
    }
    return <strong>{dias.toFixed(1)}</strong>
  }

  function rowClass(nivel) {
    if (nivel === 'URGENTE') return 'table-danger'
    if (nivel === 'ATENCION') return 'table-warning'
    return ''
  }

  // ---- filtrado ----
  const visibles = soloAlertas
    ? sugerencias.filter((s) => s.nivelUrgencia === 'URGENTE' || s.nivelUrgencia === 'ATENCION')
    : sugerencias

  const nUrgente = sugerencias.filter((s) => s.nivelUrgencia === 'URGENTE').length
  const nAtencion = sugerencias.filter((s) => s.nivelUrgencia === 'ATENCION').length

  return (
    <div>
      <PageHeader
        icon="bi-cart-plus"
        title="Sugerencias de reposición"
        subtitle="Cantidad sugerida de compra por insumo según el consumo reciente"
      />

      {error && <div className="alert alert-danger">{error}</div>}

      {/* Resumen de alertas */}
      {!loading && (nUrgente > 0 || nAtencion > 0) && (
        <div className="row g-3 mb-3">
          {nUrgente > 0 && (
            <div className="col-auto">
              <div className="alert alert-danger d-flex align-items-center mb-0 py-2 px-3">
                <i className="bi bi-exclamation-triangle-fill me-2 fs-5"></i>
                <div>
                  <strong>{nUrgente}</strong> insumo{nUrgente > 1 ? 's' : ''} en{' '}
                  <strong>URGENTE</strong> — pide ya
                </div>
              </div>
            </div>
          )}
          {nAtencion > 0 && (
            <div className="col-auto">
              <div className="alert alert-warning d-flex align-items-center mb-0 py-2 px-3">
                <i className="bi bi-exclamation-circle-fill me-2 fs-5"></i>
                <div>
                  <strong>{nAtencion}</strong> insumo{nAtencion > 1 ? 's' : ''} en{' '}
                  <strong>ATENCIÓN</strong>
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
            <div className="col-md-3">
              <label className="form-label">Ventana de consumo (días)</label>
              <input
                type="number"
                className="form-control"
                min="1"
                value={ventana}
                onChange={(e) => setVentana(Number(e.target.value))}
              />
              <div className="form-text">
                Días hacia atrás para estimar la media diaria.
              </div>
            </div>
            <div className="col-md-3">
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
            <div className="col-md-6">
              <div className="form-check mb-2">
                <input
                  type="checkbox"
                  className="form-check-input"
                  id="soloAlertas"
                  checked={soloAlertas}
                  onChange={(e) => setSoloAlertas(e.target.checked)}
                />
                <label className="form-check-label" htmlFor="soloAlertas">
                  Mostrar solo URGENTE y ATENCIÓN
                </label>
              </div>
              <div className="d-flex gap-2 flex-wrap">
                <span className="badge bg-danger">
                  <i className="bi bi-exclamation-triangle-fill me-1"></i>Urgente: stock &lt; lead time
                </span>
                <span className="badge bg-warning text-dark">
                  <i className="bi bi-exclamation-circle-fill me-1"></i>Atención: &lt; media cobertura
                </span>
                <span className="badge bg-success">
                  <i className="bi bi-check-circle-fill me-1"></i>OK: suficiente
                </span>
              </div>
            </div>
          </div>
        </div>
      </div>

      {/* Tabla de resultados */}
      <div className="card">
        <div className="card-header">
          <i className="bi bi-table me-2"></i>Sugerencias por insumo
          {!loading && (
            <span className="ms-2 text-muted fw-normal small">
              — ventana: {ventana} días · mostrando {visibles.length} de {sugerencias.length}
            </span>
          )}
        </div>
        <div className="card-body p-0">
          {loading ? (
            <div className="text-center py-4">
              <div className="spinner-border text-coffee"></div>
            </div>
          ) : visibles.length === 0 ? (
            <div className="empty-state">
              <i className="bi bi-inbox fs-1"></i>
              <p className="mt-2 mb-0">
                {soloAlertas
                  ? 'No hay insumos en URGENTE ni ATENCIÓN.'
                  : 'No hay insumos registrados.'}
              </p>
            </div>
          ) : (
            <div className="table-responsive">
              <table className="table table-hover align-middle mb-0">
                <thead>
                  <tr>
                    <th>Insumo</th>
                    <th className="text-end">Stock actual</th>
                    <th className="text-end">Consumo medio/día</th>
                    <th className="text-end">Lead time</th>
                    <th className="text-end">Cobertura</th>
                    <th className="text-end">Cantidad sugerida</th>
                    <th className="text-end">Días restantes</th>
                    <th className="text-center">Nivel</th>
                  </tr>
                </thead>
                <tbody>
                  {visibles.map((s) => (
                    <tr key={s.insumoId} className={rowClass(s.nivelUrgencia)}>
                      <td>
                        <strong>{s.insumoNombre}</strong>
                      </td>
                      <td className="text-end">
                        {s.stockActual.toFixed(2)} {s.unidadMedida}
                      </td>
                      <td className="text-end">
                        {s.consumoMedioDiario.toFixed(2)} {s.unidadMedida}/día
                      </td>
                      <td className="text-end">{s.leadTimeDias} d</td>
                      <td className="text-end">{s.diasCobertura} d</td>
                      <td className="text-end fw-bold">
                        {s.cantidadSugerida.toFixed(2)} {s.unidadMedida}
                      </td>
                      <td className="text-end">
                        {formatDiasRestantes(s.diasCoberturaRestante)}
                      </td>
                      <td className="text-center">{badgeUrgencia(s.nivelUrgencia)}</td>
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
