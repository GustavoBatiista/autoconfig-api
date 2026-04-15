import { apiFetch } from './apiClient'
import { readApiErrorMessage } from './readApiError'
import type { SpringPage } from './springPage'

export type ClientResponse = {
  id: number
  name: string
  lastName: string
  phoneNumber: string
}

export async function fetchClientsPage(page: number, size: number): Promise<SpringPage<ClientResponse>> {
  const params = new URLSearchParams({
    page: String(page),
    size: String(size),
  })
  const res = await apiFetch(`/clients?${params.toString()}`)
  if (!res.ok) {
    throw new Error(`Failed to load clients (${res.status})`)
  }
  return (await res.json()) as SpringPage<ClientResponse>
}

export type CreateClientPayload = {
  name: string
  lastName: string
  phoneNumber: string
}

export async function createClient(payload: CreateClientPayload): Promise<ClientResponse> {
  const res = await apiFetch('/clients', {
    method: 'POST',
    body: JSON.stringify(payload),
  })
  if (!res.ok) {
    throw new Error(await readApiErrorMessage(res, `Falha ao criar cliente (${res.status})`))
  }
  return (await res.json()) as ClientResponse
}

export async function fetchClientById(id: number): Promise<ClientResponse> {
  const res = await apiFetch(`/clients/${id}`)
  if (!res.ok) {
    throw new Error(await readApiErrorMessage(res, `Falha ao carregar cliente (${res.status})`))
  }
  return (await res.json()) as ClientResponse
}

export async function updateClient(id: number, payload: CreateClientPayload): Promise<ClientResponse> {
  const res = await apiFetch(`/clients/${id}`, {
    method: 'PUT',
    body: JSON.stringify(payload),
  })
  if (!res.ok) {
    throw new Error(await readApiErrorMessage(res, `Falha ao atualizar cliente (${res.status})`))
  }
  return (await res.json()) as ClientResponse
}

export async function deleteClient(id: number): Promise<void> {
  const res = await apiFetch(`/clients/${id}`, { method: 'DELETE' })
  if (!res.ok) {
    throw new Error(await readApiErrorMessage(res, `Falha ao excluir cliente (${res.status})`))
  }
}
