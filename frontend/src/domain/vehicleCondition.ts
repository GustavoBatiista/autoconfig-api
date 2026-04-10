export const VEHICLE_CONDITION_OPTIONS = [
  { value: 'PERFECT', label: 'Perfeito' },
  { value: 'MINOR_DAMAGE', label: 'Danos leves' },
  { value: 'MAJOR_DAMAGE', label: 'Danos graves' },
] as const

export function vehicleConditionLabelPt(condition: string): string {
  const found = VEHICLE_CONDITION_OPTIONS.find((o) => o.value === condition)
  if (found) return found.label
  switch (condition) {
    case 'PERFECT':
      return 'Perfeito'
    case 'MINOR_DAMAGE':
      return 'Danos leves'
    case 'MAJOR_DAMAGE':
      return 'Danos graves'
    default:
      return condition
  }
}
