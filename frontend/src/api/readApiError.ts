/** Best-effort message from Spring ProblemDetail or similar JSON bodies. */
export async function readApiErrorMessage(res: Response, fallback: string): Promise<string> {
  let detail = fallback
  try {
    const body: unknown = await res.json()
    if (body && typeof body === 'object') {
      const o = body as Record<string, unknown>
      if (typeof o.detail === 'string' && o.detail) detail = o.detail
      else if (typeof o.message === 'string' && o.message) detail = o.message
      else if (Array.isArray(o.errors)) detail = JSON.stringify(o.errors)
    }
  } catch {
    /* ignore */
  }
  if (!detail && res.statusText) detail = res.statusText
  return detail || fallback
}
