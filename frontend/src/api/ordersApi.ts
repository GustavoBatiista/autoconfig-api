import { apiFetch } from './apiClient'
import { readApiErrorMessage } from './readApiError'
import type { SpringPage } from './springPage'

export type ClientDto = {
  id: number
  name: string
  lastName: string
  phoneNumber: string
}

export type CarDto = {
  id: number
  brand: string
  model: string
  version: string
}

export type OrderAccessoryDto = {
  id: number
  name: string
  description: string
  price: number
  car: CarDto | null
}

export type VehicleEntrySummaryDto = {
  id: number
  chassis: string
  arrivalDate: string
  condition: string
}

export type OrderResponse = {
  id: number
  orderDate: string
  createdAt: string | null
  updatedAt: string | null
  totalPrice: number
  status: string
  vehicleArrived: boolean
  accessoriesConfirmed: boolean
  installationCompleted: boolean
  sellerId: number
  sellerName: string | null
  client: ClientDto
  car: CarDto
  accessories: OrderAccessoryDto[]
  vehicleEntry: VehicleEntrySummaryDto | null
}

/**
 * GET /orders — Spring Data Page JSON.
 */
export async function fetchOrdersPage(page: number, size: number): Promise<SpringPage<OrderResponse>> {
  const params = new URLSearchParams({
    page: String(page),
    size: String(size),
  })
  const res = await apiFetch(`/orders?${params.toString()}`)
  if (!res.ok) {
    throw new Error(`Failed to load orders (${res.status})`)
  }
  return (await res.json()) as SpringPage<OrderResponse>
}

export type CreateOrderPayload = {
  clientId: number
  carId: number
  accessoryIds: number[]
}

export type UpdateOrderPayload = CreateOrderPayload

export async function createOrder(payload: CreateOrderPayload): Promise<OrderResponse> {
  const res = await apiFetch('/orders', {
    method: 'POST',
    body: JSON.stringify(payload),
  })
  if (!res.ok) {
    throw new Error(await readApiErrorMessage(res, `Falha ao criar pedido (${res.status})`))
  }
  return (await res.json()) as OrderResponse
}

export async function fetchOrderById(id: number): Promise<OrderResponse> {
  const res = await apiFetch(`/orders/${id}`)
  if (!res.ok) {
    throw new Error(await readApiErrorMessage(res, `Falha ao carregar pedido (${res.status})`))
  }
  return (await res.json()) as OrderResponse
}

export async function updateOrder(id: number, payload: UpdateOrderPayload): Promise<OrderResponse> {
  const res = await apiFetch(`/orders/${id}`, {
    method: 'PUT',
    body: JSON.stringify(payload),
  })
  if (!res.ok) {
    throw new Error(await readApiErrorMessage(res, `Falha ao atualizar pedido (${res.status})`))
  }
  return (await res.json()) as OrderResponse
}

export async function deleteOrder(id: number): Promise<void> {
  const res = await apiFetch(`/orders/${id}`, { method: 'DELETE' })
  if (!res.ok) {
    throw new Error(await readApiErrorMessage(res, `Falha ao excluir pedido (${res.status})`))
  }
}

export type ConfirmVehiclePayload = {
  chassis: string
  arrivalDate: string
  condition: string
}

export async function confirmOrderVehicle(orderId: number, payload: ConfirmVehiclePayload): Promise<OrderResponse> {
  const res = await apiFetch(`/orders/${orderId}/confirm-vehicle`, {
    method: 'PATCH',
    body: JSON.stringify(payload),
  })
  if (!res.ok) {
    throw new Error(await readApiErrorMessage(res, `Falha ao confirmar veiculo (${res.status})`))
  }
  return (await res.json()) as OrderResponse
}

export async function confirmOrderAccessories(orderId: number): Promise<OrderResponse> {
  const res = await apiFetch(`/orders/${orderId}/confirm-accessories`, { method: 'PATCH' })
  if (!res.ok) {
    throw new Error(await readApiErrorMessage(res, `Falha ao confirmar acessorios (${res.status})`))
  }
  return (await res.json()) as OrderResponse
}

export async function confirmOrderInstallation(orderId: number): Promise<OrderResponse> {
  const res = await apiFetch(`/orders/${orderId}/confirm-installation`, { method: 'PATCH' })
  if (!res.ok) {
    throw new Error(await readApiErrorMessage(res, `Falha ao confirmar instalacao (${res.status})`))
  }
  return (await res.json()) as OrderResponse
}
