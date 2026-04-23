import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import client, { apiErrorMessage } from '../api/client'
import PageHeader from '../components/PageHeader'

export default function VentaForm() {
  const navigate = useNavigate()

  // Lista de items disponibles (de la API)
  const [items, setItems] = useState([])

  // Líneas de la venta que el empleado va añadiendo
  const [lineas, setLineas] = useState([])

  // Controles del selector de nueva línea
  const [itemSelId, setItemSelId] = useState('')
  const [cantidad, setCantidad] = useState(1)

  // Estado del formulario
  const [submitting, setSubmitting] = useState(false)
  const [error, setError] = useState(null)

  useEffect(() => {
    client.get('/api/items', { params: { size: 100 } })
      .then(r => {
        const list = r.data.content ?? r.data
        setItems(list)
        if (list.length > 0) setItemSelId(String(list[0].id))
      })
      .catch(e => setError(apiErrorMessage(e)))
  }, [])

  // ---- añadir línea ----
  function añadirLinea() {
    if (!itemSelId) return
    const itemObj = items.find(i => String(i.id) === itemSelId)
    if (!itemObj) return

    // Si ya existe el mismo producto, suma unidades
    setLineas(prev => {
      const idx = prev.findIndex(l => l.itemId === itemObj.id)
      if (idx !== -1) {
        const copy = [...prev]
        copy[idx] = { ...copy[idx], cantidadUnidades: copy[idx].cantidadUnidades + Number(cantidad) }
        return copy
      }
      return [...prev, { itemId: itemObj.id, itemNombre: itemObj.name, cantidadUnidades: Number(cantidad) }]
    })
    setCantidad(1)
  }

  // ---- eliminar línea ----
  function eliminarLinea(idx) {
    setLineas(prev => prev.filter((_, i) => i !== idx))
  }

  // ---- enviar venta ----
  async function handleSubmit(e) {
    e.preventDefault()
    if (lineas.length === 0) {
      setError('Añade al menos un producto antes de registrar la venta.')
      return
    }
    setSubmitting(true)
    setError(null)
    try {
      await client.post('/api/ventas', {
        lineas: lineas.map(l => ({ itemId: l.itemId, cantidadUnidades: l.cantidadUnidades })),
      })
      navigate('/ventas')
    } catch (err) {
      setError(apiErrorMessage(err))
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <>
      <PageHeader title="Nueva venta" icon="bi-receipt" />

      {error && (
        <div className="alert alert-danger alert-dismissible fade show" role="alert">
          <i className="bi bi-exclamation-triangle-fill me-2" />
          {error}
          <button type="button" className="btn-close" onClick={() => setError(null)} />
        </div>
      )}

      <div className="row g-4">
        {/* ── Panel izquierdo: selector de productos ── */}
        <div className="col-md-5">
          <div className="card h-100">
            <div className="card-header fw-semibold">
              <i className="bi bi-list-ul me-2" />
              Añadir producto
            </div>
            <div className="card-body">
              <div className="mb-3">
                <label htmlFor="item-select" className="form-label">Producto</label>
                <select
                  id="item-select"
                  className="form-select"
                  value={itemSelId}
                  onChange={e => setItemSelId(e.target.value)}
                >
                  {items.map(i => (
                    <option key={i.id} value={String(i.id)}>{i.name}</option>
                  ))}
                </select>
              </div>
              <div className="mb-3">
                <label htmlFor="cantidad-input" className="form-label">Unidades</label>
                <input
                  id="cantidad-input"
                  type="number"
                  min={1}
                  className="form-control"
                  value={cantidad}
                  onChange={e => setCantidad(Math.max(1, Number(e.target.value)))}
                />
              </div>
              <button
                type="button"
                className="btn btn-outline-coffee w-100"
                onClick={añadirLinea}
                disabled={!itemSelId}
              >
                <i className="bi bi-plus-lg me-1" />
                Añadir a la venta
              </button>
            </div>
          </div>
        </div>

        {/* ── Panel derecho: resumen de la venta ── */}
        <div className="col-md-7">
          <div className="card h-100">
            <div className="card-header fw-semibold">
              <i className="bi bi-cart me-2" />
              Resumen de la venta
            </div>
            <div className="card-body p-0">
              {lineas.length === 0 ? (
                <p className="text-muted text-center py-4 mb-0">
                  Aún no has añadido productos.
                </p>
              ) : (
                <table className="table table-hover mb-0">
                  <thead className="table-light">
                    <tr>
                      <th>Producto</th>
                      <th className="text-end">Unidades</th>
                      <th></th>
                    </tr>
                  </thead>
                  <tbody>
                    {lineas.map((l, i) => (
                      <tr key={i}>
                        <td>{l.itemNombre}</td>
                        <td className="text-end">
                          <span className="badge bg-secondary">{l.cantidadUnidades}</span>
                        </td>
                        <td className="text-end">
                          <button
                            type="button"
                            className="btn btn-sm btn-outline-danger"
                            onClick={() => eliminarLinea(i)}
                            title="Eliminar línea"
                          >
                            <i className="bi bi-trash" />
                          </button>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                  <tfoot>
                    <tr className="fw-bold table-light">
                      <td>Total</td>
                      <td className="text-end" colSpan={2}>
                        {lineas.reduce((s, l) => s + l.cantidadUnidades, 0)} uds.
                      </td>
                    </tr>
                  </tfoot>
                </table>
              )}
            </div>
            <div className="card-footer d-flex gap-2 justify-content-end">
              <button
                type="button"
                className="btn btn-outline-secondary"
                onClick={() => navigate('/ventas')}
              >
                Cancelar
              </button>
              <button
                type="submit"
                className="btn btn-coffee"
                disabled={submitting || lineas.length === 0}
                onClick={handleSubmit}
              >
                {submitting
                  ? <><span className="spinner-border spinner-border-sm me-2" />Registrando…</>
                  : <><i className="bi bi-check-lg me-1" />Registrar venta</>}
              </button>
            </div>
          </div>
        </div>
      </div>
    </>
  )
}
