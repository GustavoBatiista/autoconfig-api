import { apiFetch } from './apiClient'
import { readApiErrorMessage } from './readApiError'
import type { SpringPage } from './springPage'

export type CarResponse = {
  id: number
  brand: string
  model: string
  version: string
}

export async function fetchCarsPage(page: number, size: number): Promise<SpringPage<CarResponse>> {
  const params = new URLSearchParams({
    page: String(page),
    size: String(size),
  })
  const res = await apiFetch(`/cars?${params.toString()}`)
  if (!res.ok) {
    throw new Error(`Failed to load cars (${res.status})`)
  }
  return (await res.json()) as SpringPage<CarResponse>
}

export type CreateCarPayload = {
  brand: string
  model: string
  version: string
}

export async function createCar(payload: CreateCarPayload): Promise<CarResponse> {
  const res = await apiFetch('/cars', {
    method: 'POST',
    body: JSON.stringify(payload),
  })
  if (!res.ok) {
    throw new Error(await readApiErrorMessage(res, `Falha ao criar carro (${res.status})`))
  }
  return (await res.json()) as CarResponse
}

export async function fetchCarById(id: number): Promise<CarResponse> {
  const res = await apiFetch(`/cars/${id}`)
  if (!res.ok) {
    throw new Error(await readApiErrorMessage(res, `Falha ao carregar carro (${res.status})`))
  }
  return (await res.json()) as CarResponse
}

export async function updateCar(id: number, payload: CreateCarPayload): Promise<CarResponse> {
  const res = await apiFetch(`/cars/${id}`, {
    method: 'PUT',
    body: JSON.stringify(payload),
  })
  if (!res.ok) {
    throw new Error(await readApiErrorMessage(res, `Falha ao atualizar carro (${res.status})`))
  }
  return (await res.json()) as CarResponse
}

export async function deleteCar(id: number): Promise<void> {
  const res = await apiFetch(`/cars/${id}`, { method: 'DELETE' })
  if (!res.ok) {
    throw new Error(await readApiErrorMessage(res, `Falha ao excluir carro (${res.status})`))
  }
}
