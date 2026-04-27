import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import client, { apiErrorMessage } from '../api/client'
import PageHeader from '../components/PageHeader'

export default function Ventas() {
  const [ventas, setVentas] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)

  useEffect(() => {
    client.get('/api/ventas')
      .then(r => setVentas(r.data))
      .catch(e => setError(apiErrorMessage(e)))
      .finally(() => setLoading(false))
  }, [])

  if (loading) return <p className="text-muted mt-4">Cargando ventas…</p>
  if (error)   return <p className="text-danger mt-4">{error}</p>

  return (
    <>
      <PageHeader
        title="Ventas"
        icon="bi-receipt"
        actions={
          <Link to="/ventas/nueva" className="btn btn-coffee">
            <i className="bi bi-plus-lg me-1" />
            Nueva venta
          </Link>
        }
      />

      {ventas.length === 0 ? (
        <div className="alert alert-info">No hay ventas registradas todavía.</div>
      ) : (
        <div className="table-responsive">
          <table className="table table-hover align-middle">
            <thead className="table-dark">
              <tr>
                <th>#</th>
                <th>Fecha y hora</th>
                <th>Empleado</th>
                <th>Productos</th>
                <th>Unidades totales</th>
                <th></th>
              </tr>
            </thead>
            <tbody>
              {ventas.map(v => {
                const totalUnidades = v.lineas.reduce((s, l) => s + l.cantidadUnidades, 0)
                const productos = v.lineas.map(l => l.itemNombre).join(', ')
                const fecha = new Date(v.fechaHora).toLocaleString('es-ES', {
                  dateStyle: 'short',
                  timeStyle: 'short',
                })
                return (
                  <tr key={v.id}>
                    <td className="text-muted">#{v.id}</td>
                    <td>{fecha}</td>
                    <td>
                      <i className="bi bi-person me-1" />
                      {v.usuario}
                    </td>
                    <td className="text-truncate" style={{ maxWidth: 260 }} title={productos}>
                      {productos}
                    </td>
                    <td>
                      <span className="badge bg-secondary">{totalUnidades}</span>
                    </td>
                    <td className="text-end">
                      <Link to={`/ventas/${v.id}`} className="btn btn-sm btn-outline-secondary">
                        <i className="bi bi-eye me-1" />
                        Detalle
                      </Link>
                    </td>
                  </tr>
                )
              })}
            </tbody>
          </table>
        </div>
      )}
    </>
  )
}
