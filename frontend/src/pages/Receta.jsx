import { useEffect, useState } from 'react'
import { useParams, Link, useNavigate } from 'react-router-dom'
import client, { apiErrorMessage } from '../api/client'
import PageHeader from '../components/PageHeader'
import { useAuth } from '../context/AuthContext'

export default function Receta() {
  const { itemId } = useParams()
  const { hasRole } = useAuth()
  const navigate = useNavigate()
  const puedeEditar = hasRole('PROPIETARIO', 'ROOT')

  const [item, setItem] = useState(null)
  const [receta, setReceta] = useState(null)
  const [insumos, setInsumos] = useState([])
  const [rows, setRows] = useState([{ insumoId: '', cantidad: '' }])
  const [loading, setLoading] = useState(true)
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState(null)
  const [info, setInfo] = useState(null)

  async function load() {
    try {
      setLoading(true)
      const [r1, r2] = await Promise.all([
        client.get(`/api/items/${itemId}`),
        client.get('/api/insumos', { params: { size: 200 } }),
      ])
      setItem(r1.data)
      setInsumos(r2.data.content ?? r2.data)

      try {
        const { data } = await client.get(`/api/items/${itemId}/receta`)
        setReceta(data)
        if (data.ingredientes?.length) {
          setRows(
            data.ingredientes.map((ing) => ({
              insumoId: String(ing.insumoId),
              cantidad: ing.cantidad,
            }))
          )
        }
      } catch (e) {
        if (e.response?.status !== 404) throw e
        setReceta(null)
      }
    } catch (err) {
      setError(apiErrorMessage(err))
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    load()
  }, [itemId])

  function addRow() {
    setRows([...rows, { insumoId: '', cantidad: '' }])
  }

  function removeRow(i) {
    setRows(rows.filter((_, idx) => idx !== i))
  }

  function updateRow(i, field, value) {
    const copy = [...rows]
    copy[i] = { ...copy[i], [field]: value }
    setRows(copy)
  }

  async function handleSave(e) {
    e.preventDefault()
    setError(null)
    setInfo(null)

    const ingredientes = rows
      .filter((r) => r.insumoId && r.cantidad)
      .map((r) => ({ insumoId: parseInt(r.insumoId, 10), cantidad: parseFloat(r.cantidad) }))

    if (ingredientes.length === 0) {
      setError('Debes añadir al menos un ingrediente.')
      return
    }
    const ids = ingredientes.map((x) => x.insumoId)
    if (new Set(ids).size !== ids.length) {
      setError('Hay insumos repetidos. Cada insumo solo puede aparecer una vez.')
      return
    }
    if (ingredientes.some((x) => x.cantidad <= 0)) {
      setError('Todas las cantidades deben ser mayores que 0.')
      return
    }

    setSaving(true)
    try {
      const { data } = await client.put(`/api/items/${itemId}/receta`, { ingredientes })
      setReceta(data)
      setInfo('Receta guardada correctamente.')
    } catch (err) {
      setError(apiErrorMessage(err))
    } finally {
      setSaving(false)
    }
  }

  async function handleDelete() {
    if (!window.confirm('¿Eliminar la receta completa de este producto?')) return
    try {
      await client.delete(`/api/items/${itemId}/receta`)
      setReceta(null)
      setRows([{ insumoId: '', cantidad: '' }])
      setInfo('Receta eliminada.')
    } catch (err) {
      setError(apiErrorMessage(err))
    }
  }

  if (loading) {
    return (
      <div className="text-center py-5">
        <div className="spinner-border text-coffee"></div>
      </div>
    )
  }

  return (
    <div>
      <PageHeader
        icon="bi-journal-text"
        title={`Receta · ${item?.name}`}
        subtitle="Define qué insumos y cantidades consume este producto por venta"
        actions={
          <Link to="/items" className="btn btn-outline-secondary">
            <i className="bi bi-arrow-left me-1"></i>Volver
          </Link>
        }
      />

      {error && <div className="alert alert-danger">{error}</div>}
      {info && <div className="alert alert-success">{info}</div>}

      <div className="row">
        <div className="col-lg-8">
          <div className="card">
            <div className="card-header d-flex justify-content-between align-items-center">
              <span>
                <i className="bi bi-list-check me-2"></i>
                {puedeEditar ? 'Editar ingredientes' : 'Ingredientes'}
              </span>
              {receta && puedeEditar && (
                <button className="btn btn-sm btn-outline-danger" onClick={handleDelete}>
                  <i className="bi bi-trash me-1"></i>Eliminar receta
                </button>
              )}
            </div>
            <div className="card-body">
              {!puedeEditar && receta ? (
                <ul className="list-group list-group-flush">
                  {receta.ingredientes.map((ing) => (
                    <li
                      key={ing.id}
                      className="list-group-item d-flex justify-content-between"
                    >
                      <span>{ing.insumoNombre}</span>
                      <strong>
                        {ing.cantidad} {ing.unidadMedida}
                      </strong>
                    </li>
                  ))}
                </ul>
              ) : !puedeEditar ? (
                <div className="empty-state">
                  <i className="bi bi-info-circle fs-1"></i>
                  <p className="mt-2 mb-0">Este producto aún no tiene receta definida.</p>
                </div>
              ) : (
                <form onSubmit={handleSave}>
                  <table className="table align-middle">
                    <thead>
                      <tr>
                        <th style={{ width: '55%' }}>Insumo</th>
                        <th>Cantidad</th>
                        <th style={{ width: '40px' }}></th>
                      </tr>
                    </thead>
                    <tbody>
                      {rows.map((r, i) => {
                        const insumo = insumos.find((x) => String(x.id) === r.insumoId)
                        return (
                          <tr key={i}>
                            <td>
                              <select
                                className="form-select form-select-sm"
                                required
                                value={r.insumoId}
                                onChange={(e) => updateRow(i, 'insumoId', e.target.value)}
                              >
                                <option value="">— Seleccionar —</option>
                                {insumos.map((ins) => (
                                  <option key={ins.id} value={ins.id}>
                                    {ins.nombre} ({ins.unidadMedida})
                                  </option>
                                ))}
                              </select>
                            </td>
                            <td>
                              <div className="input-group input-group-sm">
                                <input
                                  type="number"
                                  step="0.001"
                                  min="0.001"
                                  className="form-control"
                                  required
                                  value={r.cantidad}
                                  onChange={(e) => updateRow(i, 'cantidad', e.target.value)}
                                />
                                {insumo && (
                                  <span className="input-group-text">{insumo.unidadMedida}</span>
                                )}
                              </div>
                            </td>
                            <td>
                              <button
                                type="button"
                                className="btn btn-sm btn-outline-danger"
                                onClick={() => removeRow(i)}
                                disabled={rows.length === 1}
                              >
                                <i className="bi bi-x"></i>
                              </button>
                            </td>
                          </tr>
                        )
                      })}
                    </tbody>
                  </table>
                  <button
                    type="button"
                    className="btn btn-sm btn-outline-coffee"
                    onClick={addRow}
                  >
                    <i className="bi bi-plus-lg me-1"></i>Añadir ingrediente
                  </button>
                  <hr />
                  <div className="d-flex gap-2">
                    <button type="submit" className="btn btn-coffee" disabled={saving}>
                      {saving ? 'Guardando...' : 'Guardar receta'}
                    </button>
                  </div>
                </form>
              )}
            </div>
          </div>
        </div>

        <div className="col-lg-4 mt-3 mt-lg-0">
          <div className="card">
            <div className="card-header">
              <i className="bi bi-info-circle me-2"></i>Cómo funciona
            </div>
            <div className="card-body small">
              <p>
                La receta define qué cantidad de cada insumo se consume <strong>por cada unidad
                vendida</strong> del producto.
              </p>
              <p>
                Al registrar una venta, el sistema podrá descontar automáticamente los insumos del
                stock siguiendo la política FEFO (primero los que vencen antes).
              </p>
              <p className="mb-0">
                Si este producto ya tiene receta, guardar la sustituye por completo.
              </p>
            </div>
          </div>
        </div>
      </div>
    </div>
  )
}
