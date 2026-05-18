import { useEffect, useState } from 'react'
import { Link, NavLink, Outlet, useNavigate } from 'react-router-dom'
import client from '../api/client'
import { useAuth } from '../context/AuthContext'

export default function Layout() {
  const { user, logout, hasRole } = useAuth()
  const navigate = useNavigate()
  const [alertasStock, setAlertasStock] = useState(0)

  useEffect(() => {
    if (user && hasRole('PROPIETARIO', 'ROOT')) {
      client.get('/api/stock/insumos')
        .then(res => {
          const count = res.data.filter(s => s.tieneRiesgoFaltante).length
          setAlertasStock(count)
        })
        .catch(err => console.error("Error al cargar alertas de stock", err))
    }
  }, [user, hasRole])

  function handleLogout() {
    logout()
    navigate('/login')
  }

  const navItem = (to, icon, label) => (
    <li className="nav-item">
      <NavLink
        to={to}
        className={({ isActive }) =>
          'nav-link' + (isActive ? ' active fw-bold' : '')
        }
      >
        <i className={`bi ${icon} me-1`}></i>
        {label}
      </NavLink>
    </li>
  )

  return (
    <>
      <nav className="navbar navbar-expand-lg navbar-dark bg-coffee shadow-sm">
        <div className="container-fluid">
          <Link to="/" className="navbar-brand fw-bold">
            <i className="bi bi-cup-hot-fill me-2"></i>
            CoffeeStack
          </Link>
          <button
            className="navbar-toggler"
            type="button"
            data-bs-toggle="collapse"
            data-bs-target="#mainNav"
          >
            <span className="navbar-toggler-icon"></span>
          </button>
          <div className="collapse navbar-collapse" id="mainNav">
            <ul className="navbar-nav me-auto mb-2 mb-lg-0">
              {navItem('/', 'bi-speedometer2', 'Dashboard')}
              {navItem('/insumos', 'bi-box-seam', 'Insumos')}
              {navItem('/items', 'bi-list-ul', 'Productos')}
              {navItem('/lotes', 'bi-boxes', 'Lotes')}
              {navItem('/proveedores', 'bi-truck', 'Proveedores')}
              {navItem('/ajustes', 'bi-clipboard-data', 'Ajustes')}
              {hasRole('PROPIETARIO', 'ROOT') && navItem('/historial', 'bi-journal-text', 'Historial')}
              {hasRole('PROPIETARIO', 'ROOT') && navItem('/estimacion-consumo', 'bi-graph-up-arrow', 'Estimación')}
              {hasRole('PROPIETARIO', 'ROOT') && navItem('/cobertura-insumos', 'bi-shield-exclamation', 'Cobertura')}
              {hasRole('PROPIETARIO', 'ROOT') && navItem('/reposicion', 'bi-cart-plus', 'Reposición')}
              {hasRole('PROPIETARIO', 'ROOT') && navItem('/chatbot', 'bi-robot', 'Asistente IA')}
              {hasRole('PROPIETARIO', 'ROOT') && navItem('/reportes', 'bi-file-earmark-arrow-down', 'Exportar')}
              {hasRole('PROPIETARIO', 'ROOT') && navItem('/reportes/consumo', 'bi-bar-chart-line', 'Reporte consumo')}
              {hasRole('PROPIETARIO', 'ROOT') && navItem('/reportes/comparativa', 'bi-bar-chart-steps', 'Comparativa')}
              {hasRole('PROPIETARIO', 'ROOT') && navItem('/reportes/motivos', 'bi-clipboard-data-fill', 'Motivos')}
              {navItem('/ventas', 'bi-receipt', 'Ventas')}
              {hasRole('ROOT') && navItem('/usuarios', 'bi-people-fill', 'Usuarios')}
            </ul>
            <ul className="navbar-nav ms-auto align-items-center">
              {hasRole('PROPIETARIO', 'ROOT') && (
                <li className="nav-item me-3">
                  <Link to="/insumos" className="nav-link position-relative p-1" title="Alertas de stock">
                    <i className="bi bi-bell-fill fs-5"></i>
                    {alertasStock > 0 && (
                      <span className="position-absolute top-0 start-100 translate-middle badge rounded-pill bg-danger" style={{ fontSize: '0.65em' }}>
                        {alertasStock}
                        <span className="visually-hidden">alertas de stock</span>
                      </span>
                    )}
                  </Link>
                </li>
              )}
              <li className="nav-item dropdown">
                <a
                  className="nav-link dropdown-toggle"
                  href="#"
                  role="button"
                  data-bs-toggle="dropdown"
                >
                  <i className="bi bi-person-circle me-1"></i>
                  {user?.username}
                  <span
                    className={`badge ms-2 badge-rol-${user?.rol}`}
                    style={{ fontSize: '0.7em' }}
                  >
                    {user?.rol}
                  </span>
                </a>
                <ul className="dropdown-menu dropdown-menu-end">
                  <li>
                    <Link className="dropdown-item" to="/perfil">
                      <i className="bi bi-person-lines-fill me-2"></i>Mi perfil
                    </Link>
                  </li>
                  <li>
                    <hr className="dropdown-divider" />
                  </li>
                  <li>
                    <button className="dropdown-item text-danger" onClick={handleLogout}>
                      <i className="bi bi-box-arrow-right me-2"></i>Cerrar sesión
                    </button>
                  </li>
                </ul>
              </li>
            </ul>
          </div>
        </div>
      </nav>

      <main className="container-fluid py-4 px-md-4">
        <Outlet />
      </main>

      <footer className="text-center py-3 text-muted small">
        CoffeeStack &middot; Proceso de Software y Calidad &middot; Universidad de Deusto
      </footer>
    </>
  )
}
