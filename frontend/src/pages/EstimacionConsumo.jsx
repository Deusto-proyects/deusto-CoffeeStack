import { useEffect, useState } from 'react'
import client, { apiErrorMessage } from '../api/client'
import PageHeader from '../components/PageHeader'

export default function EstimacionConsumo() {
  const [insumos, setInsumos] = useState([])
  const [estimaciones, setEstimaciones] = useState([])
  const [ventana, setVentana] = useState(30)
  const [horizonte, setHorizonte] = useState(7)
  const [loading, setLoading] = useState(true)
  const [calculando, setCalculando] = useState(false)
  const [error, setError] = useState(null)

  async function loadInsumos() {
    try {
      setLoading(true)
      const { data } = await client.get('/api/stock/insumos')
      setInsumos(data.map((s) => s.insumo))
    } catch (err) {
      setError(apiErrorMessage(err))
    } finally {
      setLoading(false)
    }
  }

  async function calcular() {
    if (ventana < 1) {
      setError('La ventana debe ser al menos 1 día.')
      return
    }
    if (horizonte < 0) {
      setError('El horizonte no puede ser negativo.')
      return
    }
    try {
      setCalculando(true)
      setError(null)
      const resultados = await Promise.all(
        insumos.map((i) =>
          client
            .get(`/api/insumos/${i.id}/estimacion-consumo`, {
              params: { ventana, horizonte },
            })
            .then((r) => r.data)
            .catch(() => null)
        )
      )
      setEstimaciones(resultados.filter(Boolean))
    } catch (err) {
      setError(apiErrorMessage(err))
    } finally {
      setCalculando(false)
    }
  }

  useEffect(() => {
    loadInsumos()
  }, [])

  useEffect(() => {
    if (insumos.length > 0) calcular()
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [insumos])

  return (
    <div>
      <PageHeader
        icon="bi-graph-up-arrow"
        title="Estimación de consumo"
        subtitle="Consumo medio diario y proyección al horizonte por insumo"
      />

      {error && <div className="alert alert-danger">{error}</div>}

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
              <div className="form-text">Días hacia atrás que se usan para calcular la media.</div>
            </div>
            <div className="col-md-4">
              <label className="form-label">Horizonte (días)</label>
              <input
                type="number"
                className="form-control"
                min="0"
                value={horizonte}
                onChange={(e) => setHorizonte(Number(e.target.value))}
              />
              <div className="form-text">Días futuros sobre los que proyectar el consumo.</div>
            </div>
            <div className="col-md-4">
              <button
                className="btn btn-coffee w-100"
                onClick={calcular}
                disabled={calculando || insumos.length === 0}
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
          </div>
        </div>
      </div>

      <div className="card">
        <div className="card-header">
          <i className="bi bi-table me-2"></i>Resultados
        </div>
        <div className="card-body p-0">
          {loading ? (
            <div className="text-center py-4">
              <div className="spinner-border text-coffee"></div>
            </div>
          ) : estimaciones.length === 0 ? (
            <div className="empty-state">
              <i className="bi bi-inbox fs-1"></i>
              <p className="mt-2 mb-0">Sin datos para mostrar.</p>
            </div>
          ) : (
            <div className="table-responsive">
              <table className="table table-hover align-middle mb-0">
                <thead>
                  <tr>
                    <th>Insumo</th>
                    <th>Unidad</th>
                    <th className="text-end">Consumo medio diario</th>
                    <th className="text-end">Días con actividad</th>
                    <th className="text-end">Consumo proyectado ({horizonte}d)</th>
                  </tr>
                </thead>
                <tbody>
                  {estimaciones.map((e) => (
                    <tr key={e.insumoId}>
                      <td>
                        <strong>{e.insumoNombre}</strong>
                      </td>
                      <td>{e.unidadMedida}</td>
                      <td className="text-end">
                        {e.consumoMedioDiario.toFixed(2)} {e.unidadMedida}/día
                      </td>
                      <td className="text-end">
                        {e.diasConActividad} / {e.ventanaDias}
                      </td>
                      <td className="text-end fw-bold">
                        {e.consumoProyectado.toFixed(2)} {e.unidadMedida}
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
