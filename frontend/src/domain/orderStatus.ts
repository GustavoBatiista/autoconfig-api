export type StatusBucket = 'pending' | 'processing' | 'done'

const PENDING = new Set(['WAITING_VEHICLE'])
const PROCESSING = new Set(['WAITING_ACCESSORIES', 'WAITING_INSPECTION', 'WAITING_INSTALLATION'])
const DONE = new Set(['READY_FOR_DELIVERY'])

export function orderStatusBucket(status: string): StatusBucket {
  if (PENDING.has(status)) return 'pending'
  if (PROCESSING.has(status)) return 'processing'
  if (DONE.has(status)) return 'done'
  return 'pending'
}

export function orderStatusLabelPt(status: string): string {
  switch (status) {
    case 'WAITING_VEHICLE':
      return 'Aguardando veículo'
    case 'WAITING_ACCESSORIES':
      return 'Aguardando acessório'
    case 'WAITING_INSPECTION':
      return 'Aguardando inspeção'
    case 'WAITING_INSTALLATION':
      return 'Aguardando instalação'
    case 'READY_FOR_DELIVERY':
      return 'Pronto para entrega'
    default:
      return status
  }
}

export function orderStatusShortLabelPt(status: string): string {
  switch (status) {
    case 'WAITING_VEHICLE':
      return 'Aguardando veículo'
    case 'WAITING_ACCESSORIES':
      return 'Aguardando acessórios'
    case 'WAITING_INSPECTION':
      return 'Aguardando inspeção'
    case 'WAITING_INSTALLATION':
      return 'Aguardando instalação'
    case 'READY_FOR_DELIVERY':
      return 'Pronto p/ entrega'
    default:
      return orderStatusLabelPt(status)
  }
}

export function orderStatusBadgeClass(status: string): string {
  const b = orderStatusBucket(status)
  if (b === 'pending') return 'dash-badge dash-badge--pending'
  if (b === 'processing') return 'dash-badge dash-badge--processing'
  return 'dash-badge dash-badge--done'
}
