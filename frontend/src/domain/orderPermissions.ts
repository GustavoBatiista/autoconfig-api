import type { MeResponse } from '../api/authApi'
import type { OrderResponse } from '../api/ordersApi'

/** Roles allowed by the API to PUT/DELETE orders (see SecurityConfig). */
const ORDER_MUTATION_ROLES = new Set(['ROLE_ADMIN', 'ROLE_MANAGER', 'ROLE_SELLER'])

/**
 * Whether the UI should offer edit/delete for this order.
 * SELLER only for orders they created ({@code sellerId}); ADMIN/MANAGER for any.
 */
export function canMutateOrderInUi(me: MeResponse, order: OrderResponse): boolean {
  if (!ORDER_MUTATION_ROLES.has(me.role)) return false
  if (me.role === 'ROLE_SELLER') return order.sellerId === me.id
  return true
}

const CONFIRM_VEHICLE_ROLES = new Set([
  'ROLE_ADMIN',
  'ROLE_MANAGER',
  'ROLE_VEHICLE_STOCK',
  'ROLE_SELLER',
])

export function canConfirmVehicleInUi(me: MeResponse, order: OrderResponse): boolean {
  if (!CONFIRM_VEHICLE_ROLES.has(me.role)) return false
  if (order.vehicleArrived) return false
  if (me.role === 'ROLE_SELLER') return order.sellerId === me.id
  return true
}

const CONFIRM_ACCESSORIES_ROLES = new Set([
  'ROLE_ADMIN',
  'ROLE_MANAGER',
  'ROLE_ACCESSORY_STOCK',
  'ROLE_SELLER',
])

export function canConfirmAccessoriesInUi(me: MeResponse, order: OrderResponse): boolean {
  if (!CONFIRM_ACCESSORIES_ROLES.has(me.role)) return false
  if (order.accessoriesConfirmed) return false
  if (me.role === 'ROLE_SELLER') return order.sellerId === me.id
  return true
}
