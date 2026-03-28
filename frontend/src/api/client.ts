import { getApiBaseUrl } from '../config'

const TOKEN_KEY = 'autoconfig_token'

export function getStoredToken(): string | null {
  return sessionStorage.getItem(TOKEN_KEY)
}

export function setStoredToken(token: string | null): void {
  if (token) {
    sessionStorage.setItem(TOKEN_KEY, token)
  } else {
    sessionStorage.removeItem(TOKEN_KEY)
  }
}

/**
 * Chamada à API com JSON opcional e header Authorization quando há token guardado.
 */
export async function apiFetch(path: string, init?: RequestInit): Promise<Response> {
  const base = getApiBaseUrl()
  const token = getStoredToken()
  const headers = new Headers(init?.headers)

  if (init?.body != null && !headers.has('Content-Type')) {
    headers.set('Content-Type', 'application/json')
  }
  if (token) {
    headers.set('Authorization', `Bearer ${token}`)
  }

  return fetch(`${base}${path.startsWith('/') ? path : `/${path}`}`, {
    ...init,
    headers,
  })
}
