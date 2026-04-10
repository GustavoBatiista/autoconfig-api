import { apiFetch } from './apiClient'
import { readApiErrorMessage } from './readApiError'

/** Nested order summary from VehicleEntryResponseDTO (backend may send full OrderResponseDTO). */
export type VehicleEntryOrderRef = {
  id: number
}

export type VehicleEntryResponse = {
  id: number
  chassis: string
  arrivalDate: string
  condition: string
  order: VehicleEntryOrderRef | null
}

/**
 * GET /vehicles — backend returns List (not Page).
 */
export async function fetchVehicleEntries(): Promise<VehicleEntryResponse[]> {
  const res = await apiFetch('/vehicles')
  if (!res.ok) {
    throw new Error(`Failed to load vehicles (${res.status})`)
  }
  return (await res.json()) as VehicleEntryResponse[]
}

export type CreateVehicleEntryPayload = {
  chassis: string
  arrivalDate: string
  condition: string
  orderId: number
}

export async function createVehicleEntry(payload: CreateVehicleEntryPayload): Promise<VehicleEntryResponse> {
  const res = await apiFetch('/vehicles', {
    method: 'POST',
    body: JSON.stringify(payload),
  })
  if (!res.ok) {
    throw new Error(await readApiErrorMessage(res, `Falha ao registrar veiculo (${res.status})`))
  }
  return (await res.json()) as VehicleEntryResponse
}
