import { apiFetch } from './apiClient'
import { readApiErrorMessage } from './readApiError'
import type { SpringPage } from './springPage'

export type CarSummary = {
  id: number
  brand: string
  model: string
  version: string
}

export type AccessoryResponse = {
  id: number
  name: string
  description: string
  price: number
  car: CarSummary
}

export async function fetchAccessoriesPage(page: number, size: number): Promise<SpringPage<AccessoryResponse>> {
  const params = new URLSearchParams({
    page: String(page),
    size: String(size),
  })
  const res = await apiFetch(`/accessories?${params.toString()}`)
  if (!res.ok) {
    throw new Error(`Failed to load accessories (${res.status})`)
  }
  return (await res.json()) as SpringPage<AccessoryResponse>
}

export type CreateAccessoryPayload = {
  name: string
  description: string
  price: number
  carId: number
}

export async function createAccessory(payload: CreateAccessoryPayload): Promise<AccessoryResponse> {
  const res = await apiFetch('/accessories', {
    method: 'POST',
    body: JSON.stringify(payload),
  })
  if (!res.ok) {
    throw new Error(await readApiErrorMessage(res, `Falha ao criar acessorio (${res.status})`))
  }
  return (await res.json()) as AccessoryResponse
}
