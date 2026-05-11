import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import client, { apiErrorMessage } from '../api/client'
import PageHeader from '../components/PageHeader'
import { useAuth } from '../context/AuthContext'
import { getExpiryStatus } from '../utils/dateUtils'

export default function Dashboard() {
  const { user, hasRole } = useAuth()
  const [stock, setStock] = useState([])
  const [movimientos, setMovimientos] = useState([])
  const [cobertura, setCobertura] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)

  async function load() {
    try {
      setLoading(true)
      const requests = [
        client.get('/api/stock/insumos'),
        client.get('/api/ajustes'),
      ]
      if (hasRole('PROPIETARIO', 'ROOT')) {
        requests.push(client.get('/api/stock/cobertura', { params: { ventana: 30 } }))
      }
      const [{ data: s }, { data: m }, coberturaRes] = await Promise.all(requests)
      setStock(s)
      setMovimientos(m)
      if (coberturaRes) setCobertura(coberturaRes.data)
    } catch (err) {
      setError(apiErrorMessage(err))
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    load()
  }, [])

  const insumosConRiesgo = stock.filter((s) => s.tieneRiesgoFaltante)
  const totalInsumos = stock.length
  const totalLotes = stock.reduce((acc, s) => acc + (s.lotes?.length || 0), 0)

  const proximosAVencer = stock
    .flatMap((s) =>
      (s.lotes || [])
        .filter((l) => l.fechaVencimiento)
        .map((l) => ({ ...l, insumoNombre: s.insumo.nombre, unidad: s.insumo.unidadMedida }))
    )
    .sort((a, b) => (a.fechaVencimiento > b.fechaVencimiento ? 1 : -1))
    .slice(0, 5)

  return (
    <div>
      <PageHeader
        icon="bi-speedometer2"
        title={`Bienvenido, ${user?.username}`}
        subtitle="Resumen general del inventario"
      />

      {error && <div className="alert alert-danger">{error}</div>}

      {loading ? (
        <div className="text-center py-5">
          <div className="spinner-border text-coffee"></div>
        </div>
      ) : (
        <>
          <div className="row g-3 mb-4">
            <div className="col-md-3 col-sm-6">
              <div className="card h-100">
                <div className="card-body">
                  <div className="text-muted small">Insumos activos</div>
                  <div className="display-6 text-coffee fw-bold">{totalInsumos}</div>
                  <Link to="/insumos" className="small text-coffee">
                    Ver insumos <i className="bi bi-arrow-right"></i>
                  </Link>
                </div>
              </div>
            </div>
            <div className="col-md-3 col-sm-6">
              <div className="card h-100">
                <div className="card-body">
                  <div className="text-muted small">Lotes en stock</div>
                  <div className="display-6 text-coffee fw-bold">{totalLotes}</div>
                  <Link to="/lotes" className="small text-coffee">
                    Ver lotes <i className="bi bi-arrow-right"></i>
                  </Link>
                </div>
              </div>
            </div>
            <div className="col-md-3 col-sm-6">
              <div className="card h-100">
                <div className="card-body">
                  <div className="text-muted small">Insumos bajo mínimo</div>
                  <div className={`display-6 fw-bold ${insumosConRiesgo.length > 0 ? 'text-danger' : 'text-success'}`}>
                    {insumosConRiesgo.length}
                  </div>
                  <span className="small text-muted">
                    <i className="bi bi-exclamation-triangle me-1"></i>
                    requieren reposición
                  </span>
                </div>
              </div>
            </div>
            <div className="col-md-3 col-sm-6">
              <div className="card h-100">
                <div className="card-body">
                  <div className="text-muted small">Movimientos registrados</div>
                  <div className="display-6 text-coffee fw-bold">{movimientos.length}</div>
                  <Link to="/ajustes" className="small text-coffee">
                    Ver historial <i className="bi bi-arrow-right"></i>
                  </Link>
                </div>
              </div>
            </div>
          </div>

          {/* Tarjeta de riesgos de cobertura – solo PROPIETARIO/ROOT */}
          {hasRole('PROPIETARIO', 'ROOT') && cobertura.length > 0 && (() => {
            const criticos = cobertura.filter(r => r.nivelRiesgo === 'CRITICO')
            const bajos    = cobertura.filter(r => r.nivelRiesgo === 'BAJO')
            return (
              <div className="card mb-4">
                <div className="card-header">
                  <i className="bi bi-shield-exclamation me-2"></i>
                  Riesgos operativos — días de cobertura
                  <Link to="/cobertura-insumos" className="btn btn-sm btn-outline-coffee float-end">
                    Ver detalle <i className="bi bi-arrow-right"></i>
                  </Link>
                </div>
                <div className="card-body">
                  {criticos.length === 0 && bajos.length === 0 ? (
                    <div className="d-flex align-items-center gap-2 text-success">
                      <i className="bi bi-check-circle-fill fs-4"></i>
                      <span>Todos los insumos tienen cobertura suficiente (≥ 7 días).</span>
                    </div>
                  ) : (
                    <div className="d-flex flex-wrap gap-3">
                      {criticos.length > 0 && (
                        <div className="d-flex align-items-center gap-2">
                          <span className="badge-riesgo badge-riesgo-critico">
                            <i className="bi bi-exclamation-triangle-fill me-1"></i>
                            {criticos.length} insumo{criticos.length > 1 ? 's' : ''} crítico{criticos.length > 1 ? 's' : ''}
                          </span>
                          <small className="text-muted">
                            {criticos.map(r => r.insumoNombre).join(', ')}
                          </small>
                        </div>
                      )}
                      {bajos.length > 0 && (
                        <div className="d-flex align-items-center gap-2">
                          <span className="badge-riesgo badge-riesgo-bajo">
                            <i className="bi bi-exclamation-circle-fill me-1"></i>
                            {bajos.length} insumo{bajos.length > 1 ? 's' : ''} con cobertura baja
                          </span>
                          <small className="text-muted">
                            {bajos.map(r => r.insumoNombre).join(', ')}
                          </small>
                        </div>
                      )}
                    </div>
                  )}
                </div>
              </div>
            )
          })()}

          <div className="row g-3">
            <div className="col-lg-6">
              <div className="card h-100">
                <div className="card-header">
                  <i className="bi bi-exclamation-triangle me-2"></i>
                  Alertas de stock
                </div>
                <div className="card-body p-0">
                  {insumosConRiesgo.length === 0 ? (
                    <div className="empty-state">
                      <i className="bi bi-check-circle-fill text-success fs-1"></i>
                      <p className="mt-2 mb-0">Todo el stock está por encima del mínimo.</p>
                    </div>
                  ) : (
                    <ul className="list-group list-group-flush">
                      {insumosConRiesgo.map((s) => (
                        <li
                          key={s.insumo.id}
                          className="list-group-item stock-alert d-flex justify-content-between align-items-center"
                        >
                          <div>
                            <strong>{s.insumo.nombre}</strong>
                            <br />
                            <small className="text-muted">
                              {s.cantidadTotal} / mín. {s.insumo.stockMinimoAlerta} {s.insumo.unidadMedida}
                            </small>
                          </div>
                          <Link
                            to={`/insumos/${s.insumo.id}/stock`}
                            className="btn btn-sm btn-outline-coffee"
                          >
                            Ver
                          </Link>
                        </li>
                      ))}
                    </ul>
                  )}
                </div>
              </div>
            </div>

            <div className="col-lg-6">
              <div className="card h-100">
                <div className="card-header">
                  <i className="bi bi-calendar-x me-2"></i>
                  Próximos vencimientos
                </div>
                <div className="card-body p-0">
                  {proximosAVencer.length === 0 ? (
                    <div className="empty-state">
                      <i className="bi bi-inbox fs-1"></i>
                      <p className="mt-2 mb-0">No hay lotes con fecha de vencimiento.</p>
                    </div>
                  ) : (
                    <ul className="list-group list-group-flush">
                      {proximosAVencer.map((l) => {
                        const expiry = getExpiryStatus(l.fechaVencimiento)
                        
                        return (
                          <li
                            key={l.id}
                            className={`list-group-item d-flex justify-content-between align-items-center ${expiry.status === 'danger' ? 'list-group-item-danger' : ''}`}
                          >
                            <div>
                              <strong>{l.insumoNombre}</strong>
                              <br />
                              <small className={`${expiry.status === 'danger' ? 'text-danger fw-bold' : 'text-muted'}`}>
                                Lote {l.numeroLote} · {l.cantidadActual} {l.unidad}
                              </small>
                            </div>
                            <span className={`badge bg-${expiry.status !== 'none' ? expiry.status : 'secondary'}`}>
                              {l.fechaVencimiento}
                            </span>
                          </li>
                        )
                      })}
                    </ul>
                  )}
                </div>
              </div>
            </div>
          </div>
        </>
      )}
    </div>
  )
}
