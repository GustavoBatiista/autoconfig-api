import { NavLink, Outlet } from 'react-router-dom'

export type DashboardNavItem = {
  to: string
  label: string
  icon: string
  end?: boolean
}

type DashboardShellProps = {
  navItems: DashboardNavItem[]
  userEmail: string
  onLogout: () => void
}

export function DashboardShell({ navItems, userEmail, onLogout }: DashboardShellProps) {
  return (
    <div className="dash-app">
      <aside className="dash-sidebar" aria-label="Navegação principal">
        <div className="dash-sidebar__brand">AutoConfig</div>
        <nav className="dash-nav">
          {navItems.map((item) => (
            <NavLink
              key={item.to}
              to={item.to}
              end={item.end}
              className={({ isActive }) => (isActive ? 'dash-nav__link is-active' : 'dash-nav__link')}
            >
              <span className="dash-nav__icon" aria-hidden>
                {item.icon}
              </span>
              {item.label}
            </NavLink>
          ))}
        </nav>
      </aside>

      <div className="dash-column">
        <header className="dash-topbar">
          <div className="dash-userbar">
            <span className="dash-userbar__name" title={userEmail}>
              {userEmail}
            </span>
            <button type="button" className="dash-btn-logout" onClick={onLogout}>
              Sair
            </button>
          </div>
        </header>

        <main className="dash-main">
          <Outlet />
        </main>
      </div>
    </div>
  )
}
