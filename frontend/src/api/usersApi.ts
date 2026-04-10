import { apiFetch } from './apiClient'
import type { SpringPage } from './springPage'

export type UserResponse = {
  id: number
  name: string
  lastName: string
  nickName: string
  email: string
  role: string
}

export type CreateUserPayload = {
  name: string
  lastName: string
  nickName: string
  email: string
  password: string
  role: string
}

export async function fetchUsersPage(page: number, size: number): Promise<SpringPage<UserResponse>> {
  const params = new URLSearchParams({ page: String(page), size: String(size) })
  const res = await apiFetch(`/users?${params.toString()}`)
  if (!res.ok) {
    throw new Error(`Failed to load users (${res.status})`)
  }
  return (await res.json()) as SpringPage<UserResponse>
}

export async function createUser(payload: CreateUserPayload): Promise<UserResponse> {
  const res = await apiFetch('/users', {
    method: 'POST',
    body: JSON.stringify(payload),
  })
  if (!res.ok) {
    let detail = res.statusText
    try {
      const body: unknown = await res.json()
      if (body && typeof body === 'object') {
        const o = body as Record<string, unknown>
        if (typeof o.detail === 'string') detail = o.detail
        else if (typeof o.message === 'string') detail = o.message
      }
    } catch {
      /* ignore */
    }
    throw new Error(detail || `Failed to create user (${res.status})`)
  }
  return (await res.json()) as UserResponse
}
