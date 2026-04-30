import { apiFetch } from './apiClient'
import { readApiErrorMessage } from './readApiError'
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
    throw new Error(await readApiErrorMessage(res, `Falha ao criar usuário (${res.status})`))
  }
  return (await res.json()) as UserResponse
}

export async function fetchUserById(id: number): Promise<UserResponse> {
  const res = await apiFetch(`/users/${id}`)
  if (!res.ok) {
    throw new Error(await readApiErrorMessage(res, `Falha ao carregar usuário (${res.status})`))
  }
  return (await res.json()) as UserResponse
}

export async function deleteUser(id: number): Promise<void> {
  const res = await apiFetch(`/users/${id}`, { method: 'DELETE' })
  if (!res.ok) {
    throw new Error(await readApiErrorMessage(res, `Falha ao excluir usuário (${res.status})`))
  }
}
