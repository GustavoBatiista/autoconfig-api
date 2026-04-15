/** Single line label for a car (list / accessory context). */
export function formatCarDisplay(brand: string, model: string, version?: string): string {
  const v = version?.trim()
  return [brand, model, v].filter(Boolean).join(' ')
}
