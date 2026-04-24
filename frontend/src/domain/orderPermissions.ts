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

export function canConfirmInspectionInUi(me: MeResponse, order: OrderResponse): boolean {
  if (!ORDER_MUTATION_ROLES.has(me.role)) return false
  if (!order.vehicleArrived || !order.accessoriesConfirmed) return false
  if (order.inspectionCompleted) return false
  if (me.role === 'ROLE_SELLER') return order.sellerId === me.id
  return true
}

export function canConfirmInstallationInUi(me: MeResponse, order: OrderResponse): boolean {
  if (!ORDER_MUTATION_ROLES.has(me.role)) return false
  if (!order.vehicleArrived || !order.accessoriesConfirmed) return false
  if (order.installationCompleted) return false
  if (me.role === 'ROLE_SELLER') return order.sellerId === me.id
  return true
}

function sellerOwnsOrder(me: MeResponse, order: OrderResponse): boolean {
  if (me.role !== 'ROLE_SELLER') return true
  return order.sellerId === me.id
}

export function showVehicleWorkflowButton(me: MeResponse, order: OrderResponse): boolean {
  return CONFIRM_VEHICLE_ROLES.has(me.role) && sellerOwnsOrder(me, order)
}

export function showAccessoriesWorkflowButton(me: MeResponse, order: OrderResponse): boolean {
  return CONFIRM_ACCESSORIES_ROLES.has(me.role) && sellerOwnsOrder(me, order)
}

export function showInspectionWorkflowButton(me: MeResponse, order: OrderResponse): boolean {
  return ORDER_MUTATION_ROLES.has(me.role) && sellerOwnsOrder(me, order)
}

export function showInstallationWorkflowButton(me: MeResponse, order: OrderResponse): boolean {
  return ORDER_MUTATION_ROLES.has(me.role) && sellerOwnsOrder(me, order)
}

export function showsFourStepOrderToolbar(me: MeResponse, order: OrderResponse): boolean {
  return (
    showVehicleWorkflowButton(me, order) &&
    showAccessoriesWorkflowButton(me, order) &&
    showInspectionWorkflowButton(me, order) &&
    showInstallationWorkflowButton(me, order)
  )
}

export function canShowOrderWorkflowArea(me: MeResponse, order: OrderResponse): boolean {
  return (
    showVehicleWorkflowButton(me, order) ||
    showAccessoriesWorkflowButton(me, order) ||
    showInspectionWorkflowButton(me, order) ||
    showInstallationWorkflowButton(me, order)
  )
}

export function vehicleWorkflowDisabled(me: MeResponse, order: OrderResponse, busy: boolean): boolean {
  return busy || !canConfirmVehicleInUi(me, order)
}

export function accessoriesWorkflowDisabled(me: MeResponse, order: OrderResponse, busy: boolean): boolean {
  return busy || !canConfirmAccessoriesInUi(me, order)
}

export function inspectionWorkflowDisabled(me: MeResponse, order: OrderResponse, busy: boolean): boolean {
  return busy || !canConfirmInspectionInUi(me, order)
}

export function installationWorkflowDisabled(me: MeResponse, order: OrderResponse, busy: boolean): boolean {
  return busy || !canConfirmInstallationInUi(me, order)
}

export function vehicleWorkflowTitle(me: MeResponse, order: OrderResponse, busy: boolean): string | undefined {
  if (busy) return 'Aguarde a operação em andamento'
  if (canConfirmVehicleInUi(me, order)) return undefined
  if (order.vehicleArrived) return 'Veículo já confirmado'
  return undefined
}

export function accessoriesWorkflowTitle(me: MeResponse, order: OrderResponse, busy: boolean): string | undefined {
  if (busy) return 'Aguarde a operação em andamento'
  if (canConfirmAccessoriesInUi(me, order)) return undefined
  if (order.accessoriesConfirmed) return 'Acessórios já confirmados'
  return undefined
}

export function inspectionWorkflowTitle(me: MeResponse, order: OrderResponse, busy: boolean): string | undefined {
  if (busy) return 'Aguarde a operação em andamento'
  if (canConfirmInspectionInUi(me, order)) return undefined
  if (order.inspectionCompleted) return 'Inspeção já concluída'
  if (!order.vehicleArrived || !order.accessoriesConfirmed) return 'Confirme veículo e acessórios antes'
  return undefined
}

export function installationWorkflowTitle(me: MeResponse, order: OrderResponse, busy: boolean): string | undefined {
  if (busy) return 'Aguarde a operação em andamento'
  if (canConfirmInstallationInUi(me, order)) return undefined
  if (order.installationCompleted) return 'Instalação já concluída'
  if (!order.vehicleArrived || !order.accessoriesConfirmed) return 'Confirme veículo e acessórios antes'
  return undefined
}
