/** Parses a positive integer entity id from user input (e.g. table ID). */
export function parseEntityId(raw: string): number | null {
  const id = Number.parseInt(raw.trim(), 10)
  if (!Number.isFinite(id) || id < 1) return null
  return id
}
