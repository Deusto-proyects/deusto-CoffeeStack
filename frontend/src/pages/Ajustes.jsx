import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import client, { apiErrorMessage } from '../api/client'
import PageHeader from '../components/PageHeader'
import { useAuth } from '../context/AuthContext'

export default function Ajustes() {
  const { hasRole } = useAuth()
  const [movs, setMovs] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)
  const [filterTipo, setFilterTipo] = useState('')

  async function load() {
    try {
      setLoading(true)
      const { data } = await client.get('/api/ajustes')
      setMovs(data)
    } catch (err) {
      setError(apiErrorMessage(err))
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    load()
  }, [])

  const filtered = filterTipo ? movs.filter((m) => m.tipoMovimiento === filterTipo) : movs

  return (
    <div>
      <PageHeader
        icon="bi-clipboard-data"
        title="Ajustes e historial"
        subtitle="Mermas, roturas y ajustes manuales de inventario"
        actions={
          hasRole('PROPIETARIO', 'ROOT') && (
            <Link to="/ajustes/nuevo" className="btn btn-coffee">
              <i className="bi bi-plus-lg me-1"></i>Registrar ajuste
            </Link>
          )
        }
      />

      {error && <div className="alert alert-danger">{error}</div>}

      <div className="card">
        <div className="card-body">
          <div className="row mb-3">
            <div className="col-md-4">
              <label className="form-label">Filtrar por tipo</label>
              <select
                className="form-select"
                value={filterTipo}
                onChange={(e) => setFilterTipo(e.target.value)}
              >
                <option value="">Todos</option>
                <option value="MERMA">MERMA</option>
                <option value="ROTURA">ROTURA</option>
                <option value="AJUSTE_POSITIVO">AJUSTE_POSITIVO</option>
                <option value="AJUSTE_NEGATIVO">AJUSTE_NEGATIVO</option>
              </select>
            </div>
          </div>

          {loading ? (
            <div className="text-center py-4">
              <div className="spinner-border text-coffee"></div>
            </div>
          ) : filtered.length === 0 ? (
            <div className="empty-state">
              <i className="bi bi-clock-history fs-1"></i>
              <p className="mt-2 mb-0">Sin movimientos registrados.</p>
            </div>
          ) : (
            <div className="table-responsive">
              <table className="table table-hover align-middle">
                <thead>
                  <tr>
                    <th>Fecha / hora</th>
                    <th>Insumo</th>
                    <th>Lote</th>
                    <th>Tipo</th>
                    <th className="text-end">Cantidad</th>
                    <th>Motivo</th>
                    <th>Usuario</th>
                  </tr>
                </thead>
                <tbody>
                  {filtered.map((m) => (
                    <tr key={m.id}>
                      <td>
                        <small>{new Date(m.fechaHora).toLocaleString()}</small>
                      </td>
                      <td>
                        <strong>{m.insumoNombre}</strong>
                      </td>
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
