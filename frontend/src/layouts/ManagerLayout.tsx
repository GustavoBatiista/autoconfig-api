import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { DashboardShell, type DashboardNavItem } from '../components/dashboard/DashboardShell'
import { fetchMe, logout, type MeResponse } from '../api/authApi'
import { canAccessManagerDashboard } from '../domain/roles'

const MANAGER_NAV: DashboardNavItem[] = [
  { to: '/manager', label: 'Pedidos', icon: '📋', end: true },
  { to: '/manager/cars', label: 'Carros', icon: '🚗' },
  { to: '/manager/clients', label: 'Cliente', icon: '👤' },
  { to: '/manager/accessories', label: 'Acessórios', icon: '🔩' },
  { to: '/manager/users', label: 'Usuários', icon: '👥' },
]

export function ManagerLayout() {
  const navigate = useNavigate()
  const [me, setMe] = useState<MeResponse | null>(null)
  const [forbidden, setForbidden] = useState(false)

  useEffect(() => {
    let cancelled = false
    void (async () => {
      try {
        const data = await fetchMe()
        if (cancelled) return
        if (!canAccessManagerDashboard(data.role)) {
          setForbidden(true)
          navigate('/', { replace: true })
          return
        }
        setMe(data)
      } catch {
        if (!cancelled) navigate('/login', { replace: true })
      }
    })()
    return () => {
      cancelled = true
    }
  }, [navigate])

  function handleLogout() {
    logout()
    navigate('/login', { replace: true })
  }

  if (forbidden) {
    return null
  }

  if (!me) {
    return (
      <div className="dash-app dash-app--loading">
        <p className="dash-muted">Carregando...</p>
      </div>
    )
  }

  return (
    <DashboardShell navItems={MANAGER_NAV} userEmail={me.email} onLogout={handleLogout} />
  )
}
