export const ROLE_SELLER = 'ROLE_SELLER'
export const ROLE_MANAGER = 'ROLE_MANAGER'
export const ROLE_ADMIN = 'ROLE_ADMIN'

export function isSellerRole(role: string): boolean {
  return role === ROLE_SELLER
}

export function isManagerRole(role: string): boolean {
  return role === ROLE_MANAGER
}

export function isAdminRole(role: string): boolean {
  return role === ROLE_ADMIN
}


export function canAccessManagerDashboard(role: string): boolean {
  return isManagerRole(role) || isAdminRole(role)
}
