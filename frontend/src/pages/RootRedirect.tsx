import { useEffect, useState } from 'react'
import { Navigate } from 'react-router-dom'
import { fetchMe } from '../api/authApi'
import { canAccessManagerDashboard, isSellerRole } from '../domain/roles'
import { HomePage } from './HomePage'


export function RootRedirect() {
  const [role, setRole] = useState<string | null>(null)
  const [error, setError] = useState(false)

  useEffect(() => {
    let cancelled = false
    void (async () => {
      try {
        const me = await fetchMe()
        if (!cancelled) setRole(me.role)
      } catch {
        if (!cancelled) setError(true)
      }
    })()
    return () => {
      cancelled = true
    }
  }, [])

  if (error) {
    return <Navigate to="/login" replace />
  }

  if (role === null) {
    return (
      <div className="shell">
        <p>Carregando...</p>
      </div>
    )
  }

  if (isSellerRole(role)) {
    return <Navigate to="/seller" replace />
  }

  if (canAccessManagerDashboard(role)) {
    return <Navigate to="/manager" replace />
  }

  return <HomePage />
}
