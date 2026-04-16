import { Link, NavLink, Outlet, useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'

export default function Layout() {
  const { user, logout, hasRole } = useAuth()
  const navigate = useNavigate()

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
              {hasRole('ROOT') && navItem('/usuarios', 'bi-people-fill', 'Usuarios')}
            </ul>
            <ul className="navbar-nav ms-auto">
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
