export type StatusBucket = 'pending' | 'processing' | 'done'

const PENDING = new Set(['WAITING_FOR_VEHICLE'])
const PROCESSING = new Set(['VEHICLE_ARRIVED', 'IN_INSTALLATION'])
const DONE = new Set(['READY'])

export function orderStatusBucket(status: string): StatusBucket {
  if (PENDING.has(status)) return 'pending'
  if (PROCESSING.has(status)) return 'processing'
  if (DONE.has(status)) return 'done'
  return 'pending'
}

export function orderStatusLabelPt(status: string): string {
  switch (status) {
    case 'WAITING_FOR_VEHICLE':
      return 'Aguardando veículo'
    case 'VEHICLE_ARRIVED':
      return 'Veículo chegou'
    case 'IN_INSTALLATION':
      return 'Em instalação'
    case 'READY':
      return 'Concluído'
    default:
      return status
  }
}

/** Short label for table badges (closer to the mock). */
export function orderStatusShortLabelPt(status: string): string {
  switch (status) {
    case 'WAITING_FOR_VEHICLE':
      return 'Aguardando'
    case 'VEHICLE_ARRIVED':
    case 'IN_INSTALLATION':
      return 'Em processamento'
    case 'READY':
      return 'Concluído'
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


export const ORDER_STATUS_FORM_OPTIONS = [
  { value: 'WAITING_FOR_VEHICLE', label: 'Aguardando veículo' },
  { value: 'VEHICLE_ARRIVED', label: 'Veículo chegou' },
  { value: 'IN_INSTALLATION', label: 'Em instalação' },
  { value: 'READY', label: 'Concluído' },
] as const
