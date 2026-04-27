import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import client, { apiErrorMessage } from '../api/client'
import PageHeader from '../components/PageHeader'

export default function Lotes() {
  const [insumos, setInsumos] = useState([])
  const [selectedInsumo, setSelectedInsumo] = useState('')
  const [lotes, setLotes] = useState([])
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState(null)

  useEffect(() => {
    client
      .get('/api/insumos', { params: { size: 200 } })
      .then(({ data }) => {
        const list = data.content ?? data
        setInsumos(list)
        if (list.length > 0) {
          setSelectedInsumo(String(list[0].id))
        }
      })
      .catch((e) => setError(apiErrorMessage(e)))
  }, [])

  useEffect(() => {
    if (!selectedInsumo) {
      setLotes([])
      return
    }
    setLoading(true)
    client
      .get(`/api/lotes/insumo/${selectedInsumo}`)
      .then(({ data }) => setLotes(data))
      .catch((e) => setError(apiErrorMessage(e)))
      .finally(() => setLoading(false))
  }, [selectedInsumo])

  return (
    <div>
      <PageHeader
        icon="bi-boxes"
        title="Lotes"
        subtitle="Recepciones de insumos por lote"
        actions={
          <Link to="/lotes/nuevo" className="btn btn-coffee">
            <i className="bi bi-plus-lg me-1"></i>Recibir lote
          </Link>
        }
      />

      {error && <div className="alert alert-danger">{error}</div>}

      <div className="card">
        <div className="card-body">
          <div className="row mb-3">
            <div className="col-md-6 col-lg-4">
              <label className="form-label">Filtrar por insumo</label>
              <select
                className="form-select"
                value={selectedInsumo}
                onChange={(e) => setSelectedInsumo(e.target.value)}
              >
                <option value="">— Seleccionar insumo —</option>
                {insumos.map((i) => (
                  <option key={i.id} value={i.id}>
                    {i.nombre}
                  </option>
                ))}
              </select>
            </div>
          </div>

          {loading ? (
            <div className="text-center py-4">
              <div className="spinner-border text-coffee"></div>
            </div>
          ) : lotes.length === 0 ? (
            <div className="empty-state">
              <i className="bi bi-inbox fs-1"></i>
              <p className="mt-2 mb-0">
                {selectedInsumo
                  ? 'No hay lotes para este insumo.'
                  : 'Selecciona un insumo para ver sus lotes.'}
              </p>
            </div>
          ) : (
            <div className="table-responsive">
              <table className="table table-hover align-middle">
                <thead>
                  <tr>
                    <th>Nº Lote</th>
                    <th>Proveedor</th>
                    <th className="text-end">Inicial</th>
                    <th className="text-end">Actual</th>
                    <th>Vencimiento</th>
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
                        {l.fechaVencimiento || (
                          <span className="text-muted">sin fecha</span>
                        )}
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
