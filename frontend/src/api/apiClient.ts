import { getApiBaseUrl } from '../config'
import { getAccessToken } from './tokenStorage'

/**
 * Cliente HTTP da API: base URL, JSON e Authorization quando há token.
 */
export async function apiFetch(path: string, init?: RequestInit): Promise<Response> {
  const base = getApiBaseUrl()
  const token = getAccessToken()
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
