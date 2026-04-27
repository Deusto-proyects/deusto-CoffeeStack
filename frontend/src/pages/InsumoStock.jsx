import { useEffect, useState } from 'react'
import { useParams, Link } from 'react-router-dom'
import client, { apiErrorMessage } from '../api/client'
import PageHeader from '../components/PageHeader'
import { useAuth } from '../context/AuthContext'

export default function InsumoStock() {
  const { id } = useParams()
  const { hasRole } = useAuth()
  const [data, setData] = useState(null)
  const [movs, setMovs] = useState([])
  const [error, setError] = useState(null)
  const [loading, setLoading] = useState(true)

  async function load() {
    try {
      setLoading(true)
      const [r1, r2] = await Promise.all([
        client.get(`/api/stock/insumos/${id}`),
        client.get(`/api/ajustes/insumo/${id}`).catch(() => ({ data: [] })),
      ])
      setData(r1.data)
      setMovs(r2.data)
    } catch (err) {
      setError(apiErrorMessage(err))
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    load()
  }, [id])

  if (loading) {
    return (
      <div className="text-center py-5">
        <div className="spinner-border text-coffee"></div>
      </div>
    )
  }

  if (error) return <div className="alert alert-danger">{error}</div>
  if (!data) return null

  const { insumo, cantidadTotal, tieneRiesgoFaltante, lotes } = data

  return (
    <div>
      <PageHeader
        icon="bi-bar-chart"
        title={`Stock · ${insumo.nombre}`}
        subtitle={`Unidad: ${insumo.unidadMedida}`}
        actions={
          <>
            <Link to={`/lotes/nuevo?insumoId=${insumo.id}`} className="btn btn-coffee">
              <i className="bi bi-plus-lg me-1"></i>Recibir lote
            </Link>
            <Link to="/insumos" className="btn btn-outline-secondary">
              Volver
            </Link>
          </>
        }
      />

      <div className="row g-3 mb-4">
        <div className="col-md-4">
          <div className="card h-100">
            <div className="card-body">
              <div className="text-muted small">Stock total</div>
              <div
                className={`display-5 fw-bold ${
                  tieneRiesgoFaltante ? 'text-danger' : 'text-success'
                }`}
              >
                {cantidadTotal} {insumo.unidadMedida}
              </div>
              {tieneRiesgoFaltante && (
                <div className="alert alert-warning py-2 mb-0 mt-2">
                  <i className="bi bi-exclamation-triangle me-1"></i>
                  Por debajo del mínimo ({insumo.stockMinimoAlerta})
                </div>
              )}
            </div>
          </div>
        </div>
        <div className="col-md-4">
          <div className="card h-100">
            <div className="card-body">
              <div className="text-muted small">Número de lotes</div>
              <div className="display-5 text-coffee fw-bold">{lotes?.length || 0}</div>
            </div>
          </div>
        </div>
        <div className="col-md-4">
          <div className="card h-100">
            <div className="card-body">
              <div className="text-muted small">Mínimo de alerta</div>
              <div className="display-5 text-coffee fw-bold">
                {insumo.stockMinimoAlerta} {insumo.unidadMedida}
              </div>
            </div>
          </div>
        </div>
      </div>

      <div className="card mb-4">
        <div className="card-header">
          <i className="bi bi-boxes me-2"></i>Lotes en stock
        </div>
        <div className="card-body p-0">
          {!lotes || lotes.length === 0 ? (
            <div className="empty-state">
              <i className="bi bi-inbox fs-1"></i>
              <p className="mt-2 mb-0">No hay lotes para este insumo.</p>
            </div>
          ) : (
            <div className="table-responsive">
              <table className="table table-hover mb-0 align-middle">
                <thead>
                  <tr>
                    <th>Nº Lote</th>
                    <th>Proveedor</th>
                    <th className="text-end">Inicial</th>
                    <th className="text-end">Actual</th>
                    <th>Vencimiento</th>
                    {hasRole('PROPIETARIO', 'ROOT') && <th className="text-end">Acciones</th>}
                  </tr>
                </thead>
                <tbody>
                  {lotes.map((l) => (
                    <tr key={l.id}>
                      <td>
                        <strong>{l.numeroLote}</strong>
                      </td>
                      <td>{l.proveedorNombre || <span className="text-muted">—</span>}</td>
                      <td className="text-end">{l.cantidadInicial}</td>
                      <td className="text-end">
                        <strong>{l.cantidadActual}</strong>
                      </td>
                      <td>
                        {l.fechaVencimiento || <span className="text-muted">sin fecha</span>}
                      </td>
                      {hasRole('PROPIETARIO', 'ROOT') && (
                        <td className="text-end">
                          <Link
                            to={`/ajustes/nuevo?loteId=${l.id}`}
                            className="btn btn-sm btn-outline-coffee"
                          >
                            <i className="bi bi-clipboard-plus me-1"></i>Ajuste
                          </Link>
                        </td>
                      )}
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </div>
      </div>

      <div className="card">
        <div className="card-header">
          <i className="bi bi-clipboard-data me-2"></i>Historial de movimientos
        </div>
        <div className="card-body p-0">
          {movs.length === 0 ? (
            <div className="empty-state">
              <i className="bi bi-clock-history fs-1"></i>
              <p className="mt-2 mb-0">Sin movimientos registrados.</p>
            </div>
          ) : (
            <div className="table-responsive">
              <table className="table table-sm mb-0">
                <thead>
                  <tr>
                    <th>Fecha</th>
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
                      <td>{new Date(m.fechaHora).toLocaleString()}</td>
                      <td>{m.numeroLote}</td>
                      <td>
                        <TipoBadge tipo={m.tipoMovimiento} />
                      </td>
                      <td className="text-end">{m.cantidad}</td>
                      <td>
                        <small>{m.motivo}</small>
                      </td>
                      <td>
                        <small>{m.usuario}</small>
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

function TipoBadge({ tipo }) {
  const styles = {
    MERMA: 'bg-warning text-dark',
    ROTURA: 'bg-danger',
    AJUSTE_POSITIVO: 'bg-success',
    AJUSTE_NEGATIVO: 'bg-secondary',
  }
  return <span className={`badge ${styles[tipo] || 'bg-secondary'}`}>{tipo}</span>
}
