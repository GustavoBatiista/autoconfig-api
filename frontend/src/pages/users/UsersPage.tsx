import { type FormEvent, useEffect, useMemo, useState } from 'react'
import { createUser, fetchUsersPage, type CreateUserPayload, type UserResponse } from '../../api/usersApi'
import { fetchMe, type MeResponse } from '../../api/authApi'
import { isAdminRole, ROLE_ADMIN } from '../../domain/roles'

const ROLES_WITHOUT_ADMIN = [
  'ROLE_MANAGER',
  'ROLE_SELLER',
  'ROLE_VEHICLE_STOCK',
  'ROLE_ACCESSORY_STOCK',
] as const

const ALL_ASSIGNABLE_ROLES = [...ROLES_WITHOUT_ADMIN, ROLE_ADMIN] as const

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
  const [me, setMe] = useState<MeResponse | null>(null)
  const [users, setUsers] = useState<UserResponse[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [createError, setCreateError] = useState<string | null>(null)
  const [creating, setCreating] = useState(false)

  const [name, setName] = useState('')
  const [lastName, setLastName] = useState('')
  const [nickName, setNickName] = useState('')
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [newUserRole, setNewUserRole] = useState<string>(ROLES_WITHOUT_ADMIN[0])

  const assignableRoles = useMemo(() => {
    if (me && isAdminRole(me.role)) {
      return [...ALL_ASSIGNABLE_ROLES]
    }
    return [...ROLES_WITHOUT_ADMIN]
  }, [me])

  async function loadUsers() {
    setLoading(true)
    setError(null)
    try {
      const page = await fetchUsersPage(0, 100)
      setUsers(page.content)
    } catch (e) {
      setError(e instanceof Error ? e.message : 'Erro ao carregar usuários')
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    let cancelled = false
    void (async () => {
      try {
        const m = await fetchMe()
        if (!cancelled) {
          setMe(m)
          if (!isAdminRole(m.role)) {
            setNewUserRole(ROLES_WITHOUT_ADMIN[0])
          }
        }
      } catch {
        if (!cancelled) {
          setMe(null)
        }
      }
    })()
    return () => {
      cancelled = true
    }
  }, [])

  useEffect(() => {
    void loadUsers()
  }, [])

  useEffect(() => {
    if (assignableRoles.length && !assignableRoles.some((r) => r === newUserRole)) {
      setNewUserRole(assignableRoles[0])
    }
  }, [assignableRoles, newUserRole])

  async function onCreateUser(e: FormEvent) {
    e.preventDefault()
    setCreateError(null)
    setCreating(true)
    try {
      const payload: CreateUserPayload = {
        name: name.trim(),
        lastName: lastName.trim(),
        nickName: nickName.trim(),
        email: email.trim(),
        password,
        role: newUserRole,
      }
      await createUser(payload)
      setName('')
      setLastName('')
      setNickName('')
      setEmail('')
      setPassword('')
      await loadUsers()
    } catch (err) {
      setCreateError(err instanceof Error ? err.message : 'Erro ao criar usuário')
    } finally {
      setCreating(false)
    }
  }

  return (
    <div>
      <h2 className="dash-page__heading">Usuários</h2>

      <p className="dash-hint">
        Apenas administradores podem criar ou atribuir a função <strong>Administrador</strong>. Gestores gerem os demais
        perfis; o backend aplica a mesma regra em atualizações e exclusões.
      </p>

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

      <h3 className="dash-page__heading" style={{ marginTop: '2rem', fontSize: '1.1rem' }}>
        Novo usuário
      </h3>

      {createError ? <p className="dash-error">{createError}</p> : null}

      <form className="dash-user-form" onSubmit={onCreateUser}>
        <div className="dash-user-form__grid">
          <label>
            Nome
            <input value={name} onChange={(e) => setName(e.target.value)} required minLength={3} maxLength={50} />
          </label>
          <label>
            Sobrenome
            <input
              value={lastName}
              onChange={(e) => setLastName(e.target.value)}
              required
              minLength={3}
              maxLength={50}
            />
          </label>
          <label>
            Apelido
            <input
              value={nickName}
              onChange={(e) => setNickName(e.target.value)}
              required
              minLength={3}
              maxLength={50}
            />
          </label>
          <label>
            Email
            <input type="email" value={email} onChange={(e) => setEmail(e.target.value)} required />
          </label>
          <label>
            Senha
            <input
              type="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              required
              minLength={8}
              maxLength={50}
            />
          </label>
          <label>
            Função
            <select value={newUserRole} onChange={(e) => setNewUserRole(e.target.value)}>
              {assignableRoles.map((r) => (
                <option key={r} value={r}>
                  {roleLabel(r)}
                </option>
              ))}
            </select>
          </label>
        </div>
        <button type="submit" className="dash-btn-primary" disabled={creating}>
          {creating ? 'Criando...' : 'Criar usuário'}
        </button>
      </form>
    </div>
  )
}
