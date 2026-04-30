import { type FormEvent, useEffect, useMemo, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { createUser, type CreateUserPayload } from '../../api/usersApi'
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

export function UserCreatePage() {
  const navigate = useNavigate()
  const [me, setMe] = useState<MeResponse | null>(null)
  const [error, setError] = useState<string | null>(null)
  const [saving, setSaving] = useState(false)

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
    if (assignableRoles.length && !assignableRoles.some((r) => r === newUserRole)) {
      setNewUserRole(assignableRoles[0])
    }
  }, [assignableRoles, newUserRole])

  function goBack() {
    navigate('..')
  }

  async function onSubmit(e: FormEvent) {
    e.preventDefault()
    setError(null)
    setSaving(true)
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
      navigate('..')
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Erro ao criar usuário')
    } finally {
      setSaving(false)
    }
  }

  return (
    <div>
      <h2 className="dash-page__heading">Novo usuário</h2>

      <p className="dash-hint">
        Apenas administradores podem criar ou atribuir a função <strong>Administrador</strong>. Gestores gerem os demais
        perfis; o backend aplica a mesma regra em atualizações e exclusões.
      </p>

      {error ? <p className="dash-error">{error}</p> : null}

      <form className="dash-user-form" onSubmit={onSubmit}>
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
        <div className="dash-form-actions">
          <button type="submit" className="dash-btn-primary" disabled={saving}>
            {saving ? 'Salvando...' : 'Salvar'}
          </button>
          <button type="button" className="dash-btn-secondary" onClick={goBack} disabled={saving}>
            Cancelar
          </button>
        </div>
      </form>
    </div>
  )
}
