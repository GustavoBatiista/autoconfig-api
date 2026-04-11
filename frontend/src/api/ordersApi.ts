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

export type OrderResponse = {
  id: number
  orderDate: string
  /** ISO timestamp from backend auditing (createdAt); may be null for old rows. */
  createdAt: string | null
  totalPrice: number
  status: string
  client: ClientDto
  car: CarDto
  accessories: OrderAccessoryDto[]
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
  status: string
}

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
