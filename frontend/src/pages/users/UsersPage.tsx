import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { fetchUsersPage, type UserResponse } from '../../api/usersApi'
import { fetchMe, type MeResponse } from '../../api/authApi'
import { canAccessManagerDashboard } from '../../domain/roles'
import { DashListHeader } from '../../components/dashboard/DashListHeader'

function roleLabel(role: string): string {
  const map: Record<string, string> = {
    ROLE_ADMIN: 'Administrador',
    ROLE_MANAGER: 'Gestor',
    ROLE_SELLER: 'Vendedor',
    ROLE_VEHICLE_STOCK: 'Estoque veículos',
    ROLE_ACCESSORY_STOCK: 'Estoque acessórios',
  }
  return map[role] ?? role
}

export function UsersPage() {
  const navigate = useNavigate()
  const [me, setMe] = useState<MeResponse | null>(null)
  const [users, setUsers] = useState<UserResponse[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  const canMutateUsers = me != null && canAccessManagerDashboard(me.role)

  useEffect(() => {
    let cancelled = false
    void (async () => {
      try {
        const m = await fetchMe()
        if (!cancelled) setMe(m)
      } catch {
        if (!cancelled) setMe(null)
      }
    })()
    return () => {
      cancelled = true
    }
  }, [])

  useEffect(() => {
    let cancelled = false
    void (async () => {
      setLoading(true)
      setError(null)
      try {
        const page = await fetchUsersPage(0, 100)
        if (!cancelled) setUsers(page.content)
      } catch (e) {
        if (!cancelled) setError(e instanceof Error ? e.message : 'Erro ao carregar usuários')
      } finally {
        if (!cancelled) setLoading(false)
      }
    })()
    return () => {
      cancelled = true
    }
  }, [])

  return (
    <div>
      <DashListHeader
        title="Usuários"
        actions={
          canMutateUsers ? (
            <>
              <button type="button" className="dash-btn-primary" onClick={() => navigate('new')}>
                Criar
              </button>
              <button type="button" className="dash-btn-secondary" onClick={() => navigate('delete')}>
                Excluir
              </button>
            </>
          ) : undefined
        }
      />

      {error ? <p className="dash-error">{error}</p> : null}

      <div className="dash-table-wrap">
        <table className="dash-table">
          <thead>
            <tr>
              <th>ID</th>
              <th>Nome</th>
              <th>Email</th>
              <th>Função</th>
            </tr>
          </thead>
          <tbody>
            {loading ? (
              <tr>
                <td colSpan={4} className="dash-table__empty">
                  Carregando...
                </td>
              </tr>
            ) : users.length === 0 ? (
              <tr>
                <td colSpan={4} className="dash-table__empty">
                  Nenhum usuário encontrado.
                </td>
              </tr>
            ) : (
              users.map((u) => (
                <tr key={u.id}>
                  <td>{u.id}</td>
                  <td>
                    {u.name} {u.lastName}
                  </td>
                  <td>{u.email}</td>
                  <td>{roleLabel(u.role)}</td>
                </tr>
              ))
            )}
          </tbody>
        </table>
      </div>
    </div>
  )
}
