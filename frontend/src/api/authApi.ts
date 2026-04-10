import { apiFetch } from './apiClient'
import { setAccessToken } from './tokenStorage'

export type AuthResponse = {
  token: string
  type: string
  email: string
  role: string
}

export type MeResponse = {
  id: number
  email: string
  role: string
}

export async function login(email: string, password: string): Promise<AuthResponse> {
  const res = await apiFetch('/auth/login', {
    method: 'POST',
    body: JSON.stringify({ email, password }),
  })

  if (!res.ok) {
    let detail = res.statusText
    try {
      const body: unknown = await res.json()
      if (body && typeof body === 'object') {
        const o = body as Record<string, unknown>
        if (typeof o.detail === 'string' && o.detail) {
          detail = o.detail
        } else if (typeof o.message === 'string' && o.message) {
          detail = o.message
        }
      }
    } catch {
      /* ignore */
    }
    throw new Error(detail || `Falha no login (${res.status})`)
  }

  const data = (await res.json()) as AuthResponse
  setAccessToken(data.token)
  return data
}

export async function fetchMe(): Promise<MeResponse> {
  const res = await apiFetch('/auth/me')
  if (!res.ok) {
    throw new Error('Sessão inválida ou expirada')
  }
  return (await res.json()) as MeResponse
}

export function logout(): void {
  setAccessToken(null)
}
