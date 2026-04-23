import { useEffect, useState } from 'react'
import { useParams, Link } from 'react-router-dom'
import client, { apiErrorMessage } from '../api/client'
import PageHeader from '../components/PageHeader'

export default function VentaDetalle() {
  const { id } = useParams()
  const [venta, setVenta] = useState(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)

  useEffect(() => {
    client.get(`/api/ventas/${id}`)
      .then(r => setVenta(r.data))
      .catch(e => setError(apiErrorMessage(e)))
      .finally(() => setLoading(false))
  }, [id])

  if (loading) return <p className="text-muted mt-4">Cargando…</p>
  if (error)   return <p className="text-danger mt-4">{error}</p>
  if (!venta)  return null

  const fecha = new Date(venta.fechaHora).toLocaleString('es-ES', {
    dateStyle: 'long',
    timeStyle: 'short',
  })

  return (
    <>
      <PageHeader
        title={`Venta #${venta.id}`}
        icon="bi-receipt"
        action={
          <Link to="/ventas" className="btn btn-outline-secondary">
            <i className="bi bi-arrow-left me-1" />
            Volver
          </Link>
        }
      />

      <div className="card mb-4">
        <div className="card-body">
          <dl className="row mb-0">
            <dt className="col-sm-3">Fecha y hora</dt>
            <dd className="col-sm-9">{fecha}</dd>

            <dt className="col-sm-3">Empleado</dt>
            <dd className="col-sm-9">{venta.usuario}</dd>
          </dl>
        </div>
      </div>

      <h5 className="mb-3">Líneas de venta</h5>
      <div className="table-responsive">
        <table className="table table-bordered align-middle">
          <thead className="table-dark">
            <tr>
              <th>Producto</th>
              <th className="text-end">Unidades</th>
            </tr>
          </thead>
          <tbody>
            {venta.lineas.map((l, i) => (
              <tr key={i}>
                <td>{l.itemNombre}</td>
                <td className="text-end">
                  <span className="badge bg-secondary">{l.cantidadUnidades}</span>
                </td>
              </tr>
            ))}
          </tbody>
          <tfoot>
            <tr className="fw-bold">
              <td>Total unidades</td>
              <td className="text-end">
                {venta.lineas.reduce((s, l) => s + l.cantidadUnidades, 0)}
              </td>
            </tr>
          </tfoot>
        </table>
      </div>
    </>
  )
}
